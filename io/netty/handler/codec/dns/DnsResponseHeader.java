// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

public final class DnsResponseHeader extends DnsHeader
{
    private boolean authoritativeAnswer;
    private boolean truncated;
    private boolean recursionAvailable;
    private DnsResponseCode responseCode;
    
    public DnsResponseHeader(final DnsMessage parent, final int id) {
        super(parent);
        this.setId(id);
    }
    
    public boolean isAuthoritativeAnswer() {
        return this.authoritativeAnswer;
    }
    
    public boolean isTruncated() {
        return this.truncated;
    }
    
    public boolean isRecursionAvailable() {
        return this.recursionAvailable;
    }
    
    public DnsResponseCode responseCode() {
        return this.responseCode;
    }
    
    @Override
    public int type() {
        return 1;
    }
    
    public DnsResponseHeader setAuthoritativeAnswer(final boolean authoritativeAnswer) {
        this.authoritativeAnswer = authoritativeAnswer;
        return this;
    }
    
    public DnsResponseHeader setTruncated(final boolean truncated) {
        this.truncated = truncated;
        return this;
    }
    
    public DnsResponseHeader setRecursionAvailable(final boolean recursionAvailable) {
        this.recursionAvailable = recursionAvailable;
        return this;
    }
    
    public DnsResponseHeader setResponseCode(final DnsResponseCode responseCode) {
        this.responseCode = responseCode;
        return this;
    }
    
    @Override
    public DnsResponseHeader setType(final int type) {
        if (type != 1) {
            throw new IllegalArgumentException("type cannot be anything but TYPE_RESPONSE (1) for a response header.");
        }
        super.setType(type);
        return this;
    }
    
    @Override
    public DnsResponseHeader setId(final int id) {
        super.setId(id);
        return this;
    }
    
    @Override
    public DnsHeader setRecursionDesired(final boolean recursionDesired) {
        return super.setRecursionDesired(recursionDesired);
    }
    
    @Override
    public DnsResponseHeader setOpcode(final int opcode) {
        super.setOpcode(opcode);
        return this;
    }
    
    @Override
    public DnsResponseHeader setZ(final int z) {
        super.setZ(z);
        return this;
    }
}
