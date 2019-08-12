// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

import io.netty.util.internal.StringUtil;

public class MqttConnAckVariableHeader
{
    private final MqttConnectReturnCode connectReturnCode;
    
    public MqttConnAckVariableHeader(final MqttConnectReturnCode connectReturnCode) {
        this.connectReturnCode = connectReturnCode;
    }
    
    public MqttConnectReturnCode connectReturnCode() {
        return this.connectReturnCode;
    }
    
    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + '[' + "connectReturnCode=" + this.connectReturnCode + ']';
    }
}
