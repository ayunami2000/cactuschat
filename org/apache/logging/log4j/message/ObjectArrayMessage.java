// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.message;

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

public final class ObjectArrayMessage implements Message
{
    private static final Object[] EMPTY_OBJECT_ARRAY;
    private static final long serialVersionUID = -5903272448334166185L;
    private transient Object[] array;
    private transient String arrayString;
    
    public ObjectArrayMessage(final Object... obj) {
        this.array = ((obj == null) ? ObjectArrayMessage.EMPTY_OBJECT_ARRAY : obj);
    }
    
    private boolean equalObjectsOrStrings(final Object[] left, final Object[] right) {
        return left.equals(right) || String.valueOf(left).equals(String.valueOf(right));
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ObjectArrayMessage that = (ObjectArrayMessage)o;
        return (this.array == null) ? (that.array == null) : this.equalObjectsOrStrings(this.array, that.array);
    }
    
    @Override
    public String getFormat() {
        return this.getFormattedMessage();
    }
    
    @Override
    public String getFormattedMessage() {
        if (this.arrayString == null) {
            this.arrayString = Arrays.toString(this.array);
        }
        return this.arrayString;
    }
    
    @Override
    public Object[] getParameters() {
        return this.array;
    }
    
    @Override
    public Throwable getThrowable() {
        return null;
    }
    
    @Override
    public int hashCode() {
        return this.array.hashCode();
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.array = (Object[])in.readObject();
    }
    
    @Override
    public String toString() {
        return "ObjectArrayMessage[obj=" + this.getFormattedMessage() + ']';
    }
    
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.array);
    }
    
    static {
        EMPTY_OBJECT_ARRAY = new Object[0];
    }
}
