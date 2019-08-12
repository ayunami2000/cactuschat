// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class CompressionTest extends TestCase
{
    public void setUp() {
        Options.set("verbosecompression");
    }
    
    public void test() throws TextParseException {
        final Compression c = new Compression();
        final Name n = Name.fromString("www.amazon.com.");
        c.add(10, n);
        Assert.assertEquals(10, c.get(n));
        final Name n2 = Name.fromString("www.cnn.com.");
        c.add(10, n2);
        Assert.assertEquals(10, c.get(n2));
    }
}
