// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

public final class Lookup
{
    private static Resolver defaultResolver;
    private static Name[] defaultSearchPath;
    private static Map defaultCaches;
    private static int defaultNdots;
    private Resolver resolver;
    private Name[] searchPath;
    private Cache cache;
    private boolean temporary_cache;
    private int credibility;
    private Name name;
    private int type;
    private int dclass;
    private boolean verbose;
    private int iterations;
    private boolean foundAlias;
    private boolean done;
    private boolean doneCurrent;
    private List aliases;
    private Record[] answers;
    private int result;
    private String error;
    private boolean nxdomain;
    private boolean badresponse;
    private String badresponse_error;
    private boolean networkerror;
    private boolean timedout;
    private boolean nametoolong;
    private boolean referral;
    private static final Name[] noAliases;
    public static final int SUCCESSFUL = 0;
    public static final int UNRECOVERABLE = 1;
    public static final int TRY_AGAIN = 2;
    public static final int HOST_NOT_FOUND = 3;
    public static final int TYPE_NOT_FOUND = 4;
    static /* synthetic */ Class class$org$xbill$DNS$Lookup;
    
    public static synchronized void refreshDefault() {
        try {
            Lookup.defaultResolver = new ExtendedResolver();
        }
        catch (UnknownHostException e) {
            throw new RuntimeException("Failed to initialize resolver");
        }
        Lookup.defaultSearchPath = ResolverConfig.getCurrentConfig().searchPath();
        Lookup.defaultCaches = new HashMap();
        Lookup.defaultNdots = ResolverConfig.getCurrentConfig().ndots();
    }
    
    public static synchronized Resolver getDefaultResolver() {
        return Lookup.defaultResolver;
    }
    
    public static synchronized void setDefaultResolver(final Resolver resolver) {
        Lookup.defaultResolver = resolver;
    }
    
    public static synchronized Cache getDefaultCache(final int dclass) {
        DClass.check(dclass);
        Cache c = Lookup.defaultCaches.get(Mnemonic.toInteger(dclass));
        if (c == null) {
            c = new Cache(dclass);
            Lookup.defaultCaches.put(Mnemonic.toInteger(dclass), c);
        }
        return c;
    }
    
    public static synchronized void setDefaultCache(final Cache cache, final int dclass) {
        DClass.check(dclass);
        Lookup.defaultCaches.put(Mnemonic.toInteger(dclass), cache);
    }
    
    public static synchronized Name[] getDefaultSearchPath() {
        return Lookup.defaultSearchPath;
    }
    
    public static synchronized void setDefaultSearchPath(final Name[] domains) {
        Lookup.defaultSearchPath = domains;
    }
    
    public static synchronized void setDefaultSearchPath(final String[] domains) throws TextParseException {
        if (domains == null) {
            Lookup.defaultSearchPath = null;
            return;
        }
        final Name[] newdomains = new Name[domains.length];
        for (int i = 0; i < domains.length; ++i) {
            newdomains[i] = Name.fromString(domains[i], Name.root);
        }
        Lookup.defaultSearchPath = newdomains;
    }
    
    private final void reset() {
        this.iterations = 0;
        this.foundAlias = false;
        this.done = false;
        this.doneCurrent = false;
        this.aliases = null;
        this.answers = null;
        this.result = -1;
        this.error = null;
        this.nxdomain = false;
        this.badresponse = false;
        this.badresponse_error = null;
        this.networkerror = false;
        this.timedout = false;
        this.nametoolong = false;
        this.referral = false;
        if (this.temporary_cache) {
            this.cache.clearCache();
        }
    }
    
    public Lookup(final Name name, final int type, final int dclass) {
        Type.check(type);
        DClass.check(dclass);
        if (!Type.isRR(type) && type != 255) {
            throw new IllegalArgumentException("Cannot query for meta-types other than ANY");
        }
        this.name = name;
        this.type = type;
        this.dclass = dclass;
        Class class$;
        Class class$org$xbill$DNS$Lookup;
        if (Lookup.class$org$xbill$DNS$Lookup == null) {
            class$org$xbill$DNS$Lookup = (Lookup.class$org$xbill$DNS$Lookup = (class$ = class$("org.xbill.DNS.Lookup")));
        }
        else {
            class$ = (class$org$xbill$DNS$Lookup = Lookup.class$org$xbill$DNS$Lookup);
        }
        final Class clazz = class$org$xbill$DNS$Lookup;
        synchronized (class$) {
            this.resolver = getDefaultResolver();
            this.searchPath = getDefaultSearchPath();
            this.cache = getDefaultCache(dclass);
        }
        this.credibility = 3;
        this.verbose = Options.check("verbose");
        this.result = -1;
    }
    
    public Lookup(final Name name, final int type) {
        this(name, type, 1);
    }
    
    public Lookup(final Name name) {
        this(name, 1, 1);
    }
    
    public Lookup(final String name, final int type, final int dclass) throws TextParseException {
        this(Name.fromString(name), type, dclass);
    }
    
    public Lookup(final String name, final int type) throws TextParseException {
        this(Name.fromString(name), type, 1);
    }
    
    public Lookup(final String name) throws TextParseException {
        this(Name.fromString(name), 1, 1);
    }
    
    public void setResolver(final Resolver resolver) {
        this.resolver = resolver;
    }
    
    public void setSearchPath(final Name[] domains) {
        this.searchPath = domains;
    }
    
    public void setSearchPath(final String[] domains) throws TextParseException {
        if (domains == null) {
            this.searchPath = null;
            return;
        }
        final Name[] newdomains = new Name[domains.length];
        for (int i = 0; i < domains.length; ++i) {
            newdomains[i] = Name.fromString(domains[i], Name.root);
        }
        this.searchPath = newdomains;
    }
    
    public void setCache(final Cache cache) {
        if (cache == null) {
            this.cache = new Cache(this.dclass);
            this.temporary_cache = true;
        }
        else {
            this.cache = cache;
            this.temporary_cache = false;
        }
    }
    
    public void setNdots(final int ndots) {
        if (ndots < 0) {
            throw new IllegalArgumentException("Illegal ndots value: " + ndots);
        }
        Lookup.defaultNdots = ndots;
    }
    
    public void setCredibility(final int credibility) {
        this.credibility = credibility;
    }
    
    private void follow(final Name name, final Name oldname) {
        this.foundAlias = true;
        this.badresponse = false;
        this.networkerror = false;
        this.timedout = false;
        this.nxdomain = false;
        this.referral = false;
        ++this.iterations;
        if (this.iterations >= 6 || name.equals(oldname)) {
            this.result = 1;
            this.error = "CNAME loop";
            this.done = true;
            return;
        }
        if (this.aliases == null) {
            this.aliases = new ArrayList();
        }
        this.aliases.add(oldname);
        this.lookup(name);
    }
    
    private void processResponse(final Name name, final SetResponse response) {
        if (response.isSuccessful()) {
            final RRset[] rrsets = response.answers();
            final List l = new ArrayList();
            for (int i = 0; i < rrsets.length; ++i) {
                final Iterator it = rrsets[i].rrs();
                while (it.hasNext()) {
                    l.add(it.next());
                }
            }
            this.result = 0;
            this.answers = l.toArray(new Record[l.size()]);
            this.done = true;
        }
        else if (response.isNXDOMAIN()) {
            this.nxdomain = true;
            this.doneCurrent = true;
            if (this.iterations > 0) {
                this.result = 3;
                this.done = true;
            }
        }
        else if (response.isNXRRSET()) {
            this.result = 4;
            this.answers = null;
            this.done = true;
        }
        else if (response.isCNAME()) {
            final CNAMERecord cname = response.getCNAME();
            this.follow(cname.getTarget(), name);
        }
        else if (response.isDNAME()) {
            final DNAMERecord dname = response.getDNAME();
            try {
                this.follow(name.fromDNAME(dname), name);
            }
            catch (NameTooLongException e) {
                this.result = 1;
                this.error = "Invalid DNAME target";
                this.done = true;
            }
        }
        else if (response.isDelegation()) {
            this.referral = true;
        }
    }
    
    private void lookup(final Name current) {
        SetResponse sr = this.cache.lookupRecords(current, this.type, this.credibility);
        if (this.verbose) {
            System.err.println("lookup " + current + " " + Type.string(this.type));
            System.err.println(sr);
        }
        this.processResponse(current, sr);
        if (this.done || this.doneCurrent) {
            return;
        }
        final Record question = Record.newRecord(current, this.type, this.dclass);
        final Message query = Message.newQuery(question);
        Message response = null;
        try {
            response = this.resolver.send(query);
        }
        catch (IOException e) {
            if (e instanceof InterruptedIOException) {
                this.timedout = true;
            }
            else {
                this.networkerror = true;
            }
            return;
        }
        final int rcode = response.getHeader().getRcode();
        if (rcode != 0 && rcode != 3) {
            this.badresponse = true;
            this.badresponse_error = Rcode.string(rcode);
            return;
        }
        if (!query.getQuestion().equals(response.getQuestion())) {
            this.badresponse = true;
            this.badresponse_error = "response does not match query";
            return;
        }
        sr = this.cache.addMessage(response);
        if (sr == null) {
            sr = this.cache.lookupRecords(current, this.type, this.credibility);
        }
        if (this.verbose) {
            System.err.println("queried " + current + " " + Type.string(this.type));
            System.err.println(sr);
        }
        this.processResponse(current, sr);
    }
    
    private void resolve(final Name current, final Name suffix) {
        this.doneCurrent = false;
        Name tname = null;
        if (suffix == null) {
            tname = current;
        }
        else {
            try {
                tname = Name.concatenate(current, suffix);
            }
            catch (NameTooLongException e) {
                this.nametoolong = true;
                return;
            }
        }
        this.lookup(tname);
    }
    
    public Record[] run() {
        if (this.done) {
            this.reset();
        }
        if (this.name.isAbsolute()) {
            this.resolve(this.name, null);
        }
        else if (this.searchPath == null) {
            this.resolve(this.name, Name.root);
        }
        else {
            if (this.name.labels() > Lookup.defaultNdots) {
                this.resolve(this.name, Name.root);
            }
            if (this.done) {
                return this.answers;
            }
            for (int i = 0; i < this.searchPath.length; ++i) {
                this.resolve(this.name, this.searchPath[i]);
                if (this.done) {
                    return this.answers;
                }
                if (this.foundAlias) {
                    break;
                }
            }
        }
        if (!this.done) {
            if (this.badresponse) {
                this.result = 2;
                this.error = this.badresponse_error;
                this.done = true;
            }
            else if (this.timedout) {
                this.result = 2;
                this.error = "timed out";
                this.done = true;
            }
            else if (this.networkerror) {
                this.result = 2;
                this.error = "network error";
                this.done = true;
            }
            else if (this.nxdomain) {
                this.result = 3;
                this.done = true;
            }
            else if (this.referral) {
                this.result = 1;
                this.error = "referral";
                this.done = true;
            }
            else if (this.nametoolong) {
                this.result = 1;
                this.error = "name too long";
                this.done = true;
            }
        }
        return this.answers;
    }
    
    private void checkDone() {
        if (this.done && this.result != -1) {
            return;
        }
        final StringBuffer sb = new StringBuffer("Lookup of " + this.name + " ");
        if (this.dclass != 1) {
            sb.append(DClass.string(this.dclass) + " ");
        }
        sb.append(Type.string(this.type) + " isn't done");
        throw new IllegalStateException(sb.toString());
    }
    
    public Record[] getAnswers() {
        this.checkDone();
        return this.answers;
    }
    
    public Name[] getAliases() {
        this.checkDone();
        if (this.aliases == null) {
            return Lookup.noAliases;
        }
        return this.aliases.toArray(new Name[this.aliases.size()]);
    }
    
    public int getResult() {
        this.checkDone();
        return this.result;
    }
    
    public String getErrorString() {
        this.checkDone();
        if (this.error != null) {
            return this.error;
        }
        switch (this.result) {
            case 0: {
                return "successful";
            }
            case 1: {
                return "unrecoverable error";
            }
            case 2: {
                return "try again";
            }
            case 3: {
                return "host not found";
            }
            case 4: {
                return "type not found";
            }
            default: {
                throw new IllegalStateException("unknown result");
            }
        }
    }
    
    static /* synthetic */ Class class$(final String x0) {
        try {
            return Class.forName(x0);
        }
        catch (ClassNotFoundException x) {
            throw new NoClassDefFoundError().initCause(x);
        }
    }
    
    static {
        noAliases = new Name[0];
        refreshDefault();
    }
}
