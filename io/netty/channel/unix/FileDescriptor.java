// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.unix;

import java.io.IOException;

public class FileDescriptor
{
    private final int fd;
    private volatile boolean open;
    
    public FileDescriptor(final int fd) {
        this.open = true;
        if (fd < 0) {
            throw new IllegalArgumentException("fd must be >= 0");
        }
        this.fd = fd;
    }
    
    public int intValue() {
        return this.fd;
    }
    
    public void close() throws IOException {
        this.open = false;
        close(this.fd);
    }
    
    public boolean isOpen() {
        return this.open;
    }
    
    @Override
    public String toString() {
        return "FileDescriptor{fd=" + this.fd + '}';
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof FileDescriptor && this.fd == ((FileDescriptor)o).fd);
    }
    
    @Override
    public int hashCode() {
        return this.fd;
    }
    
    private static native int close(final int p0);
}
