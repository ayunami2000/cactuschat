// 
// Decompiled by Procyon v0.5.36
// 

package jline.internal;

import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;

public class Urls
{
    public static URL create(final String input) {
        if (input == null) {
            return null;
        }
        try {
            return new URL(input);
        }
        catch (MalformedURLException e) {
            return create(new File(input));
        }
    }
    
    public static URL create(final File file) {
        try {
            return (file != null) ? file.toURI().toURL() : null;
        }
        catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}
