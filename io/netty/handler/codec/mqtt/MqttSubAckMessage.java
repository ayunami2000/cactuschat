// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

public class MqttSubAckMessage extends MqttMessage
{
    public MqttSubAckMessage(final MqttFixedHeader mqttFixedHeader, final MqttMessageIdVariableHeader variableHeader, final MqttSubAckPayload payload) {
        super(mqttFixedHeader, variableHeader, payload);
    }
    
    @Override
    public MqttMessageIdVariableHeader variableHeader() {
        return (MqttMessageIdVariableHeader)super.variableHeader();
    }
    
    @Override
    public MqttSubAckPayload payload() {
        return (MqttSubAckPayload)super.payload();
    }
}
