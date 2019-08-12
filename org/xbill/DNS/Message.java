// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Message implements Cloneable
{
    public static final int MAXLENGTH = 65535;
    private Header header;
    private List[] sections;
    private int size;
    private TSIG tsigkey;
    private TSIGRecord querytsig;
    private int tsigerror;
    int tsigstart;
    int tsigState;
    int sig0start;
    static final int TSIG_UNSIGNED = 0;
    static final int TSIG_VERIFIED = 1;
    static final int TSIG_INTERMEDIATE = 2;
    static final int TSIG_SIGNED = 3;
    static final int TSIG_FAILED = 4;
    private static Record[] emptyRecordArray;
    private static RRset[] emptyRRsetArray;
    
    private Message(final Header header) {
        this.sections = new List[4];
        this.header = header;
    }
    
    public Message(final int id) {
        this(new Header(id));
    }
    
    public Message() {
        this(new Header());
    }
    
    public static Message newQuery(final Record r) {
        final Message m = new Message();
        m.header.setOpcode(0);
        m.header.setFlag(7);
        m.addRecord(r, 0);
        return m;
    }
    
    public static Message newUpdate(final Name zone) {
        return new Update(zone);
    }
    
    Message(final DNSInput in) throws IOException {
        this(new Header(in));
        final boolean isUpdate = this.header.getOpcode() == 5;
        final boolean truncated = this.header.getFlag(6);
        try {
            for (int i = 0; i < 4; ++i) {
                final int count = this.header.getCount(i);
                if (count > 0) {
                    this.sections[i] = new ArrayList(count);
                }
                for (int j = 0; j < count; ++j) {
                    final int pos = in.current();
                    final Record rec = Record.fromWire(in, i, isUpdate);
                    this.sections[i].add(rec);
                    if (rec.getType() == 250) {
                        this.tsigstart = pos;
                    }
                    if (rec.getType() == 24 && ((SIGRecord)rec).getTypeCovered() == 0) {
                        this.sig0start = pos;
                    }
                }
            }
        }
        catch (WireParseException e) {
            if (!truncated) {
                throw e;
            }
        }
        this.size = in.current();
    }
    
    public Message(final byte[] b) throws IOException {
        this(new DNSInput(b));
    }
    
    public void setHeader(final Header h) {
        this.header = h;
    }
    
    public Header getHeader() {
        return this.header;
    }
    
    public void addRecord(final Record r, final int section) {
        if (this.sections[section] == null) {
            this.sections[section] = new LinkedList();
        }
        this.header.incCount(section);
        this.sections[section].add(r);
    }
    
    public boolean removeRecord(final Record r, final int section) {
        if (this.sections[section] != null && this.sections[section].remove(r)) {
            this.header.decCount(section);
            return true;
        }
        return false;
    }
    
    public void removeAllRecords(final int section) {
        this.sections[section] = null;
        this.header.setCount(section, 0);
    }
    
    public boolean findRecord(final Record r, final int section) {
        return this.sections[section] != null && this.sections[section].contains(r);
    }
    
    public boolean findRecord(final Record r) {
        for (int i = 1; i <= 3; ++i) {
            if (this.sections[i] != null && this.sections[i].contains(r)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean findRRset(final Name name, final int type, final int section) {
        if (this.sections[section] == null) {
            return false;
        }
        for (int i = 0; i < this.sections[section].size(); ++i) {
            final Record r = this.sections[section].get(i);
            if (r.getType() == type && name.equals(r.getName())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean findRRset(final Name name, final int type) {
        return this.findRRset(name, type, 1) || this.findRRset(name, type, 2) || this.findRRset(name, type, 3);
    }
    
    public Record getQuestion() {
        final List l = this.sections[0];
        if (l == null || l.size() == 0) {
            return null;
        }
        return l.get(0);
    }
    
    public TSIGRecord getTSIG() {
        final int count = this.header.getCount(3);
        if (count == 0) {
            return null;
        }
        final List l = this.sections[3];
        final Record rec = l.get(count - 1);
        if (rec.type != 250) {
            return null;
        }
        return (TSIGRecord)rec;
    }
    
    public boolean isSigned() {
        return this.tsigState == 3 || this.tsigState == 1 || this.tsigState == 4;
    }
    
    public boolean isVerified() {
        return this.tsigState == 1;
    }
    
    public OPTRecord getOPT() {
        final Record[] additional = this.getSectionArray(3);
        for (int i = 0; i < additional.length; ++i) {
            if (additional[i] instanceof OPTRecord) {
                return (OPTRecord)additional[i];
            }
        }
        return null;
    }
    
    public int getRcode() {
        int rcode = this.header.getRcode();
        final OPTRecord opt = this.getOPT();
        if (opt != null) {
            rcode += opt.getExtendedRcode() << 4;
        }
        return rcode;
    }
    
    public Record[] getSectionArray(final int section) {
        if (this.sections[section] == null) {
            return Message.emptyRecordArray;
        }
        final List l = this.sections[section];
        return l.toArray(new Record[l.size()]);
    }
    
    private static boolean sameSet(final Record r1, final Record r2) {
        return r1.getRRsetType() == r2.getRRsetType() && r1.getDClass() == r2.getDClass() && r1.getName().equals(r2.getName());
    }
    
    public RRset[] getSectionRRsets(final int section) {
        if (this.sections[section] == null) {
            return Message.emptyRRsetArray;
        }
        final List sets = new LinkedList();
        final Record[] recs = this.getSectionArray(section);
        final Set hash = new HashSet();
        for (int i = 0; i < recs.length; ++i) {
            final Name name = recs[i].getName();
            boolean newset = true;
            if (hash.contains(name)) {
                for (int j = sets.size() - 1; j >= 0; --j) {
                    final RRset set = sets.get(j);
                    if (set.getType() == recs[i].getRRsetType() && set.getDClass() == recs[i].getDClass() && set.getName().equals(name)) {
                        set.addRR(recs[i]);
                        newset = false;
                        break;
                    }
                }
            }
            if (newset) {
                final RRset set2 = new RRset(recs[i]);
                sets.add(set2);
                hash.add(name);
            }
        }
        return sets.toArray(new RRset[sets.size()]);
    }
    
    void toWire(final DNSOutput out) {
        this.header.toWire(out);
        final Compression c = new Compression();
        for (int i = 0; i < 4; ++i) {
            if (this.sections[i] != null) {
                for (int j = 0; j < this.sections[i].size(); ++j) {
                    final Record rec = this.sections[i].get(j);
                    rec.toWire(out, i, c);
                }
            }
        }
    }
    
    private int sectionToWire(final DNSOutput out, final int section, final Compression c, final int maxLength) {
        final int n = this.sections[section].size();
        int pos = out.current();
        int rendered = 0;
        Record lastrec = null;
        for (int i = 0; i < n; ++i) {
            final Record rec = this.sections[section].get(i);
            if (lastrec != null && !sameSet(rec, lastrec)) {
                pos = out.current();
                rendered = i;
            }
            lastrec = rec;
            rec.toWire(out, section, c);
            if (out.current() > maxLength) {
                out.jump(pos);
                return n - rendered;
            }
        }
        return 0;
    }
    
    private boolean toWire(final DNSOutput out, final int maxLength) {
        if (maxLength < 12) {
            return false;
        }
        Header newheader = null;
        int tempMaxLength = maxLength;
        if (this.tsigkey != null) {
            tempMaxLength -= this.tsigkey.recordLength();
        }
        final int startpos = out.current();
        this.header.toWire(out);
        final Compression c = new Compression();
        for (int i = 0; i < 4; ++i) {
            if (this.sections[i] != null) {
                final int skipped = this.sectionToWire(out, i, c, tempMaxLength);
                if (skipped != 0) {
                    if (newheader == null) {
                        newheader = (Header)this.header.clone();
                    }
                    if (i != 3) {
                        newheader.setFlag(6);
                    }
                    final int count = newheader.getCount(i);
                    newheader.setCount(i, count - skipped);
                    for (int j = i + 1; j < 4; ++j) {
                        newheader.setCount(j, 0);
                    }
                    out.save();
                    out.jump(startpos);
                    newheader.toWire(out);
                    out.restore();
                    break;
                }
            }
        }
        if (this.tsigkey != null) {
            final TSIGRecord tsigrec = this.tsigkey.generate(this, out.toByteArray(), this.tsigerror, this.querytsig);
            if (newheader == null) {
                newheader = (Header)this.header.clone();
            }
            tsigrec.toWire(out, 3, c);
            newheader.incCount(3);
            out.save();
            out.jump(startpos);
            newheader.toWire(out);
            out.restore();
        }
        return true;
    }
    
    public byte[] toWire() {
        final DNSOutput out = new DNSOutput();
        this.toWire(out);
        this.size = out.current();
        return out.toByteArray();
    }
    
    public byte[] toWire(final int maxLength) {
        final DNSOutput out = new DNSOutput();
        this.toWire(out, maxLength);
        this.size = out.current();
        return out.toByteArray();
    }
    
    public void setTSIG(final TSIG key, final int error, final TSIGRecord querytsig) {
        this.tsigkey = key;
        this.tsigerror = error;
        this.querytsig = querytsig;
    }
    
    public int numBytes() {
        return this.size;
    }
    
    public String sectionToString(final int i) {
        if (i > 3) {
            return null;
        }
        final StringBuffer sb = new StringBuffer();
        final Record[] records = this.getSectionArray(i);
        for (int j = 0; j < records.length; ++j) {
            final Record rec = records[j];
            if (i == 0) {
                sb.append(";;\t" + rec.name);
                sb.append(", type = " + Type.string(rec.type));
                sb.append(", class = " + DClass.string(rec.dclass));
            }
            else {
                sb.append(rec);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        final OPTRecord opt = this.getOPT();
        if (opt != null) {
            sb.append(this.header.toStringWithRcode(this.getRcode()) + "\n");
        }
        else {
            sb.append(this.header + "\n");
        }
        if (this.isSigned()) {
            sb.append(";; TSIG ");
            if (this.isVerified()) {
                sb.append("ok");
            }
            else {
                sb.append("invalid");
            }
            sb.append('\n');
        }
        for (int i = 0; i < 4; ++i) {
            if (this.header.getOpcode() != 5) {
                sb.append(";; " + Section.longString(i) + ":\n");
            }
            else {
                sb.append(";; " + Section.updString(i) + ":\n");
            }
            sb.append(this.sectionToString(i) + "\n");
        }
        sb.append(";; Message size: " + this.numBytes() + " bytes");
        return sb.toString();
    }
    
    public Object clone() {
        final Message m = new Message();
        for (int i = 0; i < this.sections.length; ++i) {
            if (this.sections[i] != null) {
                m.sections[i] = new LinkedList(this.sections[i]);
            }
        }
        m.header = (Header)this.header.clone();
        m.size = this.size;
        return m;
    }
    
    static {
        Message.emptyRecordArray = new Record[0];
        Message.emptyRRsetArray = new RRset[0];
    }
}
