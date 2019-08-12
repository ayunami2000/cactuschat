// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import java.nio.charset.Charset;

public class TlsSyslogFrame
{
    private String message;
    private int messageLengthInBytes;
    
    public TlsSyslogFrame(final String message) {
        this.setMessage(message);
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public void setMessage(final String message) {
        this.message = message;
        this.setLengthInBytes();
    }
    
    private void setLengthInBytes() {
        this.messageLengthInBytes = this.message.length();
    }
    
    public byte[] getBytes() {
        final String frame = this.toString();
        return frame.getBytes(Charset.defaultCharset());
    }
    
    @Override
    public String toString() {
        final String length = Integer.toString(this.messageLengthInBytes);
        return length + ' ' + this.message;
    }
    
    public boolean equals(final TlsSyslogFrame frame) {
        return this.isLengthEquals(frame) && this.isMessageEquals(frame);
    }
    
    private boolean isLengthEquals(final TlsSyslogFrame frame) {
        return this.messageLengthInBytes == frame.messageLengthInBytes;
    }
    
    private boolean isMessageEquals(final TlsSyslogFrame frame) {
        return this.message.equals(frame.message);
    }
}
