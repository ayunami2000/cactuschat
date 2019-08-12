// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Date;
import junit.framework.Assert;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import junit.framework.TestCase;

public class FormattedTimeTest extends TestCase
{
    public void test_format() {
        final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2005, 2, 19, 4, 4, 5);
        final String out = FormattedTime.format(cal.getTime());
        Assert.assertEquals("20050319040405", out);
    }
    
    public void test_parse() throws TextParseException {
        final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2005, 2, 19, 4, 4, 5);
        cal.set(14, 0);
        final Date out = FormattedTime.parse("20050319040405");
        final GregorianCalendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal2.setTimeInMillis(out.getTime());
        cal2.set(14, 0);
        Assert.assertEquals(cal, cal2);
    }
    
    public void test_parse_invalid() {
        try {
            FormattedTime.parse("2004010101010");
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        try {
            FormattedTime.parse("200401010101010");
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
        try {
            FormattedTime.parse("2004010101010A");
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex3) {}
    }
}
