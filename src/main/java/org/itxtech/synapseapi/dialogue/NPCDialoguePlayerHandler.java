package org.itxtech.synapseapi.dialogue;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import org.itxtech.synapseapi.SynapsePlayer;

public class NPCDialoguePlayerHandler {

    private final SynapsePlayer player;
    private NPCDialogueScene currentScene;
    private String currentSceneName;
    private Entity currentEntity;

    public NPCDialoguePlayerHandler(SynapsePlayer player) {
        this.player = player;
    }

    public Entity getCurrentEntity() {
        return currentEntity;
    }

    public SynapsePlayer getPlayer() {
        return player;
    }

    public void openDialogue(NPCDialogueScene scene) {
        if (this.currentEntity == null) {
            throw new IllegalStateException("首次发送，请先调用 sendDialogue(NPCDialogueScene scene, Entity entity)");
        }
        this.currentSceneName = scene.getSceneName();
        this.currentScene = scene;
        scene.sendTo(player, currentEntity.getId(), currentEntity.getName());
    }

    public void openDialogue(NPCDialogueScene scene, Entity entity) {
        this.currentSceneName = scene.getSceneName();
        this.currentScene = scene;
        this.currentEntity = entity;
        scene.sendTo(player, entity.getId(), entity.getName());
    }

    public void onDialogueResponse(String sceneName, int buttonId) {
        if (this.currentSceneName == null || !this.currentSceneName.equals(sceneName)) {
            return;
        }
        if (this.currentEntity == null) {
            throw new IllegalStateException("首次发送，请先调用 sendDialogue(NPCDialogueScene scene, Entity entity)");
        }
        try {
            NPCDialogueButton button = this.currentScene.getButton(buttonId);
            if (button != null) {
                if (button.getClickCallback() != null) {
                    button.getClickCallback().accept(this);
                }
                if (button.isForceCloseOnClick()) {
                    this.currentScene.close(player, currentEntity.getId(), currentEntity.getName());
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Server.getInstance().getLogger().debug("NPC对话框按钮索引越界: " + e.getMessage());
        }
    }
}
