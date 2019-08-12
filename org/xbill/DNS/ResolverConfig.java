// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.File;
import java.io.BufferedInputStream;
import java.util.ResourceBundle;
import java.util.Locale;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;

public class ResolverConfig
{
    private String[] servers;
    private Name[] searchlist;
    private int ndots;
    private static ResolverConfig currentConfig;
    static /* synthetic */ Class class$org$xbill$DNS$ResolverConfig;
    
    public ResolverConfig() {
        this.servers = null;
        this.searchlist = null;
        this.ndots = -1;
        if (this.findProperty()) {
            return;
        }
        if (this.findSunJVM()) {
            return;
        }
        if (this.servers == null || this.searchlist == null) {
            final String OS = System.getProperty("os.name");
            final String vendor = System.getProperty("java.vendor");
            if (OS.indexOf("Windows") != -1) {
                if (OS.indexOf("95") != -1 || OS.indexOf("98") != -1 || OS.indexOf("ME") != -1) {
                    this.find95();
                }
                else {
                    this.findNT();
                }
            }
            else if (OS.indexOf("NetWare") != -1) {
                this.findNetware();
            }
            else if (vendor.indexOf("Android") != -1) {
                this.findAndroid();
            }
            else {
                this.findUnix();
            }
        }
    }
    
    private void addServer(final String server, final List list) {
        if (list.contains(server)) {
            return;
        }
        if (Options.check("verbose")) {
            System.out.println("adding server " + server);
        }
        list.add(server);
    }
    
    private void addSearch(final String search, final List list) {
        if (Options.check("verbose")) {
            System.out.println("adding search " + search);
        }
        Name name;
        try {
            name = Name.fromString(search, Name.root);
        }
        catch (TextParseException e) {
            return;
        }
        if (list.contains(name)) {
            return;
        }
        list.add(name);
    }
    
    private int parseNdots(String token) {
        token = token.substring(6);
        try {
            final int ndots = Integer.parseInt(token);
            if (ndots >= 0) {
                if (Options.check("verbose")) {
                    System.out.println("setting ndots " + token);
                }
                return ndots;
            }
        }
        catch (NumberFormatException ex) {}
        return -1;
    }
    
    private void configureFromLists(final List lserver, final List lsearch) {
        if (this.servers == null && lserver.size() > 0) {
            this.servers = lserver.toArray(new String[0]);
        }
        if (this.searchlist == null && lsearch.size() > 0) {
            this.searchlist = lsearch.toArray(new Name[0]);
        }
    }
    
    private void configureNdots(final int lndots) {
        if (this.ndots < 0 && lndots > 0) {
            this.ndots = lndots;
        }
    }
    
    private boolean findProperty() {
        final List lserver = new ArrayList(0);
        final List lsearch = new ArrayList(0);
        String prop = System.getProperty("dns.server");
        if (prop != null) {
            final StringTokenizer st = new StringTokenizer(prop, ",");
            while (st.hasMoreTokens()) {
                this.addServer(st.nextToken(), lserver);
            }
        }
        prop = System.getProperty("dns.search");
        if (prop != null) {
            final StringTokenizer st = new StringTokenizer(prop, ",");
            while (st.hasMoreTokens()) {
                this.addSearch(st.nextToken(), lsearch);
            }
        }
        this.configureFromLists(lserver, lsearch);
        return this.servers != null && this.searchlist != null;
    }
    
    private boolean findSunJVM() {
        final List lserver = new ArrayList(0);
        final List lsearch = new ArrayList(0);
        List lserver_tmp;
        List lsearch_tmp;
        try {
            final Class[] noClasses = new Class[0];
            final Object[] noObjects = new Object[0];
            final String resConfName = "sun.net.dns.ResolverConfiguration";
            final Class resConfClass = Class.forName(resConfName);
            final Method open = resConfClass.getDeclaredMethod("open", (Class[])noClasses);
            final Object resConf = open.invoke(null, noObjects);
            final Method nameservers = resConfClass.getMethod("nameservers", (Class[])noClasses);
            lserver_tmp = (List)nameservers.invoke(resConf, noObjects);
            final Method searchlist = resConfClass.getMethod("searchlist", (Class[])noClasses);
            lsearch_tmp = (List)searchlist.invoke(resConf, noObjects);
        }
        catch (Exception e) {
            return false;
        }
        if (lserver_tmp.size() == 0) {
            return false;
        }
        if (lserver_tmp.size() > 0) {
            final Iterator it = lserver_tmp.iterator();
            while (it.hasNext()) {
                this.addServer(it.next(), lserver);
            }
        }
        if (lsearch_tmp.size() > 0) {
            final Iterator it = lsearch_tmp.iterator();
            while (it.hasNext()) {
                this.addSearch(it.next(), lsearch);
            }
        }
        this.configureFromLists(lserver, lsearch);
        return true;
    }
    
    private void findResolvConf(final String file) {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
            return;
        }
        final InputStreamReader isr = new InputStreamReader(in);
        final BufferedReader br = new BufferedReader(isr);
        final List lserver = new ArrayList(0);
        final List lsearch = new ArrayList(0);
        int lndots = -1;
        try {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("nameserver")) {
                    final StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    this.addServer(st.nextToken(), lserver);
                }
                else if (line.startsWith("domain")) {
                    final StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    if (!st.hasMoreTokens()) {
                        continue;
                    }
                    if (!lsearch.isEmpty()) {
                        continue;
                    }
                    this.addSearch(st.nextToken(), lsearch);
                }
                else if (line.startsWith("search")) {
                    if (!lsearch.isEmpty()) {
                        lsearch.clear();
                    }
                    final StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    while (st.hasMoreTokens()) {
                        this.addSearch(st.nextToken(), lsearch);
                    }
                }
                else {
                    if (!line.startsWith("options")) {
                        continue;
                    }
                    final StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    while (st.hasMoreTokens()) {
                        final String token = st.nextToken();
                        if (token.startsWith("ndots:")) {
                            lndots = this.parseNdots(token);
                        }
                    }
                }
            }
            br.close();
        }
        catch (IOException ex) {}
        this.configureFromLists(lserver, lsearch);
        this.configureNdots(lndots);
    }
    
    private void findUnix() {
        this.findResolvConf("/etc/resolv.conf");
    }
    
    private void findNetware() {
        this.findResolvConf("sys:/etc/resolv.cfg");
    }
    
    private void findWin(final InputStream in, final Locale locale) {
        final String packageName = ResolverConfig.class.getPackage().getName();
        final String resPackageName = packageName + ".windows.DNSServer";
        ResourceBundle res;
        if (locale != null) {
            res = ResourceBundle.getBundle(resPackageName, locale);
        }
        else {
            res = ResourceBundle.getBundle(resPackageName);
        }
        final String host_name = res.getString("host_name");
        final String primary_dns_suffix = res.getString("primary_dns_suffix");
        final String dns_suffix = res.getString("dns_suffix");
        final String dns_servers = res.getString("dns_servers");
        final BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            final List lserver = new ArrayList();
            final List lsearch = new ArrayList();
            String line = null;
            boolean readingServers = false;
            boolean readingSearches = false;
            while ((line = br.readLine()) != null) {
                final StringTokenizer st = new StringTokenizer(line);
                if (!st.hasMoreTokens()) {
                    readingServers = false;
                    readingSearches = false;
                }
                else {
                    String s = st.nextToken();
                    if (line.indexOf(":") != -1) {
                        readingServers = false;
                        readingSearches = false;
                    }
                    if (line.indexOf(host_name) != -1) {
                        while (st.hasMoreTokens()) {
                            s = st.nextToken();
                        }
                        Name name;
                        try {
                            name = Name.fromString(s, null);
                        }
                        catch (TextParseException e) {
                            continue;
                        }
                        if (name.labels() == 1) {
                            continue;
                        }
                        this.addSearch(s, lsearch);
                    }
                    else if (line.indexOf(primary_dns_suffix) != -1) {
                        while (st.hasMoreTokens()) {
                            s = st.nextToken();
                        }
                        if (s.equals(":")) {
                            continue;
                        }
                        this.addSearch(s, lsearch);
                        readingSearches = true;
                    }
                    else if (readingSearches || line.indexOf(dns_suffix) != -1) {
                        while (st.hasMoreTokens()) {
                            s = st.nextToken();
                        }
                        if (s.equals(":")) {
                            continue;
                        }
                        this.addSearch(s, lsearch);
                        readingSearches = true;
                    }
                    else {
                        if (!readingServers && line.indexOf(dns_servers) == -1) {
                            continue;
                        }
                        while (st.hasMoreTokens()) {
                            s = st.nextToken();
                        }
                        if (s.equals(":")) {
                            continue;
                        }
                        this.addServer(s, lserver);
                        readingServers = true;
                    }
                }
            }
            this.configureFromLists(lserver, lsearch);
        }
        catch (IOException ex) {}
    }
    
    private void findWin(final InputStream in) {
        final String property = "org.xbill.DNS.windows.parse.buffer";
        final int defaultBufSize = 8192;
        final int bufSize = Integer.getInteger(property, 8192);
        final BufferedInputStream b = new BufferedInputStream(in, bufSize);
        b.mark(bufSize);
        this.findWin(b, null);
        if (this.servers == null) {
            try {
                b.reset();
            }
            catch (IOException e) {
                return;
            }
            this.findWin(b, new Locale("", ""));
        }
    }
    
    private void find95() {
        final String s = "winipcfg.out";
        try {
            final Process p = Runtime.getRuntime().exec("winipcfg /all /batch " + s);
            p.waitFor();
            final File f = new File(s);
            this.findWin(new FileInputStream(f));
            new File(s).delete();
        }
        catch (Exception e) {}
    }
    
    private void findNT() {
        try {
            final Process p = Runtime.getRuntime().exec("ipconfig /all");
            this.findWin(p.getInputStream());
            p.destroy();
        }
        catch (Exception e) {}
    }
    
    private void findAndroid() {
        final String re1 = "^\\d+(\\.\\d+){3}$";
        final String re2 = "^[0-9a-f]+(:[0-9a-f]*)+:[0-9a-f]+$";
        try {
            final ArrayList lserver = new ArrayList();
            final ArrayList lsearch = new ArrayList();
            final Process p = Runtime.getRuntime().exec("getprop");
            final InputStream in = p.getInputStream();
            final InputStreamReader isr = new InputStreamReader(in);
            final BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                final StringTokenizer t = new StringTokenizer(line, ":");
                final String name = t.nextToken();
                if (name.indexOf("net.dns") > -1) {
                    String v = t.nextToken();
                    v = v.replaceAll("[ \\[\\]]", "");
                    if ((!v.matches(re1) && !v.matches(re2)) || lserver.contains(v)) {
                        continue;
                    }
                    lserver.add(v);
                }
            }
            this.configureFromLists(lserver, lsearch);
        }
        catch (Exception ex) {}
    }
    
    public String[] servers() {
        return this.servers;
    }
    
    public String server() {
        if (this.servers == null) {
            return null;
        }
        return this.servers[0];
    }
    
    public Name[] searchPath() {
        return this.searchlist;
    }
    
    public int ndots() {
        if (this.ndots < 0) {
            return 1;
        }
        return this.ndots;
    }
    
    public static synchronized ResolverConfig getCurrentConfig() {
        return ResolverConfig.currentConfig;
    }
    
    public static void refresh() {
        final ResolverConfig newConfig = new ResolverConfig();
        Class class$;
        Class class$org$xbill$DNS$ResolverConfig;
        if (ResolverConfig.class$org$xbill$DNS$ResolverConfig == null) {
            class$org$xbill$DNS$ResolverConfig = (ResolverConfig.class$org$xbill$DNS$ResolverConfig = (class$ = class$("org.xbill.DNS.ResolverConfig")));
        }
        else {
            class$ = (class$org$xbill$DNS$ResolverConfig = ResolverConfig.class$org$xbill$DNS$ResolverConfig);
        }
        final Class clazz = class$org$xbill$DNS$ResolverConfig;
        synchronized (class$) {
            ResolverConfig.currentConfig = newConfig;
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
        refresh();
    }
}
