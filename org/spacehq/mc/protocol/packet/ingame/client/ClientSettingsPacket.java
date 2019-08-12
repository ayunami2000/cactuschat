// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client;

import org.spacehq.mc.protocol.util.ReflectionToString;
import java.util.Iterator;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import java.util.ArrayList;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import java.util.Arrays;
import org.spacehq.mc.protocol.data.game.entity.player.Hand;
import org.spacehq.mc.protocol.data.game.setting.SkinPart;
import java.util.List;
import org.spacehq.mc.protocol.data.game.setting.ChatVisibility;
import org.spacehq.packetlib.packet.Packet;

public class ClientSettingsPacket implements Packet
{
    private String locale;
    private int renderDistance;
    private ChatVisibility chatVisibility;
    private boolean chatColors;
    private List<SkinPart> visibleParts;
    private Hand mainHand;
    
    private ClientSettingsPacket() {
    }
    
    public ClientSettingsPacket(final String locale, final int renderDistance, final ChatVisibility chatVisibility, final boolean chatColors, final SkinPart[] visibleParts, final Hand mainHand) {
        this.locale = locale;
        this.renderDistance = renderDistance;
        this.chatVisibility = chatVisibility;
        this.chatColors = chatColors;
        this.visibleParts = Arrays.asList(visibleParts);
        this.mainHand = mainHand;
    }
    
    public String getLocale() {
        return this.locale;
    }
    
    public int getRenderDistance() {
        return this.renderDistance;
    }
    
    public ChatVisibility getChatVisibility() {
        return this.chatVisibility;
    }
    
    public boolean getUseChatColors() {
        return this.chatColors;
    }
    
    public List<SkinPart> getVisibleParts() {
        return this.visibleParts;
    }
    
    public Hand getMainHand() {
        return this.mainHand;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.locale = in.readString();
        this.renderDistance = in.readByte();
        this.chatVisibility = MagicValues.key(ChatVisibility.class, in.readVarInt());
        this.chatColors = in.readBoolean();
        this.visibleParts = new ArrayList<SkinPart>();
        final int flags = in.readUnsignedByte();
        for (final SkinPart part : SkinPart.values()) {
            final int bit = 1 << part.ordinal();
            if ((flags & bit) == bit) {
                this.visibleParts.add(part);
            }
        }
        this.mainHand = MagicValues.key(Hand.class, in.readVarInt());
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeString(this.locale);
        out.writeByte(this.renderDistance);
        out.writeVarInt(MagicValues.value(Integer.class, this.chatVisibility));
        out.writeBoolean(this.chatColors);
        int flags = 0;
        for (final SkinPart part : this.visibleParts) {
            flags |= 1 << part.ordinal();
        }
        out.writeByte(flags);
        out.writeVarInt(MagicValues.value(Integer.class, this.mainHand));
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
