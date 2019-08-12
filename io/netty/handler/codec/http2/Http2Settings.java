// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.collection.IntObjectMap;
import io.netty.util.collection.IntObjectHashMap;

public final class Http2Settings extends IntObjectHashMap<Long>
{
    public Http2Settings() {
        this(6);
    }
    
    public Http2Settings(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }
    
    public Http2Settings(final int initialCapacity) {
        super(initialCapacity);
    }
    
    @Override
    public Long put(final int key, final Long value) {
        verifyStandardSetting(key, value);
        return super.put(key, value);
    }
    
    public Long headerTableSize() {
        return this.get(1);
    }
    
    public Http2Settings headerTableSize(final int value) {
        this.put(1, (long)value);
        return this;
    }
    
    public Boolean pushEnabled() {
        final Long value = this.get(2);
        if (value == null) {
            return null;
        }
        return value != 0L;
    }
    
    public Http2Settings pushEnabled(final boolean enabled) {
        this.put(2, (long)(enabled ? 1 : 0));
        return this;
    }
    
    public Long maxConcurrentStreams() {
        return this.get(3);
    }
    
    public Http2Settings maxConcurrentStreams(final long value) {
        this.put(3, value);
        return this;
    }
    
    public Integer initialWindowSize() {
        return this.getIntValue(4);
    }
    
    public Http2Settings initialWindowSize(final int value) {
        this.put(4, (long)value);
        return this;
    }
    
    public Integer maxFrameSize() {
        return this.getIntValue(5);
    }
    
    public Http2Settings maxFrameSize(final int value) {
        this.put(5, (long)value);
        return this;
    }
    
    public Integer maxHeaderListSize() {
        return this.getIntValue(6);
    }
    
    public Http2Settings maxHeaderListSize(final int value) {
        this.put(6, (long)value);
        return this;
    }
    
    public Http2Settings copyFrom(final Http2Settings settings) {
        this.clear();
        this.putAll(settings);
        return this;
    }
    
    Integer getIntValue(final int key) {
        final Long value = this.get(key);
        if (value == null) {
            return null;
        }
        return value.intValue();
    }
    
    private static void verifyStandardSetting(final int key, final Long value) {
        ObjectUtil.checkNotNull(value, "value");
        switch (key) {
            case 1: {
                if (value < 0L || value > 2147483647L) {
                    throw new IllegalArgumentException("Setting HEADER_TABLE_SIZE is invalid: " + value);
                }
                break;
            }
            case 2: {
                if (value != 0L && value != 1L) {
                    throw new IllegalArgumentException("Setting ENABLE_PUSH is invalid: " + value);
                }
                break;
            }
            case 3: {
                if (value < 0L || value > 4294967295L) {
                    throw new IllegalArgumentException("Setting MAX_CONCURRENT_STREAMS is invalid: " + value);
                }
                break;
            }
            case 4: {
                if (value < 0L || value > 2147483647L) {
                    throw new IllegalArgumentException("Setting INITIAL_WINDOW_SIZE is invalid: " + value);
                }
                break;
            }
            case 5: {
                if (!Http2CodecUtil.isMaxFrameSizeValid(value.intValue())) {
                    throw new IllegalArgumentException("Setting MAX_FRAME_SIZE is invalid: " + value);
                }
                break;
            }
            case 6: {
                if (value < 0L || value > Long.MAX_VALUE) {
                    throw new IllegalArgumentException("Setting MAX_HEADER_LIST_SIZE is invalid: " + value);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("key");
            }
        }
    }
    
    @Override
    protected String keyToString(final int key) {
        switch (key) {
            case 1: {
                return "HEADER_TABLE_SIZE";
            }
            case 2: {
                return "ENABLE_PUSH";
            }
            case 3: {
                return "MAX_CONCURRENT_STREAMS";
            }
            case 4: {
                return "INITIAL_WINDOW_SIZE";
            }
            case 5: {
                return "MAX_FRAME_SIZE";
            }
            case 6: {
                return "MAX_HEADER_LIST_SIZE";
            }
            default: {
                return super.keyToString(key);
            }
        }
    }
}
