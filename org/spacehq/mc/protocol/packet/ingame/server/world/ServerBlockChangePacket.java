// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.world;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.world.block.BlockChangeRecord;
import org.spacehq.packetlib.packet.Packet;

public class ServerBlockChangePacket implements Packet
{
    private BlockChangeRecord record;
    
    private ServerBlockChangePacket() {
    }
    
    public ServerBlockChangePacket(final BlockChangeRecord record) {
        this.record = record;
    }
    
    public BlockChangeRecord getRecord() {
        return this.record;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.record = new BlockChangeRecord(NetUtil.readPosition(in), NetUtil.readBlockState(in));
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        NetUtil.writePosition(out, this.record.getPosition());
        NetUtil.writeBlockState(out, this.record.getBlock());
    }
    
    @Override
    public boolean isPriority() {
        return false;
    }
    
    @Override
    public String toString() {
        return ReflectionToString.toString(this);
    }
}
