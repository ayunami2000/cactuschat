// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class RRset implements Serializable
{
    private static final long serialVersionUID = -3270249290171239695L;
    private List rrs;
    private short nsigs;
    private short position;
    
    public RRset() {
        this.rrs = new ArrayList(1);
        this.nsigs = 0;
        this.position = 0;
    }
    
    public RRset(final Record record) {
        this();
        this.safeAddRR(record);
    }
    
    public RRset(final RRset rrset) {
        synchronized (rrset) {
            this.rrs = (List)((ArrayList)rrset.rrs).clone();
            this.nsigs = rrset.nsigs;
            this.position = rrset.position;
        }
    }
    
    private void safeAddRR(final Record r) {
        if (!(r instanceof RRSIGRecord)) {
            if (this.nsigs == 0) {
                this.rrs.add(r);
            }
            else {
                this.rrs.add(this.rrs.size() - this.nsigs, r);
            }
        }
        else {
            this.rrs.add(r);
            ++this.nsigs;
        }
    }
    
    public synchronized void addRR(Record r) {
        if (this.rrs.size() == 0) {
            this.safeAddRR(r);
            return;
        }
        final Record first = this.first();
        if (!r.sameRRset(first)) {
            throw new IllegalArgumentException("record does not match rrset");
        }
        if (r.getTTL() != first.getTTL()) {
            if (r.getTTL() > first.getTTL()) {
                r = r.cloneRecord();
                r.setTTL(first.getTTL());
            }
            else {
                for (int i = 0; i < this.rrs.size(); ++i) {
                    Record tmp = this.rrs.get(i);
                    tmp = tmp.cloneRecord();
                    tmp.setTTL(r.getTTL());
                    this.rrs.set(i, tmp);
                }
            }
        }
        if (!this.rrs.contains(r)) {
            this.safeAddRR(r);
        }
    }
    
    public synchronized void deleteRR(final Record r) {
        if (this.rrs.remove(r) && r instanceof RRSIGRecord) {
            --this.nsigs;
        }
    }
    
    public synchronized void clear() {
        this.rrs.clear();
        this.position = 0;
        this.nsigs = 0;
    }
    
    private synchronized Iterator iterator(final boolean data, final boolean cycle) {
        final int total = this.rrs.size();
        int size;
        if (data) {
            size = total - this.nsigs;
        }
        else {
            size = this.nsigs;
        }
        if (size == 0) {
            return Collections.EMPTY_LIST.iterator();
        }
        int start;
        if (data) {
            if (!cycle) {
                start = 0;
            }
            else {
                if (this.position >= size) {
                    this.position = 0;
                }
                final short position = this.position;
                this.position = (short)(position + 1);
                start = position;
            }
        }
        else {
            start = total - this.nsigs;
        }
        final List list = new ArrayList(size);
        if (data) {
            list.addAll(this.rrs.subList(start, size));
            if (start != 0) {
                list.addAll(this.rrs.subList(0, start));
            }
        }
        else {
            list.addAll(this.rrs.subList(start, total));
        }
        return list.iterator();
    }
    
    public synchronized Iterator rrs(final boolean cycle) {
        return this.iterator(true, cycle);
    }
    
    public synchronized Iterator rrs() {
        return this.iterator(true, true);
    }
    
    public synchronized Iterator sigs() {
        return this.iterator(false, false);
    }
    
    public synchronized int size() {
        return this.rrs.size() - this.nsigs;
    }
    
    public Name getName() {
        return this.first().getName();
    }
    
    public int getType() {
        return this.first().getRRsetType();
    }
    
    public int getDClass() {
        return this.first().getDClass();
    }
    
    public synchronized long getTTL() {
        return this.first().getTTL();
    }
    
    public synchronized Record first() {
        if (this.rrs.size() == 0) {
            throw new IllegalStateException("rrset is empty");
        }
        return this.rrs.get(0);
    }
    
    private String iteratorToString(final Iterator it) {
        final StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            final Record rr = it.next();
            sb.append("[");
            sb.append(rr.rdataToString());
            sb.append("]");
            if (it.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    
    public String toString() {
        if (this.rrs == null) {
            return "{empty}";
        }
        final StringBuffer sb = new StringBuffer();
        sb.append("{ ");
        sb.append(this.getName() + " ");
        sb.append(this.getTTL() + " ");
        sb.append(DClass.string(this.getDClass()) + " ");
        sb.append(Type.string(this.getType()) + " ");
        sb.append(this.iteratorToString(this.iterator(true, false)));
        if (this.nsigs > 0) {
            sb.append(" sigs: ");
            sb.append(this.iteratorToString(this.iterator(false, false)));
        }
        sb.append(" }");
        return sb.toString();
    }
}
