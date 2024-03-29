// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.xml;

import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Configuration;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Validator;
import javax.xml.validation.Schema;
import org.apache.logging.log4j.core.util.FileWatcher;
import java.util.Iterator;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import javax.xml.transform.Source;
import javax.xml.validation.SchemaFactory;
import javax.xml.transform.stream.StreamSource;
import org.apache.logging.log4j.core.util.Loader;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import org.xml.sax.SAXException;
import org.apache.logging.log4j.core.config.ConfiguratonFileWatcher;
import java.util.Collection;
import java.util.Arrays;
import org.apache.logging.log4j.core.util.Patterns;
import java.util.Map;
import org.apache.logging.log4j.core.config.status.StatusConfiguration;
import org.apache.logging.log4j.core.util.Throwables;
import java.io.InputStream;
import org.xml.sax.InputSource;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import org.apache.logging.log4j.core.util.Closer;
import java.util.ArrayList;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.w3c.dom.Element;
import java.util.List;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.AbstractConfiguration;

public class XmlConfiguration extends AbstractConfiguration implements Reconfigurable
{
    private static final long serialVersionUID = 1L;
    private static final String XINCLUDE_FIXUP_LANGUAGE = "http://apache.org/xml/features/xinclude/fixup-language";
    private static final String XINCLUDE_FIXUP_BASE_URIS = "http://apache.org/xml/features/xinclude/fixup-base-uris";
    private static final String[] VERBOSE_CLASSES;
    private static final String LOG4J_XSD = "Log4j-config.xsd";
    private final List<Status> status;
    private Element rootElement;
    private boolean strict;
    private String schemaResource;
    
    public XmlConfiguration(final ConfigurationSource configSource) {
        super(configSource);
        this.status = new ArrayList<Status>();
        final File configFile = configSource.getFile();
        byte[] buffer = null;
        try {
            final InputStream configStream = configSource.getInputStream();
            try {
                buffer = AbstractConfiguration.toByteArray(configStream);
            }
            finally {
                Closer.closeSilently(configStream);
            }
            final InputSource source = new InputSource(new ByteArrayInputStream(buffer));
            source.setSystemId(configSource.getLocation());
            final DocumentBuilder documentBuilder = newDocumentBuilder(true);
            Document document;
            try {
                document = documentBuilder.parse(source);
            }
            catch (Exception e) {
                final Throwable throwable = Throwables.getRootCause(e);
                if (!(throwable instanceof UnsupportedOperationException)) {
                    throw e;
                }
                XmlConfiguration.LOGGER.warn("The DocumentBuilder {} does not support an operation: {}.Trying again without XInclude...", new Object[] { documentBuilder, e });
                document = newDocumentBuilder(false).parse(source);
            }
            this.rootElement = document.getDocumentElement();
            final Map<String, String> attrs = this.processAttributes(this.rootNode, this.rootElement);
            final StatusConfiguration statusConfig = new StatusConfiguration().withVerboseClasses(XmlConfiguration.VERBOSE_CLASSES).withStatus(this.getDefaultStatus());
            for (final Map.Entry<String, String> entry : attrs.entrySet()) {
                final String key = entry.getKey();
                final String value = this.getStrSubstitutor().replace(entry.getValue());
                if ("status".equalsIgnoreCase(key)) {
                    statusConfig.withStatus(value);
                }
                else if ("dest".equalsIgnoreCase(key)) {
                    statusConfig.withDestination(value);
                }
                else if ("shutdownHook".equalsIgnoreCase(key)) {
                    this.isShutdownHookEnabled = !"disable".equalsIgnoreCase(value);
                }
                else if ("verbose".equalsIgnoreCase(key)) {
                    statusConfig.withVerbosity(value);
                }
                else if ("packages".equalsIgnoreCase(key)) {
                    this.pluginPackages.addAll(Arrays.asList(value.split(Patterns.COMMA_SEPARATOR)));
                }
                else if ("name".equalsIgnoreCase(key)) {
                    this.setName(value);
                }
                else if ("strict".equalsIgnoreCase(key)) {
                    this.strict = Boolean.parseBoolean(value);
                }
                else if ("schema".equalsIgnoreCase(key)) {
                    this.schemaResource = value;
                }
                else if ("monitorInterval".equalsIgnoreCase(key)) {
                    final int intervalSeconds = Integer.parseInt(value);
                    if (intervalSeconds <= 0) {
                        continue;
                    }
                    this.getWatchManager().setIntervalSeconds(intervalSeconds);
                    if (configFile == null) {
                        continue;
                    }
                    final FileWatcher watcher = new ConfiguratonFileWatcher(this, this.listeners);
                    this.getWatchManager().watchFile(configFile, watcher);
                }
                else {
                    if (!"advertiser".equalsIgnoreCase(key)) {
                        continue;
                    }
                    this.createAdvertiser(value, configSource, buffer, "text/xml");
                }
            }
            statusConfig.initialize();
        }
        catch (SAXException domEx) {
            XmlConfiguration.LOGGER.error("Error parsing {}", new Object[] { configSource.getLocation(), domEx });
        }
        catch (IOException ioe) {
            XmlConfiguration.LOGGER.error("Error parsing {}", new Object[] { configSource.getLocation(), ioe });
        }
        catch (ParserConfigurationException pex) {
            XmlConfiguration.LOGGER.error("Error parsing {}", new Object[] { configSource.getLocation(), pex });
        }
        if (this.strict && this.schemaResource != null && buffer != null) {
            InputStream is = null;
            try {
                is = Loader.getResourceAsStream(this.schemaResource, XmlConfiguration.class.getClassLoader());
            }
            catch (Exception ex) {
                XmlConfiguration.LOGGER.error("Unable to access schema {}", new Object[] { this.schemaResource, ex });
            }
            if (is != null) {
                final Source src = new StreamSource(is, "Log4j-config.xsd");
                final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                Schema schema = null;
                try {
                    schema = factory.newSchema(src);
                }
                catch (SAXException ex2) {
                    XmlConfiguration.LOGGER.error("Error parsing Log4j schema", ex2);
                }
                if (schema != null) {
                    final Validator validator = schema.newValidator();
                    try {
                        validator.validate(new StreamSource(new ByteArrayInputStream(buffer)));
                    }
                    catch (IOException ioe2) {
                        XmlConfiguration.LOGGER.error("Error reading configuration for validation", ioe2);
                    }
                    catch (SAXException ex3) {
                        XmlConfiguration.LOGGER.error("Error validating configuration", ex3);
                    }
                }
            }
        }
        if (this.getName() == null) {
            this.setName(configSource.getLocation());
        }
    }
    
    static DocumentBuilder newDocumentBuilder(final boolean xIncludeAware) throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        if (xIncludeAware) {
            enableXInclude(factory);
        }
        return factory.newDocumentBuilder();
    }
    
    private static void enableXInclude(final DocumentBuilderFactory factory) {
        try {
            factory.setXIncludeAware(true);
        }
        catch (UnsupportedOperationException e) {
            XmlConfiguration.LOGGER.warn("The DocumentBuilderFactory [{}] does not support XInclude: {}", new Object[] { factory, e });
        }
        catch (AbstractMethodError err) {
            XmlConfiguration.LOGGER.warn("The DocumentBuilderFactory [{}] is out of date and does not support XInclude: {}", new Object[] { factory, err });
        }
        catch (NoSuchMethodError err2) {
            XmlConfiguration.LOGGER.warn("The DocumentBuilderFactory [{}] is out of date and does not support XInclude: {}", new Object[] { factory, err2 });
        }
        try {
            factory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", true);
        }
        catch (ParserConfigurationException e2) {
            XmlConfiguration.LOGGER.warn("The DocumentBuilderFactory [{}] does not support the feature [{}]: {}", new Object[] { factory, "http://apache.org/xml/features/xinclude/fixup-base-uris", e2 });
        }
        catch (AbstractMethodError err) {
            XmlConfiguration.LOGGER.warn("The DocumentBuilderFactory [{}] is out of date and does not support setFeature: {}", new Object[] { factory, err });
        }
        try {
            factory.setFeature("http://apache.org/xml/features/xinclude/fixup-language", true);
        }
        catch (ParserConfigurationException e2) {
            XmlConfiguration.LOGGER.warn("The DocumentBuilderFactory [{}] does not support the feature [{}]: {}", new Object[] { factory, "http://apache.org/xml/features/xinclude/fixup-language", e2 });
        }
        catch (AbstractMethodError err) {
            XmlConfiguration.LOGGER.warn("The DocumentBuilderFactory [{}] is out of date and does not support setFeature: {}", new Object[] { factory, err });
        }
    }
    
    public void setup() {
        if (this.rootElement == null) {
            XmlConfiguration.LOGGER.error("No logging configuration");
            return;
        }
        this.constructHierarchy(this.rootNode, this.rootElement);
        if (this.status.size() > 0) {
            for (final Status s : this.status) {
                XmlConfiguration.LOGGER.error("Error processing element {} ({}): {}", new Object[] { s.name, s.element, s.errorType });
            }
            return;
        }
        this.rootElement = null;
    }
    
    @Override
    public Configuration reconfigure() {
        try {
            final ConfigurationSource source = this.getConfigurationSource().resetInputStream();
            if (source == null) {
                return null;
            }
            final XmlConfiguration config = new XmlConfiguration(source);
            return (config.rootElement == null) ? null : config;
        }
        catch (IOException ex) {
            XmlConfiguration.LOGGER.error("Cannot locate file {}", new Object[] { this.getConfigurationSource(), ex });
            return null;
        }
    }
    
    private void constructHierarchy(final Node node, final Element element) {
        this.processAttributes(node, element);
        final StringBuilder buffer = new StringBuilder();
        final NodeList list = element.getChildNodes();
        final List<Node> children = node.getChildren();
        for (int i = 0; i < list.getLength(); ++i) {
            final org.w3c.dom.Node w3cNode = list.item(i);
            if (w3cNode instanceof Element) {
                final Element child = (Element)w3cNode;
                final String name = this.getType(child);
                final PluginType<?> type = this.pluginManager.getPluginType(name);
                final Node childNode = new Node(node, name, type);
                this.constructHierarchy(childNode, child);
                if (type == null) {
                    final String value = childNode.getValue();
                    if (!childNode.hasChildren() && value != null) {
                        node.getAttributes().put(name, value);
                    }
                    else {
                        this.status.add(new Status(name, element, ErrorType.CLASS_NOT_FOUND));
                    }
                }
                else {
                    children.add(childNode);
                }
            }
            else if (w3cNode instanceof Text) {
                final Text data = (Text)w3cNode;
                buffer.append(data.getData());
            }
        }
        final String text = buffer.toString().trim();
        if (text.length() > 0 || (!node.hasChildren() && !node.isRoot())) {
            node.setValue(text);
        }
    }
    
    private String getType(final Element element) {
        if (this.strict) {
            final NamedNodeMap attrs = element.getAttributes();
            for (int i = 0; i < attrs.getLength(); ++i) {
                final org.w3c.dom.Node w3cNode = attrs.item(i);
                if (w3cNode instanceof Attr) {
                    final Attr attr = (Attr)w3cNode;
                    if (attr.getName().equalsIgnoreCase("type")) {
                        final String type = attr.getValue();
                        attrs.removeNamedItem(attr.getName());
                        return type;
                    }
                }
            }
        }
        return element.getTagName();
    }
    
    private Map<String, String> processAttributes(final Node node, final Element element) {
        final NamedNodeMap attrs = element.getAttributes();
        final Map<String, String> attributes = node.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            final org.w3c.dom.Node w3cNode = attrs.item(i);
            if (w3cNode instanceof Attr) {
                final Attr attr = (Attr)w3cNode;
                if (!attr.getName().equals("xml:base")) {
                    attributes.put(attr.getName(), attr.getValue());
                }
            }
        }
        return attributes;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[location=" + this.getConfigurationSource() + "]";
    }
    
    static {
        VERBOSE_CLASSES = new String[] { ResolverUtil.class.getName() };
    }
    
    private enum ErrorType
    {
        CLASS_NOT_FOUND;
    }
    
    private static class Status
    {
        private final Element element;
        private final String name;
        private final ErrorType errorType;
        
        public Status(final String name, final Element element, final ErrorType errorType) {
            this.name = name;
            this.element = element;
            this.errorType = errorType;
        }
        
        @Override
        public String toString() {
            return "Status [name=" + this.name + ", element=" + this.element + ", errorType=" + this.errorType + "]";
        }
    }
}
