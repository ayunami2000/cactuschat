// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.spi;

import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Record;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Name;
import java.net.InetAddress;
import java.lang.reflect.Method;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;
import java.net.UnknownHostException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.ExtendedResolver;
import java.util.StringTokenizer;
import java.lang.reflect.InvocationHandler;

public class DNSJavaNameService implements InvocationHandler
{
    private static final String nsProperty = "sun.net.spi.nameservice.nameservers";
    private static final String domainProperty = "sun.net.spi.nameservice.domain";
    private static final String v6Property = "java.net.preferIPv6Addresses";
    private boolean preferV6;
    
    protected DNSJavaNameService() {
        this.preferV6 = false;
        final String nameServers = System.getProperty("sun.net.spi.nameservice.nameservers");
        final String domain = System.getProperty("sun.net.spi.nameservice.domain");
        final String v6 = System.getProperty("java.net.preferIPv6Addresses");
        if (nameServers != null) {
            final StringTokenizer st = new StringTokenizer(nameServers, ",");
            final String[] servers = new String[st.countTokens()];
            int n = 0;
            while (st.hasMoreTokens()) {
                servers[n++] = st.nextToken();
            }
            try {
                final Resolver res = new ExtendedResolver(servers);
                Lookup.setDefaultResolver(res);
            }
            catch (UnknownHostException e) {
                System.err.println("DNSJavaNameService: invalid sun.net.spi.nameservice.nameservers");
            }
        }
        if (domain != null) {
            try {
                Lookup.setDefaultSearchPath(new String[] { domain });
            }
            catch (TextParseException e2) {
                System.err.println("DNSJavaNameService: invalid sun.net.spi.nameservice.domain");
            }
        }
        if (v6 != null && v6.equalsIgnoreCase("true")) {
            this.preferV6 = true;
        }
    }
    
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            if (method.getName().equals("getHostByAddr")) {
                return this.getHostByAddr((byte[])args[0]);
            }
            if (method.getName().equals("lookupAllHostAddr")) {
                final InetAddress[] addresses = this.lookupAllHostAddr((String)args[0]);
                final Class returnType = method.getReturnType();
                if (returnType.equals(InetAddress[].class)) {
                    return addresses;
                }
                if (returnType.equals(byte[][].class)) {
                    final int naddrs = addresses.length;
                    final byte[][] byteAddresses = new byte[naddrs][];
                    for (int i = 0; i < naddrs; ++i) {
                        final byte[] addr = addresses[i].getAddress();
                        byteAddresses[i] = addr;
                    }
                    return byteAddresses;
                }
            }
        }
        catch (Throwable e) {
            System.err.println("DNSJavaNameService: Unexpected error.");
            e.printStackTrace();
            throw e;
        }
        throw new IllegalArgumentException("Unknown function name or arguments.");
    }
    
    public InetAddress[] lookupAllHostAddr(final String host) throws UnknownHostException {
        Name name = null;
        try {
            name = new Name(host);
        }
        catch (TextParseException e) {
            throw new UnknownHostException(host);
        }
        Record[] records = null;
        if (this.preferV6) {
            records = new Lookup(name, 28).run();
        }
        if (records == null) {
            records = new Lookup(name, 1).run();
        }
        if (records == null && !this.preferV6) {
            records = new Lookup(name, 28).run();
        }
        if (records == null) {
            throw new UnknownHostException(host);
        }
        final InetAddress[] array = new InetAddress[records.length];
        for (int i = 0; i < records.length; ++i) {
            final Record record = records[i];
            if (records[i] instanceof ARecord) {
                final ARecord a = (ARecord)records[i];
                array[i] = a.getAddress();
            }
            else {
                final AAAARecord aaaa = (AAAARecord)records[i];
                array[i] = aaaa.getAddress();
            }
        }
        return array;
    }
    
    public String getHostByAddr(final byte[] addr) throws UnknownHostException {
        final Name name = ReverseMap.fromAddress(InetAddress.getByAddress(addr));
        final Record[] records = new Lookup(name, 12).run();
        if (records == null) {
            throw new UnknownHostException();
        }
        return ((PTRRecord)records[0]).getTarget().toString();
    }
}
