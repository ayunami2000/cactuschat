import org.xbill.DNS.Section;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.Rcode;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import org.xbill.DNS.Type;
import org.xbill.DNS.TTL;
import org.xbill.DNS.Record;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.net.SocketException;
import java.io.InterruptedIOException;
import org.xbill.DNS.TextParseException;
import java.util.Date;
import org.xbill.DNS.DClass;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Tokenizer;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.io.InputStream;
import java.io.PrintStream;
import org.xbill.DNS.Name;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Message;

// 
// Decompiled by Procyon v0.5.36
// 

public class update
{
    Message query;
    Message response;
    Resolver res;
    String server;
    Name zone;
    long defaultTTL;
    int defaultClass;
    PrintStream log;
    
    void print(final Object o) {
        System.out.println(o);
        if (this.log != null) {
            this.log.println(o);
        }
    }
    
    public Message newMessage() {
        final Message msg = new Message();
        msg.getHeader().setOpcode(5);
        return msg;
    }
    
    public update(final InputStream in) throws IOException {
        this.server = null;
        this.zone = Name.root;
        this.defaultClass = 1;
        this.log = null;
        final List inputs = new LinkedList();
        final List istreams = new LinkedList();
        this.query = this.newMessage();
        final InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);
        inputs.add(br);
        istreams.add(in);
    Label_0088_Outer:
        while (true) {
            while (true) {
                try {
                    while (true) {
                        String line = null;
                        do {
                            final InputStream is = istreams.get(0);
                            br = inputs.get(0);
                            if (is == System.in) {
                                System.out.print("> ");
                            }
                            line = br.readLine();
                            if (line == null) {
                                br.close();
                                inputs.remove(0);
                                istreams.remove(0);
                                if (inputs.isEmpty()) {
                                    return;
                                }
                                continue Label_0088_Outer;
                            }
                        } while (line == null);
                        if (this.log != null) {
                            this.log.println("> " + line);
                        }
                        if (line.length() == 0 || line.charAt(0) == '#') {
                            continue Label_0088_Outer;
                        }
                        if (line.charAt(0) == '>') {
                            line = line.substring(1);
                        }
                        final Tokenizer st = new Tokenizer(line);
                        Tokenizer.Token token = st.get();
                        if (token.isEOL()) {
                            continue Label_0088_Outer;
                        }
                        final String operation = token.value;
                        if (operation.equals("server")) {
                            this.server = st.getString();
                            this.res = new SimpleResolver(this.server);
                            token = st.get();
                            if (!token.isString()) {
                                continue Label_0088_Outer;
                            }
                            final String portstr = token.value;
                            this.res.setPort(Short.parseShort(portstr));
                        }
                        else if (operation.equals("key")) {
                            final String keyname = st.getString();
                            final String keydata = st.getString();
                            if (this.res == null) {
                                this.res = new SimpleResolver(this.server);
                            }
                            this.res.setTSIGKey(new TSIG(keyname, keydata));
                        }
                        else if (operation.equals("edns")) {
                            if (this.res == null) {
                                this.res = new SimpleResolver(this.server);
                            }
                            this.res.setEDNS(st.getUInt16());
                        }
                        else if (operation.equals("port")) {
                            if (this.res == null) {
                                this.res = new SimpleResolver(this.server);
                            }
                            this.res.setPort(st.getUInt16());
                        }
                        else if (operation.equals("tcp")) {
                            if (this.res == null) {
                                this.res = new SimpleResolver(this.server);
                            }
                            this.res.setTCP(true);
                        }
                        else if (operation.equals("class")) {
                            final String classStr = st.getString();
                            final int newClass = DClass.value(classStr);
                            if (newClass > 0) {
                                this.defaultClass = newClass;
                            }
                            else {
                                this.print("Invalid class " + classStr);
                            }
                        }
                        else if (operation.equals("ttl")) {
                            this.defaultTTL = st.getTTL();
                        }
                        else if (operation.equals("origin") || operation.equals("zone")) {
                            this.zone = st.getName(Name.root);
                        }
                        else if (operation.equals("require")) {
                            this.doRequire(st);
                        }
                        else if (operation.equals("prohibit")) {
                            this.doProhibit(st);
                        }
                        else if (operation.equals("add")) {
                            this.doAdd(st);
                        }
                        else if (operation.equals("delete")) {
                            this.doDelete(st);
                        }
                        else if (operation.equals("glue")) {
                            this.doGlue(st);
                        }
                        else if (operation.equals("help") || operation.equals("?")) {
                            token = st.get();
                            if (token.isString()) {
                                help(token.value);
                            }
                            else {
                                help(null);
                            }
                        }
                        else if (operation.equals("echo")) {
                            this.print(line.substring(4).trim());
                        }
                        else if (operation.equals("send")) {
                            this.sendUpdate();
                            this.query = this.newMessage();
                        }
                        else if (operation.equals("show")) {
                            this.print(this.query);
                        }
                        else if (operation.equals("clear")) {
                            this.query = this.newMessage();
                        }
                        else if (operation.equals("query")) {
                            this.doQuery(st);
                        }
                        else if (operation.equals("quit") || operation.equals("q")) {
                            if (this.log != null) {
                                this.log.close();
                            }
                            for (final BufferedReader tbr : inputs) {
                                tbr.close();
                            }
                            System.exit(0);
                        }
                        else if (operation.equals("file")) {
                            this.doFile(st, inputs, istreams);
                        }
                        else if (operation.equals("log")) {
                            this.doLog(st);
                        }
                        else if (operation.equals("assert")) {
                            if (!this.doAssert(st)) {
                                return;
                            }
                            continue Label_0088_Outer;
                        }
                        else if (operation.equals("sleep")) {
                            final long interval = st.getUInt32();
                            try {
                                Thread.sleep(interval);
                            }
                            catch (InterruptedException ex) {}
                        }
                        else if (operation.equals("date")) {
                            final Date now = new Date();
                            token = st.get();
                            if (token.isString() && token.value.equals("-ms")) {
                                this.print(Long.toString(now.getTime()));
                            }
                            else {
                                this.print(now);
                            }
                        }
                        else {
                            this.print("invalid keyword: " + operation);
                        }
                    }
                }
                catch (TextParseException tpe) {
                    System.out.println(tpe.getMessage());
                    continue Label_0088_Outer;
                }
                catch (InterruptedIOException iioe) {
                    System.out.println("Operation timed out");
                    continue Label_0088_Outer;
                }
                catch (SocketException se) {
                    System.out.println("Socket error");
                    continue Label_0088_Outer;
                }
                catch (IOException ioe) {
                    System.out.println(ioe);
                    continue Label_0088_Outer;
                }
                continue;
            }
        }
    }
    
    void sendUpdate() throws IOException {
        if (this.query.getHeader().getCount(2) == 0) {
            this.print("Empty update message.  Ignoring.");
            return;
        }
        if (this.query.getHeader().getCount(0) == 0) {
            Name updzone = this.zone;
            int dclass = this.defaultClass;
            if (updzone == null) {
                final Record[] recs = this.query.getSectionArray(2);
                for (int i = 0; i < recs.length; ++i) {
                    if (updzone == null) {
                        updzone = new Name(recs[i].getName(), 1);
                    }
                    if (recs[i].getDClass() != 254 && recs[i].getDClass() != 255) {
                        dclass = recs[i].getDClass();
                        break;
                    }
                }
            }
            final Record soa = Record.newRecord(updzone, 6, dclass);
            this.query.addRecord(soa, 0);
        }
        if (this.res == null) {
            this.res = new SimpleResolver(this.server);
        }
        this.print(this.response = this.res.send(this.query));
    }
    
    Record parseRR(final Tokenizer st, int classValue, final long TTLValue) throws IOException {
        final Name name = st.getName(this.zone);
        String s = st.getString();
        long ttl;
        try {
            ttl = TTL.parseTTL(s);
            s = st.getString();
        }
        catch (NumberFormatException e) {
            ttl = TTLValue;
        }
        if (DClass.value(s) >= 0) {
            classValue = DClass.value(s);
            s = st.getString();
        }
        final int type;
        if ((type = Type.value(s)) < 0) {
            throw new IOException("Invalid type: " + s);
        }
        final Record record = Record.fromString(name, type, classValue, ttl, st, this.zone);
        if (record != null) {
            return record;
        }
        throw new IOException("Parse error");
    }
    
    void doRequire(final Tokenizer st) throws IOException {
        final Name name = st.getName(this.zone);
        Tokenizer.Token token = st.get();
        Record record;
        if (token.isString()) {
            final int type;
            if ((type = Type.value(token.value)) < 0) {
                throw new IOException("Invalid type: " + token.value);
            }
            token = st.get();
            final boolean iseol = token.isEOL();
            st.unget();
            if (!iseol) {
                record = Record.fromString(name, type, this.defaultClass, 0L, st, this.zone);
            }
            else {
                record = Record.newRecord(name, type, 255, 0L);
            }
        }
        else {
            record = Record.newRecord(name, 255, 255, 0L);
        }
        this.query.addRecord(record, 1);
        this.print(record);
    }
    
    void doProhibit(final Tokenizer st) throws IOException {
        final Name name = st.getName(this.zone);
        final Tokenizer.Token token = st.get();
        int type;
        if (token.isString()) {
            if ((type = Type.value(token.value)) < 0) {
                throw new IOException("Invalid type: " + token.value);
            }
        }
        else {
            type = 255;
        }
        final Record record = Record.newRecord(name, type, 254, 0L);
        this.query.addRecord(record, 1);
        this.print(record);
    }
    
    void doAdd(final Tokenizer st) throws IOException {
        final Record record = this.parseRR(st, this.defaultClass, this.defaultTTL);
        this.query.addRecord(record, 2);
        this.print(record);
    }
    
    void doDelete(final Tokenizer st) throws IOException {
        final Name name = st.getName(this.zone);
        Tokenizer.Token token = st.get();
        Record record;
        if (token.isString()) {
            String s = token.value;
            if (DClass.value(s) >= 0) {
                s = st.getString();
            }
            final int type;
            if ((type = Type.value(s)) < 0) {
                throw new IOException("Invalid type: " + s);
            }
            token = st.get();
            final boolean iseol = token.isEOL();
            st.unget();
            if (!iseol) {
                record = Record.fromString(name, type, 254, 0L, st, this.zone);
            }
            else {
                record = Record.newRecord(name, type, 255, 0L);
            }
        }
        else {
            record = Record.newRecord(name, 255, 255, 0L);
        }
        this.query.addRecord(record, 2);
        this.print(record);
    }
    
    void doGlue(final Tokenizer st) throws IOException {
        final Record record = this.parseRR(st, this.defaultClass, this.defaultTTL);
        this.query.addRecord(record, 3);
        this.print(record);
    }
    
    void doQuery(final Tokenizer st) throws IOException {
        Name name = null;
        int type = 1;
        int dclass = this.defaultClass;
        name = st.getName(this.zone);
        Tokenizer.Token token = st.get();
        if (token.isString()) {
            type = Type.value(token.value);
            if (type < 0) {
                throw new IOException("Invalid type");
            }
            token = st.get();
            if (token.isString()) {
                dclass = DClass.value(token.value);
                if (dclass < 0) {
                    throw new IOException("Invalid class");
                }
            }
        }
        final Record rec = Record.newRecord(name, type, dclass);
        final Message newQuery = Message.newQuery(rec);
        if (this.res == null) {
            this.res = new SimpleResolver(this.server);
        }
        this.print(this.response = this.res.send(newQuery));
    }
    
    void doFile(final Tokenizer st, final List inputs, final List istreams) throws IOException {
        final String s = st.getString();
        try {
            InputStream is;
            if (s.equals("-")) {
                is = System.in;
            }
            else {
                is = new FileInputStream(s);
            }
            istreams.add(0, is);
            inputs.add(0, new BufferedReader(new InputStreamReader(is)));
        }
        catch (FileNotFoundException e) {
            this.print(s + " not found");
        }
    }
    
    void doLog(final Tokenizer st) throws IOException {
        final String s = st.getString();
        try {
            final FileOutputStream fos = new FileOutputStream(s);
            this.log = new PrintStream(fos);
        }
        catch (Exception e) {
            this.print("Error opening " + s);
        }
    }
    
    boolean doAssert(final Tokenizer st) throws IOException {
        final String field = st.getString();
        final String expected = st.getString();
        String value = null;
        boolean flag = true;
        if (this.response == null) {
            this.print("No response has been received");
            return true;
        }
        if (field.equalsIgnoreCase("rcode")) {
            final int rcode = this.response.getHeader().getRcode();
            if (rcode != Rcode.value(expected)) {
                value = Rcode.string(rcode);
                flag = false;
            }
        }
        else if (field.equalsIgnoreCase("serial")) {
            final Record[] answers = this.response.getSectionArray(1);
            if (answers.length < 1 || !(answers[0] instanceof SOARecord)) {
                this.print("Invalid response (no SOA)");
            }
            else {
                final SOARecord soa = (SOARecord)answers[0];
                final long serial = soa.getSerial();
                if (serial != Long.parseLong(expected)) {
                    value = Long.toString(serial);
                    flag = false;
                }
            }
        }
        else if (field.equalsIgnoreCase("tsig")) {
            if (this.response.isSigned()) {
                if (this.response.isVerified()) {
                    value = "ok";
                }
                else {
                    value = "failed";
                }
            }
            else {
                value = "unsigned";
            }
            if (!value.equalsIgnoreCase(expected)) {
                flag = false;
            }
        }
        else {
            final int section;
            if ((section = Section.value(field)) >= 0) {
                final int count = this.response.getHeader().getCount(section);
                if (count != Integer.parseInt(expected)) {
                    value = new Integer(count).toString();
                    flag = false;
                }
            }
            else {
                this.print("Invalid assertion keyword: " + field);
            }
        }
        if (!flag) {
            this.print("Expected " + field + " " + expected + ", received " + value);
            while (true) {
                final Tokenizer.Token token = st.get();
                if (!token.isString()) {
                    break;
                }
                this.print(token.value);
            }
            st.unget();
        }
        return flag;
    }
    
    static void help(String topic) {
        System.out.println();
        if (topic == null) {
            System.out.println("The following are supported commands:\nadd      assert   class    clear    date     delete\necho     edns     file     glue     help     key\nlog      port     prohibit query    quit     require\nsend     server   show     sleep    tcp      ttl\nzone     #\n");
            return;
        }
        topic = topic.toLowerCase();
        if (topic.equals("add")) {
            System.out.println("add <name> [ttl] [class] <type> <data>\n\nspecify a record to be added\n");
        }
        else if (topic.equals("assert")) {
            System.out.println("assert <field> <value> [msg]\n\nasserts that the value of the field in the last\nresponse matches the value specified.  If not,\nthe message is printed (if present) and the\nprogram exits.  The field may be any of <rcode>,\n<serial>, <tsig>, <qu>, <an>, <au>, or <ad>.\n");
        }
        else if (topic.equals("class")) {
            System.out.println("class <class>\n\nclass of the zone to be updated (default: IN)\n");
        }
        else if (topic.equals("clear")) {
            System.out.println("clear\n\nclears the current update packet\n");
        }
        else if (topic.equals("date")) {
            System.out.println("date [-ms]\n\nprints the current date and time in human readable\nformat or as the number of milliseconds since the\nepoch");
        }
        else if (topic.equals("delete")) {
            System.out.println("delete <name> [ttl] [class] <type> <data> \ndelete <name> <type> \ndelete <name>\n\nspecify a record or set to be deleted, or that\nall records at a name should be deleted\n");
        }
        else if (topic.equals("echo")) {
            System.out.println("echo <text>\n\nprints the text\n");
        }
        else if (topic.equals("edns")) {
            System.out.println("edns <level>\n\nEDNS level specified when sending messages\n");
        }
        else if (topic.equals("file")) {
            System.out.println("file <file>\n\nopens the specified file as the new input source\n(- represents stdin)\n");
        }
        else if (topic.equals("glue")) {
            System.out.println("glue <name> [ttl] [class] <type> <data>\n\nspecify an additional record\n");
        }
        else if (topic.equals("help")) {
            System.out.println("help\nhelp [topic]\n\nprints a list of commands or help about a specific\ncommand\n");
        }
        else if (topic.equals("key")) {
            System.out.println("key <name> <data>\n\nTSIG key used to sign messages\n");
        }
        else if (topic.equals("log")) {
            System.out.println("log <file>\n\nopens the specified file and uses it to log output\n");
        }
        else if (topic.equals("port")) {
            System.out.println("port <port>\n\nUDP/TCP port messages are sent to (default: 53)\n");
        }
        else if (topic.equals("prohibit")) {
            System.out.println("prohibit <name> <type> \nprohibit <name>\n\nrequire that a set or name is not present\n");
        }
        else if (topic.equals("query")) {
            System.out.println("query <name> [type [class]] \n\nissues a query\n");
        }
        else if (topic.equals("q") || topic.equals("quit")) {
            System.out.println("quit\n\nquits the program\n");
        }
        else if (topic.equals("require")) {
            System.out.println("require <name> [ttl] [class] <type> <data> \nrequire <name> <type> \nrequire <name>\n\nrequire that a record, set, or name is present\n");
        }
        else if (topic.equals("send")) {
            System.out.println("send\n\nsends and resets the current update packet\n");
        }
        else if (topic.equals("server")) {
            System.out.println("server <name> [port]\n\nserver that receives send updates/queries\n");
        }
        else if (topic.equals("show")) {
            System.out.println("show\n\nshows the current update packet\n");
        }
        else if (topic.equals("sleep")) {
            System.out.println("sleep <milliseconds>\n\npause for interval before next command\n");
        }
        else if (topic.equals("tcp")) {
            System.out.println("tcp\n\nTCP should be used to send all messages\n");
        }
        else if (topic.equals("ttl")) {
            System.out.println("ttl <ttl>\n\ndefault ttl of added records (default: 0)\n");
        }
        else if (topic.equals("zone") || topic.equals("origin")) {
            System.out.println("zone <zone>\n\nzone to update (default: .\n");
        }
        else if (topic.equals("#")) {
            System.out.println("# <text>\n\na comment\n");
        }
        else {
            System.out.println("Topic '" + topic + "' unrecognized\n");
        }
    }
    
    public static void main(final String[] args) throws IOException {
        InputStream in = null;
        if (args.length >= 1) {
            try {
                in = new FileInputStream(args[0]);
            }
            catch (FileNotFoundException e) {
                System.out.println(args[0] + " not found.");
                System.exit(1);
            }
        }
        else {
            in = System.in;
        }
        final update u = new update(in);
    }
}
