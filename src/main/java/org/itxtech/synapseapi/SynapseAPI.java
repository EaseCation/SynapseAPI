package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.BatchPacketsEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItemPaletteInterface;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.GlobalBlockPaletteInterface;
import cn.nukkit.level.GlobalBlockPaletteInterface.StaticVersion;
import cn.nukkit.level.format.generic.ChunkRequestTask;
import cn.nukkit.network.RakNetInterface;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.itxtech.synapseapi.camera.CameraManager;
import org.itxtech.synapseapi.command.*;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.ConfigSection;
import org.itxtech.synapseapi.messaging.Messenger;
import org.itxtech.synapseapi.messaging.StandardMessenger;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AvailableEntityIdentifiersPalette;
import org.itxtech.synapseapi.multiprotocol.utils.BiomeDefinitions;
import org.itxtech.synapseapi.multiprotocol.utils.CraftingPacketManager;
import org.itxtech.synapseapi.multiprotocol.utils.CreativeItemsPalette;
import org.itxtech.synapseapi.multiprotocol.utils.EntityPropertiesPalette;
import org.itxtech.synapseapi.multiprotocol.utils.ItemComponentDefinitions;
import org.itxtech.synapseapi.multiprotocol.utils.item.CraftingManagerLegacy;
import org.itxtech.synapseapi.multiprotocol.utils.item.CraftingManagerMedieval;
import org.itxtech.synapseapi.multiprotocol.utils.item.CraftingManagerNew;
import org.itxtech.synapseapi.utils.ClientData;
import org.itxtech.synapseapi.utils.NetTest;
import org.itxtech.synapseapi.utils.SynapsePlayerViolationListener;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static cn.nukkit.GameVersion.*;
import static org.itxtech.synapseapi.SynapseSharedConstants.*;

/**
 * @author boybook
 */
public class SynapseAPI extends PluginBase implements Listener {

    public static boolean enable = true;
    private static SynapseAPI instance;
    private boolean autoConnect = true;
    private boolean loadingScreen = true;
    private boolean autoCompress = true;  //Compress in Nukkit, not Nemisys
    private boolean recordPacketStack = false;
    private final Map<String, SynapseEntry> synapseEntries = new Object2ObjectOpenHashMap<>();
    private Messenger messenger;
    private boolean networkBroadcastPlayerMove;
    private int blobCacheChunkSendPreTick;

    public static SynapseAPI getInstance() {
        return instance;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public boolean isNetworkBroadcastPlayerMove() {
        return networkBroadcastPlayerMove;
    }

    public int getBlobCacheChunkSendPreTick() {
        return blobCacheChunkSendPreTick;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Player.setViolationListener(new SynapsePlayerViolationListener());

        this.messenger = new StandardMessenger();
        loadEntries();

        this.getServer().getPluginManager().registerEvents(this, this);

        this.networkBroadcastPlayerMove = this.getConfig().getBoolean("network-broadcast-player-move", false);
        this.blobCacheChunkSendPreTick = this.getConfig().getInt("blob-cache-chunk-send-pre-tick", 0);
        this.recordPacketStack = this.getConfig().getBoolean("record-packet-stack", false);

        saveResource("recipes11.json", true);

        PacketRegister.init();

        GlobalBlockPalette.setInstance(new GlobalBlockPaletteInterface(){
            @Override
            public int getOrCreateRuntimeId0(int id, int meta) {
                return AdvancedGlobalBlockPalette.getOrCreateRuntimeId(AbstractProtocol.getValues()[AbstractProtocol.getValues().length - 1], false, id, meta);
            }

            @Override
            public int getOrCreateRuntimeId0(int legacyId) throws NoSuchElementException {
                return AdvancedGlobalBlockPalette.getOrCreateRuntimeId(AbstractProtocol.getValues()[AbstractProtocol.getValues().length - 1], false, legacyId);
            }

            @Override
            public int getOrCreateRuntimeIdGeneral0(int id, int meta) {
                return AdvancedGlobalBlockPalette.getOrCreateRuntimeId(AbstractProtocol.PROTOCOL_116_20, false, id, meta);
            }

            @Override
            public int getOrCreateRuntimeIdGeneral0(int legacyId) throws NoSuchElementException {
                return AdvancedGlobalBlockPalette.getOrCreateRuntimeId(AbstractProtocol.PROTOCOL_116_20, false, legacyId);
            }

            @Override
            public int getLegacyId0(int runtimeId) {
                return AdvancedGlobalBlockPalette.getLegacyId(AbstractProtocol.getValues()[AbstractProtocol.getValues().length - 1], false, runtimeId);
            }

            @Override
            public GlobalBlockPaletteInterface getStaticBlockPalette0(StaticVersion version) {
                return AdvancedGlobalBlockPalette.getStaticBlockPalette0(version);
            }
        });

        RuntimeItems.setInstance(new RuntimeItemPaletteInterface(){
            @Override
            public int getNetworkFullId0(Item item) {
                return AdvancedRuntimeItemPalette.getNetworkFullId(AbstractProtocol.getValues()[AbstractProtocol.getValues().length - 1], false, item);
            }

            @Override
            public int getLegacyFullId0(int networkId) {
                return AdvancedRuntimeItemPalette.getLegacyFullId(AbstractProtocol.getValues()[AbstractProtocol.getValues().length - 1], false, networkId);
            }

            @Override
            public int getId0(int fullId) {
                return AdvancedRuntimeItemPalette.getId(AbstractProtocol.getValues()[AbstractProtocol.getValues().length - 1], false, fullId);
            }

            @Override
            public int getData0(int fullId) {
                return AdvancedRuntimeItemPalette.getData(AbstractProtocol.getValues()[AbstractProtocol.getValues().length - 1], false, fullId);
            }

            @Override
            public int getNetworkId0(int networkFullId) {
                return AdvancedRuntimeItemPalette.getNetworkId(AbstractProtocol.getValues()[AbstractProtocol.getValues().length - 1], false, networkFullId);
            }

            @Override
            public boolean hasData0(int id) {
                return AdvancedRuntimeItemPalette.hasData(AbstractProtocol.getValues()[AbstractProtocol.getValues().length - 1], false, id);
            }
        });

        /*
        Map<String, int[]> data = new LinkedHashMap<>();

        Field[] fields = LevelSoundEventPacketV319.class.getDeclaredFields();
        LevelSoundEventPacketV319 pk = new LevelSoundEventPacketV319();
        for (Field field : fields) {
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (field.getName().startsWith("SOUND_")) {
                        if (!data.containsKey(field.getName())) data.put(field.getName(), new int[]{-1, -1, -1});
                        data.get(field.getName())[2] = (int) field.get(pk);
                    }
                }
            } catch (Exception e) {
                //ignore
            }
        }
        Field[] fields1 = LevelSoundEventPacket.class.getDeclaredFields();
        LevelSoundEventPacket pk1 = new LevelSoundEventPacket();
        for (Field field : fields1) {
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (field.getName().startsWith("SOUND_")) {
                        if (!data.containsKey(field.getName())) data.put(field.getName(), new int[]{-1, -1, -1});
                        data.get(field.getName())[0] = (int) field.get(pk1);
                    }
                }
            } catch (Exception e) {
                //ignore
            }
        }

        Field[] fields2 = LevelSoundEventIDTranslator.class.getDeclaredFields();
        LevelSoundEventIDTranslator pk2 = new LevelSoundEventIDTranslator();
        for (Field field : fields2) {
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (field.getName().startsWith("SOUND_")) {
                        if (!data.containsKey(field.getName())) data.put(field.getName(), new int[]{-1, -1, -1});
                        data.get(field.getName())[1] = (int) field.get(pk2);
                    }
                }
            } catch (Exception e) {
                //ignore
            }
        }

        data.forEach((n, v) -> {
            System.out.println(n + "(" + v[0] + ", " +  v[1] + ", " + v[2] + "),");
        });*/
        this.getMessenger().registerIncomingPluginChannel(this, "nettest", new NetTest(this));
        this.getMessenger().registerOutgoingPluginChannel(this, "nettest");

        AdvancedGlobalBlockPalette.init();
        AdvancedRuntimeItemPalette.init();
        CreativeItemsPalette.init();
        AvailableEntityIdentifiersPalette.init();
        BiomeDefinitions.init();
        ItemComponentDefinitions.init();
        EntityPropertiesPalette.init();
        CameraManager.init();

        getServer().setCraftingManager(V1_19_0.isAvailable() ? new CraftingManagerNew()
                : V1_18_0.isAvailable() ? new CraftingManagerMedieval()
                : new CraftingManagerLegacy());
        CraftingPacketManager.rebuildPacket();

        ChunkRequestTask.addPreloadVersion(StaticVersion.V1_20_10_NETEASE);
        ChunkRequestTask.addPreloadVersion(StaticVersion.V1_20_50_NETEASE);

        //仅用于开发测试
/*
        this.getServer().getCommandMap().register("dcpk", new Command("dcpk") {
            {
                this.commandData.flags.add(CommandFlag.TEST);
                this.commandData.flags.add(CommandFlag.HIDDEN_FROM_COMMAND_BLOCK_ORIGIN);
                this.commandData.flags.add(CommandFlag.HIDDEN_FROM_PLAYER_ORIGIN);
                this.commandData.flags.add(CommandFlag.DISALLOWED);
                this.commandData.flags.add(CommandFlag.NOT_CHEAT);
                this.setPermission("console.command.dcpk");
            }

            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {

                final int protocol = 422;
                final boolean netease = true;

                if (sender instanceof ConsoleCommandSender) {
                    String data = String.join("", args);
                    byte[] bytes = Utils.parseHexBinary(data);
                    DataPacket packet = PacketRegister.getFullPacket(bytes, protocol, true);
                    if (packet != null) {
                        getLogger().info(packet.toString());
                        if (packet.pid() == ProtocolInfo.BATCH_PACKET) {
                            List<DataPacket> packets = SynapseEntry.processBatch((BatchPacket) packet, protocol, netease);
                            if (packets == null) {
                                getLogger().warn("Decoding batch failed!");
                            } else {
                                for (DataPacket pk : packets) {
                                    getLogger().info("Packet " + Binary.bytesToHexString(new byte[]{(byte) pk.pid()}));
                                }
                            }
                        }
                    } else {
                        getLogger().warn("Decode failed!");
                    }
                    return true;
                }
                return false;
            }
        });
*/
        if (NETWORK_STACK_LATENCY_TELEMETRY) {
            this.getServer().getCommandMap().register("synapse", new LatencyCommand(this));
        }
        this.getServer().getCommandMap().register("synapse", new ChunkNetVerCommand(this));
        this.getServer().getCommandMap().register("synapse", new NetDataCommand(this));
        this.getServer().getCommandMap().register("synapse", new DebugExportCommand(this));
        this.getServer().getCommandMap().register("synapse", new CommunityToolCommand(this));
    }

    public boolean isUseLoadingScreen() {
        return loadingScreen;
    }

    public boolean isAutoCompress() {
        return autoCompress;
    }

    public Map<String, SynapseEntry> getSynapseEntries() {
        return synapseEntries;
    }

    public void addSynapseAPI(SynapseEntry entry) {
        this.synapseEntries.put(entry.getHash(), entry);
    }

    public SynapseEntry getSynapseEntry(String hash) {
        return this.synapseEntries.get(hash);
    }

    public boolean isRecordPacketStack() {
        return recordPacketStack;
    }

    public void shutdownAll() {
        for (SynapseEntry entry : new ArrayList<>(this.synapseEntries.values())) {
            entry.shutdown();
        }
    }

    @Override
    public void onDisable() {
        this.shutdownAll();
    }

    public static DataPacket getPacket(byte[] buffer) {
        byte pid = buffer[0];

        byte start = 3;
        DataPacket data;
        data = Server.getInstance().getNetwork().getPacket(pid);

        if (data == null) {
            Server.getInstance().getLogger().info("C => S 未找到匹配数据包: " + pid);
            return null;
        }
        data.setBuffer(buffer, start);
        return data;
    }

    private void loadEntries() {
        this.saveDefaultConfig();
        enable = this.getConfig().getBoolean("enable", true);
        this.autoCompress = this.getConfig().getBoolean("autoCompress", true);
        if (!enable) {
            this.getLogger().warning("The SynapseAPI is not be enabled!");
            this.setEnabled(false);
        } else {
            if (this.getConfig().getBoolean("disable-rak")) {
                for (SourceInterface sourceInterface : this.getServer().getNetwork().getInterfaces()) {
                    if (sourceInterface instanceof RakNetInterface) {
                        sourceInterface.shutdown();
                    }
                }
            }

            List entries = this.getConfig().getList("entries");

            for (Object entry : entries) {
                @SuppressWarnings("unchecked")
                ConfigSection section = new ConfigSection((LinkedHashMap) entry);
                String serverIp = section.getString("server-ip", "127.0.0.1");
                int port = section.getInt("server-port", 10305);
                boolean isMainServer = section.getBoolean("isMainServer");
                String password = section.getString("password");
                String serverDescription = section.getString("description");
                this.loadingScreen = section.getBoolean("loadingScreen", true);
                this.autoConnect = section.getBoolean("autoConnect", true);
                if (this.autoConnect) {
                    this.addSynapseAPI(new SynapseEntry(this, serverIp, port, isMainServer, password, serverDescription));
                }
            }

        }
    }

    public Messenger getMessenger() {
        return messenger;
    }

    @EventHandler
    public void onBatchPackets(BatchPacketsEvent e) {
        e.setCancelled();
       /* Set<DataPacket> sortedPackets = new HashSet<>();
        Set<DataPacket> sortedPackets11 = new HashSet<>();*/

        DataPacket[] packets = e.getPackets();
        Player[] players = e.getPlayers();
        HashMap<SynapseEntry, List<SynapsePlayer>> map = new HashMap<>();

        for(Player p : players) {
            SynapsePlayer player = (SynapsePlayer) p;

            SynapseEntry entry = player.getSynapseEntry();
            if (entry == null) continue;
            List<SynapsePlayer> list = map.get(entry);
            if(list == null) {
                list = new ArrayList<>();
            }

            list.add(player);
            map.put(entry, list);
        }

        for (Entry<SynapseEntry, List<SynapsePlayer>> entry : map.entrySet()) {
            entry.getKey().getSynapseInterface().getPutPacketThread().addMainToThreadBroadcast(entry.getValue().toArray(new SynapsePlayer[0]), packets);
        }

        /*for(DataPacket pk : packets) {
            if(pk instanceof Packet11) {
                sortedPackets11.add(pk);
                sortedPackets.add(((Packet11) pk).toDefault());
            } else {
                sortedPackets.add(pk);
                DataPacket compatible = PacketRegister.getCompatiblePacket(pk, 113, true);

                if(compatible != null) {
                    sortedPackets11.add(compatible);
                }
            }
        }*/
    }

    public List<ClientData.Entry> getAllClientDataEntries() {
        List<ClientData.Entry> list = new ArrayList<>();
        this.getSynapseEntries().values().forEach(entry -> {
            if (entry.getClientData() != null) {
                entry.getClientData().clientList.values().forEach(e -> {
                    if (!list.contains(e)) list.add(e);
                });
            }
        });
        return list;
    }

    public List<ClientData.Entry> collectClientDataEntriesNotInList(Collection<ClientData.Entry> find, Collection<ClientData.Entry> all) {
        List<ClientData.Entry> list = new ArrayList<>();
        for (ClientData.Entry entry : all) {
            if (!find.contains(entry)) list.add(entry);
        }
        return list;
    }

    public List<SynapseEntry> getUnusualEntries() {
        return this.getSynapseEntries().values().stream().filter(e -> !e.isVerified() || e.getClientData() == null || e.getClientData().clientList == null).collect(Collectors.toList());
    }
/*
    @EventHandler
    public void onNEPlayerModEventS2S(NetEasePlayerModEventC2SEvent event) {
        // 实现中国版Emote适配
        if ("Minecraft".equals(event.getModName())) {
            if (event.getSystemName().equals("emote") && event.getCustomEventName().equals("PlayEmoteEvent")) {
                JsonObject args = event.getArgs();
                String emote = args.get("animName").getAsString();

                if (emote.length() > 100) {
                    event.getPlayer().violation += 60;
                    return;
                }
                if (!event.getPlayer().emoteRequest()) {
                    event.getPlayer().violation += 10;
                    return;
                }

                event.getPlayer().getViewers().values().forEach(v -> {
                    v.playAnimation(emote, event.getPlayer().getId());
                });
                event.getPlayer().playAnimation(emote, event.getPlayer().getId());
            }
        }
    }
*/
}
