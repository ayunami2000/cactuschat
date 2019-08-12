// 
// Decompiled by Procyon v0.5.36
// 

package com.radthorne.CactusChat.console;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import com.radthorne.CactusChat.Main;
import java.util.logging.Handler;
import java.util.logging.Logger;
import com.radthorne.CactusChat.bot.Bot;
import jline.console.ConsoleReader;

public class Console
{
    private ConsoleReader reader;
    private ConsoleCommandThread thread;
    private BotConsoleHandler consoleHandler;
    private Bot bot;
    private boolean running;
    
    public Console(final Bot bot) {
        this.running = true;
        this.bot = bot;
        this.consoleHandler = new BotConsoleHandler();
        final Logger logger = Logger.getLogger("");
        for (final Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        logger.addHandler(this.consoleHandler);
        this.reader = Main.getReader();
    }
    
    public void stop() {
        this.consoleHandler.flush();
        this.running = false;
    }
    
    public void setup() {
        (this.thread = new ConsoleCommandThread()).setDaemon(true);
        this.thread.start();
    }
    
    private class ConsoleCommandThread extends Thread
    {
        @Override
        public void run() {
            while (Console.this.running) {
                try {
                    final String command = Console.this.reader.readLine("", null);
                    if (command == null || command.trim().length() == 0) {
                        continue;
                    }
                    Console.this.bot.chat(command);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private class BotConsoleHandler extends ConsoleHandler
    {
        @Override
        public synchronized void flush() {
            try {
                Console.this.reader.print("\r");
                Console.this.reader.flush();
                super.flush();
                try {
                    Console.this.reader.drawLine();
                }
                catch (Throwable ex2) {
                    Console.this.reader.getCursorBuffer().clear();
                }
                Console.this.reader.flush();
            }
            catch (IOException ex) {
                System.err.println("Exception flushing console output");
                ex.printStackTrace();
            }
        }
    }
}
