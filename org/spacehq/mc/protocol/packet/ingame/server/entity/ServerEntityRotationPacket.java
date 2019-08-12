// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity;

import org.spacehq.mc.protocol.util.ReflectionToString;

public class ServerEntityRotationPacket extends ServerEntityMovementPacket
{
    protected ServerEntityRotationPacket() {
        this.rot = true;
    }
    
    public ServerEntityRotationPacket(final int entityId, final float yaw, final float pitch, final boolean onGround) {
        super(entityId, onGround);
        this.rot = true;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    @Override
    public String toString() {
        return ReflectionToString.toString(this);
    }
}
