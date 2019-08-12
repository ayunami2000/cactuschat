// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Iterator;
import java.io.IOException;

public class Update extends Message
{
    private Name origin;
    private int dclass;
    
    public Update(final Name zone, final int dclass) {
        if (!zone.isAbsolute()) {
            throw new RelativeNameException(zone);
        }
        DClass.check(dclass);
        this.getHeader().setOpcode(5);
        final Record soa = Record.newRecord(zone, 6, 1);
        this.addRecord(soa, 0);
        this.origin = zone;
        this.dclass = dclass;
    }
    
    public Update(final Name zone) {
        this(zone, 1);
    }
    
    private void newPrereq(final Record rec) {
        this.addRecord(rec, 1);
    }
    
    private void newUpdate(final Record rec) {
        this.addRecord(rec, 2);
    }
    
    public void present(final Name name) {
        this.newPrereq(Record.newRecord(name, 255, 255, 0L));
    }
    
    public void present(final Name name, final int type) {
        this.newPrereq(Record.newRecord(name, type, 255, 0L));
    }
    
    public void present(final Name name, final int type, final String record) throws IOException {
        this.newPrereq(Record.fromString(name, type, this.dclass, 0L, record, this.origin));
    }
    
    public void present(final Name name, final int type, final Tokenizer tokenizer) throws IOException {
        this.newPrereq(Record.fromString(name, type, this.dclass, 0L, tokenizer, this.origin));
    }
    
    public void present(final Record record) {
        this.newPrereq(record);
    }
    
    public void absent(final Name name) {
        this.newPrereq(Record.newRecord(name, 255, 254, 0L));
    }
    
    public void absent(final Name name, final int type) {
        this.newPrereq(Record.newRecord(name, type, 254, 0L));
    }
    
    public void add(final Name name, final int type, final long ttl, final String record) throws IOException {
        this.newUpdate(Record.fromString(name, type, this.dclass, ttl, record, this.origin));
    }
    
    public void add(final Name name, final int type, final long ttl, final Tokenizer tokenizer) throws IOException {
        this.newUpdate(Record.fromString(name, type, this.dclass, ttl, tokenizer, this.origin));
    }
    
    public void add(final Record record) {
        this.newUpdate(record);
    }
    
    public void add(final Record[] records) {
        for (int i = 0; i < records.length; ++i) {
            this.add(records[i]);
        }
    }
    
    public void add(final RRset rrset) {
        final Iterator it = rrset.rrs();
        while (it.hasNext()) {
            this.add(it.next());
        }
    }
    
    public void delete(final Name name) {
        this.newUpdate(Record.newRecord(name, 255, 255, 0L));
    }
    
    public void delete(final Name name, final int type) {
        this.newUpdate(Record.newRecord(name, type, 255, 0L));
    }
    
    public void delete(final Name name, final int type, final String record) throws IOException {
        this.newUpdate(Record.fromString(name, type, 254, 0L, record, this.origin));
    }
    
    public void delete(final Name name, final int type, final Tokenizer tokenizer) throws IOException {
        this.newUpdate(Record.fromString(name, type, 254, 0L, tokenizer, this.origin));
    }
    
    public void delete(final Record record) {
        this.newUpdate(record.withDClass(254, 0L));
    }
    
    public void delete(final Record[] records) {
        for (int i = 0; i < records.length; ++i) {
            this.delete(records[i]);
        }
    }
    
    public void delete(final RRset rrset) {
        final Iterator it = rrset.rrs();
        while (it.hasNext()) {
            this.delete(it.next());
        }
    }
    
    public void replace(final Name name, final int type, final long ttl, final String record) throws IOException {
        this.delete(name, type);
        this.add(name, type, ttl, record);
    }
    
    public void replace(final Name name, final int type, final long ttl, final Tokenizer tokenizer) throws IOException {
        this.delete(name, type);
        this.add(name, type, ttl, tokenizer);
    }
    
    public void replace(final Record record) {
        this.delete(record.getName(), record.getType());
        this.add(record);
    }
    
    public void replace(final Record[] records) {
        for (int i = 0; i < records.length; ++i) {
            this.replace(records[i]);
        }
    }
    
    public void replace(final RRset rrset) {
        this.delete(rrset.getName(), rrset.getType());
        final Iterator it = rrset.rrs();
        while (it.hasNext()) {
            this.add(it.next());
        }
    }
}
