// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.multipart;

import io.netty.buffer.ByteBufHolder;
import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelException;
import java.io.File;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaderNames;
import java.io.IOException;
import java.nio.charset.Charset;

public class DiskFileUpload extends AbstractDiskHttpData implements FileUpload
{
    public static String baseDirectory;
    public static boolean deleteOnExitTemporaryFile;
    public static final String prefix = "FUp_";
    public static final String postfix = ".tmp";
    private String filename;
    private String contentType;
    private String contentTransferEncoding;
    
    public DiskFileUpload(final String name, final String filename, final String contentType, final String contentTransferEncoding, final Charset charset, final long size) {
        super(name, charset, size);
        this.setFilename(filename);
        this.setContentType(contentType);
        this.setContentTransferEncoding(contentTransferEncoding);
    }
    
    @Override
    public InterfaceHttpData.HttpDataType getHttpDataType() {
        return InterfaceHttpData.HttpDataType.FileUpload;
    }
    
    @Override
    public String getFilename() {
        return this.filename;
    }
    
    @Override
    public void setFilename(final String filename) {
        if (filename == null) {
            throw new NullPointerException("filename");
        }
        this.filename = filename;
    }
    
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Attribute)) {
            return false;
        }
        final Attribute attribute = (Attribute)o;
        return this.getName().equalsIgnoreCase(attribute.getName());
    }
    
    @Override
    public int compareTo(final InterfaceHttpData o) {
        if (!(o instanceof FileUpload)) {
            throw new ClassCastException("Cannot compare " + this.getHttpDataType() + " with " + o.getHttpDataType());
        }
        return this.compareTo((FileUpload)o);
    }
    
    public int compareTo(final FileUpload o) {
        final int v = this.getName().compareToIgnoreCase(o.getName());
        if (v != 0) {
            return v;
        }
        return v;
    }
    
    @Override
    public void setContentType(final String contentType) {
        if (contentType == null) {
            throw new NullPointerException("contentType");
        }
        this.contentType = contentType;
    }
    
    @Override
    public String getContentType() {
        return this.contentType;
    }
    
    @Override
    public String getContentTransferEncoding() {
        return this.contentTransferEncoding;
    }
    
    @Override
    public void setContentTransferEncoding(final String contentTransferEncoding) {
        this.contentTransferEncoding = contentTransferEncoding;
    }
    
    @Override
    public String toString() {
        File file = null;
        try {
            file = this.getFile();
        }
        catch (IOException ex) {}
        return (Object)HttpHeaderNames.CONTENT_DISPOSITION + ": " + (Object)HttpHeaderValues.FORM_DATA + "; " + (Object)HttpHeaderValues.NAME + "=\"" + this.getName() + "\"; " + (Object)HttpHeaderValues.FILENAME + "=\"" + this.filename + "\"\r\n" + (Object)HttpHeaderNames.CONTENT_TYPE + ": " + this.contentType + ((this.getCharset() != null) ? ("; " + (Object)HttpHeaderValues.CHARSET + '=' + this.getCharset() + "\r\n") : "\r\n") + (Object)HttpHeaderNames.CONTENT_LENGTH + ": " + this.length() + "\r\n" + "Completed: " + this.isCompleted() + "\r\nIsInMemory: " + this.isInMemory() + "\r\nRealFile: " + ((file != null) ? file.getAbsolutePath() : "null") + " DefaultDeleteAfter: " + DiskFileUpload.deleteOnExitTemporaryFile;
    }
    
    @Override
    protected boolean deleteOnExit() {
        return DiskFileUpload.deleteOnExitTemporaryFile;
    }
    
    @Override
    protected String getBaseDirectory() {
        return DiskFileUpload.baseDirectory;
    }
    
    @Override
    protected String getDiskFilename() {
        final File file = new File(this.filename);
        return file.getName();
    }
    
    @Override
    protected String getPostfix() {
        return ".tmp";
    }
    
    @Override
    protected String getPrefix() {
        return "FUp_";
    }
    
    @Override
    public FileUpload copy() {
        final DiskFileUpload upload = new DiskFileUpload(this.getName(), this.getFilename(), this.getContentType(), this.getContentTransferEncoding(), this.getCharset(), this.size);
        final ByteBuf buf = this.content();
        if (buf != null) {
            try {
                upload.setContent(buf.copy());
            }
            catch (IOException e) {
                throw new ChannelException(e);
            }
        }
        return upload;
    }
    
    @Override
    public FileUpload duplicate() {
        final DiskFileUpload upload = new DiskFileUpload(this.getName(), this.getFilename(), this.getContentType(), this.getContentTransferEncoding(), this.getCharset(), this.size);
        final ByteBuf buf = this.content();
        if (buf != null) {
            try {
                upload.setContent(buf.duplicate());
            }
            catch (IOException e) {
                throw new ChannelException(e);
            }
        }
        return upload;
    }
    
    @Override
    public FileUpload retain(final int increment) {
        super.retain(increment);
        return this;
    }
    
    @Override
    public FileUpload retain() {
        super.retain();
        return this;
    }
    
    @Override
    public FileUpload touch() {
        super.touch();
        return this;
    }
    
    @Override
    public FileUpload touch(final Object hint) {
        super.touch(hint);
        return this;
    }
    
    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true;
    }
}
