// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.mqtt;

import io.netty.util.internal.StringUtil;

public class MqttTopicSubscription
{
    private final String topicFilter;
    private final MqttQoS qualityOfService;
    
    public MqttTopicSubscription(final String topicFilter, final MqttQoS qualityOfService) {
        this.topicFilter = topicFilter;
        this.qualityOfService = qualityOfService;
    }
    
    public String topicName() {
        return this.topicFilter;
    }
    
    public MqttQoS qualityOfService() {
        return this.qualityOfService;
    }
    
    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + '[' + "topicFilter=" + this.topicFilter + ", qualityOfService=" + this.qualityOfService + ']';
    }
}
