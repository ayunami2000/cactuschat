// 
// Decompiled by Procyon v0.5.36
// 

package jline.internal;

import java.io.IOException;
import java.io.InputStream;

public class NonBlockingInputStream extends InputStream implements Runnable
{
    private InputStream in;
    private int ch;
    private boolean threadIsReading;
    private boolean isShutdown;
    private IOException exception;
    private boolean nonBlockingEnabled;
    
    public NonBlockingInputStream(final InputStream in, final boolean isNonBlockingEnabled) {
        this.ch = -2;
        this.threadIsReading = false;
        this.isShutdown = false;
        this.exception = null;
        this.in = in;
        this.nonBlockingEnabled = isNonBlockingEnabled;
        if (isNonBlockingEnabled) {
            final Thread t = new Thread(this);
            t.setName("NonBlockingInputStreamThread");
            t.setDaemon(true);
            t.start();
        }
    }
    
    public synchronized void shutdown() {
        if (!this.isShutdown && this.nonBlockingEnabled) {
            this.isShutdown = true;
            this.notify();
        }
    }
    
    public boolean isNonBlockingEnabled() {
        return this.nonBlockingEnabled && !this.isShutdown;
    }
    
    @Override
    public void close() throws IOException {
        this.in.close();
        this.shutdown();
    }
    
    @Override
    public int read() throws IOException {
        if (this.nonBlockingEnabled) {
            return this.read(0L, false);
        }
        return this.in.read();
    }
    
    public int peek(final long timeout) throws IOException {
        if (!this.nonBlockingEnabled || this.isShutdown) {
            throw new UnsupportedOperationException("peek() cannot be called as non-blocking operation is disabled");
        }
        return this.read(timeout, true);
    }
    
    public int read(final long timeout) throws IOException {
        if (!this.nonBlockingEnabled || this.isShutdown) {
            throw new UnsupportedOperationException("read() with timeout cannot be called as non-blocking operation is disabled");
        }
        return this.read(timeout, false);
    }
    
    private synchronized int read(long timeout, final boolean isPeek) throws IOException {
        if (this.exception == null) {
            if (this.ch >= -1) {
                assert this.exception == null;
            }
            else if ((timeout == 0L || this.isShutdown) && !this.threadIsReading) {
                this.ch = this.in.read();
            }
            else {
                if (!this.threadIsReading) {
                    this.threadIsReading = true;
                    this.notify();
                }
                final boolean isInfinite = timeout <= 0L;
                while (isInfinite || timeout > 0L) {
                    final long start = System.currentTimeMillis();
                    try {
                        this.wait(timeout);
                    }
                    catch (InterruptedException ex) {}
                    if (this.exception != null) {
                        assert this.ch == -2;
                        final IOException toBeThrown = this.exception;
                        if (!isPeek) {
                            this.exception = null;
                        }
                        throw toBeThrown;
                    }
                    else if (this.ch >= -1) {
                        assert this.exception == null;
                        break;
                    }
                    else {
                        if (isInfinite) {
                            continue;
                        }
                        timeout -= System.currentTimeMillis() - start;
                    }
                }
            }
            final int ret = this.ch;
            if (!isPeek) {
                this.ch = -2;
            }
            return ret;
        }
        assert this.ch == -2;
        final IOException toBeThrown2 = this.exception;
        if (!isPeek) {
            this.exception = null;
        }
        throw toBeThrown2;
    }
    
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        int c;
        if (this.nonBlockingEnabled) {
            c = this.read(0L);
        }
        else {
            c = this.in.read();
        }
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;
        return 1;
    }
    
    public void run() {
        Log.debug("NonBlockingInputStream start");
        boolean needToShutdown = false;
        boolean needToRead = false;
        while (!needToShutdown) {
            synchronized (this) {
                needToShutdown = this.isShutdown;
                needToRead = this.threadIsReading;
                try {
                    if (!needToShutdown && !needToRead) {
                        this.wait(0L);
                    }
                }
                catch (InterruptedException ex) {}
            }
            if (!needToShutdown && needToRead) {
                int charRead = -2;
                IOException failure = null;
                try {
                    charRead = this.in.read();
                }
                catch (IOException e) {
                    failure = e;
                }
                synchronized (this) {
                    this.exception = failure;
                    this.ch = charRead;
                    this.threadIsReading = false;
                    this.notify();
                }
            }
        }
        Log.debug("NonBlockingInputStream shutdown");
    }
}
