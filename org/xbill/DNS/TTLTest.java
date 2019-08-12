// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TTLTest extends TestCase
{
    private final long S = 1L;
    private final long M = 60L;
    private final long H = 3600L;
    private final long D = 86400L;
    private final long W = 604800L;
    
    public void test_parseTTL() {
        Assert.assertEquals(9876L, TTL.parseTTL("9876"));
        Assert.assertEquals(0L, TTL.parseTTL("0S"));
        Assert.assertEquals(0L, TTL.parseTTL("0M"));
        Assert.assertEquals(0L, TTL.parseTTL("0H"));
        Assert.assertEquals(0L, TTL.parseTTL("0D"));
        Assert.assertEquals(0L, TTL.parseTTL("0W"));
        Assert.assertEquals(1L, TTL.parseTTL("1s"));
        Assert.assertEquals(60L, TTL.parseTTL("1m"));
        Assert.assertEquals(3600L, TTL.parseTTL("1h"));
        Assert.assertEquals(86400L, TTL.parseTTL("1d"));
        Assert.assertEquals(604800L, TTL.parseTTL("1w"));
        Assert.assertEquals(98L, TTL.parseTTL("98S"));
        Assert.assertEquals(4560L, TTL.parseTTL("76M"));
        Assert.assertEquals(194400L, TTL.parseTTL("54H"));
        Assert.assertEquals(2764800L, TTL.parseTTL("32D"));
        Assert.assertEquals(6048000L, TTL.parseTTL("10W"));
        Assert.assertEquals(5220758L, TTL.parseTTL("98S11M1234H2D01W"));
    }
    
    public void test_parseTTL_invalid() {
        try {
            TTL.parseTTL(null);
            Assert.fail("NumberFormatException not throw");
        }
        catch (NumberFormatException ex) {}
        try {
            TTL.parseTTL("");
            Assert.fail("NumberFormatException not throw");
        }
        catch (NumberFormatException ex2) {}
        try {
            TTL.parseTTL("S");
            Assert.fail("NumberFormatException not throw");
        }
        catch (NumberFormatException ex3) {}
        try {
            TTL.parseTTL("10S4B");
            Assert.fail("NumberFormatException not throw");
        }
        catch (NumberFormatException ex4) {}
        try {
            TTL.parseTTL("1S4294967295S");
            Assert.fail("NumberFormatException not throw");
        }
        catch (NumberFormatException ex5) {}
        try {
            TTL.parseTTL("4294967296");
            Assert.fail("NumberFormatException not throw");
        }
        catch (NumberFormatException ex6) {}
    }
    
    public void test_format() {
        Assert.assertEquals("0S", TTL.format(0L));
        Assert.assertEquals("1S", TTL.format(1L));
        Assert.assertEquals("59S", TTL.format(59L));
        Assert.assertEquals("1M", TTL.format(60L));
        Assert.assertEquals("59M", TTL.format(3540L));
        Assert.assertEquals("1M33S", TTL.format(93L));
        Assert.assertEquals("59M59S", TTL.format(3599L));
        Assert.assertEquals("1H", TTL.format(3600L));
        Assert.assertEquals("10H1M21S", TTL.format(36081L));
        Assert.assertEquals("23H59M59S", TTL.format(86399L));
        Assert.assertEquals("1D", TTL.format(86400L));
        Assert.assertEquals("4D18H45M30S", TTL.format(413130L));
        Assert.assertEquals("6D23H59M59S", TTL.format(604799L));
        Assert.assertEquals("1W", TTL.format(604800L));
        Assert.assertEquals("10W4D1H21M29S", TTL.format(6398489L));
        Assert.assertEquals("3550W5D3H14M7S", TTL.format(2147483647L));
    }
    
    public void test_format_invalid() {
        try {
            TTL.format(-1L);
            Assert.fail("InvalidTTLException not thrown");
        }
        catch (InvalidTTLException ex) {}
        try {
            TTL.format(4294967296L);
            Assert.fail("InvalidTTLException not thrown");
        }
        catch (InvalidTTLException ex2) {}
    }
}
