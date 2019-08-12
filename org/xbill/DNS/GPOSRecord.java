// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

public class GPOSRecord extends Record
{
    private static final long serialVersionUID = -6349714958085750705L;
    private byte[] latitude;
    private byte[] longitude;
    private byte[] altitude;
    
    GPOSRecord() {
    }
    
    Record getObject() {
        return new GPOSRecord();
    }
    
    private void validate(final double longitude, final double latitude) throws IllegalArgumentException {
        if (longitude < -90.0 || longitude > 90.0) {
            throw new IllegalArgumentException("illegal longitude " + longitude);
        }
        if (latitude < -180.0 || latitude > 180.0) {
            throw new IllegalArgumentException("illegal latitude " + latitude);
        }
    }
    
    public GPOSRecord(final Name name, final int dclass, final long ttl, final double longitude, final double latitude, final double altitude) {
        super(name, 27, dclass, ttl);
        this.validate(longitude, latitude);
        this.longitude = Double.toString(longitude).getBytes();
        this.latitude = Double.toString(latitude).getBytes();
        this.altitude = Double.toString(altitude).getBytes();
    }
    
    public GPOSRecord(final Name name, final int dclass, final long ttl, final String longitude, final String latitude, final String altitude) {
        super(name, 27, dclass, ttl);
        try {
            this.longitude = Record.byteArrayFromString(longitude);
            this.latitude = Record.byteArrayFromString(latitude);
            this.validate(this.getLongitude(), this.getLatitude());
            this.altitude = Record.byteArrayFromString(altitude);
        }
        catch (TextParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.longitude = in.readCountedString();
        this.latitude = in.readCountedString();
        this.altitude = in.readCountedString();
        try {
            this.validate(this.getLongitude(), this.getLatitude());
        }
        catch (IllegalArgumentException e) {
            throw new WireParseException(e.getMessage());
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        try {
            this.longitude = Record.byteArrayFromString(st.getString());
            this.latitude = Record.byteArrayFromString(st.getString());
            this.altitude = Record.byteArrayFromString(st.getString());
        }
        catch (TextParseException e) {
            throw st.exception(e.getMessage());
        }
        try {
            this.validate(this.getLongitude(), this.getLatitude());
        }
        catch (IllegalArgumentException e2) {
            throw new WireParseException(e2.getMessage());
        }
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(Record.byteArrayToString(this.longitude, true));
        sb.append(" ");
        sb.append(Record.byteArrayToString(this.latitude, true));
        sb.append(" ");
        sb.append(Record.byteArrayToString(this.altitude, true));
        return sb.toString();
    }
    
    public String getLongitudeString() {
        return Record.byteArrayToString(this.longitude, false);
    }
    
    public double getLongitude() {
        return Double.parseDouble(this.getLongitudeString());
    }
    
    public String getLatitudeString() {
        return Record.byteArrayToString(this.latitude, false);
    }
    
    public double getLatitude() {
        return Double.parseDouble(this.getLatitudeString());
    }
    
    public String getAltitudeString() {
        return Record.byteArrayToString(this.altitude, false);
    }
    
    public double getAltitude() {
        return Double.parseDouble(this.getAltitudeString());
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeCountedString(this.longitude);
        out.writeCountedString(this.latitude);
        out.writeCountedString(this.altitude);
    }
}
