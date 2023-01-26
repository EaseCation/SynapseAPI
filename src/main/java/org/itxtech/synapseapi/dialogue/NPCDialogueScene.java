package org.itxtech.synapseapi.dialogue;

import cn.nukkit.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.NpcDialoguePacket11710;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NPCDialogueScene {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final String text;
    private final List<NPCDialogueButton> buttons = new ArrayList<>();
    private final String sceneName = UUID.randomUUID().toString();

    public NPCDialogueScene(String text, NPCDialogueButton... buttons) {
        this.text = text;
        this.buttons.addAll(Arrays.asList(buttons));
    }

    public String getText() {
        return text;
    }

    public String getSceneName() {
        return sceneName;
    }

    public List<NPCDialogueButton> getButtons() {
        return buttons;
    }

    public NPCDialogueButton getButton(int index) {
        return buttons.get(index);
    }

    public NPCDialogueScene button(NPCDialogueButton button) {
        this.buttons.add(button);
        return this;
    }

    public NPCDialogueScene button(String text, boolean forceCloseOnClick) {
        this.buttons.add(new NPCDialogueButton(text).setForceCloseOnClick(forceCloseOnClick));
        return this;
    }

    public NPCDialogueScene button(String text) {
        this.buttons.add(new NPCDialogueButton(text));
        return this;
    }

    public NPCDialogueScene button(String text, Consumer<NPCDialoguePlayerHandler> handler) {
        return button(text, handler, false);
    }

    public NPCDialogueScene button(String text, Consumer<NPCDialoguePlayerHandler> handler, boolean forceCloseOnClick) {
        NPCDialogueButton button = new NPCDialogueButton(text);
        button.clickCallback(handler);
        button.setForceCloseOnClick(forceCloseOnClick);
        this.buttons.add(button);
        return this;
    }

    /**
     * 请不要直接调用这个，而应该调用 NPCDialoguePlayerHandler.openScene，这样才能正常注册回调
     */
    public void sendTo(Player player, long entityId, String npcName) {
        NpcDialoguePacket11710 packet = new NpcDialoguePacket11710();
        packet.npcEntityUniqueId = entityId;
        packet.actionType = NpcDialoguePacket11710.ACTION_OPEN;
        packet.dialogue = this.text;
        packet.npcName = npcName;
        packet.sceneName = sceneName;
        packet.actionJson = GSON.toJson(buttons.stream().map(NPCDialogueButton::toJsonObject).collect(Collectors.toList()));
        player.dataPacket(packet);
    }

    /**
     * 但是可以调用这个，正确发送给 SynapsePlayer
     */
    public void sendTo(SynapsePlayer player) {
        if (player.getNpcDialoguePlayerHandler() != null) {
            player.getNpcDialoguePlayerHandler().openDialogue(this);
        }
    }

    public void close(Player player, long entityId, String npcName) {
        NpcDialoguePacket11710 packet = new NpcDialoguePacket11710();
        packet.npcEntityUniqueId = entityId;
        packet.actionType = NpcDialoguePacket11710.ACTION_CLOSE;
        packet.dialogue = this.text;
        packet.npcName = npcName;
        packet.sceneName = sceneName;
        packet.actionJson = GSON.toJson(buttons.stream().map(NPCDialogueButton::toJsonObject).collect(Collectors.toList()));
        player.dataPacket(packet);
    }

}
