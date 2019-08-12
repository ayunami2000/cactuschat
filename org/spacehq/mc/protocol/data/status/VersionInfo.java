// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.status;

public class VersionInfo
{
    public static final VersionInfo CURRENT;
    private String name;
    private int protocol;
    
    public VersionInfo(final String name, final int protocol) {
        this.name = name;
        this.protocol = protocol;
    }
    
    public String getVersionName() {
        return this.name;
    }
    
    public int getProtocolVersion() {
        return this.protocol;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof VersionInfo && this.name.equals(((VersionInfo)o).name) && this.protocol == ((VersionInfo)o).protocol;
    }
    
    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.protocol;
        return result;
    }
    
    static {
        CURRENT = new VersionInfo("1.10.2", 210);
    }
}
