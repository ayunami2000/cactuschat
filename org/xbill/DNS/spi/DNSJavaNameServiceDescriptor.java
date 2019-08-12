// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.spi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;

public class DNSJavaNameServiceDescriptor implements NameServiceDescriptor
{
    private static NameService nameService;
    
    public NameService createNameService() {
        return DNSJavaNameServiceDescriptor.nameService;
    }
    
    public String getType() {
        return "dns";
    }
    
    public String getProviderName() {
        return "dnsjava";
    }
    
    static {
        final ClassLoader loader = NameService.class.getClassLoader();
        DNSJavaNameServiceDescriptor.nameService = (NameService)Proxy.newProxyInstance(loader, new Class[] { NameService.class }, new DNSJavaNameService());
    }
}
