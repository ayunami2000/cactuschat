// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ipfilter;

import java.net.InetSocketAddress;

public interface IpFilterRule
{
    boolean matches(final InetSocketAddress p0);
    
    IpFilterRuleType ruleType();
}
