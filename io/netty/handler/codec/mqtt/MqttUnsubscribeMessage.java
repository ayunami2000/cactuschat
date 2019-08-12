// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

public class MqttUnsubscribeMessage extends MqttMessage
{
    public MqttUnsubscribeMessage(final MqttFixedHeader mqttFixedHeader, final MqttMessageIdVariableHeader variableHeader, final MqttUnsubscribePayload payload) {
        super(mqttFixedHeader, variableHeader, payload);
    }
    
    @Override
    public MqttMessageIdVariableHeader variableHeader() {
        return (MqttMessageIdVariableHeader)super.variableHeader();
    }
    
    @Override
    public MqttUnsubscribePayload payload() {
        return (MqttUnsubscribePayload)super.payload();
    }
}
