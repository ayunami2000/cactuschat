// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Iterator;
import java.net.SocketAddress;
import java.io.IOException;
import java.util.List;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class SimpleResolver implements Resolver
{
    public static final int DEFAULT_PORT = 53;
    public static final int DEFAULT_EDNS_PAYLOADSIZE = 1280;
    private InetSocketAddress address;
    private InetSocketAddress localAddress;
    private boolean useTCP;
    private boolean ignoreTruncation;
    private OPTRecord queryOPT;
    private TSIG tsig;
    private long timeoutValue;
    private static final short DEFAULT_UDPSIZE = 512;
    private static String defaultResolver;
    private static int uniqueID;
    
    public SimpleResolver(String hostname) throws UnknownHostException {
        this.timeoutValue = 10000L;
        if (hostname == null) {
            hostname = ResolverConfig.getCurrentConfig().server();
            if (hostname == null) {
                hostname = SimpleResolver.defaultResolver;
            }
        }
        InetAddress addr;
        if (hostname.equals("0")) {
            addr = InetAddress.getLocalHost();
        }
        else {
            addr = InetAddress.getByName(hostname);
        }
        this.address = new InetSocketAddress(addr, 53);
    }
    
    public SimpleResolver() throws UnknownHostException {
        this(null);
    }
    
    InetSocketAddress getAddress() {
        return this.address;
    }
    
    public static void setDefaultResolver(final String hostname) {
        SimpleResolver.defaultResolver = hostname;
    }
    
    public void setPort(final int port) {
        this.address = new InetSocketAddress(this.address.getAddress(), port);
    }
    
    public void setAddress(final InetSocketAddress addr) {
        this.address = addr;
    }
    
    public void setAddress(final InetAddress addr) {
        this.address = new InetSocketAddress(addr, this.address.getPort());
    }
    
    public void setLocalAddress(final InetSocketAddress addr) {
        this.localAddress = addr;
    }
    
    public void setLocalAddress(final InetAddress addr) {
        this.localAddress = new InetSocketAddress(addr, 0);
    }
    
    public void setTCP(final boolean flag) {
        this.useTCP = flag;
    }
    
    public void setIgnoreTruncation(final boolean flag) {
        this.ignoreTruncation = flag;
    }
    
    public void setEDNS(final int level, int payloadSize, final int flags, final List options) {
        if (level != 0 && level != -1) {
            throw new IllegalArgumentException("invalid EDNS level - must be 0 or -1");
        }
        if (payloadSize == 0) {
            payloadSize = 1280;
        }
        this.queryOPT = new OPTRecord(payloadSize, 0, level, flags, options);
    }
    
    public void setEDNS(final int level) {
        this.setEDNS(level, 0, 0, null);
    }
    
    public void setTSIGKey(final TSIG key) {
        this.tsig = key;
    }
    
    TSIG getTSIGKey() {
        return this.tsig;
    }
    
    public void setTimeout(final int secs, final int msecs) {
        this.timeoutValue = secs * 1000L + msecs;
    }
    
    public void setTimeout(final int secs) {
        this.setTimeout(secs, 0);
    }
    
    long getTimeout() {
        return this.timeoutValue;
    }
    
    private Message parseMessage(final byte[] b) throws WireParseException {
        try {
            return new Message(b);
        }
        catch (IOException e) {
            if (Options.check("verbose")) {
                e.printStackTrace();
            }
            if (!(e instanceof WireParseException)) {
                e = new WireParseException("Error parsing message");
            }
            throw (WireParseException)e;
        }
    }
    
    private void verifyTSIG(final Message query, final Message response, final byte[] b, final TSIG tsig) {
        if (tsig == null) {
            return;
        }
        final int error = tsig.verify(response, b, query.getTSIG());
        if (Options.check("verbose")) {
            System.err.println("TSIG verify: " + Rcode.string(error));
        }
    }
    
    private void applyEDNS(final Message query) {
        if (this.queryOPT == null || query.getOPT() != null) {
            return;
        }
        query.addRecord(this.queryOPT, 3);
    }
    
    private int maxUDPSize(final Message query) {
        final OPTRecord opt = query.getOPT();
        if (opt == null) {
            return 512;
        }
        return opt.getPayloadSize();
    }
    
    public Message send(Message query) throws IOException {
        if (Options.check("verbose")) {
            System.err.println("Sending to " + this.address.getAddress().getHostAddress() + ":" + this.address.getPort());
        }
        if (query.getHeader().getOpcode() == 0) {
            final Record question = query.getQuestion();
            if (question != null && question.getType() == 252) {
                return this.sendAXFR(query);
            }
        }
        query = (Message)query.clone();
        this.applyEDNS(query);
        if (this.tsig != null) {
            this.tsig.apply(query, null);
        }
        final byte[] out = query.toWire(65535);
        final int udpSize = this.maxUDPSize(query);
        boolean tcp = false;
        final long endTime = System.currentTimeMillis() + this.timeoutValue;
        while (true) {
            if (this.useTCP || out.length > udpSize) {
                tcp = true;
            }
            byte[] in;
            if (tcp) {
                in = TCPClient.sendrecv(this.localAddress, this.address, out, endTime);
            }
            else {
                in = UDPClient.sendrecv(this.localAddress, this.address, out, udpSize, endTime);
            }
            if (in.length < 12) {
                throw new WireParseException("invalid DNS header - too short");
            }
            final int id = ((in[0] & 0xFF) << 8) + (in[1] & 0xFF);
            final int qid = query.getHeader().getID();
            if (id != qid) {
                final String error = "invalid message id: expected " + qid + "; got id " + id;
                if (tcp) {
                    throw new WireParseException(error);
                }
                if (!Options.check("verbose")) {
                    continue;
                }
                System.err.println(error);
            }
            else {
                final Message response = this.parseMessage(in);
                this.verifyTSIG(query, response, in, this.tsig);
                if (tcp || this.ignoreTruncation || !response.getHeader().getFlag(6)) {
                    return response;
                }
                tcp = true;
            }
        }
    }
    
    public Object sendAsync(final Message query, final ResolverListener listener) {
        final Object id;
        synchronized (this) {
            id = new Integer(SimpleResolver.uniqueID++);
        }
        final Record question = query.getQuestion();
        String qname;
        if (question != null) {
            qname = question.getName().toString();
        }
        else {
            qname = "(none)";
        }
        final String name = this.getClass() + ": " + qname;
        final Thread thread = new ResolveThread(this, query, id, listener);
        thread.setName(name);
        thread.setDaemon(true);
        thread.start();
        return id;
    }
    
    private Message sendAXFR(final Message query) throws IOException {
        final Name qname = query.getQuestion().getName();
        final ZoneTransferIn xfrin = ZoneTransferIn.newAXFR(qname, this.address, this.tsig);
        xfrin.setTimeout((int)(this.getTimeout() / 1000L));
        xfrin.setLocalAddress(this.localAddress);
        try {
            xfrin.run();
        }
        catch (ZoneTransferException e) {
            throw new WireParseException(e.getMessage());
        }
        final List records = xfrin.getAXFR();
        final Message response = new Message(query.getHeader().getID());
        response.getHeader().setFlag(5);
        response.getHeader().setFlag(0);
        response.addRecord(query.getQuestion(), 0);
        final Iterator it = records.iterator();
        while (it.hasNext()) {
            response.addRecord(it.next(), 1);
        }
        return response;
    }
    
    static {
        SimpleResolver.defaultResolver = "localhost";
        SimpleResolver.uniqueID = 0;
    }
}
