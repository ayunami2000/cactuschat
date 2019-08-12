// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.ArrayList;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;
import java.util.List;
import java.net.SocketAddress;

public class ZoneTransferIn
{
    private static final int INITIALSOA = 0;
    private static final int FIRSTDATA = 1;
    private static final int IXFR_DELSOA = 2;
    private static final int IXFR_DEL = 3;
    private static final int IXFR_ADDSOA = 4;
    private static final int IXFR_ADD = 5;
    private static final int AXFR = 6;
    private static final int END = 7;
    private Name zname;
    private int qtype;
    private int dclass;
    private long ixfr_serial;
    private boolean want_fallback;
    private SocketAddress localAddress;
    private SocketAddress address;
    private TCPClient client;
    private TSIG tsig;
    private TSIG.StreamVerifier verifier;
    private long timeout;
    private int state;
    private long end_serial;
    private long current_serial;
    private Record initialsoa;
    private int rtype;
    private List axfr;
    private List ixfr;
    
    private ZoneTransferIn() {
        this.timeout = 900000L;
    }
    
    private ZoneTransferIn(final Name zone, final int xfrtype, final long serial, final boolean fallback, final SocketAddress address, final TSIG key) {
        this.timeout = 900000L;
        this.address = address;
        this.tsig = key;
        if (zone.isAbsolute()) {
            this.zname = zone;
        }
        else {
            try {
                this.zname = Name.concatenate(zone, Name.root);
            }
            catch (NameTooLongException e) {
                throw new IllegalArgumentException("ZoneTransferIn: name too long");
            }
        }
        this.qtype = xfrtype;
        this.dclass = 1;
        this.ixfr_serial = serial;
        this.want_fallback = fallback;
        this.state = 0;
    }
    
    public static ZoneTransferIn newAXFR(final Name zone, final SocketAddress address, final TSIG key) {
        return new ZoneTransferIn(zone, 252, 0L, false, address, key);
    }
    
    public static ZoneTransferIn newAXFR(final Name zone, final String host, int port, final TSIG key) throws UnknownHostException {
        if (port == 0) {
            port = 53;
        }
        return newAXFR(zone, new InetSocketAddress(host, port), key);
    }
    
    public static ZoneTransferIn newAXFR(final Name zone, final String host, final TSIG key) throws UnknownHostException {
        return newAXFR(zone, host, 0, key);
    }
    
    public static ZoneTransferIn newIXFR(final Name zone, final long serial, final boolean fallback, final SocketAddress address, final TSIG key) {
        return new ZoneTransferIn(zone, 251, serial, fallback, address, key);
    }
    
    public static ZoneTransferIn newIXFR(final Name zone, final long serial, final boolean fallback, final String host, int port, final TSIG key) throws UnknownHostException {
        if (port == 0) {
            port = 53;
        }
        return newIXFR(zone, serial, fallback, new InetSocketAddress(host, port), key);
    }
    
    public static ZoneTransferIn newIXFR(final Name zone, final long serial, final boolean fallback, final String host, final TSIG key) throws UnknownHostException {
        return newIXFR(zone, serial, fallback, host, 0, key);
    }
    
    public Name getName() {
        return this.zname;
    }
    
    public int getType() {
        return this.qtype;
    }
    
    public void setTimeout(final int secs) {
        if (secs < 0) {
            throw new IllegalArgumentException("timeout cannot be negative");
        }
        this.timeout = 1000L * secs;
    }
    
    public void setDClass(final int dclass) {
        DClass.check(dclass);
        this.dclass = dclass;
    }
    
    public void setLocalAddress(final SocketAddress addr) {
        this.localAddress = addr;
    }
    
    private void openConnection() throws IOException {
        final long endTime = System.currentTimeMillis() + this.timeout;
        this.client = new TCPClient(endTime);
        if (this.localAddress != null) {
            this.client.bind(this.localAddress);
        }
        this.client.connect(this.address);
    }
    
    private void sendQuery() throws IOException {
        final Record question = Record.newRecord(this.zname, this.qtype, this.dclass);
        final Message query = new Message();
        query.getHeader().setOpcode(0);
        query.addRecord(question, 0);
        if (this.qtype == 251) {
            final Record soa = new SOARecord(this.zname, this.dclass, 0L, Name.root, Name.root, this.ixfr_serial, 0L, 0L, 0L, 0L);
            query.addRecord(soa, 2);
        }
        if (this.tsig != null) {
            this.tsig.apply(query, null);
            this.verifier = new TSIG.StreamVerifier(this.tsig, query.getTSIG());
        }
        final byte[] out = query.toWire(65535);
        this.client.send(out);
    }
    
    private long getSOASerial(final Record rec) {
        final SOARecord soa = (SOARecord)rec;
        return soa.getSerial();
    }
    
    private void logxfr(final String s) {
        if (Options.check("verbose")) {
            System.out.println(this.zname + ": " + s);
        }
    }
    
    private void fail(final String s) throws ZoneTransferException {
        throw new ZoneTransferException(s);
    }
    
    private void fallback() throws ZoneTransferException {
        if (!this.want_fallback) {
            this.fail("server doesn't support IXFR");
        }
        this.logxfr("falling back to AXFR");
        this.qtype = 252;
        this.state = 0;
    }
    
    private void parseRR(final Record rec) throws ZoneTransferException {
        final int type = rec.getType();
        switch (this.state) {
            case 0: {
                if (type != 6) {
                    this.fail("missing initial SOA");
                }
                this.initialsoa = rec;
                this.end_serial = this.getSOASerial(rec);
                if (this.qtype == 251 && Serial.compare(this.end_serial, this.ixfr_serial) <= 0) {
                    this.logxfr("up to date");
                    this.state = 7;
                    break;
                }
                this.state = 1;
                break;
            }
            case 1: {
                if (this.qtype == 251 && type == 6 && this.getSOASerial(rec) == this.ixfr_serial) {
                    this.rtype = 251;
                    this.ixfr = new ArrayList();
                    this.logxfr("got incremental response");
                    this.state = 2;
                }
                else {
                    this.rtype = 252;
                    (this.axfr = new ArrayList()).add(this.initialsoa);
                    this.logxfr("got nonincremental response");
                    this.state = 6;
                }
                this.parseRR(rec);
            }
            case 2: {
                final Delta delta = new Delta();
                this.ixfr.add(delta);
                delta.start = this.getSOASerial(rec);
                delta.deletes.add(rec);
                this.state = 3;
                break;
            }
            case 3: {
                if (type == 6) {
                    this.current_serial = this.getSOASerial(rec);
                    this.state = 4;
                    this.parseRR(rec);
                    return;
                }
                final Delta delta = this.ixfr.get(this.ixfr.size() - 1);
                delta.deletes.add(rec);
                break;
            }
            case 4: {
                final Delta delta = this.ixfr.get(this.ixfr.size() - 1);
                delta.end = this.getSOASerial(rec);
                delta.adds.add(rec);
                this.state = 5;
                break;
            }
            case 5: {
                if (type == 6) {
                    final long soa_serial = this.getSOASerial(rec);
                    if (soa_serial == this.end_serial) {
                        this.state = 7;
                        break;
                    }
                    if (soa_serial == this.current_serial) {
                        this.state = 2;
                        this.parseRR(rec);
                        return;
                    }
                    this.fail("IXFR out of sync: expected serial " + this.current_serial + " , got " + soa_serial);
                }
                final Delta delta = this.ixfr.get(this.ixfr.size() - 1);
                delta.adds.add(rec);
                break;
            }
            case 6: {
                if (type == 1 && rec.getDClass() != this.dclass) {
                    break;
                }
                this.axfr.add(rec);
                if (type == 6) {
                    this.state = 7;
                    break;
                }
                break;
            }
            case 7: {
                this.fail("extra data");
                break;
            }
            default: {
                this.fail("invalid state");
                break;
            }
        }
    }
    
    private void closeConnection() {
        try {
            if (this.client != null) {
                this.client.cleanup();
            }
        }
        catch (IOException ex) {}
    }
    
    private Message parseMessage(final byte[] b) throws WireParseException {
        try {
            return new Message(b);
        }
        catch (IOException e) {
            if (e instanceof WireParseException) {
                throw (WireParseException)e;
            }
            throw new WireParseException("Error parsing message");
        }
    }
    
    private void doxfr() throws IOException, ZoneTransferException {
        this.sendQuery();
        while (this.state != 7) {
            final byte[] in = this.client.recv();
            final Message response = this.parseMessage(in);
            if (response.getHeader().getRcode() == 0 && this.verifier != null) {
                final TSIGRecord tsigrec = response.getTSIG();
                final int error = this.verifier.verify(response, in);
                if (error != 0) {
                    this.fail("TSIG failure");
                }
            }
            final Record[] answers = response.getSectionArray(1);
            if (this.state == 0) {
                final int rcode = response.getRcode();
                if (rcode != 0) {
                    if (this.qtype == 251 && rcode == 4) {
                        this.fallback();
                        this.doxfr();
                        return;
                    }
                    this.fail(Rcode.string(rcode));
                }
                final Record question = response.getQuestion();
                if (question != null && question.getType() != this.qtype) {
                    this.fail("invalid question section");
                }
                if (answers.length == 0 && this.qtype == 251) {
                    this.fallback();
                    this.doxfr();
                    return;
                }
            }
            for (int i = 0; i < answers.length; ++i) {
                this.parseRR(answers[i]);
            }
            if (this.state == 7 && this.verifier != null && !response.isVerified()) {
                this.fail("last message must be signed");
            }
        }
    }
    
    public List run() throws IOException, ZoneTransferException {
        try {
            this.openConnection();
            this.doxfr();
        }
        finally {
            this.closeConnection();
        }
        if (this.axfr != null) {
            return this.axfr;
        }
        return this.ixfr;
    }
    
    public boolean isAXFR() {
        return this.rtype == 252;
    }
    
    public List getAXFR() {
        return this.axfr;
    }
    
    public boolean isIXFR() {
        return this.rtype == 251;
    }
    
    public List getIXFR() {
        return this.ixfr;
    }
    
    public boolean isCurrent() {
        return this.axfr == null && this.ixfr == null;
    }
    
    public static class Delta
    {
        public long start;
        public long end;
        public List adds;
        public List deletes;
        
        private Delta() {
            this.adds = new ArrayList();
            this.deletes = new ArrayList();
        }
    }
}
