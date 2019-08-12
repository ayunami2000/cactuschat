// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.hexdump;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

class Client
{
    protected long endTime;
    protected SelectionKey key;
    
    protected Client(final SelectableChannel channel, final long endTime) throws IOException {
        boolean done = false;
        Selector selector = null;
        this.endTime = endTime;
        try {
            selector = Selector.open();
            channel.configureBlocking(false);
            this.key = channel.register(selector, 1);
            done = true;
        }
        finally {
            if (!done && selector != null) {
                selector.close();
            }
            if (!done) {
                channel.close();
            }
        }
    }
    
    protected static void blockUntil(final SelectionKey key, final long endTime) throws IOException {
        final long timeout = endTime - System.currentTimeMillis();
        int nkeys = 0;
        if (timeout > 0L) {
            nkeys = key.selector().select(timeout);
        }
        else if (timeout == 0L) {
            nkeys = key.selector().selectNow();
        }
        if (nkeys == 0) {
            throw new SocketTimeoutException();
        }
    }
    
    protected static void verboseLog(final String prefix, final byte[] data) {
        if (Options.check("verbosemsg")) {
            System.err.println(hexdump.dump(prefix, data));
        }
    }
    
    void cleanup() throws IOException {
        this.key.selector().close();
        this.key.channel().close();
    }
}
