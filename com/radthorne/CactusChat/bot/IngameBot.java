// 
// Decompiled by Procyon v0.5.36
// 

package com.radthorne.CactusChat.bot;

import org.spacehq.packetlib.packet.Packet;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.radthorne.CactusChat.console.ReadThread;
import org.spacehq.packetlib.event.session.SessionListener;
import org.spacehq.mc.auth.exception.request.RequestException;
import java.io.IOException;
import com.radthorne.CactusChat.Main;
import com.radthorne.CactusChat.console.Console;
import com.radthorne.CactusChat.util.CactusChatClient;

public class IngameBot extends Bot
{
    private CactusChatClient conn;
    private Console console;
    private boolean loggedin;
    private String username;
    private int entityId;
    private double y;
    private double x;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;
    
    public IngameBot() {
        this.loggedin = false;
    }
    
    @Override
    public void start(final Console console, final String username, final String password, final String host, final int port) {
        this.username = username;
        this.console = console;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (IngameBot.this.loggedin) {
                    IngameBot.this.quit();
                }
            }
        });
        this.conn = new CactusChatClient();
        try {
            this.conn.login(username, password);
        }
        catch (RequestException e) {
            System.out.println(e.getMessage());
            System.out.println("Login error: Press any key to exit");
            try {
                Main.getReader().readCharacter();
                System.exit(-1);
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        this.conn.connect(host, port, new SessionAdapter(this));
        new ReadThread().start();
        Main.debug("sucessfully started");
    }
    
    @Override
    public void chat(final String message) {
        this.conn.getSession().send(new ClientChatPacket(message));
    }
    
    @Override
    public void error(final String message) {
    }
    
    @Override
    public void console(final String message) {
    }
    
    @Override
    public void raw(final String message) {
    }
    
    @Override
    public String getUsername() {
        return this.username;
    }
    
    public CactusChatClient getConn() {
        return this.conn;
    }
    
    @Override
    public void quit() {
        if (this.conn.isConnected()) {
            this.conn.disconnect("Quitting.");
        }
        this.console.stop();
    }
    
    public void setEntityId(final int entityId) {
        this.entityId = entityId;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public void setY(final double y) {
        this.y = y;
    }
    
    public double getY() {
        return this.y;
    }
    
    public void setX(final double x) {
        this.x = x;
    }
    
    public double getX() {
        return this.x;
    }
    
    public void setZ(final double z) {
        this.z = z;
    }
    
    public double getZ() {
        return this.z;
    }
    
    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }
    
    public float getYaw() {
        return this.yaw;
    }
    
    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }
    
    public float getPitch() {
        return this.pitch;
    }
    
    public void setOnGround(final boolean onGround) {
        this.onGround = onGround;
    }
    
    public boolean isOnGround() {
        return this.onGround;
    }
    
    public void lookEast() {
    }
}
