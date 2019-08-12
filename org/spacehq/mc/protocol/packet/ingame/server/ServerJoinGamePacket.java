// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.world.WorldType;
import org.spacehq.mc.protocol.data.game.setting.Difficulty;
import org.spacehq.mc.protocol.data.game.entity.player.GameMode;
import org.spacehq.packetlib.packet.Packet;

public class ServerJoinGamePacket implements Packet
{
    private int entityId;
    private boolean hardcore;
    private GameMode gamemode;
    private int dimension;
    private Difficulty difficulty;
    private int maxPlayers;
    private WorldType worldType;
    private boolean reducedDebugInfo;
    
    private ServerJoinGamePacket() {
    }
    
    public ServerJoinGamePacket(final int entityId, final boolean hardcore, final GameMode gamemode, final int dimension, final Difficulty difficulty, final int maxPlayers, final WorldType worldType, final boolean reducedDebugInfo) {
        this.entityId = entityId;
        this.hardcore = hardcore;
        this.gamemode = gamemode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.maxPlayers = maxPlayers;
        this.worldType = worldType;
        this.reducedDebugInfo = reducedDebugInfo;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public boolean getHardcore() {
        return this.hardcore;
    }
    
    public GameMode getGameMode() {
        return this.gamemode;
    }
    
    public int getDimension() {
        return this.dimension;
    }
    
    public Difficulty getDifficulty() {
        return this.difficulty;
    }
    
    public int getMaxPlayers() {
        return this.maxPlayers;
    }
    
    public WorldType getWorldType() {
        return this.worldType;
    }
    
    public boolean getReducedDebugInfo() {
        return this.reducedDebugInfo;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readInt();
        int gamemode = in.readUnsignedByte();
        this.hardcore = ((gamemode & 0x8) == 0x8);
        gamemode &= 0xFFFFFFF7;
        this.gamemode = MagicValues.key(GameMode.class, gamemode);
        this.dimension = in.readInt();
        this.difficulty = MagicValues.key(Difficulty.class, in.readUnsignedByte());
        this.maxPlayers = in.readUnsignedByte();
        this.worldType = MagicValues.key(WorldType.class, in.readString().toLowerCase());
        this.reducedDebugInfo = in.readBoolean();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeInt(this.entityId);
        int gamemode = MagicValues.value(Integer.class, this.gamemode);
        if (this.hardcore) {
            gamemode |= 0x8;
        }
        out.writeByte(gamemode);
        out.writeInt(this.dimension);
        out.writeByte(MagicValues.value(Integer.class, this.difficulty));
        out.writeByte(this.maxPlayers);
        out.writeString(MagicValues.value(String.class, this.worldType));
        out.writeBoolean(this.reducedDebugInfo);
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
