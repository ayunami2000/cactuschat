// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol;

import org.spacehq.mc.protocol.packet.status.client.StatusPingPacket;
import org.spacehq.mc.protocol.packet.status.client.StatusQueryPacket;
import org.spacehq.mc.protocol.packet.status.server.StatusPongPacket;
import org.spacehq.mc.protocol.packet.status.server.StatusResponsePacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerUseItemPacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPlaceBlockPacket;
import org.spacehq.mc.protocol.packet.ingame.client.world.ClientSpectatePacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerSwingArmPacket;
import org.spacehq.mc.protocol.packet.ingame.client.world.ClientUpdateSignPacket;
import org.spacehq.mc.protocol.packet.ingame.client.window.ClientCreativeInventoryActionPacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerChangeHeldItemPacket;
import org.spacehq.mc.protocol.packet.ingame.client.ClientResourcePackStatusPacket;
import org.spacehq.mc.protocol.packet.ingame.client.world.ClientSteerVehiclePacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerStatePacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerAbilitiesPacket;
import org.spacehq.mc.protocol.packet.ingame.client.world.ClientSteerBoatPacket;
import org.spacehq.mc.protocol.packet.ingame.client.world.ClientVehicleMovePacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerMovementPacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import org.spacehq.mc.protocol.packet.ingame.client.ClientKeepAlivePacket;
import org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerInteractEntityPacket;
import org.spacehq.mc.protocol.packet.ingame.client.ClientPluginMessagePacket;
import org.spacehq.mc.protocol.packet.ingame.client.window.ClientCloseWindowPacket;
import org.spacehq.mc.protocol.packet.ingame.client.window.ClientWindowActionPacket;
import org.spacehq.mc.protocol.packet.ingame.client.window.ClientEnchantItemPacket;
import org.spacehq.mc.protocol.packet.ingame.client.window.ClientConfirmTransactionPacket;
import org.spacehq.mc.protocol.packet.ingame.client.ClientSettingsPacket;
import org.spacehq.mc.protocol.packet.ingame.client.ClientRequestPacket;
import org.spacehq.mc.protocol.packet.ingame.client.ClientChatPacket;
import org.spacehq.mc.protocol.packet.ingame.client.ClientTabCompletePacket;
import org.spacehq.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityEffectPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityPropertiesPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityCollectItemPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerPlayerListDataPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerPlayBuiltinSoundPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerTitlePacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerUpdateTimePacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerSpawnPositionPacket;
import org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerUpdateScorePacket;
import org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerTeamPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntitySetPassengersPacket;
import org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerScoreboardObjectivePacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerSetExperiencePacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityEquipmentPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityVelocityPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityAttachPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityMetadataPacket;
import org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerDisplayScoreboardPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerChangeHeldItemPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerSwitchCameraPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerWorldBorderPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerRespawnPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerResourcePackSendPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityRemoveEffectPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityDestroyPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerUseBedPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerCombatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerOpenTileEntityEditorPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerVehicleMovePacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityMovementPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityPositionRotationPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityPositionPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMapDataPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerSpawnParticlePacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerPlayEffectPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerKeepAlivePacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerNotifyClientPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerUnloadChunkPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerExplosionPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityStatusPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerDisconnectPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerPlaySoundPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerPluginMessagePacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerSetCooldownPacket;
import org.spacehq.mc.protocol.packet.ingame.server.window.ServerSetSlotPacket;
import org.spacehq.mc.protocol.packet.ingame.server.window.ServerWindowPropertyPacket;
import org.spacehq.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import org.spacehq.mc.protocol.packet.ingame.server.window.ServerOpenWindowPacket;
import org.spacehq.mc.protocol.packet.ingame.server.window.ServerCloseWindowPacket;
import org.spacehq.mc.protocol.packet.ingame.server.window.ServerConfirmTransactionPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiBlockChangePacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerChatPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerTabCompletePacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerDifficultyPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerBossBarPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockValuePacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerUpdateTileEntityPacket;
import org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockBreakAnimPacket;
import org.spacehq.mc.protocol.packet.ingame.server.ServerStatisticsPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityAnimationPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPaintingPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnGlobalEntityPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnExpOrbPacket;
import org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket;
import org.spacehq.mc.protocol.packet.login.client.EncryptionResponsePacket;
import org.spacehq.mc.protocol.packet.login.client.LoginStartPacket;
import org.spacehq.mc.protocol.packet.login.server.LoginSetCompressionPacket;
import org.spacehq.mc.protocol.packet.login.server.LoginSuccessPacket;
import org.spacehq.mc.protocol.packet.login.server.EncryptionRequestPacket;
import org.spacehq.mc.protocol.packet.login.server.LoginDisconnectPacket;
import org.spacehq.packetlib.packet.Packet;
import org.spacehq.mc.protocol.packet.handshake.client.HandshakePacket;
import java.security.GeneralSecurityException;
import java.security.Key;
import org.spacehq.packetlib.Server;
import org.spacehq.packetlib.event.session.SessionListener;
import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.crypt.PacketEncryption;
import org.spacehq.mc.auth.service.AuthenticationService;
import java.net.Proxy;
import org.spacehq.mc.auth.exception.request.RequestException;
import java.util.UUID;
import org.spacehq.packetlib.packet.DefaultPacketHeader;
import org.spacehq.mc.auth.data.GameProfile;
import org.spacehq.packetlib.crypt.AESEncryption;
import org.spacehq.packetlib.packet.PacketHeader;
import org.spacehq.mc.protocol.data.SubProtocol;
import org.spacehq.packetlib.packet.PacketProtocol;

public class MinecraftProtocol extends PacketProtocol
{
    private SubProtocol subProtocol;
    private PacketHeader header;
    private AESEncryption encrypt;
    private GameProfile profile;
    private String accessToken;
    
    private MinecraftProtocol() {
        this.subProtocol = SubProtocol.HANDSHAKE;
        this.header = new DefaultPacketHeader();
        this.accessToken = "";
    }
    
    public MinecraftProtocol(final SubProtocol subProtocol) {
        this.subProtocol = SubProtocol.HANDSHAKE;
        this.header = new DefaultPacketHeader();
        this.accessToken = "";
        if (subProtocol != SubProtocol.LOGIN && subProtocol != SubProtocol.STATUS) {
            throw new IllegalArgumentException("Only login and status modes are permitted.");
        }
        if ((this.subProtocol = subProtocol) == SubProtocol.LOGIN) {
            this.profile = new GameProfile((UUID)null, "Player");
        }
    }
    
    public MinecraftProtocol(final String username) {
        this(SubProtocol.LOGIN);
        this.profile = new GameProfile((UUID)null, username);
    }
    
    public MinecraftProtocol(final String username, final String password) throws RequestException {
        this(username, password, false);
    }
    
    public MinecraftProtocol(final String username, final String using, final boolean token) throws RequestException {
        this(username, using, token, Proxy.NO_PROXY);
    }
    
    public MinecraftProtocol(final String username, final String using, final boolean token, final Proxy authProxy) throws RequestException {
        this(SubProtocol.LOGIN);
        final String clientToken = UUID.randomUUID().toString();
        final AuthenticationService auth = new AuthenticationService(clientToken, authProxy);
        auth.setUsername(username);
        if (token) {
            auth.setAccessToken(using);
        }
        else {
            auth.setPassword(using);
        }
        auth.login();
        this.profile = auth.getSelectedProfile();
        this.accessToken = auth.getAccessToken();
    }
    
    public MinecraftProtocol(final GameProfile profile, final String accessToken) {
        this(SubProtocol.LOGIN);
        this.profile = profile;
        this.accessToken = accessToken;
    }
    
    public GameProfile getProfile() {
        return this.profile;
    }
    
    public String getAccessToken() {
        return this.accessToken;
    }
    
    @Override
    public String getSRVRecordPrefix() {
        return "_minecraft";
    }
    
    @Override
    public PacketHeader getPacketHeader() {
        return this.header;
    }
    
    @Override
    public PacketEncryption getEncryption() {
        return this.encrypt;
    }
    
    @Override
    public void newClientSession(final Client client, final Session session) {
        if (this.profile != null) {
            session.setFlag("profile", this.profile);
            session.setFlag("access-token", this.accessToken);
        }
        this.setSubProtocol(this.subProtocol, true, session);
        session.addListener(new ClientListener());
    }
    
    @Override
    public void newServerSession(final Server server, final Session session) {
        this.setSubProtocol(SubProtocol.HANDSHAKE, false, session);
        session.addListener(new ServerListener());
    }
    
    protected void enableEncryption(final Key key) {
        try {
            this.encrypt = new AESEncryption(key);
        }
        catch (GeneralSecurityException e) {
            throw new Error("Failed to enable protocol encryption.", e);
        }
    }
    
    public SubProtocol getSubProtocol() {
        return this.subProtocol;
    }
    
    protected void setSubProtocol(final SubProtocol subProtocol, final boolean client, final Session session) {
        this.clearPackets();
        switch (subProtocol) {
            case HANDSHAKE: {
                if (client) {
                    this.initClientHandshake(session);
                    break;
                }
                this.initServerHandshake(session);
                break;
            }
            case LOGIN: {
                if (client) {
                    this.initClientLogin(session);
                    break;
                }
                this.initServerLogin(session);
                break;
            }
            case GAME: {
                if (client) {
                    this.initClientGame(session);
                    break;
                }
                this.initServerGame(session);
                break;
            }
            case STATUS: {
                if (client) {
                    this.initClientStatus(session);
                    break;
                }
                this.initServerStatus(session);
                break;
            }
        }
        this.subProtocol = subProtocol;
    }
    
    private void initClientHandshake(final Session session) {
        this.registerOutgoing(0, HandshakePacket.class);
    }
    
    private void initServerHandshake(final Session session) {
        this.registerIncoming(0, HandshakePacket.class);
    }
    
    private void initClientLogin(final Session session) {
        this.registerIncoming(0, LoginDisconnectPacket.class);
        this.registerIncoming(1, EncryptionRequestPacket.class);
        this.registerIncoming(2, LoginSuccessPacket.class);
        this.registerIncoming(3, LoginSetCompressionPacket.class);
        this.registerOutgoing(0, LoginStartPacket.class);
        this.registerOutgoing(1, EncryptionResponsePacket.class);
    }
    
    private void initServerLogin(final Session session) {
        this.registerIncoming(0, LoginStartPacket.class);
        this.registerIncoming(1, EncryptionResponsePacket.class);
        this.registerOutgoing(0, LoginDisconnectPacket.class);
        this.registerOutgoing(1, EncryptionRequestPacket.class);
        this.registerOutgoing(2, LoginSuccessPacket.class);
        this.registerOutgoing(3, LoginSetCompressionPacket.class);
    }
    
    private void initClientGame(final Session session) {
        this.registerIncoming(0, ServerSpawnObjectPacket.class);
        this.registerIncoming(1, ServerSpawnExpOrbPacket.class);
        this.registerIncoming(2, ServerSpawnGlobalEntityPacket.class);
        this.registerIncoming(3, ServerSpawnMobPacket.class);
        this.registerIncoming(4, ServerSpawnPaintingPacket.class);
        this.registerIncoming(5, ServerSpawnPlayerPacket.class);
        this.registerIncoming(6, ServerEntityAnimationPacket.class);
        this.registerIncoming(7, ServerStatisticsPacket.class);
        this.registerIncoming(8, ServerBlockBreakAnimPacket.class);
        this.registerIncoming(9, ServerUpdateTileEntityPacket.class);
        this.registerIncoming(10, ServerBlockValuePacket.class);
        this.registerIncoming(11, ServerBlockChangePacket.class);
        this.registerIncoming(12, ServerBossBarPacket.class);
        this.registerIncoming(13, ServerDifficultyPacket.class);
        this.registerIncoming(14, ServerTabCompletePacket.class);
        this.registerIncoming(15, ServerChatPacket.class);
        this.registerIncoming(16, ServerMultiBlockChangePacket.class);
        this.registerIncoming(17, ServerConfirmTransactionPacket.class);
        this.registerIncoming(18, ServerCloseWindowPacket.class);
        this.registerIncoming(19, ServerOpenWindowPacket.class);
        this.registerIncoming(20, ServerWindowItemsPacket.class);
        this.registerIncoming(21, ServerWindowPropertyPacket.class);
        this.registerIncoming(22, ServerSetSlotPacket.class);
        this.registerIncoming(23, ServerSetCooldownPacket.class);
        this.registerIncoming(24, ServerPluginMessagePacket.class);
        this.registerIncoming(25, ServerPlaySoundPacket.class);
        this.registerIncoming(26, ServerDisconnectPacket.class);
        this.registerIncoming(27, ServerEntityStatusPacket.class);
        this.registerIncoming(28, ServerExplosionPacket.class);
        this.registerIncoming(29, ServerUnloadChunkPacket.class);
        this.registerIncoming(30, ServerNotifyClientPacket.class);
        this.registerIncoming(31, ServerKeepAlivePacket.class);
        this.registerIncoming(32, ServerChunkDataPacket.class);
        this.registerIncoming(33, ServerPlayEffectPacket.class);
        this.registerIncoming(34, ServerSpawnParticlePacket.class);
        this.registerIncoming(35, ServerJoinGamePacket.class);
        this.registerIncoming(36, ServerMapDataPacket.class);
        this.registerIncoming(37, ServerEntityPositionPacket.class);
        this.registerIncoming(38, ServerEntityPositionRotationPacket.class);
        this.registerIncoming(39, ServerEntityRotationPacket.class);
        this.registerIncoming(40, ServerEntityMovementPacket.class);
        this.registerIncoming(41, ServerVehicleMovePacket.class);
        this.registerIncoming(42, ServerOpenTileEntityEditorPacket.class);
        this.registerIncoming(43, ServerPlayerAbilitiesPacket.class);
        this.registerIncoming(44, ServerCombatPacket.class);
        this.registerIncoming(45, ServerPlayerListEntryPacket.class);
        this.registerIncoming(46, ServerPlayerPositionRotationPacket.class);
        this.registerIncoming(47, ServerPlayerUseBedPacket.class);
        this.registerIncoming(48, ServerEntityDestroyPacket.class);
        this.registerIncoming(49, ServerEntityRemoveEffectPacket.class);
        this.registerIncoming(50, ServerResourcePackSendPacket.class);
        this.registerIncoming(51, ServerRespawnPacket.class);
        this.registerIncoming(52, ServerEntityHeadLookPacket.class);
        this.registerIncoming(53, ServerWorldBorderPacket.class);
        this.registerIncoming(54, ServerSwitchCameraPacket.class);
        this.registerIncoming(55, ServerPlayerChangeHeldItemPacket.class);
        this.registerIncoming(56, ServerDisplayScoreboardPacket.class);
        this.registerIncoming(57, ServerEntityMetadataPacket.class);
        this.registerIncoming(58, ServerEntityAttachPacket.class);
        this.registerIncoming(59, ServerEntityVelocityPacket.class);
        this.registerIncoming(60, ServerEntityEquipmentPacket.class);
        this.registerIncoming(61, ServerPlayerSetExperiencePacket.class);
        this.registerIncoming(62, ServerPlayerHealthPacket.class);
        this.registerIncoming(63, ServerScoreboardObjectivePacket.class);
        this.registerIncoming(64, ServerEntitySetPassengersPacket.class);
        this.registerIncoming(65, ServerTeamPacket.class);
        this.registerIncoming(66, ServerUpdateScorePacket.class);
        this.registerIncoming(67, ServerSpawnPositionPacket.class);
        this.registerIncoming(68, ServerUpdateTimePacket.class);
        this.registerIncoming(69, ServerTitlePacket.class);
        this.registerIncoming(70, ServerPlayBuiltinSoundPacket.class);
        this.registerIncoming(71, ServerPlayerListDataPacket.class);
        this.registerIncoming(72, ServerEntityCollectItemPacket.class);
        this.registerIncoming(73, ServerEntityTeleportPacket.class);
        this.registerIncoming(74, ServerEntityPropertiesPacket.class);
        this.registerIncoming(75, ServerEntityEffectPacket.class);
        this.registerOutgoing(0, ClientTeleportConfirmPacket.class);
        this.registerOutgoing(1, ClientTabCompletePacket.class);
        this.registerOutgoing(2, ClientChatPacket.class);
        this.registerOutgoing(3, ClientRequestPacket.class);
        this.registerOutgoing(4, ClientSettingsPacket.class);
        this.registerOutgoing(5, ClientConfirmTransactionPacket.class);
        this.registerOutgoing(6, ClientEnchantItemPacket.class);
        this.registerOutgoing(7, ClientWindowActionPacket.class);
        this.registerOutgoing(8, ClientCloseWindowPacket.class);
        this.registerOutgoing(9, ClientPluginMessagePacket.class);
        this.registerOutgoing(10, ClientPlayerInteractEntityPacket.class);
        this.registerOutgoing(11, ClientKeepAlivePacket.class);
        this.registerOutgoing(12, ClientPlayerPositionPacket.class);
        this.registerOutgoing(13, ClientPlayerPositionRotationPacket.class);
        this.registerOutgoing(14, ClientPlayerRotationPacket.class);
        this.registerOutgoing(15, ClientPlayerMovementPacket.class);
        this.registerOutgoing(16, ClientVehicleMovePacket.class);
        this.registerOutgoing(17, ClientSteerBoatPacket.class);
        this.registerOutgoing(18, ClientPlayerAbilitiesPacket.class);
        this.registerOutgoing(19, ClientPlayerActionPacket.class);
        this.registerOutgoing(20, ClientPlayerStatePacket.class);
        this.registerOutgoing(21, ClientSteerVehiclePacket.class);
        this.registerOutgoing(22, ClientResourcePackStatusPacket.class);
        this.registerOutgoing(23, ClientPlayerChangeHeldItemPacket.class);
        this.registerOutgoing(24, ClientCreativeInventoryActionPacket.class);
        this.registerOutgoing(25, ClientUpdateSignPacket.class);
        this.registerOutgoing(26, ClientPlayerSwingArmPacket.class);
        this.registerOutgoing(27, ClientSpectatePacket.class);
        this.registerOutgoing(28, ClientPlayerPlaceBlockPacket.class);
        this.registerOutgoing(29, ClientPlayerUseItemPacket.class);
    }
    
    private void initServerGame(final Session session) {
        this.registerIncoming(0, ClientTeleportConfirmPacket.class);
        this.registerIncoming(1, ClientTabCompletePacket.class);
        this.registerIncoming(2, ClientChatPacket.class);
        this.registerIncoming(3, ClientRequestPacket.class);
        this.registerIncoming(4, ClientSettingsPacket.class);
        this.registerIncoming(5, ClientConfirmTransactionPacket.class);
        this.registerIncoming(6, ClientEnchantItemPacket.class);
        this.registerIncoming(7, ClientWindowActionPacket.class);
        this.registerIncoming(8, ClientCloseWindowPacket.class);
        this.registerIncoming(9, ClientPluginMessagePacket.class);
        this.registerIncoming(10, ClientPlayerInteractEntityPacket.class);
        this.registerIncoming(11, ClientKeepAlivePacket.class);
        this.registerIncoming(12, ClientPlayerPositionPacket.class);
        this.registerIncoming(13, ClientPlayerPositionRotationPacket.class);
        this.registerIncoming(14, ClientPlayerRotationPacket.class);
        this.registerIncoming(15, ClientPlayerMovementPacket.class);
        this.registerIncoming(16, ClientVehicleMovePacket.class);
        this.registerIncoming(17, ClientSteerBoatPacket.class);
        this.registerIncoming(18, ClientPlayerAbilitiesPacket.class);
        this.registerIncoming(19, ClientPlayerActionPacket.class);
        this.registerIncoming(20, ClientPlayerStatePacket.class);
        this.registerIncoming(21, ClientSteerVehiclePacket.class);
        this.registerIncoming(22, ClientResourcePackStatusPacket.class);
        this.registerIncoming(23, ClientPlayerChangeHeldItemPacket.class);
        this.registerIncoming(24, ClientCreativeInventoryActionPacket.class);
        this.registerIncoming(25, ClientUpdateSignPacket.class);
        this.registerIncoming(26, ClientPlayerSwingArmPacket.class);
        this.registerIncoming(27, ClientSpectatePacket.class);
        this.registerIncoming(28, ClientPlayerPlaceBlockPacket.class);
        this.registerIncoming(29, ClientPlayerUseItemPacket.class);
        this.registerOutgoing(0, ServerSpawnObjectPacket.class);
        this.registerOutgoing(1, ServerSpawnExpOrbPacket.class);
        this.registerOutgoing(2, ServerSpawnGlobalEntityPacket.class);
        this.registerOutgoing(3, ServerSpawnMobPacket.class);
        this.registerOutgoing(4, ServerSpawnPaintingPacket.class);
        this.registerOutgoing(5, ServerSpawnPlayerPacket.class);
        this.registerOutgoing(6, ServerEntityAnimationPacket.class);
        this.registerOutgoing(7, ServerStatisticsPacket.class);
        this.registerOutgoing(8, ServerBlockBreakAnimPacket.class);
        this.registerOutgoing(9, ServerUpdateTileEntityPacket.class);
        this.registerOutgoing(10, ServerBlockValuePacket.class);
        this.registerOutgoing(11, ServerBlockChangePacket.class);
        this.registerOutgoing(12, ServerBossBarPacket.class);
        this.registerOutgoing(13, ServerDifficultyPacket.class);
        this.registerOutgoing(14, ServerTabCompletePacket.class);
        this.registerOutgoing(15, ServerChatPacket.class);
        this.registerOutgoing(16, ServerMultiBlockChangePacket.class);
        this.registerOutgoing(17, ServerConfirmTransactionPacket.class);
        this.registerOutgoing(18, ServerCloseWindowPacket.class);
        this.registerOutgoing(19, ServerOpenWindowPacket.class);
        this.registerOutgoing(20, ServerWindowItemsPacket.class);
        this.registerOutgoing(21, ServerWindowPropertyPacket.class);
        this.registerOutgoing(22, ServerSetSlotPacket.class);
        this.registerOutgoing(23, ServerSetCooldownPacket.class);
        this.registerOutgoing(24, ServerPluginMessagePacket.class);
        this.registerOutgoing(25, ServerPlaySoundPacket.class);
        this.registerOutgoing(26, ServerDisconnectPacket.class);
        this.registerOutgoing(27, ServerEntityStatusPacket.class);
        this.registerOutgoing(28, ServerExplosionPacket.class);
        this.registerOutgoing(29, ServerUnloadChunkPacket.class);
        this.registerOutgoing(30, ServerNotifyClientPacket.class);
        this.registerOutgoing(31, ServerKeepAlivePacket.class);
        this.registerOutgoing(32, ServerChunkDataPacket.class);
        this.registerOutgoing(33, ServerPlayEffectPacket.class);
        this.registerOutgoing(34, ServerSpawnParticlePacket.class);
        this.registerOutgoing(35, ServerJoinGamePacket.class);
        this.registerOutgoing(36, ServerMapDataPacket.class);
        this.registerOutgoing(37, ServerEntityPositionPacket.class);
        this.registerOutgoing(38, ServerEntityPositionRotationPacket.class);
        this.registerOutgoing(39, ServerEntityRotationPacket.class);
        this.registerOutgoing(40, ServerEntityMovementPacket.class);
        this.registerOutgoing(41, ServerVehicleMovePacket.class);
        this.registerOutgoing(42, ServerOpenTileEntityEditorPacket.class);
        this.registerOutgoing(43, ServerPlayerAbilitiesPacket.class);
        this.registerOutgoing(44, ServerCombatPacket.class);
        this.registerOutgoing(45, ServerPlayerListEntryPacket.class);
        this.registerOutgoing(46, ServerPlayerPositionRotationPacket.class);
        this.registerOutgoing(47, ServerPlayerUseBedPacket.class);
        this.registerOutgoing(48, ServerEntityDestroyPacket.class);
        this.registerOutgoing(49, ServerEntityRemoveEffectPacket.class);
        this.registerOutgoing(50, ServerResourcePackSendPacket.class);
        this.registerOutgoing(51, ServerRespawnPacket.class);
        this.registerOutgoing(52, ServerEntityHeadLookPacket.class);
        this.registerOutgoing(53, ServerWorldBorderPacket.class);
        this.registerOutgoing(54, ServerSwitchCameraPacket.class);
        this.registerOutgoing(55, ServerPlayerChangeHeldItemPacket.class);
        this.registerOutgoing(56, ServerDisplayScoreboardPacket.class);
        this.registerOutgoing(57, ServerEntityMetadataPacket.class);
        this.registerOutgoing(58, ServerEntityAttachPacket.class);
        this.registerOutgoing(59, ServerEntityVelocityPacket.class);
        this.registerOutgoing(60, ServerEntityEquipmentPacket.class);
        this.registerOutgoing(61, ServerPlayerSetExperiencePacket.class);
        this.registerOutgoing(62, ServerPlayerHealthPacket.class);
        this.registerOutgoing(63, ServerScoreboardObjectivePacket.class);
        this.registerOutgoing(64, ServerEntitySetPassengersPacket.class);
        this.registerOutgoing(65, ServerTeamPacket.class);
        this.registerOutgoing(66, ServerUpdateScorePacket.class);
        this.registerOutgoing(67, ServerSpawnPositionPacket.class);
        this.registerOutgoing(68, ServerUpdateTimePacket.class);
        this.registerOutgoing(69, ServerTitlePacket.class);
        this.registerOutgoing(70, ServerPlayBuiltinSoundPacket.class);
        this.registerOutgoing(71, ServerPlayerListDataPacket.class);
        this.registerOutgoing(72, ServerEntityCollectItemPacket.class);
        this.registerOutgoing(73, ServerEntityTeleportPacket.class);
        this.registerOutgoing(74, ServerEntityPropertiesPacket.class);
        this.registerOutgoing(75, ServerEntityEffectPacket.class);
    }
    
    private void initClientStatus(final Session session) {
        this.registerIncoming(0, StatusResponsePacket.class);
        this.registerIncoming(1, StatusPongPacket.class);
        this.registerOutgoing(0, StatusQueryPacket.class);
        this.registerOutgoing(1, StatusPingPacket.class);
    }
    
    private void initServerStatus(final Session session) {
        this.registerIncoming(0, StatusQueryPacket.class);
        this.registerIncoming(1, StatusPingPacket.class);
        this.registerOutgoing(0, StatusResponsePacket.class);
        this.registerOutgoing(1, StatusPongPacket.class);
    }
}
