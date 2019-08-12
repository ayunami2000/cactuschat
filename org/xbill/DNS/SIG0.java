// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Date;
import java.security.PrivateKey;

public class SIG0
{
    private static final short VALIDITY = 300;
    
    private SIG0() {
    }
    
    public static void signMessage(final Message message, final KEYRecord key, final PrivateKey privkey, final SIGRecord previous) throws DNSSEC.DNSSECException {
        int validity = Options.intValue("sig0validity");
        if (validity < 0) {
            validity = 300;
        }
        final long now = System.currentTimeMillis();
        final Date timeSigned = new Date(now);
        final Date timeExpires = new Date(now + validity * 1000);
        final SIGRecord sig = DNSSEC.signMessage(message, previous, key, privkey, timeSigned, timeExpires);
        message.addRecord(sig, 3);
    }
    
    public static void verifyMessage(final Message message, final byte[] b, final KEYRecord key, final SIGRecord previous) throws DNSSEC.DNSSECException {
        SIGRecord sig = null;
        final Record[] additional = message.getSectionArray(3);
        for (int i = 0; i < additional.length; ++i) {
            if (additional[i].getType() == 24) {
                if (((SIGRecord)additional[i]).getTypeCovered() == 0) {
                    sig = (SIGRecord)additional[i];
                    break;
                }
            }
        }
        DNSSEC.verifyMessage(message, b, sig, previous, key);
    }
}
