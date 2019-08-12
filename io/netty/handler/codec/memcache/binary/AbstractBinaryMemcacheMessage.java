// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache.binary;

import io.netty.util.ReferenceCounted;
import io.netty.handler.codec.memcache.MemcacheMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.memcache.AbstractMemcacheObject;

public abstract class AbstractBinaryMemcacheMessage extends AbstractMemcacheObject implements BinaryMemcacheMessage
{
    private String key;
    private ByteBuf extras;
    private byte magic;
    private byte opcode;
    private short keyLength;
    private byte extrasLength;
    private byte dataType;
    private int totalBodyLength;
    private int opaque;
    private long cas;
    
    protected AbstractBinaryMemcacheMessage(final String key, final ByteBuf extras) {
        this.key = key;
        this.extras = extras;
    }
    
    @Override
    public String key() {
        return this.key;
    }
    
    @Override
    public ByteBuf extras() {
        return this.extras;
    }
    
    @Override
    public BinaryMemcacheMessage setKey(final String key) {
        this.key = key;
        return this;
    }
    
    @Override
    public BinaryMemcacheMessage setExtras(final ByteBuf extras) {
        this.extras = extras;
        return this;
    }
    
    @Override
    public byte magic() {
        return this.magic;
    }
    
    @Override
    public BinaryMemcacheMessage setMagic(final byte magic) {
        this.magic = magic;
        return this;
    }
    
    @Override
    public long cas() {
        return this.cas;
    }
    
    @Override
    public BinaryMemcacheMessage setCas(final long cas) {
        this.cas = cas;
        return this;
    }
    
    @Override
    public int opaque() {
        return this.opaque;
    }
    
    @Override
    public BinaryMemcacheMessage setOpaque(final int opaque) {
        this.opaque = opaque;
        return this;
    }
    
    @Override
    public int totalBodyLength() {
        return this.totalBodyLength;
    }
    
    @Override
    public BinaryMemcacheMessage setTotalBodyLength(final int totalBodyLength) {
        this.totalBodyLength = totalBodyLength;
        return this;
    }
    
    @Override
    public byte dataType() {
        return this.dataType;
    }
    
    @Override
    public BinaryMemcacheMessage setDataType(final byte dataType) {
        this.dataType = dataType;
        return this;
    }
    
    @Override
    public byte extrasLength() {
        return this.extrasLength;
    }
    
    @Override
    public BinaryMemcacheMessage setExtrasLength(final byte extrasLength) {
        this.extrasLength = extrasLength;
        return this;
    }
    
    @Override
    public short keyLength() {
        return this.keyLength;
    }
    
    @Override
    public BinaryMemcacheMessage setKeyLength(final short keyLength) {
        this.keyLength = keyLength;
        return this;
    }
    
    @Override
    public byte opcode() {
        return this.opcode;
    }
    
    @Override
    public BinaryMemcacheMessage setOpcode(final byte opcode) {
        this.opcode = opcode;
        return this;
    }
    
    @Override
    public int refCnt() {
        if (this.extras != null) {
            return this.extras.refCnt();
        }
        return 1;
    }
    
    @Override
    public BinaryMemcacheMessage retain() {
        if (this.extras != null) {
            this.extras.retain();
        }
        return this;
    }
    
    @Override
    public BinaryMemcacheMessage retain(final int increment) {
        if (this.extras != null) {
            this.extras.retain(increment);
        }
        return this;
    }
    
    @Override
    public boolean release() {
        return this.extras != null && this.extras.release();
    }
    
    @Override
    public boolean release(final int decrement) {
        return this.extras != null && this.extras.release(decrement);
    }
    
    @Override
    public BinaryMemcacheMessage touch() {
        return this.touch(null);
    }
    
    @Override
    public BinaryMemcacheMessage touch(final Object hint) {
        if (this.extras != null) {
            this.extras.touch(hint);
        }
        return this;
    }
}
