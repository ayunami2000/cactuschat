import java.util.List;
import org.xbill.DNS.TSIG;
import java.net.InetAddress;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Type;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Record;
import java.io.IOException;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;

// 
// Decompiled by Procyon v0.5.36
// 

public class dig
{
    static Name name;
    static int type;
    static int dclass;
    
    static void usage() {
        System.out.println("Usage: dig [@server] name [<type>] [<class>] [options]");
        System.exit(0);
    }
    
    static void doQuery(final Message response, final long ms) throws IOException {
        System.out.println("; java dig 0.0");
        System.out.println(response);
        System.out.println(";; Query time: " + ms + " ms");
    }
    
    static void doAXFR(final Message response) throws IOException {
        System.out.println("; java dig 0.0 <> " + dig.name + " axfr");
        if (response.isSigned()) {
            System.out.print(";; TSIG ");
            if (response.isVerified()) {
                System.out.println("ok");
            }
            else {
                System.out.println("failed");
            }
        }
        if (response.getRcode() != 0) {
            System.out.println(response);
            return;
        }
        final Record[] records = response.getSectionArray(1);
        for (int i = 0; i < records.length; ++i) {
            System.out.println(records[i]);
        }
        System.out.print(";; done (");
        System.out.print(response.getHeader().getCount(1));
        System.out.print(" records, ");
        System.out.print(response.getHeader().getCount(3));
        System.out.println(" additional)");
    }
    
    public static void main(final String[] argv) throws IOException {
        String server = null;
        SimpleResolver res = null;
        boolean printQuery = false;
        if (argv.length < 1) {
            usage();
        }
        try {
            int arg = 0;
            if (argv[arg].startsWith("@")) {
                server = argv[arg++].substring(1);
            }
            if (server != null) {
                res = new SimpleResolver(server);
            }
            else {
                res = new SimpleResolver();
            }
            final String nameString = argv[arg++];
            if (nameString.equals("-x")) {
                dig.name = ReverseMap.fromAddress(argv[arg++]);
                dig.type = 12;
                dig.dclass = 1;
            }
            else {
                dig.name = Name.fromString(nameString, Name.root);
                dig.type = Type.value(argv[arg]);
                if (dig.type < 0) {
                    dig.type = 1;
                }
                else {
                    ++arg;
                }
                dig.dclass = DClass.value(argv[arg]);
                if (dig.dclass < 0) {
                    dig.dclass = 1;
                }
                else {
                    ++arg;
                }
            }
            while (argv[arg].startsWith("-") && argv[arg].length() > 1) {
                switch (argv[arg].charAt(1)) {
                    case 'p': {
                        String portStr;
                        if (argv[arg].length() > 2) {
                            portStr = argv[arg].substring(2);
                        }
                        else {
                            portStr = argv[++arg];
                        }
                        final int port = Integer.parseInt(portStr);
                        if (port < 0 || port > 65536) {
                            System.out.println("Invalid port");
                            return;
                        }
                        res.setPort(port);
                        break;
                    }
                    case 'b': {
                        String addrStr;
                        if (argv[arg].length() > 2) {
                            addrStr = argv[arg].substring(2);
                        }
                        else {
                            addrStr = argv[++arg];
                        }
                        InetAddress addr;
                        try {
                            addr = InetAddress.getByName(addrStr);
                        }
                        catch (Exception e) {
                            System.out.println("Invalid address");
                            return;
                        }
                        res.setLocalAddress(addr);
                        break;
                    }
                    case 'k': {
                        String key;
                        if (argv[arg].length() > 2) {
                            key = argv[arg].substring(2);
                        }
                        else {
                            key = argv[++arg];
                        }
                        res.setTSIGKey(TSIG.fromString(key));
                        break;
                    }
                    case 't': {
                        res.setTCP(true);
                        break;
                    }
                    case 'i': {
                        res.setIgnoreTruncation(true);
                        break;
                    }
                    case 'e': {
                        String ednsStr;
                        if (argv[arg].length() > 2) {
                            ednsStr = argv[arg].substring(2);
                        }
                        else {
                            ednsStr = argv[++arg];
                        }
                        final int edns = Integer.parseInt(ednsStr);
                        if (edns < 0 || edns > 1) {
                            System.out.println("Unsupported EDNS level: " + edns);
                            return;
                        }
                        res.setEDNS(edns);
                        break;
                    }
                    case 'd': {
                        res.setEDNS(0, 0, 32768, null);
                        break;
                    }
                    case 'q': {
                        printQuery = true;
                        break;
                    }
                    default: {
                        System.out.print("Invalid option: ");
                        System.out.println(argv[arg]);
                        break;
                    }
                }
                ++arg;
            }
        }
        catch (ArrayIndexOutOfBoundsException e2) {
            if (dig.name == null) {
                usage();
            }
        }
        if (res == null) {
            res = new SimpleResolver();
        }
        final Record rec = Record.newRecord(dig.name, dig.type, dig.dclass);
        final Message query = Message.newQuery(rec);
        if (printQuery) {
            System.out.println(query);
        }
        final long startTime = System.currentTimeMillis();
        final Message response = res.send(query);
        final long endTime = System.currentTimeMillis();
        if (dig.type == 252) {
            doAXFR(response);
        }
        else {
            doQuery(response, endTime - startTime);
        }
    }
    
    static {
        dig.name = null;
        dig.type = 1;
        dig.dclass = 1;
    }
}
