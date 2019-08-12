// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver.dns;

import io.netty.util.internal.PlatformDependent;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.Random;
import io.netty.util.internal.ThreadLocalRandom;
import java.lang.reflect.Method;
import java.util.Collections;
import java.net.InetAddress;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.net.InetSocketAddress;
import java.util.List;
import io.netty.util.internal.logging.InternalLogger;

public final class DnsServerAddresses
{
    private static final InternalLogger logger;
    private static final List<InetSocketAddress> DEFAULT_NAME_SERVER_LIST;
    private static final InetSocketAddress[] DEFAULT_NAME_SERVER_ARRAY;
    
    public static List<InetSocketAddress> defaultAddresses() {
        return DnsServerAddresses.DEFAULT_NAME_SERVER_LIST;
    }
    
    public static Iterable<InetSocketAddress> sequential(final Iterable<? extends InetSocketAddress> addresses) {
        return sequential0(sanitize(addresses));
    }
    
    public static Iterable<InetSocketAddress> sequential(final InetSocketAddress... addresses) {
        return sequential0(sanitize(addresses));
    }
    
    private static Iterable<InetSocketAddress> sequential0(final InetSocketAddress[] addresses) {
        return new Iterable<InetSocketAddress>() {
            @Override
            public Iterator<InetSocketAddress> iterator() {
                return new SequentialAddressIterator(addresses, 0);
            }
        };
    }
    
    public static Iterable<InetSocketAddress> shuffled(final Iterable<? extends InetSocketAddress> addresses) {
        return shuffled0(sanitize(addresses));
    }
    
    public static Iterable<InetSocketAddress> shuffled(final InetSocketAddress... addresses) {
        return shuffled0(sanitize(addresses));
    }
    
    private static Iterable<InetSocketAddress> shuffled0(final InetSocketAddress[] addresses) {
        if (addresses.length == 1) {
            return singleton(addresses[0]);
        }
        return new Iterable<InetSocketAddress>() {
            @Override
            public Iterator<InetSocketAddress> iterator() {
                return new ShuffledAddressIterator(addresses);
            }
        };
    }
    
    public static Iterable<InetSocketAddress> rotational(final Iterable<? extends InetSocketAddress> addresses) {
        return rotational0(sanitize(addresses));
    }
    
    public static Iterable<InetSocketAddress> rotational(final InetSocketAddress... addresses) {
        return rotational0(sanitize(addresses));
    }
    
    private static Iterable<InetSocketAddress> rotational0(final InetSocketAddress[] addresses) {
        return new RotationalAddresses(addresses);
    }
    
    public static Iterable<InetSocketAddress> singleton(final InetSocketAddress address) {
        if (address == null) {
            throw new NullPointerException("address");
        }
        if (address.isUnresolved()) {
            throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + address);
        }
        return new Iterable<InetSocketAddress>() {
            private final Iterator<InetSocketAddress> iterator = new Iterator<InetSocketAddress>() {
                @Override
                public boolean hasNext() {
                    return true;
                }
                
                @Override
                public InetSocketAddress next() {
                    return address;
                }
                
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
            
            @Override
            public Iterator<InetSocketAddress> iterator() {
                return this.iterator;
            }
        };
    }
    
    private static InetSocketAddress[] sanitize(final Iterable<? extends InetSocketAddress> addresses) {
        if (addresses == null) {
            throw new NullPointerException("addresses");
        }
        List<InetSocketAddress> list;
        if (addresses instanceof Collection) {
            list = new ArrayList<InetSocketAddress>(((Collection)addresses).size());
        }
        else {
            list = new ArrayList<InetSocketAddress>(4);
        }
        for (final InetSocketAddress a : addresses) {
            if (a == null) {
                break;
            }
            if (a.isUnresolved()) {
                throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + a);
            }
            list.add(a);
        }
        if (list.isEmpty()) {
            return DnsServerAddresses.DEFAULT_NAME_SERVER_ARRAY;
        }
        return list.toArray(new InetSocketAddress[list.size()]);
    }
    
    private static InetSocketAddress[] sanitize(final InetSocketAddress[] addresses) {
        if (addresses == null) {
            throw new NullPointerException("addresses");
        }
        final List<InetSocketAddress> list = new ArrayList<InetSocketAddress>(addresses.length);
        for (final InetSocketAddress a : addresses) {
            if (a == null) {
                break;
            }
            if (a.isUnresolved()) {
                throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + a);
            }
            list.add(a);
        }
        if (list.isEmpty()) {
            return DnsServerAddresses.DEFAULT_NAME_SERVER_ARRAY;
        }
        return list.toArray(new InetSocketAddress[list.size()]);
    }
    
    private DnsServerAddresses() {
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(DnsServerAddresses.class);
        final int DNS_PORT = 53;
        final List<InetSocketAddress> defaultNameServers = new ArrayList<InetSocketAddress>(2);
        try {
            final Class<?> configClass = Class.forName("sun.net.dns.ResolverConfiguration");
            final Method open = configClass.getMethod("open", (Class<?>[])new Class[0]);
            final Method nameservers = configClass.getMethod("nameservers", (Class<?>[])new Class[0]);
            final Object instance = open.invoke(null, new Object[0]);
            final List<String> list = (List<String>)nameservers.invoke(instance, new Object[0]);
            for (int size = list.size(), i = 0; i < size; ++i) {
                final String dnsAddr = list.get(i);
                if (dnsAddr != null) {
                    defaultNameServers.add(new InetSocketAddress(InetAddress.getByName(dnsAddr), 53));
                }
            }
        }
        catch (Exception ex) {}
        if (!defaultNameServers.isEmpty()) {
            if (DnsServerAddresses.logger.isDebugEnabled()) {
                DnsServerAddresses.logger.debug("Default DNS servers: {} (sun.net.dns.ResolverConfiguration)", defaultNameServers);
            }
        }
        else {
            Collections.addAll(defaultNameServers, new InetSocketAddress[] { new InetSocketAddress("8.8.8.8", 53), new InetSocketAddress("8.8.4.4", 53) });
            if (DnsServerAddresses.logger.isWarnEnabled()) {
                DnsServerAddresses.logger.warn("Default DNS servers: {} (Google Public DNS as a fallback)", defaultNameServers);
            }
        }
        DEFAULT_NAME_SERVER_LIST = Collections.unmodifiableList((List<? extends InetSocketAddress>)defaultNameServers);
        DEFAULT_NAME_SERVER_ARRAY = defaultNameServers.toArray(new InetSocketAddress[defaultNameServers.size()]);
    }
    
    private static final class SequentialAddressIterator implements Iterator<InetSocketAddress>
    {
        private final InetSocketAddress[] addresses;
        private int i;
        
        SequentialAddressIterator(final InetSocketAddress[] addresses, final int startIdx) {
            this.addresses = addresses;
            this.i = startIdx;
        }
        
        @Override
        public boolean hasNext() {
            return true;
        }
        
        @Override
        public InetSocketAddress next() {
            int i = this.i;
            final InetSocketAddress next = this.addresses[i];
            if (++i < this.addresses.length) {
                this.i = i;
            }
            else {
                this.i = 0;
            }
            return next;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    private static final class ShuffledAddressIterator implements Iterator<InetSocketAddress>
    {
        private final InetSocketAddress[] addresses;
        private int i;
        
        ShuffledAddressIterator(final InetSocketAddress[] addresses) {
            this.addresses = addresses.clone();
            this.shuffle();
        }
        
        private void shuffle() {
            final InetSocketAddress[] addresses = this.addresses;
            final Random r = ThreadLocalRandom.current();
            for (int i = addresses.length - 1; i >= 0; --i) {
                final InetSocketAddress tmp = addresses[i];
                final int j = r.nextInt(i + 1);
                addresses[i] = addresses[j];
                addresses[j] = tmp;
            }
        }
        
        @Override
        public boolean hasNext() {
            return true;
        }
        
        @Override
        public InetSocketAddress next() {
            int i = this.i;
            final InetSocketAddress next = this.addresses[i];
            if (++i < this.addresses.length) {
                this.i = i;
            }
            else {
                this.i = 0;
                this.shuffle();
            }
            return next;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    private static final class RotationalAddresses implements Iterable<InetSocketAddress>
    {
        private static final AtomicIntegerFieldUpdater<RotationalAddresses> startIdxUpdater;
        private final InetSocketAddress[] addresses;
        private volatile int startIdx;
        
        RotationalAddresses(final InetSocketAddress[] addresses) {
            this.addresses = addresses;
        }
        
        @Override
        public Iterator<InetSocketAddress> iterator() {
            int curStartIdx;
            int nextStartIdx;
            do {
                curStartIdx = this.startIdx;
                nextStartIdx = curStartIdx + 1;
                if (nextStartIdx >= this.addresses.length) {
                    nextStartIdx = 0;
                }
            } while (!RotationalAddresses.startIdxUpdater.compareAndSet(this, curStartIdx, nextStartIdx));
            return new SequentialAddressIterator(this.addresses, curStartIdx);
        }
        
        static {
            AtomicIntegerFieldUpdater<RotationalAddresses> updater = PlatformDependent.newAtomicIntegerFieldUpdater(RotationalAddresses.class, "startIdx");
            if (updater == null) {
                updater = AtomicIntegerFieldUpdater.newUpdater(RotationalAddresses.class, "startIdx");
            }
            startIdxUpdater = updater;
        }
    }
}
