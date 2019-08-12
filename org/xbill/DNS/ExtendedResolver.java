// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.SocketException;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ExtendedResolver implements Resolver
{
    private static final int quantum = 5;
    private List resolvers;
    private boolean loadBalance;
    private int lbStart;
    private int retries;
    
    private void init() {
        this.resolvers = new ArrayList();
    }
    
    public ExtendedResolver() throws UnknownHostException {
        this.loadBalance = false;
        this.lbStart = 0;
        this.retries = 3;
        this.init();
        final String[] servers = ResolverConfig.getCurrentConfig().servers();
        if (servers != null) {
            for (int i = 0; i < servers.length; ++i) {
                final Resolver r = new SimpleResolver(servers[i]);
                r.setTimeout(5);
                this.resolvers.add(r);
            }
        }
        else {
            this.resolvers.add(new SimpleResolver());
        }
    }
    
    public ExtendedResolver(final String[] servers) throws UnknownHostException {
        this.loadBalance = false;
        this.lbStart = 0;
        this.retries = 3;
        this.init();
        for (int i = 0; i < servers.length; ++i) {
            final Resolver r = new SimpleResolver(servers[i]);
            r.setTimeout(5);
            this.resolvers.add(r);
        }
    }
    
    public ExtendedResolver(final Resolver[] res) throws UnknownHostException {
        this.loadBalance = false;
        this.lbStart = 0;
        this.retries = 3;
        this.init();
        for (int i = 0; i < res.length; ++i) {
            this.resolvers.add(res[i]);
        }
    }
    
    public void setPort(final int port) {
        for (int i = 0; i < this.resolvers.size(); ++i) {
            this.resolvers.get(i).setPort(port);
        }
    }
    
    public void setTCP(final boolean flag) {
        for (int i = 0; i < this.resolvers.size(); ++i) {
            this.resolvers.get(i).setTCP(flag);
        }
    }
    
    public void setIgnoreTruncation(final boolean flag) {
        for (int i = 0; i < this.resolvers.size(); ++i) {
            this.resolvers.get(i).setIgnoreTruncation(flag);
        }
    }
    
    public void setEDNS(final int level) {
        for (int i = 0; i < this.resolvers.size(); ++i) {
            this.resolvers.get(i).setEDNS(level);
        }
    }
    
    public void setEDNS(final int level, final int payloadSize, final int flags, final List options) {
        for (int i = 0; i < this.resolvers.size(); ++i) {
            this.resolvers.get(i).setEDNS(level, payloadSize, flags, options);
        }
    }
    
    public void setTSIGKey(final TSIG key) {
        for (int i = 0; i < this.resolvers.size(); ++i) {
            this.resolvers.get(i).setTSIGKey(key);
        }
    }
    
    public void setTimeout(final int secs, final int msecs) {
        for (int i = 0; i < this.resolvers.size(); ++i) {
            this.resolvers.get(i).setTimeout(secs, msecs);
        }
    }
    
    public void setTimeout(final int secs) {
        this.setTimeout(secs, 0);
    }
    
    public Message send(final Message query) throws IOException {
        final Resolution res = new Resolution(this, query);
        return res.start();
    }
    
    public Object sendAsync(final Message query, final ResolverListener listener) {
        final Resolution res = new Resolution(this, query);
        res.startAsync(listener);
        return res;
    }
    
    public Resolver getResolver(final int n) {
        if (n < this.resolvers.size()) {
            return this.resolvers.get(n);
        }
        return null;
    }
    
    public Resolver[] getResolvers() {
        return this.resolvers.toArray(new Resolver[this.resolvers.size()]);
    }
    
    public void addResolver(final Resolver r) {
        this.resolvers.add(r);
    }
    
    public void deleteResolver(final Resolver r) {
        this.resolvers.remove(r);
    }
    
    public void setLoadBalance(final boolean flag) {
        this.loadBalance = flag;
    }
    
    public void setRetries(final int retries) {
        this.retries = retries;
    }
    
    private static class Resolution implements ResolverListener
    {
        Resolver[] resolvers;
        int[] sent;
        Object[] inprogress;
        int retries;
        int outstanding;
        boolean done;
        Message query;
        Message response;
        Throwable thrown;
        ResolverListener listener;
        
        public Resolution(final ExtendedResolver eres, final Message query) {
            final List l = eres.resolvers;
            this.resolvers = l.toArray(new Resolver[l.size()]);
            if (eres.loadBalance) {
                final int nresolvers = this.resolvers.length;
                final int start = eres.lbStart++ % nresolvers;
                if (eres.lbStart > nresolvers) {
                    eres.lbStart %= nresolvers;
                }
                if (start > 0) {
                    final Resolver[] shuffle = new Resolver[nresolvers];
                    for (int i = 0; i < nresolvers; ++i) {
                        final int pos = (i + start) % nresolvers;
                        shuffle[i] = this.resolvers[pos];
                    }
                    this.resolvers = shuffle;
                }
            }
            this.sent = new int[this.resolvers.length];
            this.inprogress = new Object[this.resolvers.length];
            this.retries = eres.retries;
            this.query = query;
        }
        
        public void send(final int n) {
            final int[] sent = this.sent;
            ++sent[n];
            ++this.outstanding;
            try {
                this.inprogress[n] = this.resolvers[n].sendAsync(this.query, this);
            }
            catch (Throwable t) {
                synchronized (this) {
                    this.thrown = t;
                    this.done = true;
                    if (this.listener == null) {
                        this.notifyAll();
                    }
                }
            }
        }
        
        public Message start() throws IOException {
            try {
                final int[] sent = this.sent;
                final int n = 0;
                ++sent[n];
                ++this.outstanding;
                this.inprogress[0] = new Object();
                return this.resolvers[0].send(this.query);
            }
            catch (Exception e) {
                this.handleException(this.inprogress[0], e);
                synchronized (this) {
                    while (!this.done) {
                        try {
                            this.wait();
                        }
                        catch (InterruptedException e2) {}
                    }
                }
                if (this.response != null) {
                    return this.response;
                }
                if (this.thrown instanceof IOException) {
                    throw (IOException)this.thrown;
                }
                if (this.thrown instanceof RuntimeException) {
                    throw (RuntimeException)this.thrown;
                }
                if (this.thrown instanceof Error) {
                    throw (Error)this.thrown;
                }
                throw new IllegalStateException("ExtendedResolver failure");
            }
        }
        
        public void startAsync(final ResolverListener listener) {
            this.listener = listener;
            this.send(0);
        }
        
        public void receiveMessage(final Object id, final Message m) {
            if (Options.check("verbose")) {
                System.err.println("ExtendedResolver: received message");
            }
            synchronized (this) {
                if (this.done) {
                    return;
                }
                this.response = m;
                this.done = true;
                if (this.listener == null) {
                    this.notifyAll();
                    return;
                }
            }
            this.listener.receiveMessage(this, this.response);
        }
        
        public void handleException(final Object id, final Exception e) {
            if (Options.check("verbose")) {
                System.err.println("ExtendedResolver: got " + e);
            }
            synchronized (this) {
                --this.outstanding;
                if (this.done) {
                    return;
                }
                int n;
                for (n = 0; n < this.inprogress.length && this.inprogress[n] != id; ++n) {}
                if (n == this.inprogress.length) {
                    return;
                }
                boolean startnext = false;
                if (this.sent[n] == 1 && n < this.resolvers.length - 1) {
                    startnext = true;
                }
                if (e instanceof InterruptedIOException) {
                    if (this.sent[n] < this.retries) {
                        this.send(n);
                    }
                    if (this.thrown == null) {
                        this.thrown = e;
                    }
                }
                else if (e instanceof SocketException) {
                    if (this.thrown == null || this.thrown instanceof InterruptedIOException) {
                        this.thrown = e;
                    }
                }
                else {
                    this.thrown = e;
                }
                if (this.done) {
                    return;
                }
                if (startnext) {
                    this.send(n + 1);
                }
                if (this.done) {
                    return;
                }
                if (this.outstanding == 0) {
                    this.done = true;
                    if (this.listener == null) {
                        this.notifyAll();
                        return;
                    }
                }
                if (!this.done) {
                    return;
                }
            }
            if (!(this.thrown instanceof Exception)) {
                this.thrown = new RuntimeException(this.thrown.getMessage());
            }
            this.listener.handleException(this, (Exception)this.thrown);
        }
    }
}
