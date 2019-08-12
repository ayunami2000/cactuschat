// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.NoSuchElementException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Iterator;
import java.io.IOException;
import java.util.Map;
import java.io.Serializable;

public class Zone implements Serializable
{
    private static final long serialVersionUID = -9220510891189510942L;
    public static final int PRIMARY = 1;
    public static final int SECONDARY = 2;
    private Map data;
    private Name origin;
    private Object originNode;
    private int dclass;
    private RRset NS;
    private SOARecord SOA;
    private boolean hasWild;
    
    private void validate() throws IOException {
        this.originNode = this.exactName(this.origin);
        if (this.originNode == null) {
            throw new IOException(this.origin + ": no data specified");
        }
        final RRset rrset = this.oneRRset(this.originNode, 6);
        if (rrset == null || rrset.size() != 1) {
            throw new IOException(this.origin + ": exactly 1 SOA must be specified");
        }
        final Iterator it = rrset.rrs();
        this.SOA = it.next();
        this.NS = this.oneRRset(this.originNode, 2);
        if (this.NS == null) {
            throw new IOException(this.origin + ": no NS set specified");
        }
    }
    
    private final void maybeAddRecord(final Record record) throws IOException {
        final int rtype = record.getType();
        final Name name = record.getName();
        if (rtype == 6 && !name.equals(this.origin)) {
            throw new IOException("SOA owner " + name + " does not match zone origin " + this.origin);
        }
        if (name.subdomain(this.origin)) {
            this.addRecord(record);
        }
    }
    
    public Zone(final Name zone, final String file) throws IOException {
        this.dclass = 1;
        this.data = new TreeMap();
        if (zone == null) {
            throw new IllegalArgumentException("no zone name specified");
        }
        final Master m = new Master(file, zone);
        this.origin = zone;
        Record record;
        while ((record = m.nextRecord()) != null) {
            this.maybeAddRecord(record);
        }
        this.validate();
    }
    
    public Zone(final Name zone, final Record[] records) throws IOException {
        this.dclass = 1;
        this.data = new TreeMap();
        if (zone == null) {
            throw new IllegalArgumentException("no zone name specified");
        }
        this.origin = zone;
        for (int i = 0; i < records.length; ++i) {
            this.maybeAddRecord(records[i]);
        }
        this.validate();
    }
    
    private void fromXFR(final ZoneTransferIn xfrin) throws IOException, ZoneTransferException {
        this.data = new TreeMap();
        this.origin = xfrin.getName();
        final List records = xfrin.run();
        for (final Record record : records) {
            this.maybeAddRecord(record);
        }
        if (!xfrin.isAXFR()) {
            throw new IllegalArgumentException("zones can only be created from AXFRs");
        }
        this.validate();
    }
    
    public Zone(final ZoneTransferIn xfrin) throws IOException, ZoneTransferException {
        this.dclass = 1;
        this.fromXFR(xfrin);
    }
    
    public Zone(final Name zone, final int dclass, final String remote) throws IOException, ZoneTransferException {
        this.dclass = 1;
        final ZoneTransferIn xfrin = ZoneTransferIn.newAXFR(zone, remote, null);
        xfrin.setDClass(dclass);
        this.fromXFR(xfrin);
    }
    
    public Name getOrigin() {
        return this.origin;
    }
    
    public RRset getNS() {
        return this.NS;
    }
    
    public SOARecord getSOA() {
        return this.SOA;
    }
    
    public int getDClass() {
        return this.dclass;
    }
    
    private synchronized Object exactName(final Name name) {
        return this.data.get(name);
    }
    
    private synchronized RRset[] allRRsets(final Object types) {
        if (types instanceof List) {
            final List typelist = (List)types;
            return typelist.toArray(new RRset[typelist.size()]);
        }
        final RRset set = (RRset)types;
        return new RRset[] { set };
    }
    
    private synchronized RRset oneRRset(final Object types, final int type) {
        if (type == 255) {
            throw new IllegalArgumentException("oneRRset(ANY)");
        }
        if (types instanceof List) {
            final List list = (List)types;
            for (int i = 0; i < list.size(); ++i) {
                final RRset set = list.get(i);
                if (set.getType() == type) {
                    return set;
                }
            }
        }
        else {
            final RRset set2 = (RRset)types;
            if (set2.getType() == type) {
                return set2;
            }
        }
        return null;
    }
    
    private synchronized RRset findRRset(final Name name, final int type) {
        final Object types = this.exactName(name);
        if (types == null) {
            return null;
        }
        return this.oneRRset(types, type);
    }
    
    private synchronized void addRRset(final Name name, final RRset rrset) {
        if (!this.hasWild && name.isWild()) {
            this.hasWild = true;
        }
        final Object types = this.data.get(name);
        if (types == null) {
            this.data.put(name, rrset);
            return;
        }
        final int rtype = rrset.getType();
        if (types instanceof List) {
            final List list = (List)types;
            for (int i = 0; i < list.size(); ++i) {
                final RRset set = list.get(i);
                if (set.getType() == rtype) {
                    list.set(i, rrset);
                    return;
                }
            }
            list.add(rrset);
        }
        else {
            final RRset set2 = (RRset)types;
            if (set2.getType() == rtype) {
                this.data.put(name, rrset);
            }
            else {
                final LinkedList list2 = new LinkedList();
                list2.add(set2);
                list2.add(rrset);
                this.data.put(name, list2);
            }
        }
    }
    
    private synchronized void removeRRset(final Name name, final int type) {
        final Object types = this.data.get(name);
        if (types == null) {
            return;
        }
        if (types instanceof List) {
            final List list = (List)types;
            for (int i = 0; i < list.size(); ++i) {
                final RRset set = list.get(i);
                if (set.getType() == type) {
                    list.remove(i);
                    if (list.size() == 0) {
                        this.data.remove(name);
                    }
                    return;
                }
            }
        }
        else {
            final RRset set2 = (RRset)types;
            if (set2.getType() != type) {
                return;
            }
            this.data.remove(name);
        }
    }
    
    private synchronized SetResponse lookup(final Name name, final int type) {
        if (!name.subdomain(this.origin)) {
            return SetResponse.ofType(1);
        }
        int labels;
        int tlabels;
        int olabels;
        for (labels = name.labels(), olabels = (tlabels = this.origin.labels()); tlabels <= labels; ++tlabels) {
            final boolean isOrigin = tlabels == olabels;
            final boolean isExact = tlabels == labels;
            Name tname;
            if (isOrigin) {
                tname = this.origin;
            }
            else if (isExact) {
                tname = name;
            }
            else {
                tname = new Name(name, labels - tlabels);
            }
            final Object types = this.exactName(tname);
            if (types != null) {
                if (!isOrigin) {
                    final RRset ns = this.oneRRset(types, 2);
                    if (ns != null) {
                        return new SetResponse(3, ns);
                    }
                }
                if (isExact && type == 255) {
                    final SetResponse sr = new SetResponse(6);
                    final RRset[] sets = this.allRRsets(types);
                    for (int i = 0; i < sets.length; ++i) {
                        sr.addRRset(sets[i]);
                    }
                    return sr;
                }
                if (isExact) {
                    RRset rrset = this.oneRRset(types, type);
                    if (rrset != null) {
                        final SetResponse sr = new SetResponse(6);
                        sr.addRRset(rrset);
                        return sr;
                    }
                    rrset = this.oneRRset(types, 5);
                    if (rrset != null) {
                        return new SetResponse(4, rrset);
                    }
                }
                else {
                    final RRset rrset = this.oneRRset(types, 39);
                    if (rrset != null) {
                        return new SetResponse(5, rrset);
                    }
                }
                if (isExact) {
                    return SetResponse.ofType(2);
                }
            }
        }
        if (this.hasWild) {
            for (int j = 0; j < labels - olabels; ++j) {
                final Name tname = name.wild(j + 1);
                final Object types = this.exactName(tname);
                if (types != null) {
                    final RRset rrset = this.oneRRset(types, type);
                    if (rrset != null) {
                        final SetResponse sr = new SetResponse(6);
                        sr.addRRset(rrset);
                        return sr;
                    }
                }
            }
        }
        return SetResponse.ofType(1);
    }
    
    public SetResponse findRecords(final Name name, final int type) {
        return this.lookup(name, type);
    }
    
    public RRset findExactMatch(final Name name, final int type) {
        final Object types = this.exactName(name);
        if (types == null) {
            return null;
        }
        return this.oneRRset(types, type);
    }
    
    public void addRRset(final RRset rrset) {
        final Name name = rrset.getName();
        this.addRRset(name, rrset);
    }
    
    public void addRecord(final Record r) {
        final Name name = r.getName();
        final int rtype = r.getRRsetType();
        synchronized (this) {
            RRset rrset = this.findRRset(name, rtype);
            if (rrset == null) {
                rrset = new RRset(r);
                this.addRRset(name, rrset);
            }
            else {
                rrset.addRR(r);
            }
        }
    }
    
    public void removeRecord(final Record r) {
        final Name name = r.getName();
        final int rtype = r.getRRsetType();
        synchronized (this) {
            final RRset rrset = this.findRRset(name, rtype);
            if (rrset == null) {
                return;
            }
            if (rrset.size() == 1 && rrset.first().equals(r)) {
                this.removeRRset(name, rtype);
            }
            else {
                rrset.deleteRR(r);
            }
        }
    }
    
    public Iterator iterator() {
        return new ZoneIterator(false);
    }
    
    public Iterator AXFR() {
        return new ZoneIterator(true);
    }
    
    private void nodeToString(final StringBuffer sb, final Object node) {
        final RRset[] sets = this.allRRsets(node);
        for (int i = 0; i < sets.length; ++i) {
            final RRset rrset = sets[i];
            Iterator it = rrset.rrs();
            while (it.hasNext()) {
                sb.append(it.next() + "\n");
            }
            it = rrset.sigs();
            while (it.hasNext()) {
                sb.append(it.next() + "\n");
            }
        }
    }
    
    public synchronized String toMasterFile() {
        final Iterator zentries = this.data.entrySet().iterator();
        final StringBuffer sb = new StringBuffer();
        this.nodeToString(sb, this.originNode);
        while (zentries.hasNext()) {
            final Map.Entry entry = zentries.next();
            if (!this.origin.equals(entry.getKey())) {
                this.nodeToString(sb, entry.getValue());
            }
        }
        return sb.toString();
    }
    
    public String toString() {
        return this.toMasterFile();
    }
    
    class ZoneIterator implements Iterator
    {
        private Iterator zentries;
        private RRset[] current;
        private int count;
        private boolean wantLastSOA;
        
        ZoneIterator(final boolean axfr) {
            synchronized (Zone.this) {
                this.zentries = Zone.this.data.entrySet().iterator();
            }
            this.wantLastSOA = axfr;
            final RRset[] sets = Zone.this.allRRsets(Zone.this.originNode);
            this.current = new RRset[sets.length];
            int i = 0;
            int j = 2;
            while (i < sets.length) {
                final int type = sets[i].getType();
                if (type == 6) {
                    this.current[0] = sets[i];
                }
                else if (type == 2) {
                    this.current[1] = sets[i];
                }
                else {
                    this.current[j++] = sets[i];
                }
                ++i;
            }
        }
        
        public boolean hasNext() {
            return this.current != null || this.wantLastSOA;
        }
        
        public Object next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            if (this.current == null) {
                this.wantLastSOA = false;
                return Zone.this.oneRRset(Zone.this.originNode, 6);
            }
            final Object set = this.current[this.count++];
            if (this.count == this.current.length) {
                this.current = null;
                while (this.zentries.hasNext()) {
                    final Map.Entry entry = this.zentries.next();
                    if (entry.getKey().equals(Zone.this.origin)) {
                        continue;
                    }
                    final RRset[] sets = Zone.this.allRRsets(entry.getValue());
                    if (sets.length == 0) {
                        continue;
                    }
                    this.current = sets;
                    this.count = 0;
                    break;
                }
            }
            return set;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
