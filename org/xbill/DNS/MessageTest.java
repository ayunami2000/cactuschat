// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.UnknownHostException;
import java.net.InetAddress;
import junit.framework.Assert;
import java.util.Arrays;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class MessageTest
{
    public static Test suite() {
        final TestSuite s = new TestSuite();
        s.addTestSuite(Test_init.class);
        return s;
    }
    
    public static class Test_init extends TestCase
    {
        public void test_0arg() {
            final Message m = new Message();
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(0)));
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(1)));
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(2)));
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(3)));
            try {
                m.getSectionArray(4);
                Assert.fail("IndexOutOfBoundsException not thrown");
            }
            catch (IndexOutOfBoundsException ex) {}
            final Header h = m.getHeader();
            Assert.assertEquals(0, h.getCount(0));
            Assert.assertEquals(0, h.getCount(1));
            Assert.assertEquals(0, h.getCount(2));
            Assert.assertEquals(0, h.getCount(3));
        }
        
        public void test_1arg() {
            final Message m = new Message(10);
            Assert.assertEquals(new Header(10).toString(), m.getHeader().toString());
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(0)));
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(1)));
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(2)));
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(3)));
            try {
                m.getSectionArray(4);
                Assert.fail("IndexOutOfBoundsException not thrown");
            }
            catch (IndexOutOfBoundsException ex) {}
            final Header h = m.getHeader();
            Assert.assertEquals(0, h.getCount(0));
            Assert.assertEquals(0, h.getCount(1));
            Assert.assertEquals(0, h.getCount(2));
            Assert.assertEquals(0, h.getCount(3));
        }
        
        public void test_newQuery() throws TextParseException, UnknownHostException {
            final Name n = Name.fromString("The.Name.");
            final ARecord ar = new ARecord(n, 1, 1L, InetAddress.getByName("192.168.101.110"));
            final Message m = Message.newQuery(ar);
            Assert.assertTrue(Arrays.equals(new Record[] { ar }, m.getSectionArray(0)));
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(1)));
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(2)));
            Assert.assertTrue(Arrays.equals(new Record[0], m.getSectionArray(3)));
            final Header h = m.getHeader();
            Assert.assertEquals(1, h.getCount(0));
            Assert.assertEquals(0, h.getCount(1));
            Assert.assertEquals(0, h.getCount(2));
            Assert.assertEquals(0, h.getCount(3));
            Assert.assertEquals(0, h.getOpcode());
            Assert.assertEquals(true, h.getFlag(7));
        }
    }
}
