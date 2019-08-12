// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import org.apache.logging.log4j.core.LogEvent;
import java.io.UnsupportedEncodingException;
import org.apache.logging.log4j.core.util.StringEncoder;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import org.apache.logging.log4j.core.StringLayout;

public abstract class AbstractStringLayout extends AbstractLayout<String> implements StringLayout
{
    protected static final int DEFAULT_STRING_BUILDER_SIZE = 1024;
    private static final ThreadLocal<StringBuilder> threadLocal;
    private static final long serialVersionUID = 1L;
    private transient Charset charset;
    private final String charsetName;
    private final boolean useCustomEncoding;
    
    protected AbstractStringLayout(final Charset charset) {
        this(charset, null, null);
    }
    
    protected AbstractStringLayout(final Charset charset, final byte[] header, final byte[] footer) {
        super(header, footer);
        this.charset = ((charset == null) ? StandardCharsets.UTF_8 : charset);
        this.charsetName = this.charset.name();
        this.useCustomEncoding = (isPreJava8() && (StandardCharsets.ISO_8859_1.equals(charset) || StandardCharsets.US_ASCII.equals(charset)));
    }
    
    private static boolean isPreJava8() {
        final String version = System.getProperty("java.version");
        final String[] parts = version.split("\\.");
        try {
            final int major = Integer.parseInt(parts[1]);
            return major < 8;
        }
        catch (Exception ex) {
            return true;
        }
    }
    
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(this.charset.name());
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        final String csName = in.readUTF();
        this.charset = Charset.forName(csName);
    }
    
    protected StringBuilder getStringBuilder() {
        StringBuilder result = AbstractStringLayout.threadLocal.get();
        if (result == null) {
            result = new StringBuilder(1024);
            AbstractStringLayout.threadLocal.set(result);
        }
        result.setLength(0);
        return result;
    }
    
    protected byte[] getBytes(final String s) {
        if (this.useCustomEncoding) {
            return StringEncoder.encodeSingleByteChars(s);
        }
        try {
            return s.getBytes(this.charsetName);
        }
        catch (UnsupportedEncodingException e) {
            return s.getBytes(this.charset);
        }
    }
    
    @Override
    public Charset getCharset() {
        return this.charset;
    }
    
    @Override
    public String getContentType() {
        return "text/plain";
    }
    
    @Override
    public byte[] toByteArray(final LogEvent event) {
        return this.getBytes(this.toSerializable(event));
    }
    
    static {
        threadLocal = new ThreadLocal<StringBuilder>();
    }
}
