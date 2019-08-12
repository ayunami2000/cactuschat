// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.DecoderException;

public class MqttUnacceptableProtocolVersionException extends DecoderException
{
    private static final long serialVersionUID = 4914652213232455749L;
    
    public MqttUnacceptableProtocolVersionException() {
    }
    
    public MqttUnacceptableProtocolVersionException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public MqttUnacceptableProtocolVersionException(final String message) {
        super(message);
    }
    
    public MqttUnacceptableProtocolVersionException(final Throwable cause) {
        super(cause);
    }
}
