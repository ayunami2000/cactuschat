// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Date;
import java.text.NumberFormat;

final class FormattedTime
{
    private static NumberFormat w2;
    private static NumberFormat w4;
    
    private FormattedTime() {
    }
    
    public static String format(final Date date) {
        final Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        final StringBuffer sb = new StringBuffer();
        c.setTime(date);
        sb.append(FormattedTime.w4.format(c.get(1)));
        sb.append(FormattedTime.w2.format(c.get(2) + 1));
        sb.append(FormattedTime.w2.format(c.get(5)));
        sb.append(FormattedTime.w2.format(c.get(11)));
        sb.append(FormattedTime.w2.format(c.get(12)));
        sb.append(FormattedTime.w2.format(c.get(13)));
        return sb.toString();
    }
    
    public static Date parse(final String s) throws TextParseException {
        if (s.length() != 14) {
            throw new TextParseException("Invalid time encoding: " + s);
        }
        final Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        c.clear();
        try {
            final int year = Integer.parseInt(s.substring(0, 4));
            final int month = Integer.parseInt(s.substring(4, 6)) - 1;
            final int date = Integer.parseInt(s.substring(6, 8));
            final int hour = Integer.parseInt(s.substring(8, 10));
            final int minute = Integer.parseInt(s.substring(10, 12));
            final int second = Integer.parseInt(s.substring(12, 14));
            c.set(year, month, date, hour, minute, second);
        }
        catch (NumberFormatException e) {
            throw new TextParseException("Invalid time encoding: " + s);
        }
        return c.getTime();
    }
    
    static {
        (FormattedTime.w2 = new DecimalFormat()).setMinimumIntegerDigits(2);
        (FormattedTime.w4 = new DecimalFormat()).setMinimumIntegerDigits(4);
        FormattedTime.w4.setGroupingUsed(false);
    }
}
