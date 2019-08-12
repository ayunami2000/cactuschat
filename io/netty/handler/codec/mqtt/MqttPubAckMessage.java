// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

public class MqttPubAckMessage extends MqttMessage
{
    public MqttPubAckMessage(final MqttFixedHeader mqttFixedHeader, final MqttMessageIdVariableHeader variableHeader) {
        super(mqttFixedHeader, variableHeader);
    }
    
    @Override
    public MqttMessageIdVariableHeader variableHeader() {
        return (MqttMessageIdVariableHeader)super.variableHeader();
    }
}
