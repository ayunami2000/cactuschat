// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.spi;

import java.util.Collections;
import java.util.HashMap;
import org.apache.logging.log4j.util.PropertiesUtil;
import java.util.Map;

public class DefaultThreadContextMap implements ThreadContextMap
{
    public static final String INHERITABLE_MAP = "isThreadContextMapInheritable";
    private final boolean useMap;
    private final ThreadLocal<Map<String, String>> localMap;
    
    public DefaultThreadContextMap(final boolean useMap) {
        this.useMap = useMap;
        this.localMap = createThreadLocalMap(useMap);
    }
    
    static ThreadLocal<Map<String, String>> createThreadLocalMap(final boolean isMapEnabled) {
        final PropertiesUtil managerProps = PropertiesUtil.getProperties();
        final boolean inheritable = managerProps.getBooleanProperty("isThreadContextMapInheritable");
        if (inheritable) {
            return new InheritableThreadLocal<Map<String, String>>() {
                @Override
                protected Map<String, String> childValue(final Map<String, String> parentValue) {
                    return (parentValue != null && isMapEnabled) ? Collections.unmodifiableMap((Map<? extends String, ? extends String>)new HashMap<String, String>(parentValue)) : null;
                }
            };
        }
        return new ThreadLocal<Map<String, String>>();
    }
    
    @Override
    public void put(final String key, final String value) {
        if (!this.useMap) {
            return;
        }
        Map<String, String> map = this.localMap.get();
        map = ((map == null) ? new HashMap<String, String>() : new HashMap<String, String>(map));
        map.put(key, value);
        this.localMap.set(Collections.unmodifiableMap((Map<? extends String, ? extends String>)map));
    }
    
    @Override
    public String get(final String key) {
        final Map<String, String> map = this.localMap.get();
        return (map == null) ? null : map.get(key);
    }
    
    @Override
    public void remove(final String key) {
        final Map<String, String> map = this.localMap.get();
        if (map != null) {
            final Map<String, String> copy = new HashMap<String, String>(map);
            copy.remove(key);
            this.localMap.set(Collections.unmodifiableMap((Map<? extends String, ? extends String>)copy));
        }
    }
    
    @Override
    public void clear() {
        this.localMap.remove();
    }
    
    @Override
    public boolean containsKey(final String key) {
        final Map<String, String> map = this.localMap.get();
        return map != null && map.containsKey(key);
    }
    
    @Override
    public Map<String, String> getCopy() {
        final Map<String, String> map = this.localMap.get();
        return (map == null) ? new HashMap<String, String>() : new HashMap<String, String>(map);
    }
    
    @Override
    public Map<String, String> getImmutableMapOrNull() {
        return this.localMap.get();
    }
    
    @Override
    public boolean isEmpty() {
        final Map<String, String> map = this.localMap.get();
        return map == null || map.size() == 0;
    }
    
    @Override
    public String toString() {
        final Map<String, String> map = this.localMap.get();
        return (map == null) ? "{}" : map.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final Map<String, String> map = this.localMap.get();
        result = 31 * result + ((map == null) ? 0 : map.hashCode());
        result = 31 * result + Boolean.valueOf(this.useMap).hashCode();
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof DefaultThreadContextMap) {
            final DefaultThreadContextMap other = (DefaultThreadContextMap)obj;
            if (this.useMap != other.useMap) {
                return false;
            }
        }
        if (!(obj instanceof ThreadContextMap)) {
            return false;
        }
        final ThreadContextMap other2 = (ThreadContextMap)obj;
        final Map<String, String> map = this.localMap.get();
        final Map<String, String> otherMap = other2.getImmutableMapOrNull();
        if (map == null) {
            if (otherMap != null) {
                return false;
            }
        }
        else if (!map.equals(otherMap)) {
            return false;
        }
        return true;
    }
}
