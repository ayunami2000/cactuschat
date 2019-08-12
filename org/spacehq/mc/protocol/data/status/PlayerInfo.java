// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.status;

import java.util.Arrays;
import org.spacehq.mc.auth.data.GameProfile;

public class PlayerInfo
{
    private int max;
    private int online;
    private GameProfile[] players;
    
    public PlayerInfo(final int max, final int online, final GameProfile[] players) {
        this.max = max;
        this.online = online;
        this.players = players;
    }
    
    public int getMaxPlayers() {
        return this.max;
    }
    
    public int getOnlinePlayers() {
        return this.online;
    }
    
    public GameProfile[] getPlayers() {
        return this.players;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof PlayerInfo && this.max == ((PlayerInfo)o).max && this.online == ((PlayerInfo)o).online && Arrays.deepEquals(this.players, ((PlayerInfo)o).players);
    }
    
    @Override
    public int hashCode() {
        int result = this.max;
        result = 31 * result + this.online;
        result = 31 * result + Arrays.deepHashCode(this.players);
        return result;
    }
}
