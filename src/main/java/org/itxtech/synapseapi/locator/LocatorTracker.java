package org.itxtech.synapseapi.locator;

import cn.nukkit.math.Vector2f;
import cn.nukkit.network.protocol.DataPacket;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12180.protocol.PlayerLocationPacket12180;
import org.itxtech.synapseapi.multiprotocol.protocol12610.protocol.LocatorBarPacket12610;
import org.itxtech.synapseapi.locator.Waypoint.DimensionLocation;
import org.itxtech.synapseapi.locator.Waypoint.UpdateFlag;
import org.itxtech.synapseapi.multiprotocol.protocol12620.protocol.LocatorBarPacket12620;

import java.awt.Color;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

//TODO: add customizable api
public final class LocatorTracker {
    private static final Vector2f DEFAULT_ICON_SIZE = new Vector2f(1, 1);

    private final Predicate<DataPacket> sender;
    private final Long2ObjectMap<Locator> locators = new Long2ObjectOpenHashMap<>();
    private final LongSet pendingRemovals = new LongOpenHashSet();

    public LocatorTracker(Predicate<DataPacket> sender) {
        this.sender = sender;
    }

    public void update(int protocol, long entityUniqueId, float x, float y, float z, int dimension) {
        if (protocol < AbstractProtocol.PROTOCOL_121_80.getProtocolStart()) {
            return;
        }

        Locator locator = locators.get(entityUniqueId);
        pendingRemovals.remove(entityUniqueId);
        if (locator != null && locator.dimension == dimension && locator.x == x && locator.y == y && locator.z == z) {
            return;
        }

        boolean add = locator == null;
        if (add) {
            locator = new Locator();
        }

        DataPacket packet;
        if (protocol >= AbstractProtocol.PROTOCOL_126_10.getProtocolStart()) {
            Waypoint waypoint = new Waypoint();
            waypoint.groupHandle = locator.groupHandle;
            waypoint.action = add ? Waypoint.ACTION_ADD : Waypoint.ACTION_UPDATE;
            waypoint.updateFlag = add ? EnumSet.allOf(UpdateFlag.class) : EnumSet.of(UpdateFlag.WORLD_POS);
            waypoint.dimensionLocation = new DimensionLocation(x, y, z, dimension);
            if (add) {
                waypoint.visible = true;
                waypoint.color = Color.WHITE;
                waypoint.clientPositionAuthority = false;
                if (protocol >= AbstractProtocol.PROTOCOL_126_20.getProtocolStart()) {
                    waypoint.texturePath = Waypoint.DEFAULT_TEXTURE;
                    waypoint.iconSize = DEFAULT_ICON_SIZE;
                } else {
                    waypoint.textureId = Waypoint.TEXTURE_CIRCLE;
                }
            }
            packet = createWaypointPacket(protocol, waypoint);
        } else {
            PlayerLocationPacket12180 location = new PlayerLocationPacket12180();
            location.entityUniqueId = entityUniqueId;
            location.x = x;
            location.y = y;
            location.z = z;
            packet = location;
        }

        if (sender.test(packet)) {
            locator.dimension = dimension;
            locator.x = x;
            locator.y = y;
            locator.z = z;
            if (add) {
                locators.put(entityUniqueId, locator);
            }
        }
    }

    public void hide(int protocol, long entityUniqueId) {
        Locator locator = locators.get(entityUniqueId);
        if (locator == null) {
            return;
        }

        DataPacket packet;
        if (protocol >= AbstractProtocol.PROTOCOL_126_10.getProtocolStart()) {
            Waypoint waypoint = new Waypoint();
            waypoint.groupHandle = locator.groupHandle;
            waypoint.action = Waypoint.ACTION_REMOVE;
            packet = createWaypointPacket(protocol, waypoint);
        } else {
            PlayerLocationPacket12180 location = new PlayerLocationPacket12180();
            location.entityUniqueId = entityUniqueId;
            location.hide = true;
            packet = location;
        }
        pendingRemovals.add(entityUniqueId);
        if (sender.test(packet)) {
            locators.remove(entityUniqueId);
            pendingRemovals.remove(entityUniqueId);
        }
    }

    public void retryRemovals(int protocol) {
        if (pendingRemovals.isEmpty()) {
            return;
        }
        LongList entityIds = new LongArrayList(pendingRemovals);
        for (int index = 0; index < entityIds.size(); index++) {
            hide(protocol, entityIds.getLong(index));
        }
    }

    public void clear(int protocol) {
        if (locators.isEmpty()) {
            return;
        }
        LongList entityIds = new LongArrayList(locators.keySet());
        for (int index = 0; index < entityIds.size(); index++) {
            hide(protocol, entityIds.getLong(index));
        }
    }

    private static DataPacket createWaypointPacket(int protocol, Waypoint waypoint) {
        if (protocol >= AbstractProtocol.PROTOCOL_126_20.getProtocolStart()) {
            LocatorBarPacket12620 packet = new LocatorBarPacket12620();
            packet.waypoints = new Waypoint[]{waypoint};
            return packet;
        }
        LocatorBarPacket12610 packet = new LocatorBarPacket12610();
        packet.waypoints = new Waypoint[]{waypoint};
        return packet;
    }

    private static final class Locator {
        private final UUID groupHandle = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong());
        private int dimension;
        private float x;
        private float y;
        private float z;
    }
}
