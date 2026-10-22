package org.itxtech.synapseapi;

import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol12070.protocol.SetEntityMotionPacket12070;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InboundMotionAdmissionTest {
    @BeforeAll static void initialize() { LegacyMovementAdmissionTest.protocols(); }

    @Test void targetRegisteredMotionPayloadDecodesCompletely() {
        try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
            for (int protocol : new int[]{859,860,898,975}) {
                SetEntityMotionPacket12070 p = packet(1, 0, false);
                p.setHelper(AbstractProtocol.fromRealProtocol(protocol).getHelper());
                p.encode();
                DataPacket decoded = PacketRegister.getFullPacket(p.getBuffer(), protocol, false);
                SetEntityMotionPacket12070 q = assertInstanceOf(SetEntityMotionPacket12070.class, decoded);
                assertEquals(p.getCount(), q.getOffset());
                assertEquals(p.eid,q.eid); assertEquals(p.tick,q.tick);
                assertEquals(p.motionX,q.motionX); assertEquals(p.motionY,q.motionY); assertEquals(p.motionZ,q.motionZ);
                assertTrue(BetweenTickPackets.supports(q));
            }
        }
    }

    @Test void incomingMotionNeverReplacesServerMotionOrPublishesAReply() {
        checkIgnored(true);
    }

    @Test void disabledPathKeepsOriginalIgnoredMotion() {
        checkIgnored(false);
    }

    private void checkIgnored(boolean enabled) {
        for (long entity : new long[]{0,1,Long.MAX_VALUE}) {
            for (long tick : new long[]{0,42}) {
                for (boolean nonfinite : new boolean[]{false,true}) {
                    try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(enabled,true)) {
                        f.player.motionX = .435; f.player.motionY = .3; f.player.motionZ = -.1;
                        f.player.setComponents(4,5,6);
                        long epoch = f.player.getMovementEpoch();
                        SetEntityMotionPacket12070 packet = packet(entity,tick,nonfinite);
                        f.deliver(packet);
                        assertEquals(.435,f.player.getMotion().x);
                        assertEquals(.3,f.player.getMotion().y);
                        assertEquals(-.1,f.player.getMotion().z);
                        assertEquals(4,f.player.x);assertEquals(5,f.player.y);assertEquals(6,f.player.z);
                        assertEquals(epoch,f.player.getMovementEpoch());assertEquals(20,f.player.getHealth());
                        assertTrue(f.sent.isEmpty());
                        verify(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
                        verifyNoInteractions(f.processor);
                        verify(f.player,never()).setMotion(any(Vector3.class));
                        verify(f.player,never()).onUpdate(anyInt());
                    }
                }
            }
        }
    }

    @Test void lifecycleAndCapabilityStatesDoNotTurnClaimsIntoMotion() {
        for (String state : List.of("dead","unspawned","offline","no-sequence","no-processor")) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true,true)) {
                if(state.equals("dead")) f.player.makeDead();
                if(state.equals("unspawned")) f.player.spawned=false;
                if(state.equals("offline")) doReturn(false).when(f.player).isOnline();
                if(state.equals("no-sequence")) doReturn(false).when(f.player).supportsPacketSequences();
                if(state.equals("no-processor")) when(f.server.getPlayerInputProcessor()).thenReturn(null);
                f.player.motionX=.435;
                f.deliver(packet(1,0,false));
                assertEquals(.435,f.player.getMotion().x);
                assertTrue(f.sent.isEmpty());verifyNoInteractions(f.processor);
            }
        }
    }

    @Test void cancelledAndUninitializedOrRetiredEntryKeepsRejection() {
        for(String state:List.of("cancel","uninitialized","retired")) {
            try(PlayerActionAdmissionTest.Fixture f=new PlayerActionAdmissionTest.Fixture(true,true)) {
                f.cancel=state.equals("cancel");
                if(state.equals("uninitialized"))doReturn(false).when(f.player).isInitialized();
                if(state.equals("retired"))doReturn(false).when(f.player).isAcceptingInputPackets();
                f.deliver(packet(1,0,false));
                verify(f.plugins,times(state.equals("cancel")?1:0)).callEvent(any(DataPacketReceiveEvent.class));
                assertTrue(f.sent.isEmpty());verifyNoInteractions(f.processor);
            }
        }
    }

    private static SetEntityMotionPacket12070 packet(long entity,long tick,boolean nonfinite) {
        SetEntityMotionPacket12070 p=new SetEntityMotionPacket12070();p.eid=entity;p.tick=tick;
        p.motionX=nonfinite?Float.NaN:100;p.motionY=nonfinite?Float.POSITIVE_INFINITY:101;p.motionZ=-100;
        return p;
    }
}
