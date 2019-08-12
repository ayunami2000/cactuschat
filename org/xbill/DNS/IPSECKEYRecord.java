// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base64;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;

public class IPSECKEYRecord extends Record
{
    private static final long serialVersionUID = 3050449702765909687L;
    private int precedence;
    private int gatewayType;
    private int algorithmType;
    private Object gateway;
    private byte[] key;
    
    IPSECKEYRecord() {
    }
    
    Record getObject() {
        return new IPSECKEYRecord();
    }
    
    public IPSECKEYRecord(final Name name, final int dclass, final long ttl, final int precedence, final int gatewayType, final int algorithmType, final Object gateway, final byte[] key) {
        super(name, 45, dclass, ttl);
        this.precedence = Record.checkU8("precedence", precedence);
        this.gatewayType = Record.checkU8("gatewayType", gatewayType);
        this.algorithmType = Record.checkU8("algorithmType", algorithmType);
        switch (gatewayType) {
            case 0: {
                this.gateway = null;
                break;
            }
            case 1: {
                if (!(gateway instanceof InetAddress)) {
                    throw new IllegalArgumentException("\"gateway\" must be an IPv4 address");
                }
                this.gateway = gateway;
                break;
            }
            case 2: {
                if (!(gateway instanceof Inet6Address)) {
                    throw new IllegalArgumentException("\"gateway\" must be an IPv6 address");
                }
                this.gateway = gateway;
                break;
            }
            case 3: {
                if (!(gateway instanceof Name)) {
                    throw new IllegalArgumentException("\"gateway\" must be a DNS name");
                }
                this.gateway = Record.checkName("gateway", (Name)gateway);
                break;
            }
            default: {
                throw new IllegalArgumentException("\"gatewayType\" must be between 0 and 3");
            }
        }
        this.key = key;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.precedence = in.readU8();
        this.gatewayType = in.readU8();
        this.algorithmType = in.readU8();
        switch (this.gatewayType) {
            case 0: {
                this.gateway = null;
                break;
            }
            case 1: {
                this.gateway = InetAddress.getByAddress(in.readByteArray(4));
                break;
            }
            case 2: {
                this.gateway = InetAddress.getByAddress(in.readByteArray(16));
                break;
            }
            case 3: {
                this.gateway = new Name(in);
                break;
            }
            default: {
                throw new WireParseException("invalid gateway type");
            }
        }
        if (in.remaining() > 0) {
            this.key = in.readByteArray();
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.precedence = st.getUInt8();
        this.gatewayType = st.getUInt8();
        this.algorithmType = st.getUInt8();
        switch (this.gatewayType) {
            case 0: {
                final String s = st.getString();
                if (!s.equals(".")) {
                    throw new TextParseException("invalid gateway format");
                }
                this.gateway = null;
                break;
            }
            case 1: {
                this.gateway = st.getAddress(1);
                break;
            }
            case 2: {
                this.gateway = st.getAddress(2);
                break;
            }
            case 3: {
                this.gateway = st.getName(origin);
                break;
            }
            default: {
                throw new WireParseException("invalid gateway type");
            }
        }
        this.key = st.getBase64(false);
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.precedence);
        sb.append(" ");
        sb.append(this.gatewayType);
        sb.append(" ");
        sb.append(this.algorithmType);
        sb.append(" ");
        switch (this.gatewayType) {
            case 0: {
                sb.append(".");
                break;
            }
            case 1:
            case 2: {
                final InetAddress gatewayAddr = (InetAddress)this.gateway;
                sb.append(gatewayAddr.getHostAddress());
                break;
            }
            case 3: {
                sb.append(this.gateway);
                break;
            }
        }
        if (this.key != null) {
            sb.append(" ");
            sb.append(base64.toString(this.key));
        }
        return sb.toString();
    }
    
    public int getPrecedence() {
        return this.precedence;
    }
    
    public int getGatewayType() {
        return this.gatewayType;
    }
    
    public int getAlgorithmType() {
        return this.algorithmType;
    }
    
    public Object getGateway() {
        return this.gateway;
    }
    
    public byte[] getKey() {
        return this.key;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU8(this.precedence);
        out.writeU8(this.gatewayType);
        out.writeU8(this.algorithmType);
        switch (this.gatewayType) {
            case 1:
            case 2: {
                final InetAddress gatewayAddr = (InetAddress)this.gateway;
                out.writeByteArray(gatewayAddr.getAddress());
                break;
            }
            case 3: {
                final Name gatewayName = (Name)this.gateway;
                gatewayName.toWire(out, null, canonical);
                break;
            }
        }
        if (this.key != null) {
            out.writeByteArray(this.key);
        }
    }
    
    public static class Algorithm
    {
        public static final int DSA = 1;
        public static final int RSA = 2;
        
        private Algorithm() {
        }
    }
    
    public static class Gateway
    {
        public static final int None = 0;
        public static final int IPv4 = 1;
        public static final int IPv6 = 2;
        public static final int Name = 3;
        
        private Gateway() {
        }
    }
}
