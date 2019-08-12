// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.net.mom.jms;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.apache.logging.log4j.core.net.server.JmsServer;

public abstract class JmsQueueReceiver
{
    public static void main(final String[] args) throws Exception {
        if (args.length != 4) {
            usage("Wrong number of arguments.");
        }
        final String qcfBindingName = args[0];
        final String queueBindingName = args[1];
        final String username = args[2];
        final String password = args[3];
        final JmsServer server = new JmsServer(qcfBindingName, queueBindingName, username, password);
        server.start();
        final Charset enc = Charset.defaultCharset();
        final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, enc));
        System.out.println("Type \"exit\" to quit JmsQueueReceiver.");
        String line;
        do {
            line = stdin.readLine();
        } while (line != null && !line.equalsIgnoreCase("exit"));
        System.out.println("Exiting. Kill the application if it does not exit due to daemon threads.");
        server.stop();
    }
    
    private static void usage(final String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + JmsQueueReceiver.class.getName() + " QueueConnectionFactoryBindingName QueueBindingName username password");
        System.exit(1);
    }
}
