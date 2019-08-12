// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util;

import java.util.Objects;

public final class Assert
{
    private Assert() {
    }
    
    @Deprecated
    public static <T> T requireNonNull(final T object, final String message) {
        return Objects.requireNonNull(object, message);
    }
}
