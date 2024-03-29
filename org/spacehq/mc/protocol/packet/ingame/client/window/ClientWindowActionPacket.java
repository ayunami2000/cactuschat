// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client.window;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.game.window.FillStackParam;
import org.spacehq.mc.protocol.data.game.window.SpreadItemParam;
import org.spacehq.mc.protocol.data.game.window.DropItemParam;
import org.spacehq.mc.protocol.data.game.window.CreativeGrabParam;
import org.spacehq.mc.protocol.data.game.window.MoveToHotbarParam;
import org.spacehq.mc.protocol.data.game.window.ShiftClickItemParam;
import org.spacehq.mc.protocol.data.game.window.ClickItemParam;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.metadata.ItemStack;
import org.spacehq.mc.protocol.data.game.window.WindowAction;
import org.spacehq.mc.protocol.data.game.window.WindowActionParam;
import org.spacehq.packetlib.packet.Packet;

public class ClientWindowActionPacket implements Packet
{
    private int windowId;
    private int slot;
    private WindowActionParam param;
    private int actionId;
    private WindowAction action;
    private ItemStack clicked;
    
    private ClientWindowActionPacket() {
    }
    
    public ClientWindowActionPacket(final int windowId, final int actionId, final int slot, final ItemStack clicked, final WindowAction action, final WindowActionParam param) {
        this.windowId = windowId;
        this.actionId = actionId;
        this.slot = slot;
        this.clicked = clicked;
        this.action = action;
        this.param = param;
    }
    
    public int getWindowId() {
        return this.windowId;
    }
    
    public int getActionId() {
        return this.actionId;
    }
    
    public int getSlot() {
        return this.slot;
    }
    
    public ItemStack getClickedItem() {
        return this.clicked;
    }
    
    public WindowAction getAction() {
        return this.action;
    }
    
    public WindowActionParam getParam() {
        return this.param;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.windowId = in.readByte();
        this.slot = in.readShort();
        final byte param = in.readByte();
        this.actionId = in.readShort();
        this.action = MagicValues.key(WindowAction.class, in.readByte());
        this.clicked = NetUtil.readItem(in);
        if (this.action == WindowAction.CLICK_ITEM) {
            this.param = MagicValues.key(ClickItemParam.class, param);
        }
        else if (this.action == WindowAction.SHIFT_CLICK_ITEM) {
            this.param = MagicValues.key(ShiftClickItemParam.class, param);
        }
        else if (this.action == WindowAction.MOVE_TO_HOTBAR_SLOT) {
            this.param = MagicValues.key(MoveToHotbarParam.class, param);
        }
        else if (this.action == WindowAction.CREATIVE_GRAB_MAX_STACK) {
            this.param = MagicValues.key(CreativeGrabParam.class, param);
        }
        else if (this.action == WindowAction.DROP_ITEM) {
            this.param = MagicValues.key(DropItemParam.class, param + ((this.slot != -999) ? 2 : 0));
        }
        else if (this.action == WindowAction.SPREAD_ITEM) {
            this.param = MagicValues.key(SpreadItemParam.class, param);
        }
        else if (this.action == WindowAction.FILL_STACK) {
            this.param = MagicValues.key(FillStackParam.class, param);
        }
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeByte(this.windowId);
        out.writeShort(this.slot);
        int param = 0;
        if (this.action == WindowAction.CLICK_ITEM) {
            param = MagicValues.value(Integer.class, this.param);
        }
        else if (this.action == WindowAction.SHIFT_CLICK_ITEM) {
            param = MagicValues.value(Integer.class, this.param);
        }
        else if (this.action == WindowAction.MOVE_TO_HOTBAR_SLOT) {
            param = MagicValues.value(Integer.class, this.param);
        }
        else if (this.action == WindowAction.CREATIVE_GRAB_MAX_STACK) {
            param = MagicValues.value(Integer.class, this.param);
        }
        else if (this.action == WindowAction.DROP_ITEM) {
            param = MagicValues.value(Integer.class, this.param) + ((this.slot != -999) ? 2 : 0);
        }
        else if (this.action == WindowAction.SPREAD_ITEM) {
            param = MagicValues.value(Integer.class, this.param);
        }
        else if (this.action == WindowAction.FILL_STACK) {
            param = MagicValues.value(Integer.class, this.param);
        }
        out.writeByte(param);
        out.writeShort(this.actionId);
        out.writeByte(MagicValues.value(Integer.class, this.action));
        NetUtil.writeItem(out, this.clicked);
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
