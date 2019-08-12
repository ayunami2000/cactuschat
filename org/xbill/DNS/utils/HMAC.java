// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.utils;

import java.util.Arrays;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

public class HMAC
{
    MessageDigest digest;
    private byte[] ipad;
    private byte[] opad;
    private static final byte IPAD = 54;
    private static final byte OPAD = 92;
    private static final byte PADLEN = 64;
    
    private void init(byte[] key) {
        if (key.length > 64) {
            key = this.digest.digest(key);
            this.digest.reset();
        }
        this.ipad = new byte[64];
        this.opad = new byte[64];
        int i;
        for (i = 0; i < key.length; ++i) {
            this.ipad[i] = (byte)(key[i] ^ 0x36);
            this.opad[i] = (byte)(key[i] ^ 0x5C);
        }
        while (i < 64) {
            this.ipad[i] = 54;
            this.opad[i] = 92;
            ++i;
        }
        this.digest.update(this.ipad);
    }
    
    public HMAC(final MessageDigest digest, final byte[] key) {
        digest.reset();
        this.digest = digest;
        this.init(key);
    }
    
    public HMAC(final String digestName, final byte[] key) {
        try {
            this.digest = MessageDigest.getInstance(digestName);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("unknown digest algorithm " + digestName);
        }
        this.init(key);
    }
    
    public void update(final byte[] b, final int offset, final int length) {
        this.digest.update(b, offset, length);
    }
    
    public void update(final byte[] b) {
        this.digest.update(b);
    }
    
    public byte[] sign() {
        final byte[] output = this.digest.digest();
        this.digest.reset();
        this.digest.update(this.opad);
        return this.digest.digest(output);
    }
    
    public boolean verify(final byte[] signature) {
        return Arrays.equals(signature, this.sign());
    }
    
    public void clear() {
        this.digest.reset();
        this.digest.update(this.ipad);
    }
}
