// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

import io.netty.util.ReferenceCounted;
import java.net.InetSocketAddress;

public final class DnsResponse extends DnsMessage
{
    private final InetSocketAddress sender;
    
    public DnsResponse(final int id, final InetSocketAddress sender) {
        super(id);
        if (sender == null) {
            throw new NullPointerException("sender");
        }
        this.sender = sender;
    }
    
    public InetSocketAddress sender() {
        return this.sender;
    }
    
    @Override
    public DnsResponse addAnswer(final DnsResource answer) {
        super.addAnswer(answer);
        return this;
    }
    
    @Override
    public DnsResponse addQuestion(final DnsQuestion question) {
        super.addQuestion(question);
        return this;
    }
    
    @Override
    public DnsResponse addAuthorityResource(final DnsResource resource) {
        super.addAuthorityResource(resource);
        return this;
    }
    
    @Override
    public DnsResponse addAdditionalResource(final DnsResource resource) {
        super.addAdditionalResource(resource);
        return this;
    }
    
    @Override
    public DnsResponse touch(final Object hint) {
        super.touch(hint);
        return this;
    }
    
    @Override
    public DnsResponse retain() {
        super.retain();
        return this;
    }
    
    @Override
    public DnsResponse retain(final int increment) {
        super.retain(increment);
        return this;
    }
    
    @Override
    public DnsResponse touch() {
        super.touch();
        return this;
    }
    
    @Override
    public DnsResponseHeader header() {
        return (DnsResponseHeader)super.header();
    }
    
    @Override
    protected DnsResponseHeader newHeader(final int id) {
        return new DnsResponseHeader(this, id);
    }
}
