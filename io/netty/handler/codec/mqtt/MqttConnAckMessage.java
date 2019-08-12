// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

public final class MqttConnAckMessage extends MqttMessage
{
    public MqttConnAckMessage(final MqttFixedHeader mqttFixedHeader, final MqttConnAckVariableHeader variableHeader) {
        super(mqttFixedHeader, variableHeader);
    }
    
    @Override
    public MqttConnAckVariableHeader variableHeader() {
        return (MqttConnAckVariableHeader)super.variableHeader();
    }
}
