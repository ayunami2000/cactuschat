// 
// Decompiled by Procyon v0.5.36
// 

package jline.console.completer;

import jline.internal.Preconditions;

public class EnumCompleter extends StringsCompleter
{
    public EnumCompleter(final Class<? extends Enum> source) {
        Preconditions.checkNotNull(source);
        for (final Enum<?> n : (Enum[])source.getEnumConstants()) {
            this.getStrings().add(n.name().toLowerCase());
        }
    }
}
