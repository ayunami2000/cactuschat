// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.text.DecimalFormat;
import java.io.IOException;
import java.text.NumberFormat;

public class LOCRecord extends Record
{
    private static final long serialVersionUID = 9058224788126750409L;
    private static NumberFormat w2;
    private static NumberFormat w3;
    private long size;
    private long hPrecision;
    private long vPrecision;
    private long latitude;
    private long longitude;
    private long altitude;
    
    LOCRecord() {
    }
    
    Record getObject() {
        return new LOCRecord();
    }
    
    public LOCRecord(final Name name, final int dclass, final long ttl, final double latitude, final double longitude, final double altitude, final double size, final double hPrecision, final double vPrecision) {
        super(name, 29, dclass, ttl);
        this.latitude = (long)(latitude * 3600.0 * 1000.0 + 2.147483648E9);
        this.longitude = (long)(longitude * 3600.0 * 1000.0 + 2.147483648E9);
        this.altitude = (long)((altitude + 100000.0) * 100.0);
        this.size = (long)(size * 100.0);
        this.hPrecision = (long)(hPrecision * 100.0);
        this.vPrecision = (long)(vPrecision * 100.0);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        final int version = in.readU8();
        if (version != 0) {
            throw new WireParseException("Invalid LOC version");
        }
        this.size = parseLOCformat(in.readU8());
        this.hPrecision = parseLOCformat(in.readU8());
        this.vPrecision = parseLOCformat(in.readU8());
        this.latitude = in.readU32();
        this.longitude = in.readU32();
        this.altitude = in.readU32();
    }
    
    private double parseFixedPoint(final String s) {
        if (s.matches("^-?\\d+$")) {
            return Integer.parseInt(s);
        }
        if (s.matches("^-?\\d+\\.\\d*$")) {
            final String[] parts = s.split("\\.");
            final double value = Integer.parseInt(parts[0]);
            double fraction = Integer.parseInt(parts[1]);
            if (value < 0.0) {
                fraction *= -1.0;
            }
            final int digits = parts[1].length();
            return value + fraction / Math.pow(10.0, digits);
        }
        throw new NumberFormatException();
    }
    
    private long parsePosition(final Tokenizer st, final String type) throws IOException {
        final boolean isLatitude = type.equals("latitude");
        int deg = 0;
        int min = 0;
        double sec = 0.0;
        deg = st.getUInt16();
        if (deg > 180 || (deg > 90 && isLatitude)) {
            throw st.exception("Invalid LOC " + type + " degrees");
        }
        String s = st.getString();
        try {
            min = Integer.parseInt(s);
            if (min < 0 || min > 59) {
                throw st.exception("Invalid LOC " + type + " minutes");
            }
            s = st.getString();
            sec = this.parseFixedPoint(s);
            if (sec < 0.0 || sec >= 60.0) {
                throw st.exception("Invalid LOC " + type + " seconds");
            }
            s = st.getString();
        }
        catch (NumberFormatException ex) {}
        if (s.length() != 1) {
            throw st.exception("Invalid LOC " + type);
        }
        long value = (long)(1000.0 * (sec + 60L * (min + 60L * deg)));
        final char c = Character.toUpperCase(s.charAt(0));
        if ((isLatitude && c == 'S') || (!isLatitude && c == 'W')) {
            value = -value;
        }
        else if ((isLatitude && c != 'N') || (!isLatitude && c != 'E')) {
            throw st.exception("Invalid LOC " + type);
        }
        value += 2147483648L;
        return value;
    }
    
    private long parseDouble(final Tokenizer st, final String type, final boolean required, final long min, final long max, final long defaultValue) throws IOException {
        final Tokenizer.Token token = st.get();
        if (token.isEOL()) {
            if (required) {
                throw st.exception("Invalid LOC " + type);
            }
            st.unget();
            return defaultValue;
        }
        else {
            String s = token.value;
            if (s.length() > 1 && s.charAt(s.length() - 1) == 'm') {
                s = s.substring(0, s.length() - 1);
            }
            try {
                final long value = (long)(100.0 * this.parseFixedPoint(s));
                if (value < min || value > max) {
                    throw st.exception("Invalid LOC " + type);
                }
                return value;
            }
            catch (NumberFormatException e) {
                throw st.exception("Invalid LOC " + type);
            }
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.latitude = this.parsePosition(st, "latitude");
        this.longitude = this.parsePosition(st, "longitude");
        this.altitude = this.parseDouble(st, "altitude", true, -10000000L, 4284967295L, 0L) + 10000000L;
        this.size = this.parseDouble(st, "size", false, 0L, 9000000000L, 100L);
        this.hPrecision = this.parseDouble(st, "horizontal precision", false, 0L, 9000000000L, 1000000L);
        this.vPrecision = this.parseDouble(st, "vertical precision", false, 0L, 9000000000L, 1000L);
    }
    
    private void renderFixedPoint(final StringBuffer sb, final NumberFormat formatter, long value, final long divisor) {
        sb.append(value / divisor);
        value %= divisor;
        if (value != 0L) {
            sb.append(".");
            sb.append(formatter.format(value));
        }
    }
    
    private String positionToString(final long value, final char pos, final char neg) {
        final StringBuffer sb = new StringBuffer();
        long temp = value - 2147483648L;
        char direction;
        if (temp < 0L) {
            temp = -temp;
            direction = neg;
        }
        else {
            direction = pos;
        }
        sb.append(temp / 3600000L);
        temp %= 3600000L;
        sb.append(" ");
        sb.append(temp / 60000L);
        temp %= 60000L;
        sb.append(" ");
        this.renderFixedPoint(sb, LOCRecord.w3, temp, 1000L);
        sb.append(" ");
        sb.append(direction);
        return sb.toString();
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.positionToString(this.latitude, 'N', 'S'));
        sb.append(" ");
        sb.append(this.positionToString(this.longitude, 'E', 'W'));
        sb.append(" ");
        this.renderFixedPoint(sb, LOCRecord.w2, this.altitude - 10000000L, 100L);
        sb.append("m ");
        this.renderFixedPoint(sb, LOCRecord.w2, this.size, 100L);
        sb.append("m ");
        this.renderFixedPoint(sb, LOCRecord.w2, this.hPrecision, 100L);
        sb.append("m ");
        this.renderFixedPoint(sb, LOCRecord.w2, this.vPrecision, 100L);
        sb.append("m");
        return sb.toString();
    }
    
    public double getLatitude() {
        return (this.latitude - 2147483648L) / 3600000.0;
    }
    
    public double getLongitude() {
        return (this.longitude - 2147483648L) / 3600000.0;
    }
    
    public double getAltitude() {
        return (this.altitude - 10000000L) / 100.0;
    }
    
    public double getSize() {
        return this.size / 100.0;
    }
    
    public double getHPrecision() {
        return this.hPrecision / 100.0;
    }
    
    public double getVPrecision() {
        return this.vPrecision / 100.0;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU8(0);
        out.writeU8(this.toLOCformat(this.size));
        out.writeU8(this.toLOCformat(this.hPrecision));
        out.writeU8(this.toLOCformat(this.vPrecision));
        out.writeU32(this.latitude);
        out.writeU32(this.longitude);
        out.writeU32(this.altitude);
    }
    
    private static long parseLOCformat(final int b) throws WireParseException {
        long out = b >> 4;
        int exp = b & 0xF;
        if (out > 9L || exp > 9) {
            throw new WireParseException("Invalid LOC Encoding");
        }
        while (exp-- > 0) {
            out *= 10L;
        }
        return out;
    }
    
    private int toLOCformat(long l) {
        byte exp = 0;
        while (l > 9L) {
            ++exp;
            l /= 10L;
        }
        return (int)((l << 4) + exp);
    }
    
    static {
        (LOCRecord.w2 = new DecimalFormat()).setMinimumIntegerDigits(2);
        (LOCRecord.w3 = new DecimalFormat()).setMinimumIntegerDigits(3);
    }
}
