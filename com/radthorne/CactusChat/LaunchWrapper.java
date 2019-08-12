// 
// Decompiled by Procyon v0.5.36
// 

package com.radthorne.CactusChat;

import javax.script.ScriptEngine;
import java.security.CodeSource;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.script.ScriptEngineManager;
import com.radthorne.CactusChat.util.OS;
import java.net.URLDecoder;

public class LaunchWrapper
{
    public static void main(final String[] args) {
        try {
            final CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
            final String path = codeSource.getLocation().getFile();
            final String decodedPath = URLDecoder.decode(path, "UTF-8");
            if (OS.isWindows() && System.console() == null) {
                new ProcessBuilder(new String[] { "cmd", "/c", "start", "java", "-cp", decodedPath, "com.radthorne.CactusChat.Main" }).start();
            }
            else if (OS.isMac() && System.console() == null) {
                final ScriptEngineManager mgr = new ScriptEngineManager();
                final ScriptEngine engine = mgr.getEngineByName("AppleScript");
                final String scriptLaunch = "tell application \"Terminal\" to activate";
                final String scriptLaunch2 = "tell application \"System Events\" to tell process \"Terminal\" to keystroke \"t\" using command down";
                final String script = "tell application \"Terminal\" to do script \"java -cp " + decodedPath + " com.radthorne.CactusChat.Main\" in window 1";
                engine.eval(scriptLaunch);
                engine.eval(scriptLaunch2);
                engine.eval(script);
            }
            else if (OS.isLinux() && System.console() == null) {
                new ProcessBuilder(new String[] { "xterm", "-e", "java", "-cp", decodedPath, "com.radthorne.CactusChat.Main" }).start();
            }
            else {
                Main.main(args);
            }
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        catch (ScriptException e3) {
            e3.printStackTrace();
        }
    }
}
