// 
// Decompiled by Procyon v0.5.36
// 

package jline.console.internal;

import java.util.Enumeration;
import java.io.IOException;
import jline.console.ConsoleReader;
import java.io.InputStream;
import java.io.SequenceInputStream;

class ConsoleReaderInputStream extends SequenceInputStream
{
    private static InputStream systemIn;
    
    public static void setIn() throws IOException {
        setIn(new ConsoleReader());
    }
    
    public static void setIn(final ConsoleReader reader) {
        System.setIn(new ConsoleReaderInputStream(reader));
    }
    
    public static void restoreIn() {
        System.setIn(ConsoleReaderInputStream.systemIn);
    }
    
    public ConsoleReaderInputStream(final ConsoleReader reader) {
        super(new ConsoleEnumeration(reader));
    }
    
    static {
        ConsoleReaderInputStream.systemIn = System.in;
    }
    
    private static class ConsoleEnumeration implements Enumeration
    {
        private final ConsoleReader reader;
        private ConsoleLineInputStream next;
        private ConsoleLineInputStream prev;
        
        public ConsoleEnumeration(final ConsoleReader reader) {
            this.next = null;
            this.prev = null;
            this.reader = reader;
        }
        
        public Object nextElement() {
            if (this.next != null) {
                final InputStream n = this.next;
                this.prev = this.next;
                this.next = null;
                return n;
            }
            return new ConsoleLineInputStream(this.reader);
        }
        
        public boolean hasMoreElements() {
            if (this.prev != null && this.prev.wasNull) {
                return false;
            }
            if (this.next == null) {
                this.next = (ConsoleLineInputStream)this.nextElement();
            }
            return this.next != null;
        }
    }
    
    private static class ConsoleLineInputStream extends InputStream
    {
        private final ConsoleReader reader;
        private String line;
        private int index;
        private boolean eol;
        protected boolean wasNull;
        
        public ConsoleLineInputStream(final ConsoleReader reader) {
            this.line = null;
            this.index = 0;
            this.eol = false;
            this.wasNull = false;
            this.reader = reader;
        }
        
        @Override
        public int read() throws IOException {
            if (this.eol) {
                return -1;
            }
            if (this.line == null) {
                this.line = this.reader.readLine();
            }
            if (this.line == null) {
                this.wasNull = true;
                return -1;
            }
            if (this.index >= this.line.length()) {
                this.eol = true;
                return 10;
            }
            return this.line.charAt(this.index++);
        }
    }
}
