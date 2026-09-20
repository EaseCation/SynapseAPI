package org.itxtech.synapseapi.network.protocol;

import cn.nukkit.network.protocol.DataPacket;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Log4j2
public class PacketSequence extends DataPacket {
    private static final Runnable NO_OP = () -> {};

    private final List<DataPacket> packets;
    private final Runnable onAppended;
    private final Runnable onDropped;
    private boolean appended;
    private boolean dropped;

    public PacketSequence(DataPacket... packets) {
        this(List.of(packets), NO_OP, NO_OP);
    }

    public PacketSequence(List<DataPacket> packets, Runnable onAppended, Runnable onDropped) {
        this.packets = packets;
        if (this.packets.isEmpty()) {
            throw new IllegalArgumentException("Packet sequence must not be empty");
        }
        this.onAppended = Objects.requireNonNull(onAppended, "onAppended");
        this.onDropped = Objects.requireNonNull(onDropped, "onDropped");
    }

    public final List<DataPacket> getPackets() {
        return packets;
    }

    @Override
    public int pid() {
        return -1;
    }

    @Override
    public void decode() {
    }

    @Override
    protected void encode() {
    }

    public static void appended(DataPacket packet) {
        if (packet instanceof PacketSequence) {
            visit(packet, Collections.newSetFromMap(new IdentityHashMap<>()), false);
        }
    }

    public static void discard(DataPacket packet) {
        if (packet instanceof PacketSequence) {
            visit(packet, Collections.newSetFromMap(new IdentityHashMap<>()), true);
        }
    }

    public static void discardReplaced(@Nullable List<DataPacket> replaced, DataPacket retained) {
        if (replaced == null) {
            return;
        }
        Set<PacketSequence> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<DataPacket> pending = new ArrayDeque<>();
        pending.add(retained);
        while (!pending.isEmpty()) {
            if (pending.removeFirst() instanceof PacketSequence sequence && visited.add(sequence)) {
                pending.addAll(sequence.packets);
            }
        }
        for (DataPacket packet : replaced) {
            visit(packet, visited, true);
        }
    }

    private static void visit(DataPacket packet, Set<PacketSequence> visited, boolean discard) {
        if (!(packet instanceof PacketSequence)) {
            return;
        }
        Deque<DataPacket> pending = new ArrayDeque<>();
        pending.add(packet);
        while (!pending.isEmpty()) {
            if (pending.removeFirst() instanceof PacketSequence sequence && visited.add(sequence)) {
                pending.addAll(sequence.packets);
                if (discard) {
                    sequence.drop();
                } else {
                    sequence.append();
                }
            }
        }
    }

    private void append() {
        if (appended || dropped) {
            return;
        }
        appended = true;
        try {
            onAppended.run();
        } catch (Throwable exception) {
            log.error("Packet sequence append callback failed", exception);
        }
    }

    private void drop() {
        if (dropped) {
            return;
        }
        dropped = true;
        try {
            onDropped.run();
        } catch (Throwable exception) {
            log.error("Packet sequence drop callback failed", exception);
        }
    }
}
