// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

import io.netty.util.ReferenceCounted;
import java.net.InetSocketAddress;

public class DnsQuery extends DnsMessage
{
    private final InetSocketAddress recipient;
    
    public DnsQuery(final int id, final InetSocketAddress recipient) {
        super(id);
        if (recipient == null) {
            throw new NullPointerException("recipient");
        }
        this.recipient = recipient;
    }
    
    public InetSocketAddress recipient() {
        return this.recipient;
    }
    
    @Override
    public DnsQuery addAnswer(final DnsResource answer) {
        super.addAnswer(answer);
        return this;
    }
    
    @Override
    public DnsQuery addQuestion(final DnsQuestion question) {
        super.addQuestion(question);
        return this;
    }
    
    @Override
    public DnsQuery addAuthorityResource(final DnsResource resource) {
        super.addAuthorityResource(resource);
        return this;
    }
    
    @Override
    public DnsQuery addAdditionalResource(final DnsResource resource) {
        super.addAdditionalResource(resource);
        return this;
    }
    
    @Override
    public DnsQuery touch(final Object hint) {
        super.touch(hint);
        return this;
    }
    
    @Override
    public DnsQuery retain() {
        super.retain();
        return this;
    }
    
    @Override
    public DnsQuery retain(final int increment) {
        super.retain(increment);
        return this;
    }
    
    @Override
    public DnsQuery touch() {
        super.touch();
        return this;
    }
    
    @Override
    public DnsQueryHeader header() {
        return (DnsQueryHeader)super.header();
    }
    
    @Override
    protected DnsQueryHeader newHeader(final int id) {
        return new DnsQueryHeader(this, id);
    }
}
