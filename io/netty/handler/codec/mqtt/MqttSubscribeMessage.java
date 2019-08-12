// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

public class MqttSubscribeMessage extends MqttMessage
{
    public MqttSubscribeMessage(final MqttFixedHeader mqttFixedHeader, final MqttMessageIdVariableHeader variableHeader, final MqttSubscribePayload payload) {
        super(mqttFixedHeader, variableHeader, payload);
    }
    
    @Override
    public MqttMessageIdVariableHeader variableHeader() {
        return (MqttMessageIdVariableHeader)super.variableHeader();
    }
    
    @Override
    public MqttSubscribePayload payload() {
        return (MqttSubscribePayload)super.payload();
    }
}
