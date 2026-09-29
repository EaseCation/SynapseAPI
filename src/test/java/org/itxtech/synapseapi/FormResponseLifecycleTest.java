package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.plugin.PluginManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.ModalFormResponsePacket11920;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FormResponseLifecycleTest {
    @BeforeAll
    static void initializeProtocols() {
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            assertTrue(AbstractProtocol.PROTOCOL_119_20.getProtocolStart() > 0);
        }
    }

    @Test
    void busyRejectionRetiresOnlyTheRejectedFormWithoutRunningCloseCallbacks() {
        TestPlayer player = player();
        FormWindowSimple shown = window();
        player.putForm(1, shown);
        player.putForm(2, window());
        player.handleDataPacket(cancel(2, ModalFormResponsePacket11920.CANCEL_REASON_USER_BUSY));
        assertSame(shown, player.form(1));
        assertNull(player.form(2));
        verify(player.getServer().getPluginManager(), never()).callEvent(any(PlayerFormRespondedEvent.class));
    }

    @Test
    void closeClearsPreviousSelectionAndIsConsumedBeforeCallback() {
        TestPlayer player = player();
        FormWindowSimple form = window();
        assertTrue(form.setResponse(0, player.getProtocol()));
        assertNotNull(form.getResponse());
        player.putForm(1, form);
        PluginManager plugins = player.getServer().getPluginManager();
        doAnswer(call -> {
            PlayerFormRespondedEvent event = call.getArgument(0);
            assertNull(player.form(1));
            assertNull(event.getResponse());
            assertTrue(event.wasClosed());
            player.putForm(2, window());
            return null;
        }).when(plugins).callEvent(any(PlayerFormRespondedEvent.class));
        ModalFormResponsePacket11920 close = cancel(1, ModalFormResponsePacket11920.CANCEL_REASON_CLOSED);
        player.handleDataPacket(close);
        player.handleDataPacket(close);
        assertNotNull(player.form(2));
        verify(plugins, times(1)).callEvent(any(PlayerFormRespondedEvent.class));
    }

    private FormWindowSimple window() {
        FormWindowSimple form = new FormWindowSimple("title", "content");
        form.addButton(new ElementButton("child"));
        return form;
    }

    private ModalFormResponsePacket11920 cancel(int id, int reason) {
        ModalFormResponsePacket11920 packet = new ModalFormResponsePacket11920();
        packet.formId = id;
        packet.canceled = true;
        packet.cancelReason = reason;
        return packet;
    }

    private TestPlayer player() {
        TestPlayer player = mock(TestPlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        player.configure(server);
        when(player.isInitialized()).thenReturn(true);
        when(player.isAlive()).thenReturn(true);
        when(player.getProtocol()).thenReturn(AbstractProtocol.PROTOCOL_119_20.getProtocolStart());
        return player;
    }

    static class TestPlayer extends SynapsePlayer116100 {
        TestPlayer(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) {
            super(source, entry, id, address);
        }

        void configure(Server server) {
            this.server = server;
            isSynapseLogin = true;
            spawned = true;
            formWindows = new Int2ObjectOpenHashMap<>();
            serverSettings = new Int2ObjectOpenHashMap<>();
        }

        void putForm(int id, FormWindow window) { formWindows.put(id, window); }
        FormWindow form(int id) { return formWindows.get(id); }

        @Override
        protected boolean callPacketReceiveEvent(DataPacket packet) { return true; }
    }
}
