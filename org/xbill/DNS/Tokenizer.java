// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base32;
import org.xbill.DNS.utils.base16;
import org.xbill.DNS.utils.base64;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class Tokenizer
{
    private static String delim;
    private static String quotes;
    public static final int EOF = 0;
    public static final int EOL = 1;
    public static final int WHITESPACE = 2;
    public static final int IDENTIFIER = 3;
    public static final int QUOTED_STRING = 4;
    public static final int COMMENT = 5;
    private PushbackInputStream is;
    private boolean ungottenToken;
    private int multiline;
    private boolean quoting;
    private String delimiters;
    private Token current;
    private StringBuffer sb;
    private boolean wantClose;
    private String filename;
    private int line;
    
    public Tokenizer(InputStream is) {
        if (!(is instanceof BufferedInputStream)) {
            is = new BufferedInputStream(is);
        }
        this.is = new PushbackInputStream(is, 2);
        this.ungottenToken = false;
        this.multiline = 0;
        this.quoting = false;
        this.delimiters = Tokenizer.delim;
        this.current = new Token();
        this.sb = new StringBuffer();
        this.filename = "<none>";
        this.line = 1;
    }
    
    public Tokenizer(final String s) {
        this(new ByteArrayInputStream(s.getBytes()));
    }
    
    public Tokenizer(final File f) throws FileNotFoundException {
        this(new FileInputStream(f));
        this.wantClose = true;
        this.filename = f.getName();
    }
    
    private int getChar() throws IOException {
        int c = this.is.read();
        if (c == 13) {
            final int next = this.is.read();
            if (next != 10) {
                this.is.unread(next);
            }
            c = 10;
        }
        if (c == 10) {
            ++this.line;
        }
        return c;
    }
    
    private void ungetChar(final int c) throws IOException {
        if (c == -1) {
            return;
        }
        this.is.unread(c);
        if (c == 10) {
            --this.line;
        }
    }
    
    private int skipWhitespace() throws IOException {
        int skipped = 0;
        int c;
        while (true) {
            c = this.getChar();
            if (c != 32 && c != 9 && (c != 10 || this.multiline <= 0)) {
                break;
            }
            ++skipped;
        }
        this.ungetChar(c);
        return skipped;
    }
    
    private void checkUnbalancedParens() throws TextParseException {
        if (this.multiline > 0) {
            throw this.exception("unbalanced parentheses");
        }
    }
    
    public Token get(final boolean wantWhitespace, final boolean wantComment) throws IOException {
        if (this.ungottenToken) {
            this.ungottenToken = false;
            if (this.current.type == 2) {
                if (wantWhitespace) {
                    return this.current;
                }
            }
            else {
                if (this.current.type != 5) {
                    if (this.current.type == 1) {
                        ++this.line;
                    }
                    return this.current;
                }
                if (wantComment) {
                    return this.current;
                }
            }
        }
        final int skipped = this.skipWhitespace();
        if (skipped > 0 && wantWhitespace) {
            return this.current.set(2, null);
        }
        int type = 3;
        this.sb.setLength(0);
        while (true) {
            int c = this.getChar();
            if (c == -1 || this.delimiters.indexOf(c) != -1) {
                if (c == -1) {
                    if (this.quoting) {
                        throw this.exception("EOF in quoted string");
                    }
                    if (this.sb.length() == 0) {
                        return this.current.set(0, null);
                    }
                    return this.current.set(type, this.sb);
                }
                else if (this.sb.length() == 0 && type != 4) {
                    if (c == 40) {
                        ++this.multiline;
                        this.skipWhitespace();
                    }
                    else if (c == 41) {
                        if (this.multiline <= 0) {
                            throw this.exception("invalid close parenthesis");
                        }
                        --this.multiline;
                        this.skipWhitespace();
                    }
                    else if (c == 34) {
                        if (!this.quoting) {
                            this.quoting = true;
                            this.delimiters = Tokenizer.quotes;
                            type = 4;
                        }
                        else {
                            this.quoting = false;
                            this.delimiters = Tokenizer.delim;
                            this.skipWhitespace();
                        }
                    }
                    else {
                        if (c == 10) {
                            return this.current.set(1, null);
                        }
                        if (c != 59) {
                            throw new IllegalStateException();
                        }
                        while (true) {
                            c = this.getChar();
                            if (c == 10 || c == -1) {
                                break;
                            }
                            this.sb.append((char)c);
                        }
                        if (wantComment) {
                            this.ungetChar(c);
                            return this.current.set(5, this.sb);
                        }
                        if (c == -1 && type != 4) {
                            this.checkUnbalancedParens();
                            return this.current.set(0, null);
                        }
                        if (this.multiline <= 0) {
                            return this.current.set(1, null);
                        }
                        this.skipWhitespace();
                        this.sb.setLength(0);
                    }
                }
                else {
                    this.ungetChar(c);
                    if (this.sb.length() == 0 && type != 4) {
                        this.checkUnbalancedParens();
                        return this.current.set(0, null);
                    }
                    return this.current.set(type, this.sb);
                }
            }
            else {
                if (c == 92) {
                    c = this.getChar();
                    if (c == -1) {
                        throw this.exception("unterminated escape sequence");
                    }
                    this.sb.append('\\');
                }
                else if (this.quoting && c == 10) {
                    throw this.exception("newline in quoted string");
                }
                this.sb.append((char)c);
            }
        }
    }
    
    public Token get() throws IOException {
        return this.get(false, false);
    }
    
    public void unget() {
        if (this.ungottenToken) {
            throw new IllegalStateException("Cannot unget multiple tokens");
        }
        if (this.current.type == 1) {
            --this.line;
        }
        this.ungottenToken = true;
    }
    
    public String getString() throws IOException {
        final Token next = this.get();
        if (!next.isString()) {
            throw this.exception("expected a string");
        }
        return next.value;
    }
    
    private String _getIdentifier(final String expected) throws IOException {
        final Token next = this.get();
        if (next.type != 3) {
            throw this.exception("expected " + expected);
        }
        return next.value;
    }
    
    public String getIdentifier() throws IOException {
        return this._getIdentifier("an identifier");
    }
    
    public long getLong() throws IOException {
        final String next = this._getIdentifier("an integer");
        if (!Character.isDigit(next.charAt(0))) {
            throw this.exception("expected an integer");
        }
        try {
            return Long.parseLong(next);
        }
        catch (NumberFormatException e) {
            throw this.exception("expected an integer");
        }
    }
    
    public long getUInt32() throws IOException {
        final long l = this.getLong();
        if (l < 0L || l > 4294967295L) {
            throw this.exception("expected an 32 bit unsigned integer");
        }
        return l;
    }
    
    public int getUInt16() throws IOException {
        final long l = this.getLong();
        if (l < 0L || l > 65535L) {
            throw this.exception("expected an 16 bit unsigned integer");
        }
        return (int)l;
    }
    
    public int getUInt8() throws IOException {
        final long l = this.getLong();
        if (l < 0L || l > 255L) {
            throw this.exception("expected an 8 bit unsigned integer");
        }
        return (int)l;
    }
    
    public long getTTL() throws IOException {
        final String next = this._getIdentifier("a TTL value");
        try {
            return TTL.parseTTL(next);
        }
        catch (NumberFormatException e) {
            throw this.exception("expected a TTL value");
        }
    }
    
    public long getTTLLike() throws IOException {
        final String next = this._getIdentifier("a TTL-like value");
        try {
            return TTL.parse(next, false);
        }
        catch (NumberFormatException e) {
            throw this.exception("expected a TTL-like value");
        }
    }
    
    public Name getName(final Name origin) throws IOException {
        final String next = this._getIdentifier("a name");
        try {
            final Name name = Name.fromString(next, origin);
            if (!name.isAbsolute()) {
                throw new RelativeNameException(name);
            }
            return name;
        }
        catch (TextParseException e) {
            throw this.exception(e.getMessage());
        }
    }
    
    public InetAddress getAddress(final int family) throws IOException {
        final String next = this._getIdentifier("an address");
        try {
            return Address.getByAddress(next, family);
        }
        catch (UnknownHostException e) {
            throw this.exception(e.getMessage());
        }
    }
    
    public void getEOL() throws IOException {
        final Token next = this.get();
        if (next.type != 1 && next.type != 0) {
            throw this.exception("expected EOL or EOF");
        }
    }
    
    private String remainingStrings() throws IOException {
        StringBuffer buffer = null;
        while (true) {
            final Token t = this.get();
            if (!t.isString()) {
                break;
            }
            if (buffer == null) {
                buffer = new StringBuffer();
            }
            buffer.append(t.value);
        }
        this.unget();
        if (buffer == null) {
            return null;
        }
        return buffer.toString();
    }
    
    public byte[] getBase64(final boolean required) throws IOException {
        final String s = this.remainingStrings();
        if (s == null) {
            if (required) {
                throw this.exception("expected base64 encoded string");
            }
            return null;
        }
        else {
            final byte[] array = base64.fromString(s);
            if (array == null) {
                throw this.exception("invalid base64 encoding");
            }
            return array;
        }
    }
    
    public byte[] getBase64() throws IOException {
        return this.getBase64(false);
    }
    
    public byte[] getHex(final boolean required) throws IOException {
        final String s = this.remainingStrings();
        if (s == null) {
            if (required) {
                throw this.exception("expected hex encoded string");
            }
            return null;
        }
        else {
            final byte[] array = base16.fromString(s);
            if (array == null) {
                throw this.exception("invalid hex encoding");
            }
            return array;
        }
    }
    
    public byte[] getHex() throws IOException {
        return this.getHex(false);
    }
    
    public byte[] getHexString() throws IOException {
        final String next = this._getIdentifier("a hex string");
        final byte[] array = base16.fromString(next);
        if (array == null) {
            throw this.exception("invalid hex encoding");
        }
        return array;
    }
    
    public byte[] getBase32String(final base32 b32) throws IOException {
        final String next = this._getIdentifier("a base32 string");
        final byte[] array = b32.fromString(next);
        if (array == null) {
            throw this.exception("invalid base32 encoding");
        }
        return array;
    }
    
    public TextParseException exception(final String s) {
        return new TokenizerException(this.filename, this.line, s);
    }
    
    public void close() {
        if (this.wantClose) {
            try {
                this.is.close();
            }
            catch (IOException ex) {}
        }
    }
    
    protected void finalize() {
        this.close();
    }
    
    static {
        Tokenizer.delim = " \t\n;()\"";
        Tokenizer.quotes = "\"";
    }
    
    public static class Token
    {
        public int type;
        public String value;
        
        private Token() {
            this.type = -1;
            this.value = null;
        }
        
        private Token set(final int type, final StringBuffer value) {
            if (type < 0) {
                throw new IllegalArgumentException();
            }
            this.type = type;
            this.value = ((value == null) ? null : value.toString());
            return this;
        }
        
        public String toString() {
            switch (this.type) {
                case 0: {
                    return "<eof>";
                }
                case 1: {
                    return "<eol>";
                }
                case 2: {
                    return "<whitespace>";
                }
                case 3: {
                    return "<identifier: " + this.value + ">";
                }
                case 4: {
                    return "<quoted_string: " + this.value + ">";
                }
                case 5: {
                    return "<comment: " + this.value + ">";
                }
                default: {
                    return "<unknown>";
                }
            }
        }
        
        public boolean isString() {
            return this.type == 3 || this.type == 4;
        }
        
        public boolean isEOL() {
            return this.type == 1 || this.type == 0;
        }
    }
    
    static class TokenizerException extends TextParseException
    {
        String message;
        
        public TokenizerException(final String filename, final int line, final String message) {
            super(filename + ":" + line + ": " + message);
            this.message = message;
        }
        
        public String getBaseMessage() {
            return this.message;
        }
    }
}
