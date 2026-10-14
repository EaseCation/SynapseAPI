package org.itxtech.synapseapi.dialogue;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.scheduler.ServerScheduler;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapsePlayer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NpcDeferredCloseOwnershipTest {
    @Test
    void oldTaskCannotCloseNewResponseWithStructurallyEqualState() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.respond(fixture.scene, fixture.entity);
            NPCDialogueState first = fixture.handler.getState();
            fixture.respond(fixture.scene, fixture.entity);
            NPCDialogueState second = fixture.handler.getState();
            assertEquals(first, second);
            assertNotSame(first, second);
            fixture.tasks.get(0).run();
            assertSame(second, fixture.handler.getState());
            verify(fixture.scene, never()).close(any(), anyLong(), anyString());
            fixture.tasks.get(1).run();
            assertNull(fixture.handler.getState());
            verify(fixture.scene).close(fixture.player, 7, "host");
        }
    }

    @Test
    void disabledModeKeepsOriginalSameScenePrematureClose() {
        try (Fixture fixture = new Fixture(false)) {
            fixture.respond(fixture.scene, fixture.entity);
            fixture.respond(fixture.scene, fixture.entity);
            fixture.tasks.get(0).run();
            assertNull(fixture.handler.getState());
            verify(fixture.scene).close(fixture.player, 7, "host");
            verify(fixture.player, never()).isAcceptingInputPackets();
        }
    }

    @Test
    void changedNpcWithSameSceneStillOwnsItsNewResponse() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.respond(fixture.scene, fixture.entity);
            Entity other = mock(Entity.class);
            when(other.getId()).thenReturn(8L);
            fixture.respond(fixture.scene, other);
            NPCDialogueState current = fixture.handler.getState();
            clearInvocations(fixture.scene);
            fixture.tasks.get(0).run();
            assertSame(current, fixture.handler.getState());
            verify(fixture.scene, never()).close(any(), anyLong(), anyString());
            fixture.tasks.get(1).run();
            verify(fixture.scene).close(fixture.player, 8, "host");
        }
    }

    @Test
    void differentSceneResponseRetainsExistingBehavior() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.respond(fixture.scene, fixture.entity);
            NPCDialogueScene next = fixture.scene("next");
            fixture.respond(next, fixture.entity);
            NPCDialogueState current = fixture.handler.getState();
            fixture.tasks.get(0).run();
            assertSame(current, fixture.handler.getState());
            fixture.tasks.get(1).run();
            assertNull(fixture.handler.getState());
            verify(next).close(fixture.player, 7, "host");
        }
    }

    @Test
    void normalResponseClosesOnceAndDuplicateTaskHasNoEffect() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.respond(fixture.scene, fixture.entity);
            fixture.tasks.getFirst().run();
            fixture.tasks.getFirst().run();
            assertNull(fixture.handler.getState());
            verify(fixture.scene, times(1)).close(fixture.player, 7, "host");
        }
    }

    @Test
    void retiredSourceCannotPublishFromOldDeferredTask() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.respond(fixture.scene, fixture.entity);
            NPCDialogueState response = fixture.handler.getState();
            doReturn(false).when(fixture.player).isAcceptingInputPackets();
            fixture.tasks.getFirst().run();
            assertSame(response, fixture.handler.getState());
            verify(fixture.scene, never()).close(any(), anyLong(), anyString());
        }
    }

    @Test
    void reopenedOpeningIsNotClosedByEarlierResponseTask() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.respond(fixture.scene, fixture.entity);
            fixture.handler.openDialogue(fixture.scene, fixture.entity, "host");
            NPCDialogueState current = fixture.handler.getState();
            fixture.tasks.getFirst().run();
            assertSame(current, fixture.handler.getState());
            assertInstanceOf(NPCDialogueState.Opening.class, current);
        }
    }

    @Test
    void ordinaryForceCloseDoesNotScheduleDeferredWork() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.button.setForceCloseOnClick(true);
            fixture.respond(fixture.scene, fixture.entity);
            assertNull(fixture.handler.getState());
            assertTrue(fixture.tasks.isEmpty());
            verify(fixture.scene).close(fixture.player, 7, "host");
        }
    }

    @Test
    void positionEpochChangeDoesNotInvalidateOwnedCloseNotification() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.respond(fixture.scene, fixture.entity);
            doReturn(3L).when(fixture.player).getMovementEpoch();
            fixture.tasks.getFirst().run();
            assertNull(fixture.handler.getState());
            verify(fixture.player, never()).getMovementEpoch();
        }
    }

    @Test
    void retiredButtonCallbackCannotContinueForceClose() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.button.setForceCloseOnClick(true).clickCallback(handler -> doReturn(false).when(fixture.player).isAcceptingInputPackets());
            fixture.respond(fixture.scene, fixture.entity);
            assertInstanceOf(NPCDialogueState.Responded.class, fixture.handler.getState());
            verify(fixture.scene, never()).close(any(), anyLong(), anyString());
            assertTrue(fixture.tasks.isEmpty());
        }
    }

    @Test
    void retiredButtonCallbackDoesNotScheduleNewWork() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.button.clickCallback(handler -> doReturn(false).when(fixture.player).isAcceptingInputPackets());
            fixture.respond(fixture.scene, fixture.entity);
            assertInstanceOf(NPCDialogueState.Responded.class, fixture.handler.getState());
            assertTrue(fixture.tasks.isEmpty());
            verify(fixture.scene, never()).close(any(), anyLong(), anyString());
        }
    }

    @Test
    void disabledModeKeepsOriginalForceCloseAfterCallback() {
        try (Fixture fixture = new Fixture(false)) {
            fixture.button.setForceCloseOnClick(true).clickCallback(handler -> doReturn(false).when(fixture.player).isAcceptingInputPackets());
            fixture.respond(fixture.scene, fixture.entity);
            assertNull(fixture.handler.getState());
            verify(fixture.scene).close(fixture.player, 7, "host");
            verify(fixture.player, never()).isAcceptingInputPackets();
        }
    }

    @Test
    void activeCallbackReopenedSceneStillHonorsForceClose() {
        try (Fixture fixture = new Fixture(true)) {
            NPCDialogueScene next = fixture.scene("next");
            fixture.button.setForceCloseOnClick(true).clickCallback(handler -> handler.openDialogue(next));
            fixture.respond(fixture.scene, fixture.entity);
            assertNull(fixture.handler.getState());
            verify(fixture.scene, never()).close(any(), anyLong(), anyString());
            verify(next).close(fixture.player, 7, "host");
        }
    }

    @Test
    void activePositionChangeInCallbackKeepsForceCloseNotification() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.button.setForceCloseOnClick(true).clickCallback(handler -> doReturn(3L).when(fixture.player).getMovementEpoch());
            fixture.respond(fixture.scene, fixture.entity);
            assertNull(fixture.handler.getState());
            verify(fixture.scene).close(fixture.player, 7, "host");
            verify(fixture.player, never()).getMovementEpoch();
        }
    }

    @Test
    void callbackExceptionKeepsExistingFailureAndNoClosingWork() {
        try (Fixture fixture = new Fixture(true)) {
            IllegalStateException failure = new IllegalStateException("Probe callback failed");
            fixture.button.setForceCloseOnClick(true).clickCallback(handler -> { throw failure; });
            fixture.handler.openDialogue(fixture.scene, fixture.entity, "host");
            assertSame(failure, assertThrows(IllegalStateException.class, () -> fixture.handler.onDialogueResponse("same", 0)));
            assertInstanceOf(NPCDialogueState.Responded.class, fixture.handler.getState());
            assertTrue(fixture.tasks.isEmpty());
            verify(fixture.scene, never()).close(any(), anyLong(), anyString());
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final SynapsePlayer player = mock(SynapsePlayer.class);
        private final SynapseAPI api = mock(SynapseAPI.class);
        private final Server server = mock(Server.class);
        private final ServerScheduler scheduler = mock(ServerScheduler.class);
        private final Entity entity = mock(Entity.class);
        private final NPCDialogueButton button = new NPCDialogueButton("probe");
        private final NPCDialogueScene scene = scene("same");
        private final NPCDialoguePlayerHandler handler = new NPCDialoguePlayerHandler(player);
        private final List<Runnable> tasks = new ArrayList<>();
        private final MockedStatic<SynapseAPI> apis = mockStatic(SynapseAPI.class);

        private Fixture(boolean inputMode) {
            apis.when(SynapseAPI::getInstance).thenReturn(api);
            when(api.getLogger()).thenReturn(mock(PluginLogger.class));
            when(api.getServer()).thenReturn(server);
            when(server.getScheduler()).thenReturn(scheduler);
            when(player.isMainThreadInputEnabled()).thenReturn(inputMode);
            when(player.isAcceptingInputPackets()).thenReturn(true);
            when(player.getNpcDialoguePlayerHandler()).thenReturn(handler);
            when(player.getName()).thenReturn("actor");
            when(entity.getId()).thenReturn(7L);
            doAnswer(call -> { tasks.add(call.getArgument(1)); return null; }).when(scheduler).scheduleTask(eq(api), any(Runnable.class));
        }

        private NPCDialogueScene scene(String name) {
            NPCDialogueScene result = mock(NPCDialogueScene.class);
            when(result.getSceneName()).thenReturn(name);
            when(result.getButton(0)).thenReturn(button);
            return result;
        }

        private void respond(NPCDialogueScene selected, Entity host) {
            handler.openDialogue(selected, host, "host");
            assertTrue(handler.onDialogueResponse(selected.getSceneName(), 0));
        }

        @Override
        public void close() { apis.close(); }
    }
}
