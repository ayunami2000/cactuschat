// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import junit.framework.Assert;
import java.util.Arrays;
import junit.framework.TestCase;

public class TokenizerTest extends TestCase
{
    private Tokenizer m_t;
    
    protected void setUp() {
        this.m_t = null;
    }
    
    private void assertEquals(final byte[] exp, final byte[] act) {
        Assert.assertTrue(Arrays.equals(exp, act));
    }
    
    public void test_get() throws IOException {
        this.m_t = new Tokenizer(new BufferedInputStream(new ByteArrayInputStream("AnIdentifier \"a quoted \\\" string\"\r\n; this is \"my\"\t(comment)\nanotherIdentifier (\ramultilineIdentifier\n)".getBytes())));
        Tokenizer.Token tt = this.m_t.get(true, true);
        Assert.assertEquals(3, tt.type);
        Assert.assertTrue(tt.isString());
        Assert.assertFalse(tt.isEOL());
        Assert.assertEquals("AnIdentifier", tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(2, tt.type);
        Assert.assertFalse(tt.isString());
        Assert.assertFalse(tt.isEOL());
        Assert.assertNull(tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(4, tt.type);
        Assert.assertTrue(tt.isString());
        Assert.assertFalse(tt.isEOL());
        Assert.assertEquals("a quoted \\\" string", tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(1, tt.type);
        Assert.assertFalse(tt.isString());
        Assert.assertTrue(tt.isEOL());
        Assert.assertNull(tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(5, tt.type);
        Assert.assertFalse(tt.isString());
        Assert.assertFalse(tt.isEOL());
        Assert.assertEquals(" this is \"my\"\t(comment)", tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(1, tt.type);
        Assert.assertFalse(tt.isString());
        Assert.assertTrue(tt.isEOL());
        Assert.assertNull(tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(3, tt.type);
        Assert.assertTrue(tt.isString());
        Assert.assertFalse(tt.isEOL());
        Assert.assertEquals("anotherIdentifier", tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(2, tt.type);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(3, tt.type);
        Assert.assertTrue(tt.isString());
        Assert.assertFalse(tt.isEOL());
        Assert.assertEquals("amultilineIdentifier", tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(2, tt.type);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(0, tt.type);
        Assert.assertFalse(tt.isString());
        Assert.assertTrue(tt.isEOL());
        Assert.assertNull(tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(0, tt.type);
        Assert.assertFalse(tt.isString());
        Assert.assertTrue(tt.isEOL());
        Assert.assertNull(tt.value);
        this.m_t = new Tokenizer("onlyOneIdentifier");
        tt = this.m_t.get();
        Assert.assertEquals(3, tt.type);
        Assert.assertEquals("onlyOneIdentifier", tt.value);
        this.m_t = new Tokenizer("identifier ;");
        tt = this.m_t.get();
        Assert.assertEquals("identifier", tt.value);
        tt = this.m_t.get();
        Assert.assertEquals(0, tt.type);
        this.m_t = new Tokenizer("identifier \nidentifier2; junk comment");
        tt = this.m_t.get(true, true);
        Assert.assertEquals(3, tt.type);
        Assert.assertEquals("identifier", tt.value);
        this.m_t.unget();
        tt = this.m_t.get(true, true);
        Assert.assertEquals(3, tt.type);
        Assert.assertEquals("identifier", tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(2, tt.type);
        this.m_t.unget();
        tt = this.m_t.get(true, true);
        Assert.assertEquals(2, tt.type);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(1, tt.type);
        this.m_t.unget();
        tt = this.m_t.get(true, true);
        Assert.assertEquals(1, tt.type);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(3, tt.type);
        Assert.assertEquals("identifier2", tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(5, tt.type);
        Assert.assertEquals(" junk comment", tt.value);
        this.m_t.unget();
        tt = this.m_t.get(true, true);
        Assert.assertEquals(5, tt.type);
        Assert.assertEquals(" junk comment", tt.value);
        tt = this.m_t.get(true, true);
        Assert.assertEquals(0, tt.type);
        this.m_t = new Tokenizer("identifier ( junk ; comment\n )");
        tt = this.m_t.get();
        Assert.assertEquals(3, tt.type);
        Assert.assertEquals(3, this.m_t.get().type);
        Assert.assertEquals(0, this.m_t.get().type);
    }
    
    public void test_get_invalid() throws IOException {
        (this.m_t = new Tokenizer("(this ;")).get();
        try {
            this.m_t.get();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        this.m_t = new Tokenizer("\"bad");
        try {
            this.m_t.get();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
        this.m_t = new Tokenizer(")");
        try {
            this.m_t.get();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex3) {}
        this.m_t = new Tokenizer("\\");
        try {
            this.m_t.get();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex4) {}
        this.m_t = new Tokenizer("\"\n");
        try {
            this.m_t.get();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex5) {}
    }
    
    public void test_File_input() throws IOException {
        final File tmp = File.createTempFile("dnsjava", "tmp");
        try {
            final FileWriter fw = new FileWriter(tmp);
            fw.write("file\ninput; test");
            fw.close();
            this.m_t = new Tokenizer(tmp);
            Tokenizer.Token tt = this.m_t.get();
            Assert.assertEquals(3, tt.type);
            Assert.assertEquals("file", tt.value);
            tt = this.m_t.get();
            Assert.assertEquals(1, tt.type);
            tt = this.m_t.get();
            Assert.assertEquals(3, tt.type);
            Assert.assertEquals("input", tt.value);
            tt = this.m_t.get(false, true);
            Assert.assertEquals(5, tt.type);
            Assert.assertEquals(" test", tt.value);
            this.m_t.close();
        }
        finally {
            tmp.delete();
        }
    }
    
    public void test_unwanted_comment() throws IOException {
        this.m_t = new Tokenizer("; this whole thing is a comment\n");
        final Tokenizer.Token tt = this.m_t.get();
        Assert.assertEquals(1, tt.type);
    }
    
    public void test_unwanted_ungotten_whitespace() throws IOException {
        this.m_t = new Tokenizer(" ");
        Tokenizer.Token tt = this.m_t.get(true, true);
        this.m_t.unget();
        tt = this.m_t.get();
        Assert.assertEquals(0, tt.type);
    }
    
    public void test_unwanted_ungotten_comment() throws IOException {
        this.m_t = new Tokenizer("; this whole thing is a comment");
        Tokenizer.Token tt = this.m_t.get(true, true);
        this.m_t.unget();
        tt = this.m_t.get();
        Assert.assertEquals(0, tt.type);
    }
    
    public void test_empty_string() throws IOException {
        this.m_t = new Tokenizer("");
        Tokenizer.Token tt = this.m_t.get();
        Assert.assertEquals(0, tt.type);
        this.m_t = new Tokenizer(" ");
        tt = this.m_t.get();
        Assert.assertEquals(0, tt.type);
    }
    
    public void test_multiple_ungets() throws IOException {
        this.m_t = new Tokenizer("a simple one");
        final Tokenizer.Token tt = this.m_t.get();
        this.m_t.unget();
        try {
            this.m_t.unget();
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex) {}
    }
    
    public void test_getString() throws IOException {
        this.m_t = new Tokenizer("just_an_identifier");
        String out = this.m_t.getString();
        Assert.assertEquals("just_an_identifier", out);
        this.m_t = new Tokenizer("\"just a string\"");
        out = this.m_t.getString();
        Assert.assertEquals("just a string", out);
        this.m_t = new Tokenizer("; just a comment");
        try {
            out = this.m_t.getString();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
    }
    
    public void test_getIdentifier() throws IOException {
        this.m_t = new Tokenizer("just_an_identifier");
        final String out = this.m_t.getIdentifier();
        Assert.assertEquals("just_an_identifier", out);
        this.m_t = new Tokenizer("\"just a string\"");
        try {
            this.m_t.getIdentifier();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
    }
    
    public void test_getLong() throws IOException {
        this.m_t = new Tokenizer("2147483648");
        final long out = this.m_t.getLong();
        Assert.assertEquals(2147483648L, out);
        this.m_t = new Tokenizer("-10");
        try {
            this.m_t.getLong();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        this.m_t = new Tokenizer("19_identifier");
        try {
            this.m_t.getLong();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
    }
    
    public void test_getUInt32() throws IOException {
        this.m_t = new Tokenizer("2882400018");
        final long out = this.m_t.getUInt32();
        Assert.assertEquals(2882400018L, out);
        this.m_t = new Tokenizer("4294967296");
        try {
            this.m_t.getUInt32();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        this.m_t = new Tokenizer("-12345");
        try {
            this.m_t.getUInt32();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
    }
    
    public void test_getUInt16() throws IOException {
        this.m_t = new Tokenizer("43981");
        final int out = this.m_t.getUInt16();
        Assert.assertEquals(43981L, out);
        this.m_t = new Tokenizer("65536");
        try {
            this.m_t.getUInt16();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        this.m_t = new Tokenizer("-125");
        try {
            this.m_t.getUInt16();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
    }
    
    public void test_getUInt8() throws IOException {
        this.m_t = new Tokenizer("205");
        final int out = this.m_t.getUInt8();
        Assert.assertEquals(205L, out);
        this.m_t = new Tokenizer("256");
        try {
            this.m_t.getUInt8();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        this.m_t = new Tokenizer("-12");
        try {
            this.m_t.getUInt8();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
    }
    
    public void test_getTTL() throws IOException {
        this.m_t = new Tokenizer("59S");
        Assert.assertEquals(59L, this.m_t.getTTL());
        this.m_t = new Tokenizer("2147483647");
        Assert.assertEquals(2147483647L, this.m_t.getTTL());
        this.m_t = new Tokenizer("2147483648");
        Assert.assertEquals(2147483647L, this.m_t.getTTL());
        this.m_t = new Tokenizer("Junk");
        try {
            this.m_t.getTTL();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
    }
    
    public void test_getTTLLike() throws IOException {
        this.m_t = new Tokenizer("59S");
        Assert.assertEquals(59L, this.m_t.getTTLLike());
        this.m_t = new Tokenizer("2147483647");
        Assert.assertEquals(2147483647L, this.m_t.getTTLLike());
        this.m_t = new Tokenizer("2147483648");
        Assert.assertEquals(2147483648L, this.m_t.getTTLLike());
        this.m_t = new Tokenizer("Junk");
        try {
            this.m_t.getTTLLike();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
    }
    
    public void test_getName() throws IOException, TextParseException {
        final Name root = Name.fromString(".");
        this.m_t = new Tokenizer("junk");
        final Name exp = Name.fromString("junk.");
        final Name out = this.m_t.getName(root);
        Assert.assertEquals(exp, out);
        final Name rel = Name.fromString("you.dig");
        this.m_t = new Tokenizer("junk");
        try {
            this.m_t.getName(rel);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
        this.m_t = new Tokenizer("");
        try {
            this.m_t.getName(root);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
    }
    
    public void test_getEOL() throws IOException {
        (this.m_t = new Tokenizer("id")).getIdentifier();
        try {
            this.m_t.getEOL();
        }
        catch (TextParseException e) {
            Assert.fail(e.getMessage());
        }
        this.m_t = new Tokenizer("\n");
        try {
            this.m_t.getEOL();
            this.m_t.getEOL();
        }
        catch (TextParseException e) {
            Assert.fail(e.getMessage());
        }
        this.m_t = new Tokenizer("id");
        try {
            this.m_t.getEOL();
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
    }
    
    public void test_getBase64() throws IOException {
        final byte[] exp = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        this.m_t = new Tokenizer("AQIDBAUGBwgJ");
        byte[] out = this.m_t.getBase64();
        this.assertEquals(exp, out);
        this.m_t = new Tokenizer("AQIDB AUGB   wgJ");
        out = this.m_t.getBase64();
        this.assertEquals(exp, out);
        this.m_t = new Tokenizer("AQIDBAUGBwgJ\nAB23DK");
        out = this.m_t.getBase64();
        this.assertEquals(exp, out);
        this.m_t = new Tokenizer("\n");
        Assert.assertNull(this.m_t.getBase64());
        this.m_t = new Tokenizer("\n");
        try {
            this.m_t.getBase64(true);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        this.m_t = new Tokenizer("not_base64");
        try {
            this.m_t.getBase64(false);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
        this.m_t = new Tokenizer("not_base64");
        try {
            this.m_t.getBase64(true);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex3) {}
    }
    
    public void test_getHex() throws IOException {
        final byte[] exp = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        this.m_t = new Tokenizer("0102030405060708090A0B0C0D0E0F");
        byte[] out = this.m_t.getHex();
        this.assertEquals(exp, out);
        this.m_t = new Tokenizer("0102030 405 060708090A0B0C      0D0E0F");
        out = this.m_t.getHex();
        this.assertEquals(exp, out);
        this.m_t = new Tokenizer("0102030405060708090A0B0C0D0E0F\n01AB3FE");
        out = this.m_t.getHex();
        this.assertEquals(exp, out);
        this.m_t = new Tokenizer("\n");
        Assert.assertNull(this.m_t.getHex());
        this.m_t = new Tokenizer("\n");
        try {
            this.m_t.getHex(true);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        this.m_t = new Tokenizer("not_hex");
        try {
            this.m_t.getHex(false);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
        this.m_t = new Tokenizer("not_hex");
        try {
            this.m_t.getHex(true);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex3) {}
    }
}
