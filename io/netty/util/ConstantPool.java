// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util;

import io.netty.util.internal.ObjectUtil;
import java.util.HashMap;
import java.util.Map;

public abstract class ConstantPool<T extends Constant<T>>
{
    private final Map<String, T> constants;
    private int nextId;
    
    public ConstantPool() {
        this.constants = new HashMap<String, T>();
        this.nextId = 1;
    }
    
    public T valueOf(final Class<?> firstNameComponent, final String secondNameComponent) {
        if (firstNameComponent == null) {
            throw new NullPointerException("firstNameComponent");
        }
        if (secondNameComponent == null) {
            throw new NullPointerException("secondNameComponent");
        }
        return this.valueOf(firstNameComponent.getName() + '#' + secondNameComponent);
    }
    
    public T valueOf(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        synchronized (this.constants) {
            T c = this.constants.get(name);
            if (c == null) {
                c = this.newConstant(this.nextId, name);
                this.constants.put(name, c);
                ++this.nextId;
            }
            return c;
        }
    }
    
    public boolean exists(final String name) {
        ObjectUtil.checkNotNull(name, "name");
        synchronized (this.constants) {
            return this.constants.containsKey(name);
        }
    }
    
    public T newInstance(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        synchronized (this.constants) {
            T c = this.constants.get(name);
            if (c == null) {
                c = this.newConstant(this.nextId, name);
                this.constants.put(name, c);
                ++this.nextId;
                return c;
            }
            throw new IllegalArgumentException(String.format("'%s' is already in use", name));
        }
    }
    
    protected abstract T newConstant(final int p0, final String p1);
}
