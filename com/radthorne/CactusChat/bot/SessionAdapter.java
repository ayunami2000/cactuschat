// 
// Decompiled by Procyon v0.5.36
// 

package com.radthorne.CactusChat.bot;

import org.spacehq.mc.protocol.data.message.ChatColor;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.spacehq.mc.protocol.data.message.ChatFormat;
import com.radthorne.CactusChat.msg.AnsiColour;
import org.spacehq.packetlib.event.session.DisconnectingEvent;
import org.spacehq.packetlib.event.session.ConnectedEvent;
import org.spacehq.packetlib.event.session.PacketSentEvent;
import org.spacehq.mc.protocol.data.message.Message;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import com.radthorne.CactusChat.Main;
import org.spacehq.packetlib.packet.Packet;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerRespawnPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionListener;

public class SessionAdapter implements SessionListener
{
    private IngameBot bot;
    
    public SessionAdapter(final IngameBot bot) {
        this.bot = bot;
    }
    
    public void packetReceived(final PacketReceivedEvent event) {
        if (event.getPacket() instanceof ServerPlayerListEntryPacket) {
            return;
        }
        if (event.getPacket() instanceof ServerChatPacket) {
            final ServerChatPacket packet = event.getPacket();
            if (packet.getMessage() != null) {
                this.handleChat(packet.getMessage());
            }
        }
        else if (event.getPacket() instanceof ServerJoinGamePacket) {
            final ServerJoinGamePacket packet2 = event.getPacket();
            this.bot.setEntityId(packet2.getEntityId());
        }
        else if (event.getPacket() instanceof ServerRespawnPacket) {
            final ServerRespawnPacket serverRespawnPacket = event.getPacket();
        }
        else if (event.getPacket() instanceof ServerPlayerPositionRotationPacket) {
            final ServerPlayerPositionRotationPacket packet3 = event.getPacket();
            this.bot.setX(packet3.getX());
            this.bot.setY(packet3.getY());
            this.bot.setZ(packet3.getZ());
            this.bot.setYaw(packet3.getYaw());
            this.bot.setPitch(packet3.getPitch());
            event.getSession().send(new ClientPlayerPositionRotationPacket(true, this.bot.getX(), this.bot.getY(), this.bot.getZ(), this.bot.getYaw(), this.bot.getPitch()));
            Main.debug("updated position");
        }
    }
    
    public void disconnected(final DisconnectedEvent event) {
        System.out.println("Disconnected: " + Message.fromString(event.getReason()).getFullText());
        Main.reconnect();
    }
    
    public void packetSent(final PacketSentEvent packetSentEvent) {
    }
    
    public void connected(final ConnectedEvent connectedEvent) {
    }
    
    public void disconnecting(final DisconnectingEvent disconnectingEvent) {
        if (disconnectingEvent.getCause() != null) {
            disconnectingEvent.getCause().printStackTrace();
        }
    }
    
    private void handleChat(final Message mes) {
        if (mes == null) {
            return;
        }
        String message = "";
        final List<Message> subMessages = mes.getExtra();
        for (final Message m : subMessages) {
            if (m != null) {
                if (m.toJson() == null) {
                    continue;
                }
                if (m.getStyle().getColor() != null) {
                    final ChatColor colour = m.getStyle().getColor();
                    message += AnsiColour.getColourCode(colour.toString());
                }
                if (m.getStyle().getFormats().size() > 0) {
                    for (final ChatFormat format : m.getStyle().getFormats()) {
                        message += AnsiColour.getColourCode(format.toString());
                    }
                }
                message += m.getText();
                message += "§r";
            }
        }
        final String time = new SimpleDateFormat("[HH:mm:ss] ").format(new Date());
        message = StringEscapeUtils.unescapeJava(message);
        Main.println(time + message, true);
    }
}
