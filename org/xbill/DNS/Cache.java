// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;

public class Cache
{
    private CacheMap data;
    private int maxncache;
    private int maxcache;
    private int dclass;
    private static final int defaultMaxEntries = 50000;
    
    private static int limitExpire(long ttl, final long maxttl) {
        if (maxttl >= 0L && maxttl < ttl) {
            ttl = maxttl;
        }
        final long expire = System.currentTimeMillis() / 1000L + ttl;
        if (expire < 0L || expire > 2147483647L) {
            return Integer.MAX_VALUE;
        }
        return (int)expire;
    }
    
    public Cache(final int dclass) {
        this.maxncache = -1;
        this.maxcache = -1;
        this.dclass = dclass;
        this.data = new CacheMap(50000);
    }
    
    public Cache() {
        this(1);
    }
    
    public Cache(final String file) throws IOException {
        this.maxncache = -1;
        this.maxcache = -1;
        this.data = new CacheMap(50000);
        final Master m = new Master(file);
        Record record;
        while ((record = m.nextRecord()) != null) {
            this.addRecord(record, 0, m);
        }
    }
    
    private synchronized Object exactName(final Name name) {
        return this.data.get(name);
    }
    
    private synchronized void removeName(final Name name) {
        this.data.remove(name);
    }
    
    private synchronized Element[] allElements(final Object types) {
        if (types instanceof List) {
            final List typelist = (List)types;
            final int size = typelist.size();
            return typelist.toArray(new Element[size]);
        }
        final Element set = (Element)types;
        return new Element[] { set };
    }
    
    private synchronized Element oneElement(final Name name, final Object types, final int type, final int minCred) {
        Element found = null;
        if (type == 255) {
            throw new IllegalArgumentException("oneElement(ANY)");
        }
        if (types instanceof List) {
            final List list = (List)types;
            for (int i = 0; i < list.size(); ++i) {
                final Element set = list.get(i);
                if (set.getType() == type) {
                    found = set;
                    break;
                }
            }
        }
        else {
            final Element set2 = (Element)types;
            if (set2.getType() == type) {
                found = set2;
            }
        }
        if (found == null) {
            return null;
        }
        if (found.expired()) {
            this.removeElement(name, type);
            return null;
        }
        if (found.compareCredibility(minCred) < 0) {
            return null;
        }
        return found;
    }
    
    private synchronized Element findElement(final Name name, final int type, final int minCred) {
        final Object types = this.exactName(name);
        if (types == null) {
            return null;
        }
        return this.oneElement(name, types, type, minCred);
    }
    
    private synchronized void addElement(final Name name, final Element element) {
        final Object types = this.data.get(name);
        if (types == null) {
            this.data.put(name, element);
            return;
        }
        final int type = element.getType();
        if (types instanceof List) {
            final List list = (List)types;
            for (int i = 0; i < list.size(); ++i) {
                final Element elt = list.get(i);
                if (elt.getType() == type) {
                    list.set(i, element);
                    return;
                }
            }
            list.add(element);
        }
        else {
            final Element elt2 = (Element)types;
            if (elt2.getType() == type) {
                this.data.put(name, element);
            }
            else {
                final LinkedList list2 = new LinkedList();
                list2.add(elt2);
                list2.add(element);
                this.data.put(name, list2);
            }
        }
    }
    
    private synchronized void removeElement(final Name name, final int type) {
        final Object types = this.data.get(name);
        if (types == null) {
            return;
        }
        if (types instanceof List) {
            final List list = (List)types;
            for (int i = 0; i < list.size(); ++i) {
                final Element elt = list.get(i);
                if (elt.getType() == type) {
                    list.remove(i);
                    if (list.size() == 0) {
                        this.data.remove(name);
                    }
                    return;
                }
            }
        }
        else {
            final Element elt2 = (Element)types;
            if (elt2.getType() != type) {
                return;
            }
            this.data.remove(name);
        }
    }
    
    public synchronized void clearCache() {
        this.data.clear();
    }
    
    public synchronized void addRecord(final Record r, final int cred, final Object o) {
        final Name name = r.getName();
        final int type = r.getRRsetType();
        if (!Type.isRR(type)) {
            return;
        }
        final Element element = this.findElement(name, type, cred);
        if (element == null) {
            final CacheRRset crrset = new CacheRRset(r, cred, this.maxcache);
            this.addRRset(crrset, cred);
        }
        else if (element.compareCredibility(cred) == 0 && element instanceof CacheRRset) {
            final CacheRRset crrset = (CacheRRset)element;
            crrset.addRR(r);
        }
    }
    
    public synchronized void addRRset(final RRset rrset, final int cred) {
        final long ttl = rrset.getTTL();
        final Name name = rrset.getName();
        final int type = rrset.getType();
        Element element = this.findElement(name, type, 0);
        if (ttl == 0L) {
            if (element != null && element.compareCredibility(cred) <= 0) {
                this.removeElement(name, type);
            }
        }
        else {
            if (element != null && element.compareCredibility(cred) <= 0) {
                element = null;
            }
            if (element == null) {
                CacheRRset crrset;
                if (rrset instanceof CacheRRset) {
                    crrset = (CacheRRset)rrset;
                }
                else {
                    crrset = new CacheRRset(rrset, cred, this.maxcache);
                }
                this.addElement(name, crrset);
            }
        }
    }
    
    public synchronized void addNegative(final Name name, final int type, final SOARecord soa, final int cred) {
        long ttl = 0L;
        if (soa != null) {
            ttl = soa.getTTL();
        }
        Element element = this.findElement(name, type, 0);
        if (ttl == 0L) {
            if (element != null && element.compareCredibility(cred) <= 0) {
                this.removeElement(name, type);
            }
        }
        else {
            if (element != null && element.compareCredibility(cred) <= 0) {
                element = null;
            }
            if (element == null) {
                this.addElement(name, new NegativeElement(name, type, soa, cred, this.maxncache));
            }
        }
    }
    
    protected synchronized SetResponse lookup(final Name name, final int type, final int minCred) {
        int tlabels;
        for (int labels = tlabels = name.labels(); tlabels >= 1; --tlabels) {
            final boolean isRoot = tlabels == 1;
            final boolean isExact = tlabels == labels;
            Name tname;
            if (isRoot) {
                tname = Name.root;
            }
            else if (isExact) {
                tname = name;
            }
            else {
                tname = new Name(name, labels - tlabels);
            }
            final Object types = this.data.get(tname);
            if (types != null) {
                if (isExact && type == 255) {
                    final SetResponse sr = new SetResponse(6);
                    final Element[] elements = this.allElements(types);
                    int added = 0;
                    for (int i = 0; i < elements.length; ++i) {
                        final Element element = elements[i];
                        if (element.expired()) {
                            this.removeElement(tname, element.getType());
                        }
                        else if (element instanceof CacheRRset) {
                            if (element.compareCredibility(minCred) >= 0) {
                                sr.addRRset((RRset)element);
                                ++added;
                            }
                        }
                    }
                    if (added > 0) {
                        return sr;
                    }
                }
                if (isExact) {
                    Element element = this.oneElement(tname, types, type, minCred);
                    if (element != null && element instanceof CacheRRset) {
                        final SetResponse sr = new SetResponse(6);
                        sr.addRRset((RRset)element);
                        return sr;
                    }
                    if (element != null) {
                        final SetResponse sr = new SetResponse(2);
                        return sr;
                    }
                    element = this.oneElement(tname, types, 5, minCred);
                    if (element != null && element instanceof CacheRRset) {
                        return new SetResponse(4, (RRset)element);
                    }
                }
                else {
                    final Element element = this.oneElement(tname, types, 39, minCred);
                    if (element != null && element instanceof CacheRRset) {
                        return new SetResponse(5, (RRset)element);
                    }
                }
                Element element = this.oneElement(tname, types, 2, minCred);
                if (element != null && element instanceof CacheRRset) {
                    return new SetResponse(3, (RRset)element);
                }
                if (isExact) {
                    element = this.oneElement(tname, types, 0, minCred);
                    if (element != null) {
                        return SetResponse.ofType(1);
                    }
                }
            }
        }
        return SetResponse.ofType(0);
    }
    
    public SetResponse lookupRecords(final Name name, final int type, final int minCred) {
        return this.lookup(name, type, minCred);
    }
    
    private RRset[] findRecords(final Name name, final int type, final int minCred) {
        final SetResponse cr = this.lookupRecords(name, type, minCred);
        if (cr.isSuccessful()) {
            return cr.answers();
        }
        return null;
    }
    
    public RRset[] findRecords(final Name name, final int type) {
        return this.findRecords(name, type, 3);
    }
    
    public RRset[] findAnyRecords(final Name name, final int type) {
        return this.findRecords(name, type, 2);
    }
    
    private final int getCred(final int section, final boolean isAuth) {
        if (section == 1) {
            if (isAuth) {
                return 4;
            }
            return 3;
        }
        else if (section == 2) {
            if (isAuth) {
                return 4;
            }
            return 3;
        }
        else {
            if (section == 3) {
                return 1;
            }
            throw new IllegalArgumentException("getCred: invalid section");
        }
    }
    
    private static void markAdditional(final RRset rrset, final Set names) {
        final Record first = rrset.first();
        if (first.getAdditionalName() == null) {
            return;
        }
        final Iterator it = rrset.rrs();
        while (it.hasNext()) {
            final Record r = it.next();
            final Name name = r.getAdditionalName();
            if (name != null) {
                names.add(name);
            }
        }
    }
    
    public SetResponse addMessage(final Message in) {
        final boolean isAuth = in.getHeader().getFlag(5);
        final Record question = in.getQuestion();
        final int rcode = in.getHeader().getRcode();
        boolean completed = false;
        SetResponse response = null;
        final boolean verbose = Options.check("verbosecache");
        if ((rcode != 0 && rcode != 3) || question == null) {
            return null;
        }
        final Name qname = question.getName();
        final int qtype = question.getType();
        final int qclass = question.getDClass();
        Name curname = qname;
        final HashSet additionalNames = new HashSet();
        final RRset[] answers = in.getSectionRRsets(1);
        for (int i = 0; i < answers.length; ++i) {
            if (answers[i].getDClass() == qclass) {
                final int type = answers[i].getType();
                final Name name = answers[i].getName();
                final int cred = this.getCred(1, isAuth);
                if ((type == qtype || qtype == 255) && name.equals(curname)) {
                    this.addRRset(answers[i], cred);
                    completed = true;
                    if (curname == qname) {
                        if (response == null) {
                            response = new SetResponse(6);
                        }
                        response.addRRset(answers[i]);
                    }
                    markAdditional(answers[i], additionalNames);
                }
                else if (type == 5 && name.equals(curname)) {
                    this.addRRset(answers[i], cred);
                    if (curname == qname) {
                        response = new SetResponse(4, answers[i]);
                    }
                    final CNAMERecord cname = (CNAMERecord)answers[i].first();
                    curname = cname.getTarget();
                }
                else if (type == 39 && curname.subdomain(name)) {
                    this.addRRset(answers[i], cred);
                    if (curname == qname) {
                        response = new SetResponse(5, answers[i]);
                    }
                    final DNAMERecord dname = (DNAMERecord)answers[i].first();
                    try {
                        curname = curname.fromDNAME(dname);
                    }
                    catch (NameTooLongException e) {
                        break;
                    }
                }
            }
        }
        final RRset[] auth = in.getSectionRRsets(2);
        RRset soa = null;
        RRset ns = null;
        for (int j = 0; j < auth.length; ++j) {
            if (auth[j].getType() == 6 && curname.subdomain(auth[j].getName())) {
                soa = auth[j];
            }
            else if (auth[j].getType() == 2 && curname.subdomain(auth[j].getName())) {
                ns = auth[j];
            }
        }
        if (!completed) {
            final int cachetype = (rcode == 3) ? 0 : qtype;
            if (rcode == 3 || soa != null || ns == null) {
                final int cred = this.getCred(2, isAuth);
                SOARecord soarec = null;
                if (soa != null) {
                    soarec = (SOARecord)soa.first();
                }
                this.addNegative(curname, cachetype, soarec, cred);
                if (response == null) {
                    int responseType;
                    if (rcode == 3) {
                        responseType = 1;
                    }
                    else {
                        responseType = 2;
                    }
                    response = SetResponse.ofType(responseType);
                }
            }
            else {
                final int cred = this.getCred(2, isAuth);
                this.addRRset(ns, cred);
                markAdditional(ns, additionalNames);
                if (response == null) {
                    response = new SetResponse(3, ns);
                }
            }
        }
        else if (rcode == 0 && ns != null) {
            final int cred = this.getCred(2, isAuth);
            this.addRRset(ns, cred);
            markAdditional(ns, additionalNames);
        }
        final RRset[] addl = in.getSectionRRsets(3);
        for (int j = 0; j < addl.length; ++j) {
            final int type2 = addl[j].getType();
            if (type2 == 1 || type2 == 28 || type2 == 38) {
                final Name name2 = addl[j].getName();
                if (additionalNames.contains(name2)) {
                    final int cred = this.getCred(3, isAuth);
                    this.addRRset(addl[j], cred);
                }
            }
        }
        if (verbose) {
            System.out.println("addMessage: " + response);
        }
        return response;
    }
    
    public void flushSet(final Name name, final int type) {
        this.removeElement(name, type);
    }
    
    public void flushName(final Name name) {
        this.removeName(name);
    }
    
    public void setMaxNCache(final int seconds) {
        this.maxncache = seconds;
    }
    
    public int getMaxNCache() {
        return this.maxncache;
    }
    
    public void setMaxCache(final int seconds) {
        this.maxcache = seconds;
    }
    
    public int getMaxCache() {
        return this.maxcache;
    }
    
    public int getSize() {
        return this.data.size();
    }
    
    public int getMaxEntries() {
        return this.data.getMaxSize();
    }
    
    public void setMaxEntries(final int entries) {
        this.data.setMaxSize(entries);
    }
    
    public int getDClass() {
        return this.dclass;
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        synchronized (this) {
            final Iterator it = this.data.values().iterator();
            while (it.hasNext()) {
                final Element[] elements = this.allElements(it.next());
                for (int i = 0; i < elements.length; ++i) {
                    sb.append(elements[i]);
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
    
    private static class CacheRRset extends RRset implements Element
    {
        private static final long serialVersionUID = 5971755205903597024L;
        int credibility;
        int expire;
        
        public CacheRRset(final Record rec, final int cred, final long maxttl) {
            this.credibility = cred;
            this.expire = limitExpire(rec.getTTL(), maxttl);
            this.addRR(rec);
        }
        
        public CacheRRset(final RRset rrset, final int cred, final long maxttl) {
            super(rrset);
            this.credibility = cred;
            this.expire = limitExpire(rrset.getTTL(), maxttl);
        }
        
        public final boolean expired() {
            final int now = (int)(System.currentTimeMillis() / 1000L);
            return now >= this.expire;
        }
        
        public final int compareCredibility(final int cred) {
            return this.credibility - cred;
        }
        
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            sb.append(" cl = ");
            sb.append(this.credibility);
            return sb.toString();
        }
    }
    
    private static class NegativeElement implements Element
    {
        int type;
        Name name;
        int credibility;
        int expire;
        
        public NegativeElement(final Name name, final int type, final SOARecord soa, final int cred, final long maxttl) {
            this.name = name;
            this.type = type;
            long cttl = 0L;
            if (soa != null) {
                cttl = soa.getMinimum();
            }
            this.credibility = cred;
            this.expire = limitExpire(cttl, maxttl);
        }
        
        public int getType() {
            return this.type;
        }
        
        public final boolean expired() {
            final int now = (int)(System.currentTimeMillis() / 1000L);
            return now >= this.expire;
        }
        
        public final int compareCredibility(final int cred) {
            return this.credibility - cred;
        }
        
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            if (this.type == 0) {
                sb.append("NXDOMAIN " + this.name);
            }
            else {
                sb.append("NXRRSET " + this.name + " " + Type.string(this.type));
            }
            sb.append(" cl = ");
            sb.append(this.credibility);
            return sb.toString();
        }
    }
    
    private static class CacheMap extends LinkedHashMap
    {
        private int maxsize;
        
        CacheMap(final int maxsize) {
            super(16, 0.75f, true);
            this.maxsize = -1;
            this.maxsize = maxsize;
        }
        
        int getMaxSize() {
            return this.maxsize;
        }
        
        void setMaxSize(final int maxsize) {
            this.maxsize = maxsize;
        }
        
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return this.maxsize >= 0 && this.size() > this.maxsize;
        }
    }
    
    private interface Element
    {
        boolean expired();
        
        int compareCredibility(final int p0);
        
        int getType();
    }
}
