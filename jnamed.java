import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.io.DataInputStream;
import org.xbill.DNS.OPTRecord;
import org.xbill.DNS.Type;
import org.xbill.DNS.Header;
import java.io.DataOutputStream;
import java.net.Socket;
import org.xbill.DNS.TSIGRecord;
import org.xbill.DNS.DNAMERecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.NameTooLongException;
import org.xbill.DNS.SetResponse;
import org.xbill.DNS.Record;
import org.xbill.DNS.Message;
import org.xbill.DNS.RRset;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.Zone;
import org.xbill.DNS.Name;
import org.xbill.DNS.ZoneTransferException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.xbill.DNS.Address;
import org.xbill.DNS.Cache;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.net.InetAddress;
import java.util.Map;

// 
// Decompiled by Procyon v0.5.36
// 

public class jnamed
{
    static final int FLAG_DNSSECOK = 1;
    static final int FLAG_SIGONLY = 2;
    Map caches;
    Map znames;
    Map TSIGs;
    
    private static String addrport(final InetAddress addr, final int port) {
        return addr.getHostAddress() + "#" + port;
    }
    
    public jnamed(final String conffile) throws IOException, ZoneTransferException {
        final List ports = new ArrayList();
        final List addresses = new ArrayList();
        FileInputStream fs;
        BufferedReader br;
        try {
            fs = new FileInputStream(conffile);
            final InputStreamReader isr = new InputStreamReader(fs);
            br = new BufferedReader(isr);
        }
        catch (Exception e) {
            System.out.println("Cannot open " + conffile);
            return;
        }
        try {
            this.caches = new HashMap();
            this.znames = new HashMap();
            this.TSIGs = new HashMap();
            String line = null;
            while ((line = br.readLine()) != null) {
                final StringTokenizer st = new StringTokenizer(line);
                if (!st.hasMoreTokens()) {
                    continue;
                }
                final String keyword = st.nextToken();
                if (!st.hasMoreTokens()) {
                    System.out.println("Invalid line: " + line);
                }
                else {
                    if (keyword.charAt(0) == '#') {
                        continue;
                    }
                    if (keyword.equals("primary")) {
                        this.addPrimaryZone(st.nextToken(), st.nextToken());
                    }
                    else if (keyword.equals("secondary")) {
                        this.addSecondaryZone(st.nextToken(), st.nextToken());
                    }
                    else if (keyword.equals("cache")) {
                        final Cache cache = new Cache(st.nextToken());
                        this.caches.put(new Integer(1), cache);
                    }
                    else if (keyword.equals("key")) {
                        final String s1 = st.nextToken();
                        final String s2 = st.nextToken();
                        if (st.hasMoreTokens()) {
                            this.addTSIG(s1, s2, st.nextToken());
                        }
                        else {
                            this.addTSIG("hmac-md5", s1, s2);
                        }
                    }
                    else if (keyword.equals("port")) {
                        ports.add(Integer.valueOf(st.nextToken()));
                    }
                    else if (keyword.equals("address")) {
                        final String addr = st.nextToken();
                        addresses.add(Address.getByAddress(addr));
                    }
                    else {
                        System.out.println("unknown keyword: " + keyword);
                    }
                }
            }
            if (ports.size() == 0) {
                ports.add(new Integer(53));
            }
            if (addresses.size() == 0) {
                addresses.add(Address.getByAddress("0.0.0.0"));
            }
            for (final InetAddress addr2 : addresses) {
                for (final int port : ports) {
                    this.addUDP(addr2, port);
                    this.addTCP(addr2, port);
                    System.out.println("jnamed: listening on " + addrport(addr2, port));
                }
            }
            System.out.println("jnamed: running");
        }
        finally {
            fs.close();
        }
    }
    
    public void addPrimaryZone(final String zname, final String zonefile) throws IOException {
        Name origin = null;
        if (zname != null) {
            origin = Name.fromString(zname, Name.root);
        }
        final Zone newzone = new Zone(origin, zonefile);
        this.znames.put(newzone.getOrigin(), newzone);
    }
    
    public void addSecondaryZone(final String zone, final String remote) throws IOException, ZoneTransferException {
        final Name zname = Name.fromString(zone, Name.root);
        final Zone newzone = new Zone(zname, 1, remote);
        this.znames.put(zname, newzone);
    }
    
    public void addTSIG(final String algstr, final String namestr, final String key) throws IOException {
        final Name name = Name.fromString(namestr, Name.root);
        this.TSIGs.put(name, new TSIG(algstr, namestr, key));
    }
    
    public Cache getCache(final int dclass) {
        Cache c = this.caches.get(new Integer(dclass));
        if (c == null) {
            c = new Cache(dclass);
            this.caches.put(new Integer(dclass), c);
        }
        return c;
    }
    
    public Zone findBestZone(final Name name) {
        Zone foundzone = null;
        foundzone = this.znames.get(name);
        if (foundzone != null) {
            return foundzone;
        }
        for (int labels = name.labels(), i = 1; i < labels; ++i) {
            final Name tname = new Name(name, i);
            foundzone = this.znames.get(tname);
            if (foundzone != null) {
                return foundzone;
            }
        }
        return null;
    }
    
    public RRset findExactMatch(final Name name, final int type, final int dclass, final boolean glue) {
        final Zone zone = this.findBestZone(name);
        if (zone != null) {
            return zone.findExactMatch(name, type);
        }
        final Cache cache = this.getCache(dclass);
        RRset[] rrsets;
        if (glue) {
            rrsets = cache.findAnyRecords(name, type);
        }
        else {
            rrsets = cache.findRecords(name, type);
        }
        if (rrsets == null) {
            return null;
        }
        return rrsets[0];
    }
    
    void addRRset(final Name name, final Message response, final RRset rrset, final int section, final int flags) {
        for (int s = 1; s <= section; ++s) {
            if (response.findRRset(name, rrset.getType(), s)) {
                return;
            }
        }
        if ((flags & 0x2) == 0x0) {
            final Iterator it = rrset.rrs();
            while (it.hasNext()) {
                Record r = it.next();
                if (r.getName().isWild() && !name.isWild()) {
                    r = r.withName(name);
                }
                response.addRecord(r, section);
            }
        }
        if ((flags & 0x3) != 0x0) {
            final Iterator it = rrset.sigs();
            while (it.hasNext()) {
                Record r = it.next();
                if (r.getName().isWild() && !name.isWild()) {
                    r = r.withName(name);
                }
                response.addRecord(r, section);
            }
        }
    }
    
    private final void addSOA(final Message response, final Zone zone) {
        response.addRecord(zone.getSOA(), 2);
    }
    
    private final void addNS(final Message response, final Zone zone, final int flags) {
        final RRset nsRecords = zone.getNS();
        this.addRRset(nsRecords.getName(), response, nsRecords, 2, flags);
    }
    
    private final void addCacheNS(final Message response, final Cache cache, final Name name) {
        final SetResponse sr = cache.lookupRecords(name, 2, 0);
        if (!sr.isDelegation()) {
            return;
        }
        final RRset nsRecords = sr.getNS();
        final Iterator it = nsRecords.rrs();
        while (it.hasNext()) {
            final Record r = it.next();
            response.addRecord(r, 2);
        }
    }
    
    private void addGlue(final Message response, final Name name, final int flags) {
        final RRset a = this.findExactMatch(name, 1, 1, true);
        if (a == null) {
            return;
        }
        this.addRRset(name, response, a, 3, flags);
    }
    
    private void addAdditional2(final Message response, final int section, final int flags) {
        final Record[] records = response.getSectionArray(section);
        for (int i = 0; i < records.length; ++i) {
            final Record r = records[i];
            final Name glueName = r.getAdditionalName();
            if (glueName != null) {
                this.addGlue(response, glueName, flags);
            }
        }
    }
    
    private final void addAdditional(final Message response, final int flags) {
        this.addAdditional2(response, 1, flags);
        this.addAdditional2(response, 2, flags);
    }
    
    byte addAnswer(final Message response, final Name name, int type, final int dclass, final int iterations, int flags) {
        byte rcode = 0;
        if (iterations > 6) {
            return 0;
        }
        if (type == 24 || type == 46) {
            type = 255;
            flags |= 0x2;
        }
        final Zone zone = this.findBestZone(name);
        SetResponse sr;
        if (zone != null) {
            sr = zone.findRecords(name, type);
        }
        else {
            final Cache cache = this.getCache(dclass);
            sr = cache.lookupRecords(name, type, 3);
        }
        if (sr.isUnknown()) {
            this.addCacheNS(response, this.getCache(dclass), name);
        }
        if (sr.isNXDOMAIN()) {
            response.getHeader().setRcode(3);
            if (zone != null) {
                this.addSOA(response, zone);
                if (iterations == 0) {
                    response.getHeader().setFlag(5);
                }
            }
            rcode = 3;
        }
        else if (sr.isNXRRSET()) {
            if (zone != null) {
                this.addSOA(response, zone);
                if (iterations == 0) {
                    response.getHeader().setFlag(5);
                }
            }
        }
        else if (sr.isDelegation()) {
            final RRset nsRecords = sr.getNS();
            this.addRRset(nsRecords.getName(), response, nsRecords, 2, flags);
        }
        else if (sr.isCNAME()) {
            final CNAMERecord cname = sr.getCNAME();
            final RRset rrset = new RRset(cname);
            this.addRRset(name, response, rrset, 1, flags);
            if (zone != null && iterations == 0) {
                response.getHeader().setFlag(5);
            }
            rcode = this.addAnswer(response, cname.getTarget(), type, dclass, iterations + 1, flags);
        }
        else if (sr.isDNAME()) {
            final DNAMERecord dname = sr.getDNAME();
            RRset rrset = new RRset(dname);
            this.addRRset(name, response, rrset, 1, flags);
            Name newname;
            try {
                newname = name.fromDNAME(dname);
            }
            catch (NameTooLongException e) {
                return 6;
            }
            rrset = new RRset(new CNAMERecord(name, dclass, 0L, newname));
            this.addRRset(name, response, rrset, 1, flags);
            if (zone != null && iterations == 0) {
                response.getHeader().setFlag(5);
            }
            rcode = this.addAnswer(response, newname, type, dclass, iterations + 1, flags);
        }
        else if (sr.isSuccessful()) {
            final RRset[] rrsets = sr.answers();
            for (int i = 0; i < rrsets.length; ++i) {
                this.addRRset(name, response, rrsets[i], 1, flags);
            }
            if (zone != null) {
                this.addNS(response, zone, flags);
                if (iterations == 0) {
                    response.getHeader().setFlag(5);
                }
            }
            else {
                this.addCacheNS(response, this.getCache(dclass), name);
            }
        }
        return rcode;
    }
    
    byte[] doAXFR(final Name name, final Message query, final TSIG tsig, TSIGRecord qtsig, final Socket s) {
        final Zone zone = this.znames.get(name);
        boolean first = true;
        if (zone == null) {
            return this.errorMessage(query, 5);
        }
        final Iterator it = zone.AXFR();
        try {
            final DataOutputStream dataOut = new DataOutputStream(s.getOutputStream());
            final int id = query.getHeader().getID();
            while (it.hasNext()) {
                final RRset rrset = it.next();
                final Message response = new Message(id);
                final Header header = response.getHeader();
                header.setFlag(0);
                header.setFlag(5);
                this.addRRset(rrset.getName(), response, rrset, 1, 1);
                if (tsig != null) {
                    tsig.applyStream(response, qtsig, first);
                    qtsig = response.getTSIG();
                }
                first = false;
                final byte[] out = response.toWire();
                dataOut.writeShort(out.length);
                dataOut.write(out);
            }
        }
        catch (IOException ex) {
            System.out.println("AXFR failed");
        }
        try {
            s.close();
        }
        catch (IOException ex2) {}
        return null;
    }
    
    byte[] generateReply(final Message query, final byte[] in, final int length, final Socket s) throws IOException {
        int flags = 0;
        final Header header = query.getHeader();
        if (header.getFlag(0)) {
            return null;
        }
        if (header.getRcode() != 0) {
            return this.errorMessage(query, 1);
        }
        if (header.getOpcode() != 0) {
            return this.errorMessage(query, 4);
        }
        final Record queryRecord = query.getQuestion();
        final TSIGRecord queryTSIG = query.getTSIG();
        TSIG tsig = null;
        if (queryTSIG != null) {
            tsig = this.TSIGs.get(queryTSIG.getName());
            if (tsig == null || tsig.verify(query, in, length, null) != 0) {
                return this.formerrMessage(in);
            }
        }
        final OPTRecord queryOPT = query.getOPT();
        if (queryOPT != null && queryOPT.getVersion() > 0) {}
        int maxLength;
        if (s != null) {
            maxLength = 65535;
        }
        else if (queryOPT != null) {
            maxLength = Math.max(queryOPT.getPayloadSize(), 512);
        }
        else {
            maxLength = 512;
        }
        if (queryOPT != null && (queryOPT.getFlags() & 0x8000) != 0x0) {
            flags = 1;
        }
        final Message response = new Message(query.getHeader().getID());
        response.getHeader().setFlag(0);
        if (query.getHeader().getFlag(7)) {
            response.getHeader().setFlag(7);
        }
        response.addRecord(queryRecord, 0);
        final Name name = queryRecord.getName();
        final int type = queryRecord.getType();
        final int dclass = queryRecord.getDClass();
        if (type == 252 && s != null) {
            return this.doAXFR(name, query, tsig, queryTSIG, s);
        }
        if (!Type.isRR(type) && type != 255) {
            return this.errorMessage(query, 4);
        }
        final byte rcode = this.addAnswer(response, name, type, dclass, 0, flags);
        if (rcode != 0 && rcode != 3) {
            return this.errorMessage(query, rcode);
        }
        this.addAdditional(response, flags);
        if (queryOPT != null) {
            final int optflags = (flags == 1) ? 32768 : 0;
            final OPTRecord opt = new OPTRecord(4096, rcode, 0, optflags);
            response.addRecord(opt, 3);
        }
        response.setTSIG(tsig, 0, queryTSIG);
        return response.toWire(maxLength);
    }
    
    byte[] buildErrorMessage(final Header header, final int rcode, final Record question) {
        final Message response = new Message();
        response.setHeader(header);
        for (int i = 0; i < 4; ++i) {
            response.removeAllRecords(i);
        }
        if (rcode == 2) {
            response.addRecord(question, 0);
        }
        header.setRcode(rcode);
        return response.toWire();
    }
    
    public byte[] formerrMessage(final byte[] in) {
        Header header;
        try {
            header = new Header(in);
        }
        catch (IOException e) {
            return null;
        }
        return this.buildErrorMessage(header, 1, null);
    }
    
    public byte[] errorMessage(final Message query, final int rcode) {
        return this.buildErrorMessage(query.getHeader(), rcode, query.getQuestion());
    }
    
    public void TCPclient(final Socket s) {
        try {
            final InputStream is = s.getInputStream();
            final DataInputStream dataIn = new DataInputStream(is);
            final int inLength = dataIn.readUnsignedShort();
            final byte[] in = new byte[inLength];
            dataIn.readFully(in);
            byte[] response = null;
            try {
                final Message query = new Message(in);
                response = this.generateReply(query, in, in.length, s);
                if (response == null) {
                    return;
                }
            }
            catch (IOException e2) {
                response = this.formerrMessage(in);
            }
            final DataOutputStream dataOut = new DataOutputStream(s.getOutputStream());
            dataOut.writeShort(response.length);
            dataOut.write(response);
        }
        catch (IOException e) {
            System.out.println("TCPclient(" + addrport(s.getLocalAddress(), s.getLocalPort()) + "): " + e);
        }
        finally {
            try {
                s.close();
            }
            catch (IOException ex) {}
        }
    }
    
    public void serveTCP(final InetAddress addr, final int port) {
        try {
            final ServerSocket sock = new ServerSocket(port, 128, addr);
            while (true) {
                final Socket s = sock.accept();
                final Thread t = new Thread(new Runnable() {
                    public void run() {
                        jnamed.this.TCPclient(s);
                    }
                });
                t.start();
            }
        }
        catch (IOException e) {
            System.out.println("serveTCP(" + addrport(addr, port) + "): " + e);
        }
    }
    
    public void serveUDP(final InetAddress addr, final int port) {
        try {
            final DatagramSocket sock = new DatagramSocket(port, addr);
            final short udpLength = 512;
            final byte[] in = new byte[512];
            final DatagramPacket indp = new DatagramPacket(in, in.length);
            DatagramPacket outdp = null;
            while (true) {
                indp.setLength(in.length);
                try {
                    sock.receive(indp);
                }
                catch (InterruptedIOException e2) {
                    continue;
                }
                byte[] response = null;
                try {
                    final Message query = new Message(in);
                    response = this.generateReply(query, in, indp.getLength(), null);
                    if (response == null) {
                        continue;
                    }
                }
                catch (IOException e3) {
                    response = this.formerrMessage(in);
                }
                if (outdp == null) {
                    outdp = new DatagramPacket(response, response.length, indp.getAddress(), indp.getPort());
                }
                else {
                    outdp.setData(response);
                    outdp.setLength(response.length);
                    outdp.setAddress(indp.getAddress());
                    outdp.setPort(indp.getPort());
                }
                sock.send(outdp);
            }
        }
        catch (IOException e) {
            System.out.println("serveUDP(" + addrport(addr, port) + "): " + e);
        }
    }
    
    public void addTCP(final InetAddress addr, final int port) {
        final Thread t = new Thread(new Runnable() {
            public void run() {
                jnamed.this.serveTCP(addr, port);
            }
        });
        t.start();
    }
    
    public void addUDP(final InetAddress addr, final int port) {
        final Thread t = new Thread(new Runnable() {
            public void run() {
                jnamed.this.serveUDP(addr, port);
            }
        });
        t.start();
    }
    
    public static void main(final String[] args) {
        if (args.length > 1) {
            System.out.println("usage: jnamed [conf]");
            System.exit(0);
        }
        try {
            String conf;
            if (args.length == 1) {
                conf = args[0];
            }
            else {
                conf = "jnamed.conf";
            }
            final jnamed s = new jnamed(conf);
        }
        catch (IOException e) {
            System.out.println(e);
        }
        catch (ZoneTransferException e2) {
            System.out.println(e2);
        }
    }
}
