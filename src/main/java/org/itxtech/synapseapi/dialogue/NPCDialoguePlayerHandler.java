package org.itxtech.synapseapi.dialogue;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapsePlayer;

public class NPCDialoguePlayerHandler {

    private final SynapsePlayer player;
    private NPCDialogueScene currentScene;
    private String currentSceneName;
    private Entity currentEntity;
    private int lastDialogueTick;

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
        if (this.currentSceneName != null && Server.getInstance().getTick() == this.lastDialogueTick) {
            SynapseAPI.getInstance().getLogger().warning("玩家 " + player.getName() + " 已经打开了一个对话框(" + this.currentSceneName + ")，正在尝试打开另一个对话框(" + scene.getSceneName() + ")");
        }
        this.currentSceneName = scene.getSceneName();
        this.currentScene = scene;
        scene.sendTo(player, currentEntity.getId(), currentEntity.getName());
    }

    public void openDialogue(NPCDialogueScene scene, Entity entity) {
        this.openDialogue(scene, entity, entity.getNameTag());
    }

    public void openDialogue(NPCDialogueScene scene, Entity entity, String name) {
        this.currentSceneName = scene.getSceneName();
        this.currentScene = scene;
        this.currentEntity = entity;
        scene.sendTo(player, entity.getId(), name);
        this.lastDialogueTick = Server.getInstance().getTick();
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
