// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

public final class DnsResource extends DnsEntry implements ByteBufHolder
{
    private final long ttl;
    private final ByteBuf content;
    
    public DnsResource(final String name, final DnsType type, final DnsClass aClass, final long ttl, final ByteBuf content) {
        super(name, type, aClass);
        this.ttl = ttl;
        this.content = content;
    }
    
    public long timeToLive() {
        return this.ttl;
    }
    
    @Override
    public ByteBuf content() {
        return this.content;
    }
    
    @Override
    public DnsResource copy() {
        return new DnsResource(this.name(), this.type(), this.dnsClass(), this.ttl, this.content.copy());
    }
    
    @Override
    public DnsResource duplicate() {
        return new DnsResource(this.name(), this.type(), this.dnsClass(), this.ttl, this.content.duplicate());
    }
    
    @Override
    public int refCnt() {
        return this.content.refCnt();
    }
    
    @Override
    public DnsResource retain() {
        this.content.retain();
        return this;
    }
    
    @Override
    public DnsResource retain(final int increment) {
        this.content.retain(increment);
        return this;
    }
    
    @Override
    public boolean release() {
        return this.content.release();
    }
    
    @Override
    public boolean release(final int decrement) {
        return this.content.release(decrement);
    }
    
    @Override
    public DnsResource touch() {
        this.content.touch();
        return this;
    }
    
    @Override
    public DnsResource touch(final Object hint) {
        this.content.touch(hint);
        return this;
    }
}
