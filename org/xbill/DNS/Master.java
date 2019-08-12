// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.io.File;

public class Master
{
    private Name origin;
    private File file;
    private Record last;
    private long defaultTTL;
    private Master included;
    private Tokenizer st;
    private int currentType;
    private int currentDClass;
    private long currentTTL;
    private boolean needSOATTL;
    private Generator generator;
    private List generators;
    private boolean noExpandGenerate;
    
    Master(final File file, final Name origin, final long initialTTL) throws IOException {
        this.last = null;
        this.included = null;
        if (origin != null && !origin.isAbsolute()) {
            throw new RelativeNameException(origin);
        }
        this.file = file;
        this.st = new Tokenizer(file);
        this.origin = origin;
        this.defaultTTL = initialTTL;
    }
    
    public Master(final String filename, final Name origin, final long ttl) throws IOException {
        this(new File(filename), origin, ttl);
    }
    
    public Master(final String filename, final Name origin) throws IOException {
        this(new File(filename), origin, -1L);
    }
    
    public Master(final String filename) throws IOException {
        this(new File(filename), null, -1L);
    }
    
    public Master(final InputStream in, final Name origin, final long ttl) {
        this.last = null;
        this.included = null;
        if (origin != null && !origin.isAbsolute()) {
            throw new RelativeNameException(origin);
        }
        this.st = new Tokenizer(in);
        this.origin = origin;
        this.defaultTTL = ttl;
    }
    
    public Master(final InputStream in, final Name origin) {
        this(in, origin, -1L);
    }
    
    public Master(final InputStream in) {
        this(in, null, -1L);
    }
    
    private Name parseName(final String s, final Name origin) throws TextParseException {
        try {
            return Name.fromString(s, origin);
        }
        catch (TextParseException e) {
            throw this.st.exception(e.getMessage());
        }
    }
    
    private void parseTTLClassAndType() throws IOException {
        boolean seen_class = false;
        seen_class = false;
        String s = this.st.getString();
        final int value = DClass.value(s);
        this.currentDClass = value;
        if (value >= 0) {
            s = this.st.getString();
            seen_class = true;
        }
        this.currentTTL = -1L;
        try {
            this.currentTTL = TTL.parseTTL(s);
            s = this.st.getString();
        }
        catch (NumberFormatException e) {
            if (this.defaultTTL >= 0L) {
                this.currentTTL = this.defaultTTL;
            }
            else if (this.last != null) {
                this.currentTTL = this.last.getTTL();
            }
        }
        if (!seen_class) {
            if ((this.currentDClass = DClass.value(s)) >= 0) {
                s = this.st.getString();
            }
            else {
                this.currentDClass = 1;
            }
        }
        if ((this.currentType = Type.value(s)) < 0) {
            throw this.st.exception("Invalid type '" + s + "'");
        }
        if (this.currentTTL < 0L) {
            if (this.currentType != 6) {
                throw this.st.exception("missing TTL");
            }
            this.needSOATTL = true;
            this.currentTTL = 0L;
        }
    }
    
    private long parseUInt32(final String s) {
        if (!Character.isDigit(s.charAt(0))) {
            return -1L;
        }
        try {
            final long l = Long.parseLong(s);
            if (l < 0L || l > 4294967295L) {
                return -1L;
            }
            return l;
        }
        catch (NumberFormatException e) {
            return -1L;
        }
    }
    
    private void startGenerate() throws IOException {
        final String s = this.st.getIdentifier();
        int n = s.indexOf("-");
        if (n < 0) {
            throw this.st.exception("Invalid $GENERATE range specifier: " + s);
        }
        final String startstr = s.substring(0, n);
        String endstr = s.substring(n + 1);
        String stepstr = null;
        n = endstr.indexOf("/");
        if (n >= 0) {
            stepstr = endstr.substring(n + 1);
            endstr = endstr.substring(0, n);
        }
        final long start = this.parseUInt32(startstr);
        final long end = this.parseUInt32(endstr);
        long step;
        if (stepstr != null) {
            step = this.parseUInt32(stepstr);
        }
        else {
            step = 1L;
        }
        if (start < 0L || end < 0L || start > end || step <= 0L) {
            throw this.st.exception("Invalid $GENERATE range specifier: " + s);
        }
        final String nameSpec = this.st.getIdentifier();
        this.parseTTLClassAndType();
        if (!Generator.supportedType(this.currentType)) {
            throw this.st.exception("$GENERATE does not support " + Type.string(this.currentType) + " records");
        }
        final String rdataSpec = this.st.getIdentifier();
        this.st.getEOL();
        this.st.unget();
        this.generator = new Generator(start, end, step, nameSpec, this.currentType, this.currentDClass, this.currentTTL, rdataSpec, this.origin);
        if (this.generators == null) {
            this.generators = new ArrayList(1);
        }
        this.generators.add(this.generator);
    }
    
    private void endGenerate() throws IOException {
        this.st.getEOL();
        this.generator = null;
    }
    
    private Record nextGenerated() throws IOException {
        try {
            return this.generator.nextRecord();
        }
        catch (Tokenizer.TokenizerException e) {
            throw this.st.exception("Parsing $GENERATE: " + e.getBaseMessage());
        }
        catch (TextParseException e2) {
            throw this.st.exception("Parsing $GENERATE: " + e2.getMessage());
        }
    }
    
    public Record _nextRecord() throws IOException {
        if (this.included != null) {
            final Record rec = this.included.nextRecord();
            if (rec != null) {
                return rec;
            }
            this.included = null;
        }
        if (this.generator != null) {
            final Record rec = this.nextGenerated();
            if (rec != null) {
                return rec;
            }
            this.endGenerate();
        }
        Name name;
        while (true) {
            Tokenizer.Token token = this.st.get(true, false);
            if (token.type == 2) {
                final Tokenizer.Token next = this.st.get();
                if (next.type == 1) {
                    continue;
                }
                if (next.type == 0) {
                    return null;
                }
                this.st.unget();
                if (this.last == null) {
                    throw this.st.exception("no owner");
                }
                name = this.last.getName();
                break;
            }
            else {
                if (token.type == 1) {
                    continue;
                }
                if (token.type == 0) {
                    return null;
                }
                if (token.value.charAt(0) == '$') {
                    final String s = token.value;
                    if (s.equalsIgnoreCase("$ORIGIN")) {
                        this.origin = this.st.getName(Name.root);
                        this.st.getEOL();
                    }
                    else if (s.equalsIgnoreCase("$TTL")) {
                        this.defaultTTL = this.st.getTTL();
                        this.st.getEOL();
                    }
                    else {
                        if (s.equalsIgnoreCase("$INCLUDE")) {
                            final String filename = this.st.getString();
                            File newfile;
                            if (this.file != null) {
                                final String parent = this.file.getParent();
                                newfile = new File(parent, filename);
                            }
                            else {
                                newfile = new File(filename);
                            }
                            Name incorigin = this.origin;
                            token = this.st.get();
                            if (token.isString()) {
                                incorigin = this.parseName(token.value, Name.root);
                                this.st.getEOL();
                            }
                            this.included = new Master(newfile, incorigin, this.defaultTTL);
                            return this.nextRecord();
                        }
                        if (!s.equalsIgnoreCase("$GENERATE")) {
                            throw this.st.exception("Invalid directive: " + s);
                        }
                        if (this.generator != null) {
                            throw new IllegalStateException("cannot nest $GENERATE");
                        }
                        this.startGenerate();
                        if (!this.noExpandGenerate) {
                            return this.nextGenerated();
                        }
                        this.endGenerate();
                    }
                }
                else {
                    final String s = token.value;
                    name = this.parseName(s, this.origin);
                    if (this.last != null && name.equals(this.last.getName())) {
                        name = this.last.getName();
                        break;
                    }
                    break;
                }
            }
        }
        this.parseTTLClassAndType();
        this.last = Record.fromString(name, this.currentType, this.currentDClass, this.currentTTL, this.st, this.origin);
        if (this.needSOATTL) {
            final long ttl = ((SOARecord)this.last).getMinimum();
            this.last.setTTL(ttl);
            this.defaultTTL = ttl;
            this.needSOATTL = false;
        }
        return this.last;
    }
    
    public Record nextRecord() throws IOException {
        Record rec = null;
        try {
            rec = this._nextRecord();
        }
        finally {
            if (rec == null) {
                this.st.close();
            }
        }
        return rec;
    }
    
    public void expandGenerate(final boolean wantExpand) {
        this.noExpandGenerate = !wantExpand;
    }
    
    public Iterator generators() {
        if (this.generators != null) {
            return Collections.unmodifiableList((List<?>)this.generators).iterator();
        }
        return Collections.EMPTY_LIST.iterator();
    }
    
    protected void finalize() {
        this.st.close();
    }
}
