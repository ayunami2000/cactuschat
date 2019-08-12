// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;
import java.lang.reflect.Array;

public class ReflectionToString
{
    private ReflectionToString() {
    }
    
    private static String memberToString(final Object o) {
        if (o == null) {
            return "null";
        }
        if (!o.getClass().isArray()) {
            return o.toString();
        }
        final int length = Array.getLength(o);
        if (length > 20) {
            return o.getClass().getSimpleName() + "(length=" + length + ')';
        }
        final StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < length; ++i) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(memberToString(Array.get(o, i)));
        }
        return builder.append(']').toString();
    }
    
    public static String toString(final Object o) {
        if (o == null) {
            return "null";
        }
        try {
            final StringBuilder builder = new StringBuilder(o.getClass().getSimpleName()).append('(');
            final List<Field> allDeclaredFields = getAllDeclaredFields(o.getClass());
            for (int i = 0; i < allDeclaredFields.size(); ++i) {
                if (i > 0) {
                    builder.append(", ");
                }
                final Field field = allDeclaredFields.get(i);
                field.setAccessible(true);
                builder.append(field.getName()).append('=').append(memberToString(field.get(o)));
            }
            return builder.append(')').toString();
        }
        catch (Throwable e) {
            return o.getClass().getSimpleName() + '@' + Integer.toHexString(o.hashCode()) + '(' + e.toString() + ')';
        }
    }
    
    private static List<Field> getAllDeclaredFields(Class<?> clazz) {
        final List<Field> fields = new ArrayList<Field>();
        while (clazz != null) {
            for (final Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
