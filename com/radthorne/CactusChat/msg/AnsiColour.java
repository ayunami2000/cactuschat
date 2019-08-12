// 
// Decompiled by Procyon v0.5.36
// 

package com.radthorne.CactusChat.msg;

import java.util.EnumMap;
import org.fusesource.jansi.Ansi;
import java.util.Map;

public class AnsiColour
{
    private static final Map<ChatColour, String> replacements;
    private static final ChatColour[] colors;
    
    public static String strip(String message) {
        if (message == null) {
            return null;
        }
        for (final ChatColour color : AnsiColour.colors) {
            final String replacement = "";
            message = message.replaceAll("(?i)" + color.getCode(), replacement);
        }
        return message + Ansi.ansi().reset().toString();
    }
    
    public static String colour(String message) {
        if (message == null) {
            return null;
        }
        for (final ChatColour color : AnsiColour.colors) {
            final String replacement = AnsiColour.replacements.get(color);
            message = message.replaceAll("(?i)" + color.getCode(), (replacement != null) ? replacement : "");
        }
        return message;
    }
    
    public static String getColourCode(final String name) {
        return ChatColour.valueOf(name.toUpperCase()).getCode();
    }
    
    public static String reset() {
        return Ansi.ansi().reset().toString();
    }
    
    static {
        replacements = new EnumMap<ChatColour, String>(ChatColour.class);
        colors = ChatColour.values();
        AnsiColour.replacements.put(ChatColour.BLACK, Ansi.ansi().fg(Ansi.Color.BLACK).boldOff().toString());
        AnsiColour.replacements.put(ChatColour.DARK_BLUE, Ansi.ansi().fg(Ansi.Color.BLUE).boldOff().toString());
        AnsiColour.replacements.put(ChatColour.DARK_GREEN, Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString());
        AnsiColour.replacements.put(ChatColour.DARK_AQUA, Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString());
        AnsiColour.replacements.put(ChatColour.DARK_RED, Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString());
        AnsiColour.replacements.put(ChatColour.DARK_PURPLE, Ansi.ansi().fg(Ansi.Color.MAGENTA).boldOff().toString());
        AnsiColour.replacements.put(ChatColour.GOLD, Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString());
        AnsiColour.replacements.put(ChatColour.GRAY, Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString());
        AnsiColour.replacements.put(ChatColour.DARK_GRAY, Ansi.ansi().fg(Ansi.Color.BLACK).bold().toString());
        AnsiColour.replacements.put(ChatColour.BLUE, Ansi.ansi().fg(Ansi.Color.BLUE).bold().toString());
        AnsiColour.replacements.put(ChatColour.GREEN, Ansi.ansi().fg(Ansi.Color.GREEN).bold().toString());
        AnsiColour.replacements.put(ChatColour.AQUA, Ansi.ansi().fg(Ansi.Color.CYAN).bold().toString());
        AnsiColour.replacements.put(ChatColour.RED, Ansi.ansi().fg(Ansi.Color.RED).bold().toString());
        AnsiColour.replacements.put(ChatColour.LIGHT_PURPLE, Ansi.ansi().fg(Ansi.Color.MAGENTA).bold().toString());
        AnsiColour.replacements.put(ChatColour.YELLOW, Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString());
        AnsiColour.replacements.put(ChatColour.WHITE, Ansi.ansi().fg(Ansi.Color.WHITE).bold().toString());
        AnsiColour.replacements.put(ChatColour.OBFUSCATED, Ansi.ansi().a(Ansi.Attribute.BLINK_SLOW).toString());
        AnsiColour.replacements.put(ChatColour.BOLD, Ansi.ansi().a(Ansi.Attribute.INTENSITY_BOLD).toString());
        AnsiColour.replacements.put(ChatColour.STRIKETHROUGH, Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString());
        AnsiColour.replacements.put(ChatColour.UNDERLINED, Ansi.ansi().a(Ansi.Attribute.UNDERLINE).toString());
        AnsiColour.replacements.put(ChatColour.ITALIC, Ansi.ansi().a(Ansi.Attribute.ITALIC).toString());
        AnsiColour.replacements.put(ChatColour.RESET, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.DEFAULT).toString());
    }
}
