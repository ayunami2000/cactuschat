// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

public class DnsHeader
{
    public static final int TYPE_QUERY = 0;
    public static final int TYPE_RESPONSE = 1;
    public static final int OPCODE_QUERY = 0;
    @Deprecated
    public static final int OPCODE_IQUERY = 1;
    private final DnsMessage parent;
    private boolean recursionDesired;
    private int opcode;
    private int id;
    private int type;
    private int z;
    
    DnsHeader(final DnsMessage parent) {
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        this.parent = parent;
    }
    
    public int questionCount() {
        return this.parent.questions().size();
    }
    
    public int answerCount() {
        return this.parent.answers().size();
    }
    
    public int authorityResourceCount() {
        return this.parent.authorityResources().size();
    }
    
    public int additionalResourceCount() {
        return this.parent.additionalResources().size();
    }
    
    public boolean isRecursionDesired() {
        return this.recursionDesired;
    }
    
    public int opcode() {
        return this.opcode;
    }
    
    public int type() {
        return this.type;
    }
    
    public int id() {
        return this.id;
    }
    
    public DnsHeader setOpcode(final int opcode) {
        this.opcode = opcode;
        return this;
    }
    
    public DnsHeader setRecursionDesired(final boolean recursionDesired) {
        this.recursionDesired = recursionDesired;
        return this;
    }
    
    public DnsHeader setType(final int type) {
        this.type = type;
        return this;
    }
    
    public DnsHeader setId(final int id) {
        this.id = id;
        return this;
    }
    
    public int z() {
        return this.z;
    }
    
    public DnsHeader setZ(final int z) {
        this.z = z;
        return this;
    }
}
