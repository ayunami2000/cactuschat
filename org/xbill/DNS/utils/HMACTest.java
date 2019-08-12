// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.utils;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import junit.framework.Assert;
import junit.framework.TestCase;

public class HMACTest extends TestCase
{
    private static test_data[] tests;
    
    public HMACTest(final String name) {
        super(name);
    }
    
    private void do_test(final int i, final HMAC h) throws CloneNotSupportedException {
        h.update(HMACTest.tests[i].data, 0, HMACTest.tests[i].data.length);
        final byte[] out = h.sign();
        Assert.assertEquals("test=" + i, HMACTest.tests[i].digest.length, out.length);
        for (int j = 0; j < out.length; ++j) {
            Assert.assertEquals("test=" + i, HMACTest.tests[i].digest[j], out[j]);
        }
        h.clear();
        h.update(HMACTest.tests[i].data);
        Assert.assertTrue(h.verify(HMACTest.tests[i].digest));
        h.clear();
        h.update(HMACTest.tests[i].data, 0, HMACTest.tests[i].data.length);
        final byte[] tmp = HMACTest.tests[i].digest.clone();
        tmp[tmp.length / 2] = -85;
        Assert.assertFalse(h.verify(tmp));
    }
    
    public void test_ctor_digest_key() throws NoSuchAlgorithmException, CloneNotSupportedException {
        for (int i = 0; i < HMACTest.tests.length; ++i) {
            final MessageDigest md = MessageDigest.getInstance("md5");
            final HMAC h = new HMAC(md, HMACTest.tests[i].key);
            this.do_test(i, h);
        }
    }
    
    public void test_ctor_digestName_key() throws NoSuchAlgorithmException, CloneNotSupportedException {
        for (int i = 0; i < HMACTest.tests.length; ++i) {
            final HMAC h = new HMAC("md5", HMACTest.tests[i].key);
            this.do_test(i, h);
        }
    }
    
    public void test_ctor_digestName_key_invalid() {
        try {
            new HMAC("no name", new byte[0]);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    static {
        HMACTest.tests = new test_data[7];
        for (int i = 0; i < HMACTest.tests.length; ++i) {
            HMACTest.tests[i] = new test_data();
        }
        HMACTest.tests[0].key = base16.fromString("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");
        HMACTest.tests[0].data = "Hi There".getBytes();
        HMACTest.tests[0].digest = base16.fromString("9294727a3638bb1c13f48ef8158bfc9d");
        HMACTest.tests[1].key = "Jefe".getBytes();
        HMACTest.tests[1].data = "what do ya want for nothing?".getBytes();
        HMACTest.tests[1].digest = base16.fromString("750c783e6ab0b503eaa86e310a5db738");
        HMACTest.tests[2].key = base16.fromString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        HMACTest.tests[2].data = new byte[50];
        for (int i = 0; i < HMACTest.tests[2].data.length; ++i) {
            HMACTest.tests[2].data[i] = -35;
        }
        HMACTest.tests[2].digest = base16.fromString("56be34521d144c88dbb8c733f0e8b3f6");
        HMACTest.tests[3].key = base16.fromString("0102030405060708090a0b0c0d0e0f10111213141516171819");
        HMACTest.tests[3].data = new byte[50];
        for (int i = 0; i < HMACTest.tests[3].data.length; ++i) {
            HMACTest.tests[3].data[i] = -51;
        }
        HMACTest.tests[3].digest = base16.fromString("697eaf0aca3a3aea3a75164746ffaa79");
        HMACTest.tests[4].key = base16.fromString("0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c");
        HMACTest.tests[4].data = "Test With Truncation".getBytes();
        HMACTest.tests[4].digest = base16.fromString("56461ef2342edc00f9bab995690efd4c");
        HMACTest.tests[5].key = new byte[80];
        for (int i = 0; i < HMACTest.tests[5].key.length; ++i) {
            HMACTest.tests[5].key[i] = -86;
        }
        HMACTest.tests[5].data = "Test Using Larger Than Block-Size Key - Hash Key First".getBytes();
        HMACTest.tests[5].digest = base16.fromString("6b1ab7fe4bd7bf8f0b62e6ce61b9d0cd");
        HMACTest.tests[6].key = new byte[80];
        for (int i = 0; i < HMACTest.tests[6].key.length; ++i) {
            HMACTest.tests[6].key[i] = -86;
        }
        HMACTest.tests[6].data = "Test Using Larger Than Block-Size Key and Larger Than One Block-Size Data".getBytes();
        HMACTest.tests[6].digest = base16.fromString("6f630fad67cda0ee1fb1f562db3aa53e");
    }
    
    private static class test_data
    {
        public byte[] key;
        public byte[] data;
        public byte[] digest;
    }
}
