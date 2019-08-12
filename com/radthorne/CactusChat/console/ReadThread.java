// 
// Decompiled by Procyon v0.5.36
// 

package com.radthorne.CactusChat.console;

import java.io.IOException;
import com.radthorne.CactusChat.Main;
import org.apache.commons.lang.WordUtils;

public class ReadThread extends Thread
{
    public static String[] breakString(String str, final int size) {
        str = WordUtils.wrap(str, size);
        return str.split("\n");
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                while (true) {
                    final String s = Main.getReader().readLine("");
                    if (!s.equals("")) {
                        final String[] breakString;
                        final String[] arr = breakString = breakString(s, 100);
                        for (final String message : breakString) {
                            Main.getBot().chat(message);
                            this.wait(1500L);
                        }
                    }
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
                continue;
            }
            catch (Exception e) {
                Main.debug(e.getMessage());
                continue;
            }
            break;
        }
    }
}
