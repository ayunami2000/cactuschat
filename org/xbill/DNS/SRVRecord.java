// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

public class SRVRecord extends Record
{
    private static final long serialVersionUID = -3886460132387522052L;
    private int priority;
    private int weight;
    private int port;
    private Name target;
    
    SRVRecord() {
    }
    
    Record getObject() {
        return new SRVRecord();
    }
    
    public SRVRecord(final Name name, final int dclass, final long ttl, final int priority, final int weight, final int port, final Name target) {
        super(name, 33, dclass, ttl);
        this.priority = Record.checkU16("priority", priority);
        this.weight = Record.checkU16("weight", weight);
        this.port = Record.checkU16("port", port);
        this.target = Record.checkName("target", target);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.priority = in.readU16();
        this.weight = in.readU16();
        this.port = in.readU16();
        this.target = new Name(in);
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.priority = st.getUInt16();
        this.weight = st.getUInt16();
        this.port = st.getUInt16();
        this.target = st.getName(origin);
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.priority + " ");
        sb.append(this.weight + " ");
        sb.append(this.port + " ");
        sb.append(this.target);
        return sb.toString();
    }
    
    public int getPriority() {
        return this.priority;
    }
    
    public int getWeight() {
        return this.weight;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public Name getTarget() {
        return this.target;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU16(this.priority);
        out.writeU16(this.weight);
        out.writeU16(this.port);
        this.target.toWire(out, null, canonical);
    }
    
    public Name getAdditionalName() {
        return this.target;
    }
}
