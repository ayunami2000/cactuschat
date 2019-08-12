// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.List;
import java.util.ArrayList;

public class SetResponse
{
    static final int UNKNOWN = 0;
    static final int NXDOMAIN = 1;
    static final int NXRRSET = 2;
    static final int DELEGATION = 3;
    static final int CNAME = 4;
    static final int DNAME = 5;
    static final int SUCCESSFUL = 6;
    private static final SetResponse unknown;
    private static final SetResponse nxdomain;
    private static final SetResponse nxrrset;
    private int type;
    private Object data;
    
    private SetResponse() {
    }
    
    SetResponse(final int type, final RRset rrset) {
        if (type < 0 || type > 6) {
            throw new IllegalArgumentException("invalid type");
        }
        this.type = type;
        this.data = rrset;
    }
    
    SetResponse(final int type) {
        if (type < 0 || type > 6) {
            throw new IllegalArgumentException("invalid type");
        }
        this.type = type;
        this.data = null;
    }
    
    static SetResponse ofType(final int type) {
        switch (type) {
            case 0: {
                return SetResponse.unknown;
            }
            case 1: {
                return SetResponse.nxdomain;
            }
            case 2: {
                return SetResponse.nxrrset;
            }
            case 3:
            case 4:
            case 5:
            case 6: {
                final SetResponse sr = new SetResponse();
                sr.type = type;
                sr.data = null;
                return sr;
            }
            default: {
                throw new IllegalArgumentException("invalid type");
            }
        }
    }
    
    void addRRset(final RRset rrset) {
        if (this.data == null) {
            this.data = new ArrayList();
        }
        final List l = (List)this.data;
        l.add(rrset);
    }
    
    public boolean isUnknown() {
        return this.type == 0;
    }
    
    public boolean isNXDOMAIN() {
        return this.type == 1;
    }
    
    public boolean isNXRRSET() {
        return this.type == 2;
    }
    
    public boolean isDelegation() {
        return this.type == 3;
    }
    
    public boolean isCNAME() {
        return this.type == 4;
    }
    
    public boolean isDNAME() {
        return this.type == 5;
    }
    
    public boolean isSuccessful() {
        return this.type == 6;
    }
    
    public RRset[] answers() {
        if (this.type != 6) {
            return null;
        }
        final List l = (List)this.data;
        return l.toArray(new RRset[l.size()]);
    }
    
    public CNAMERecord getCNAME() {
        return (CNAMERecord)((RRset)this.data).first();
    }
    
    public DNAMERecord getDNAME() {
        return (DNAMERecord)((RRset)this.data).first();
    }
    
    public RRset getNS() {
        return (RRset)this.data;
    }
    
    public String toString() {
        switch (this.type) {
            case 0: {
                return "unknown";
            }
            case 1: {
                return "NXDOMAIN";
            }
            case 2: {
                return "NXRRSET";
            }
            case 3: {
                return "delegation: " + this.data;
            }
            case 4: {
                return "CNAME: " + this.data;
            }
            case 5: {
                return "DNAME: " + this.data;
            }
            case 6: {
                return "successful";
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }
    
    static {
        unknown = new SetResponse(0);
        nxdomain = new SetResponse(1);
        nxrrset = new SetResponse(2);
    }
}
