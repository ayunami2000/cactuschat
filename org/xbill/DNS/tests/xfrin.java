// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.tests;

import java.util.Iterator;
import java.util.List;
import org.xbill.DNS.Record;
import org.xbill.DNS.ZoneTransferIn;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.TSIG;

public class xfrin
{
    private static void usage(final String s) {
        System.out.println("Error: " + s);
        System.out.println("usage: xfrin [-i serial] [-k keyname/secret] [-s server] [-p port] [-f] zone");
        System.exit(1);
    }
    
    public static void main(final String[] args) throws Exception {
        TSIG key = null;
        int ixfr_serial = -1;
        String server = null;
        int port = 53;
        boolean fallback = false;
        int arg;
        for (arg = 0; arg < args.length; ++arg) {
            if (args[arg].equals("-i")) {
                ixfr_serial = Integer.parseInt(args[++arg]);
                if (ixfr_serial < 0) {
                    usage("invalid serial number");
                }
            }
            else if (args[arg].equals("-k")) {
                final String s = args[++arg];
                final int index = s.indexOf(47);
                if (index < 0) {
                    usage("invalid key");
                }
                key = new TSIG(s.substring(0, index), s.substring(index + 1));
            }
            else if (args[arg].equals("-s")) {
                server = args[++arg];
            }
            else if (args[arg].equals("-p")) {
                port = Integer.parseInt(args[++arg]);
                if (port < 0 || port > 65535) {
                    usage("invalid port");
                }
            }
            else if (args[arg].equals("-f")) {
                fallback = true;
            }
            else {
                if (!args[arg].startsWith("-")) {
                    break;
                }
                usage("invalid option");
            }
        }
        if (arg >= args.length) {
            usage("no zone name specified");
        }
        final Name zname = Name.fromString(args[arg]);
        if (server == null) {
            final Lookup l = new Lookup(zname, 2);
            final Record[] ns = l.run();
            if (ns == null) {
                System.out.println("failed to look up NS record: " + l.getErrorString());
                System.exit(1);
            }
            server = ns[0].rdataToString();
            System.out.println("sending to server '" + server + "'");
        }
        ZoneTransferIn xfrin;
        if (ixfr_serial >= 0) {
            xfrin = ZoneTransferIn.newIXFR(zname, ixfr_serial, fallback, server, port, key);
        }
        else {
            xfrin = ZoneTransferIn.newAXFR(zname, server, port, key);
        }
        final List response = xfrin.run();
        if (xfrin.isAXFR()) {
            if (ixfr_serial >= 0) {
                System.out.println("AXFR-like IXFR response");
            }
            else {
                System.out.println("AXFR response");
            }
            final Iterator it = response.iterator();
            while (it.hasNext()) {
                System.out.println(it.next());
            }
        }
        else if (xfrin.isIXFR()) {
            System.out.println("IXFR response");
            for (final ZoneTransferIn.Delta delta : response) {
                System.out.println("delta from " + delta.start + " to " + delta.end);
                System.out.println("deletes");
                Iterator it2 = delta.deletes.iterator();
                while (it2.hasNext()) {
                    System.out.println(it2.next());
                }
                System.out.println("adds");
                it2 = delta.adds.iterator();
                while (it2.hasNext()) {
                    System.out.println(it2.next());
                }
            }
        }
        else if (xfrin.isCurrent()) {
            System.out.println("up to date");
        }
    }
}
