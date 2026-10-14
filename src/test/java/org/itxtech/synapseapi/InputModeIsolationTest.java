package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.plugin.PluginLogger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InputModeIsolationTest {
    @Test
    void eitherDisabledLocalSwitchKeepsTheLegacyEntry() {
        ServerInputDispatcher dispatcher = dispatcher();
        try {
            for (boolean core : new boolean[]{false, true}) {
                for (boolean synapse : new boolean[]{false, true}) {
                    SynapseEntry entry = entry(core ? dispatcher : null, synapse);
                    assertEquals(core && synapse, entry.isMainThreadInputEnabled());
                    ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
                    player.configure(entry);
                    assertEquals(core && synapse, player.isMainThreadInputEnabled());
                    player.isSynapseLogin = false;
                    assertFalse(player.isMainThreadInputEnabled());
                }
            }
        } finally {
            dispatcher.close();
        }
    }

    @Test
    void oppositeModesRemainLocalToTheirEntryAndReplacementPlayer() {
        ServerInputDispatcher dispatcher = dispatcher();
        try {
            SynapseEntry enabled = entry(dispatcher, true);
            SynapseEntry disabled = entry(null, false);
            ProbePlayer first = mock(ProbePlayer.class, CALLS_REAL_METHODS);
            ProbePlayer second = mock(ProbePlayer.class, CALLS_REAL_METHODS);
            first.configure(enabled);
            second.configure(disabled);
            assertTrue(first.isMainThreadInputEnabled());
            assertFalse(second.isMainThreadInputEnabled());
            // 目标创建自己的 Player/Entry；不能把来源模式沿玩家转服数据带过去。
            ProbePlayer target = mock(ProbePlayer.class, CALLS_REAL_METHODS);
            target.configure(disabled);
            assertFalse(target.isMainThreadInputEnabled());
            ProbePlayer returnTarget = mock(ProbePlayer.class, CALLS_REAL_METHODS);
            returnTarget.configure(enabled);
            assertTrue(returnTarget.isMainThreadInputEnabled());
            assertFalse(second.isMainThreadInputEnabled());
            assertTrue(first.isMainThreadInputEnabled());
        } finally {
            dispatcher.close();
        }
    }

    private static SynapseEntry entry(ServerInputDispatcher dispatcher, boolean enabled) {
        Server server = mock(Server.class);
        when(server.getInputDispatcher()).thenReturn(dispatcher);
        SynapseAPI api = mock(SynapseAPI.class);
        when(api.getServer()).thenReturn(server);
        when(api.getLogger()).thenReturn(mock(PluginLogger.class));
        when(api.isMainThreadInput()).thenReturn(enabled);
        // 与原生命周期夹具一致，构造器停在认证配置出口，不连接网络或启动线程。
        return new SynapseEntry(api, "127.0.0.1", 10325, false, "", "Offline mixed-mode contract");
    }

    private static ServerInputDispatcher dispatcher() {
        return new ServerInputDispatcher(Thread.currentThread(), 16, 4096, 16, 4096, error -> fail(error));
    }

    static class ProbePlayer extends SynapsePlayer {
        ProbePlayer() { super(null, null, 1L, null); }

        void configure(SynapseEntry entry) {
            this.synapseEntry = entry;
            this.isSynapseLogin = true;
        }
    }
}
