// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

class ResolveThread extends Thread
{
    private Message query;
    private Object id;
    private ResolverListener listener;
    private Resolver res;
    
    public ResolveThread(final Resolver res, final Message query, final Object id, final ResolverListener listener) {
        this.res = res;
        this.query = query;
        this.id = id;
        this.listener = listener;
    }
    
    public void run() {
        try {
            final Message response = this.res.send(this.query);
            this.listener.receiveMessage(this.id, response);
        }
        catch (Exception e) {
            this.listener.handleException(this.id, e);
        }
    }
}
