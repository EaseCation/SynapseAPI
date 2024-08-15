package org.itxtech.synapseapi.dialogue;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapsePlayer;

public class NPCDialoguePlayerHandler {

    private final SynapsePlayer player;

    private NPCDialogueScene currentScene;

    private Entity currentEntity;
    private String currentNpcName;

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
        if (scene == null) {
            return;
        }

        if (this.currentEntity == null) {
            throw new IllegalStateException("首次发送，请先调用 openDialogue(NPCDialogueScene scene, Entity entity)");
        }

        if (this.currentScene != null && Server.getInstance().getTick() == this.lastDialogueTick) {
            SynapseAPI.getInstance().getLogger().warning("玩家 " + player.getName() + " 已经打开了一个对话框(" + this.currentScene.getSceneName() + ")，正在尝试打开另一个对话框(" + scene.getSceneName() + ")");
        }

        this.currentScene = scene;

        scene.sendTo(player, currentEntity.getId(), currentNpcName);
    }

    public void openDialogue(NPCDialogueScene scene, Entity entity) {
        this.openDialogue(scene, entity, entity.getNameTag());
    }

    public void openDialogue(NPCDialogueScene scene, Entity entity, String name) {
        if (scene == null) {
            return;
        }

        if (this.currentScene != null) {
            if (Server.getInstance().getTick() == this.lastDialogueTick) {
                return;
            }
            closeDialogue();
        }

        this.currentScene = scene;

        this.currentEntity = entity;
        this.currentNpcName = name;

        scene.sendTo(player, entity.getId(), name);

        this.lastDialogueTick = Server.getInstance().getTick();
    }

    private void closeDialogue() {
        if (currentScene == null) {
            return;
        }
        currentScene.close(player, currentEntity.getId(), currentNpcName);
        currentScene = null;
    }

    public boolean onDialogueResponse(String sceneName, int buttonId) {
        if (this.currentScene == null || !this.currentScene.getSceneName().equals(sceneName)) {
            return false;
        }
        if (this.currentEntity == null) {
            return false;
        }
        try {
            NPCDialogueButton button = this.currentScene.getButton(buttonId);
            if (button != null) {
                if (button.getClickCallback() != null) {
                    button.getClickCallback().accept(this);
                }
                if (button.isForceCloseOnClick()) {
                    closeDialogue();
                }
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Server.getInstance().getLogger().debug("NPC对话框按钮索引越界: " + e.getMessage());
        }
        return false;
    }
}
