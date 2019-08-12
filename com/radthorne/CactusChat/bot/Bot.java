// 
// Decompiled by Procyon v0.5.36
// 

package com.radthorne.CactusChat.bot;

import com.radthorne.CactusChat.console.Console;

public abstract class Bot
{
    public abstract void start(final Console p0, final String p1, final String p2, final String p3, final int p4);
    
    public abstract void chat(final String p0);
    
    public abstract void error(final String p0);
    
    public abstract void console(final String p0);
    
    public abstract void raw(final String p0);
    
    public abstract String getUsername();
    
    public abstract void quit();
}
