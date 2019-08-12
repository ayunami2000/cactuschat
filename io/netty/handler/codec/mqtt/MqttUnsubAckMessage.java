// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

public class MqttUnsubAckMessage extends MqttMessage
{
    public MqttUnsubAckMessage(final MqttFixedHeader mqttFixedHeader, final MqttMessageIdVariableHeader variableHeader) {
        super(mqttFixedHeader, variableHeader, null);
    }
    
    @Override
    public MqttMessageIdVariableHeader variableHeader() {
        return (MqttMessageIdVariableHeader)super.variableHeader();
    }
}
