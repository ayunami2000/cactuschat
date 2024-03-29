// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum MqttConnectReturnCode
{
    CONNECTION_ACCEPTED((byte)0), 
    CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION((byte)1), 
    CONNECTION_REFUSED_IDENTIFIER_REJECTED((byte)2), 
    CONNECTION_REFUSED_SERVER_UNAVAILABLE((byte)3), 
    CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD((byte)4), 
    CONNECTION_REFUSED_NOT_AUTHORIZED((byte)5);
    
    private static final Map<Byte, MqttConnectReturnCode> valueToCodeMap;
    private final byte byteValue;
    
    private MqttConnectReturnCode(final byte byteValue) {
        this.byteValue = byteValue;
    }
    
    public byte byteValue() {
        return this.byteValue;
    }
    
    public static MqttConnectReturnCode valueOf(final byte b) {
        if (MqttConnectReturnCode.valueToCodeMap.containsKey(b)) {
            return MqttConnectReturnCode.valueToCodeMap.get(b);
        }
        throw new IllegalArgumentException("unknown connect return code: " + (b & 0xFF));
    }
    
    static {
        final Map<Byte, MqttConnectReturnCode> valueMap = new HashMap<Byte, MqttConnectReturnCode>();
        for (final MqttConnectReturnCode code : values()) {
            valueMap.put(code.byteValue, code);
        }
        valueToCodeMap = Collections.unmodifiableMap((Map<? extends Byte, ? extends MqttConnectReturnCode>)valueMap);
    }
}
