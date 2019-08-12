// 
// Decompiled by Procyon v0.5.36
// 

package com.radthorne.CactusChat;

import org.apache.commons.lang.StringUtils;
import org.spacehq.mc.protocol.data.message.Message;
import com.radthorne.CactusChat.msg.AnsiColour;
import org.apache.commons.io.FileUtils;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Lookup;
import com.radthorne.CactusChat.util.HostPortPair;
import java.security.CodeSource;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import java.io.IOException;
import org.apache.commons.cli.ParseException;
import com.radthorne.CactusChat.bot.Bot;
import java.io.FileOutputStream;
import java.io.File;
import com.radthorne.CactusChat.util.OS;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import java.io.PrintStream;
import java.io.OutputStream;
import com.radthorne.CactusChat.msg.ConsoleOutput;
import com.radthorne.CactusChat.console.Console;
import com.radthorne.CactusChat.bot.IngameBot;
import jline.console.ConsoleReader;

public class Main
{
    private static boolean reconnect;
    private static boolean debug;
    private static String username;
    private static boolean colour;
    private static String password;
    private static String host;
    private static ConsoleReader reader;
    private static IngameBot bot;
    private static Console console;
    private static int entityId;
    private static boolean inGame;
    private static boolean log;
    private static int port;
    private static int restartTime;
    
    public static void main(final String[] args) {
        try {
            Main.reader = new ConsoleReader();
            Runtime.getRuntime().addShutdownHook(new ShutDownThread());
            System.setOut(new ConsoleOutput(System.out));
            final CommandLineParser parser = new PosixParser();
            final Options options = new Options();
            options.addOption("u", "username", true, "Your minecraft username. (requires --password)");
            options.addOption("p", "password", true, "Your minecraft Password. (requires --username)");
            options.addOption("s", "server", true, "Minecraft server hostname. (<hostname<:port>>)");
            options.addOption("r", "reconnect", true, "Automatically reconnects on kick/quit after 5 seconds.");
            options.addOption("h", "help", false, "Shows this help");
            options.addOption("q", "quiet", false, "Lessens the amount of system messages you receive.");
            options.addOption("l", "log", false, "Turns chat logging off");
            options.addOption("c", "colour", false, "If it's used, it doesn't strip colour codes before logging to the chat log.");
            options.addOption("d", "debug", false, "turns debugging messages on");
            options.addOption("e", "error", false, "logs error messages");
            options.addOption("b", "bot", false, "Enables plugin loading");
            final CommandLine line = parser.parse(options, args);
            if (line.hasOption('e')) {
                System.setErr(new PrintStream(new FileOutputStream(OS.getAppDir() + File.separator + "error.log")));
            }
            if (line.hasOption('l')) {
                Main.log = true;
            }
            if (line.hasOption('c')) {
                Main.colour = true;
            }
            if (line.hasOption('d')) {
                Main.debug = true;
                System.out.println("DEBUGGING IS ON!");
            }
            if (line.hasOption('h')) {
                final CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
                final String jarFile = new File(codeSource.getLocation().getFile()).getName();
                System.out.println("usage: java -jar " + jarFile + " [OPTIONS]");
                System.out.println(" -c,--colour           If it's used, it doesn't strip colourcodes before logging to the chatlog.");
                System.out.println(" -l,--log              turns chatlogging off");
                System.out.println(" -h,--help             Shows this help");
                System.out.println(" -p,--password <pass>  Your minecraft Password. (requires --username)");
                System.out.println(" -r,--reconnect <time> Automatically reconnects on disconnect after <time> seconds");
                System.out.println(" -s,--server <host>    Minecraft server hostname. (<hostname[:port]>)");
                System.out.println(" -u,--username <user>  Your minecraft username. (requires --password)");
                System.out.println(" -d,--debug            Enables debugging messages");
                System.out.println(" -b,--bot              Enables plugin loading");
                System.exit(0);
            }
            if (line.hasOption('l')) {
                Main.log = false;
            }
            if (line.hasOption('r')) {
                Main.reconnect = true;
                Main.restartTime = Integer.parseInt(line.getOptionValue('r'));
                debug("Automatic reconnecting enabled.");
            }
            if (line.hasOption("u") && line.hasOption("p")) {
                Main.username = line.getOptionValue("u");
                Main.password = line.getOptionValue("p");
            }
            else {
                System.out.println("Username:");
                Main.username = Main.reader.readLine();
                System.out.println("Password:");
                Main.password = Main.reader.readLine('*');
            }
            if (line.hasOption('s')) {
                Main.host = line.getOptionValue('s');
            }
            else {
                System.out.println("Server:");
                Main.host = Main.reader.readLine();
            }
            final HostPortPair hpp = getHostPortPairFromSRV(Main.host);
            if (hpp == null) {
                if (Main.host.contains(":")) {
                    final String[] hostsplit = Main.host.split(":");
                    if (!hostsplit[0].equals("")) {
                        Main.host = hostsplit[0];
                    }
                    else {
                        Main.host = "localhost";
                    }
                    if (!hostsplit[1].equals("")) {
                        Main.port = Integer.parseInt(hostsplit[1]);
                    }
                    else {
                        Main.port = 25565;
                    }
                }
            }
            else {
                Main.host = hpp.getHost();
                Main.port = hpp.getPort();
            }
            Main.bot = new IngameBot();
            Main.console = new Console(Main.bot);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    Main.console.stop();
                }
            });
            Main.bot.start(Main.console, Main.username, Main.password, Main.host, Main.port);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }
    
    public static HostPortPair getHostPortPairFromSRV(final String name) {
        final String query = "_minecraft._tcp." + name;
        try {
            final Record[] records = new Lookup(query, 33).run();
            final SRVRecord srv = (SRVRecord)records[0];
            final String hostname = srv.getTarget().toString().replaceFirst("\\.$", "");
            final int port = srv.getPort();
            return new HostPortPair(hostname, port);
        }
        catch (TextParseException e) {
            e.printStackTrace();
        }
        catch (Exception ex) {}
        return null;
    }
    
    public static boolean isLog() {
        return Main.log;
    }
    
    public static void setLog(final boolean log) {
        Main.log = log;
    }
    
    public static boolean isReconnect() {
        return Main.reconnect;
    }
    
    public static void setReconnect(final boolean reconnect) {
        Main.reconnect = reconnect;
    }
    
    public static boolean isColour() {
        return Main.colour;
    }
    
    public static void setColour(final boolean colour) {
        Main.colour = colour;
    }
    
    public static String getUsername() {
        return Main.username;
    }
    
    public static void setUsername(final String username) {
        Main.username = username;
    }
    
    public static boolean isDebug() {
        return Main.debug;
    }
    
    public static void setDebug(final boolean debug) {
        Main.debug = debug;
    }
    
    public static String getPassword() {
        return Main.password;
    }
    
    public static void setPassword(final String password) {
        Main.password = password;
    }
    
    public static String getHost() {
        return Main.host;
    }
    
    public static void setHost(final String host) {
        Main.host = host;
    }
    
    public static ConsoleReader getReader() {
        return Main.reader;
    }
    
    public static int getPort() {
        return Main.port;
    }
    
    public static void setPort(final int port) {
        Main.port = port;
    }
    
    public static IngameBot getBot() {
        return Main.bot;
    }
    
    public static void setBot(final IngameBot bot) {
        Main.bot = bot;
    }
    
    public static void log(String message) throws IOException {
        debug("logging to file");
        final String time = new SimpleDateFormat("[HH:mm:ss] ").format(new Date());
        final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        final char newline = '\n';
        final String host = Main.host.contains(":") ? Main.host.split(":")[0] : Main.host;
        final String filename = "Chatlog-" + host + "-" + date + ".log";
        final String appdata = OS.getAppDir();
        final File userDir = new File(appdata + File.separator + Main.username + File.separator);
        final File dir = new File(userDir, "Chatlogs" + File.separator);
        final File logfile = new File(dir, filename);
        if (!userDir.getParentFile().exists()) {
            userDir.getParentFile().mkdir();
        }
        if (!userDir.exists()) {
            userDir.mkdir();
        }
        if (!dir.getParentFile().exists()) {
            dir.getParentFile().mkdir();
        }
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (!logfile.exists()) {
            logfile.createNewFile();
        }
        final String endOfLine = "\u001b[m";
        if (logfile.canWrite()) {
            if (message.endsWith(endOfLine)) {
                message = message.substring(0, message.length() - endOfLine.length());
            }
            FileUtils.writeStringToFile(logfile, message + newline, true);
        }
        else {
            debug("CAN'T WRITE TO FILE!!");
        }
    }
    
    public static void println(final String message) {
        println(message, false);
    }
    
    public static void println(String message, final boolean log) {
        if (getReader().getTerminal().isAnsiSupported()) {
            System.out.println(AnsiColour.colour(message));
        }
        else {
            System.out.println(message);
        }
        if (isLog()) {
            if (!isColour()) {
                message = Message.fromString(message).toJsonString();
            }
            try {
                log(message);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void debug(final String message) {
        if (isDebug()) {
            System.out.println(message);
        }
    }
    
    public static void reconnect() {
        if (!isReconnect()) {
            return;
        }
        try {
            int time = Main.restartTime;
            System.out.print("\rRestarting in " + time + " seconds.\n");
            String dots = "";
            String space = StringUtils.repeat(" ", time);
            while (time > 0) {
                dots = dots.concat("=");
                space = space.substring(0, space.length() - 1);
                System.out.print("\r[" + dots + space + "]\r");
                --time;
                Thread.sleep(1000L);
            }
            System.out.print('\n');
            Main.bot.start(Main.console, getUsername(), getPassword(), getHost(), getPort());
        }
        catch (Exception e) {
            e.printStackTrace();
            reconnect();
        }
    }
    
    public static boolean isInGame() {
        return Main.inGame;
    }
    
    public static int getEntityId() {
        return Main.entityId;
    }
    
    static {
        Main.log = true;
        Main.port = 25565;
        Main.restartTime = 30;
    }
    
    static class ShutDownThread extends Thread
    {
        @Override
        public void run() {
            try {
                Main.reader.getTerminal().restore();
                Main.bot.quit();
            }
            catch (Throwable t) {}
        }
    }
}
