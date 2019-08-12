// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.db.jpa;

import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import java.lang.reflect.Constructor;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseAppender;

@Plugin(name = "JPA", category = "Core", elementType = "appender", printObject = true)
public final class JpaAppender extends AbstractDatabaseAppender<JpaDatabaseManager>
{
    private static final long serialVersionUID = 1L;
    private final String description;
    
    private JpaAppender(final String name, final Filter filter, final boolean ignoreExceptions, final JpaDatabaseManager manager) {
        super(name, filter, ignoreExceptions, manager);
        this.description = this.getName() + "{ manager=" + ((AbstractDatabaseAppender<Object>)this).getManager() + " }";
    }
    
    @Override
    public String toString() {
        return this.description;
    }
    
    @PluginFactory
    public static JpaAppender createAppender(@PluginAttribute("name") final String name, @PluginAttribute("ignoreExceptions") final String ignore, @PluginElement("Filter") final Filter filter, @PluginAttribute("bufferSize") final String bufferSize, @PluginAttribute("entityClassName") final String entityClassName, @PluginAttribute("persistenceUnitName") final String persistenceUnitName) {
        if (Strings.isEmpty(entityClassName) || Strings.isEmpty(persistenceUnitName)) {
            JpaAppender.LOGGER.error("Attributes entityClassName and persistenceUnitName are required for JPA Appender.");
            return null;
        }
        final int bufferSizeInt = AbstractAppender.parseInt(bufferSize, 0);
        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        try {
            final Class<? extends AbstractLogEventWrapperEntity> entityClass = (Class<? extends AbstractLogEventWrapperEntity>)Loader.loadClass(entityClassName);
            if (!AbstractLogEventWrapperEntity.class.isAssignableFrom(entityClass)) {
                JpaAppender.LOGGER.error("Entity class [{}] does not extend AbstractLogEventWrapperEntity.", new Object[] { entityClassName });
                return null;
            }
            try {
                entityClass.getConstructor((Class<?>[])new Class[0]);
            }
            catch (NoSuchMethodException e2) {
                JpaAppender.LOGGER.error("Entity class [{}] does not have a no-arg constructor. The JPA provider will reject it.", new Object[] { entityClassName });
                return null;
            }
            final Constructor<? extends AbstractLogEventWrapperEntity> entityConstructor = entityClass.getConstructor(LogEvent.class);
            final String managerName = "jpaManager{ description=" + name + ", bufferSize=" + bufferSizeInt + ", persistenceUnitName=" + persistenceUnitName + ", entityClass=" + entityClass.getName() + '}';
            final JpaDatabaseManager manager = JpaDatabaseManager.getJPADatabaseManager(managerName, bufferSizeInt, entityClass, entityConstructor, persistenceUnitName);
            if (manager == null) {
                return null;
            }
            return new JpaAppender(name, filter, ignoreExceptions, manager);
        }
        catch (ClassNotFoundException e) {
            JpaAppender.LOGGER.error("Could not load entity class [{}].", new Object[] { entityClassName, e });
            return null;
        }
        catch (NoSuchMethodException e3) {
            JpaAppender.LOGGER.error("Entity class [{}] does not have a constructor with a single argument of type LogEvent.", new Object[] { entityClassName });
            return null;
        }
    }
}
