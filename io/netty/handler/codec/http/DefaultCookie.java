// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Set;

public class DefaultCookie implements Cookie
{
    private final String name;
    private String value;
    private String rawValue;
    private String domain;
    private String path;
    private String comment;
    private String commentUrl;
    private boolean discard;
    private Set<Integer> ports;
    private Set<Integer> unmodifiablePorts;
    private long maxAge;
    private int version;
    private boolean secure;
    private boolean httpOnly;
    
    public DefaultCookie(String name, final String value) {
        this.ports = Collections.emptySet();
        this.unmodifiablePorts = this.ports;
        this.maxAge = Long.MIN_VALUE;
        if (name == null) {
            throw new NullPointerException("name");
        }
        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        int i = 0;
        while (i < name.length()) {
            final char c = name.charAt(i);
            if (c > '\u007f') {
                throw new IllegalArgumentException("name contains non-ascii character: " + name);
            }
            switch (c) {
                case '\t':
                case '\n':
                case '\u000b':
                case '\f':
                case '\r':
                case ' ':
                case ',':
                case ';':
                case '=': {
                    throw new IllegalArgumentException("name contains one of the following prohibited characters: =,; \\t\\r\\n\\v\\f: " + name);
                }
                default: {
                    ++i;
                    continue;
                }
            }
        }
        if (name.charAt(0) == '$') {
            throw new IllegalArgumentException("name starting with '$' not allowed: " + name);
        }
        this.name = name;
        this.setValue(value);
    }
    
    @Override
    public String name() {
        return this.name;
    }
    
    @Override
    public String value() {
        return this.value;
    }
    
    @Override
    public void setValue(final String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.value = value;
    }
    
    @Override
    public String rawValue() {
        return this.rawValue;
    }
    
    @Override
    public void setRawValue(final String rawValue) {
        if (this.value == null) {
            throw new NullPointerException("rawValue");
        }
        this.rawValue = rawValue;
    }
    
    @Override
    public String domain() {
        return this.domain;
    }
    
    @Override
    public void setDomain(final String domain) {
        this.domain = validateValue("domain", domain);
    }
    
    @Override
    public String path() {
        return this.path;
    }
    
    @Override
    public void setPath(final String path) {
        this.path = validateValue("path", path);
    }
    
    @Override
    public String comment() {
        return this.comment;
    }
    
    @Override
    public void setComment(final String comment) {
        this.comment = validateValue("comment", comment);
    }
    
    @Override
    public String commentUrl() {
        return this.commentUrl;
    }
    
    @Override
    public void setCommentUrl(final String commentUrl) {
        this.commentUrl = validateValue("commentUrl", commentUrl);
    }
    
    @Override
    public boolean isDiscard() {
        return this.discard;
    }
    
    @Override
    public void setDiscard(final boolean discard) {
        this.discard = discard;
    }
    
    @Override
    public Set<Integer> ports() {
        if (this.unmodifiablePorts == null) {
            this.unmodifiablePorts = Collections.unmodifiableSet((Set<? extends Integer>)this.ports);
        }
        return this.unmodifiablePorts;
    }
    
    @Override
    public void setPorts(final int... ports) {
        if (ports == null) {
            throw new NullPointerException("ports");
        }
        final int[] portsCopy = ports.clone();
        if (portsCopy.length == 0) {
            final Set<Integer> emptySet = Collections.emptySet();
            this.ports = emptySet;
            this.unmodifiablePorts = emptySet;
        }
        else {
            final Set<Integer> newPorts = new TreeSet<Integer>();
            for (final int p : portsCopy) {
                if (p <= 0 || p > 65535) {
                    throw new IllegalArgumentException("port out of range: " + p);
                }
                newPorts.add(p);
            }
            this.ports = newPorts;
            this.unmodifiablePorts = null;
        }
    }
    
    @Override
    public void setPorts(final Iterable<Integer> ports) {
        final Set<Integer> newPorts = new TreeSet<Integer>();
        for (final int p : ports) {
            if (p <= 0 || p > 65535) {
                throw new IllegalArgumentException("port out of range: " + p);
            }
            newPorts.add(p);
        }
        if (newPorts.isEmpty()) {
            final Set<Integer> emptySet = Collections.emptySet();
            this.ports = emptySet;
            this.unmodifiablePorts = emptySet;
        }
        else {
            this.ports = newPorts;
            this.unmodifiablePorts = null;
        }
    }
    
    @Override
    public long maxAge() {
        return this.maxAge;
    }
    
    @Override
    public void setMaxAge(final long maxAge) {
        this.maxAge = maxAge;
    }
    
    @Override
    public int version() {
        return this.version;
    }
    
    @Override
    public void setVersion(final int version) {
        this.version = version;
    }
    
    @Override
    public boolean isSecure() {
        return this.secure;
    }
    
    @Override
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }
    
    @Override
    public boolean isHttpOnly() {
        return this.httpOnly;
    }
    
    @Override
    public void setHttpOnly(final boolean httpOnly) {
        this.httpOnly = httpOnly;
    }
    
    @Override
    public int hashCode() {
        return this.name().hashCode();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Cookie)) {
            return false;
        }
        final Cookie that = (Cookie)o;
        if (!this.name().equalsIgnoreCase(that.name())) {
            return false;
        }
        if (this.path() == null) {
            if (that.path() != null) {
                return false;
            }
        }
        else {
            if (that.path() == null) {
                return false;
            }
            if (!this.path().equals(that.path())) {
                return false;
            }
        }
        if (this.domain() == null) {
            return that.domain() == null;
        }
        return that.domain() != null && this.domain().equalsIgnoreCase(that.domain());
    }
    
    @Override
    public int compareTo(final Cookie c) {
        int v = this.name().compareToIgnoreCase(c.name());
        if (v != 0) {
            return v;
        }
        if (this.path() == null) {
            if (c.path() != null) {
                return -1;
            }
        }
        else {
            if (c.path() == null) {
                return 1;
            }
            v = this.path().compareTo(c.path());
            if (v != 0) {
                return v;
            }
        }
        if (this.domain() == null) {
            if (c.domain() != null) {
                return -1;
            }
            return 0;
        }
        else {
            if (c.domain() == null) {
                return 1;
            }
            v = this.domain().compareToIgnoreCase(c.domain());
            return v;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder().append(this.name()).append('=').append(this.value());
        if (this.domain() != null) {
            buf.append(", domain=").append(this.domain());
        }
        if (this.path() != null) {
            buf.append(", path=").append(this.path());
        }
        if (this.comment() != null) {
            buf.append(", comment=").append(this.comment());
        }
        if (this.maxAge() >= 0L) {
            buf.append(", maxAge=").append(this.maxAge()).append('s');
        }
        if (this.isSecure()) {
            buf.append(", secure");
        }
        if (this.isHttpOnly()) {
            buf.append(", HTTPOnly");
        }
        return buf.toString();
    }
    
    private static String validateValue(final String name, String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return null;
        }
        int i = 0;
        while (i < value.length()) {
            final char c = value.charAt(i);
            switch (c) {
                case '\n':
                case '\u000b':
                case '\f':
                case '\r':
                case ';': {
                    throw new IllegalArgumentException(name + " contains one of the following prohibited characters: " + ";\\r\\n\\f\\v (" + value + ')');
                }
                default: {
                    ++i;
                    continue;
                }
            }
        }
        return value;
    }
}
