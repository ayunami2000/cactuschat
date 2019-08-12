// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

import io.netty.util.CharsetUtil;

public enum MqttVersion
{
    MQTT_3_1("MQIsdp", (byte)3), 
    MQTT_3_1_1("MQTT", (byte)4);
    
    private String name;
    private byte level;
    
    private MqttVersion(final String protocolName, final byte protocolLevel) {
        this.name = protocolName;
        this.level = protocolLevel;
    }
    
    public String protocolName() {
        return this.name;
    }
    
    public byte[] protocolNameBytes() {
        return this.name.getBytes(CharsetUtil.UTF_8);
    }
    
    public byte protocolLevel() {
        return this.level;
    }
    
    public static MqttVersion fromProtocolNameAndLevel(final String protocolName, final byte protocolLevel) {
        final MqttVersion[] arr$ = values();
        final int len$ = arr$.length;
        int i$ = 0;
        while (i$ < len$) {
            final MqttVersion mv = arr$[i$];
            if (mv.name.equals(protocolName)) {
                if (mv.level == protocolLevel) {
                    return mv;
                }
                throw new MqttUnacceptableProtocolVersionException(protocolName + " and " + protocolLevel + " are not match");
            }
            else {
                ++i$;
            }
        }
        throw new MqttUnacceptableProtocolVersionException(protocolName + "is unknown protocol name");
    }
}
