package org.itxtech.synapseapi.dialogue;

import cn.nukkit.entity.Entity;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapsePlayer;

import javax.annotation.Nullable;

public class NPCDialoguePlayerHandler {

    private final SynapsePlayer player;
    private NPCDialogueState state = null;

    public NPCDialoguePlayerHandler(SynapsePlayer player) {
        this.player = player;
    }

    public SynapsePlayer getPlayer() {
        return player;
    }

    @Nullable
    public NPCDialogueState getState() {
        return state;
    }

    @Nullable
    public Entity getCurrentEntity() {
        return state == null ? null : state.currentEntity();
    }

    public void openDialogue(NPCDialogueScene scene) {
        if (scene == null) {
            return;
        }
        SynapseAPI.getInstance().getLogger().trace("玩家(" + player.getName() + ") 尝试打开对话框(" + scene.getSceneName() + ")" );

        switch (this.state) {
            case NPCDialogueState.Opening opening -> {
                scene.sendTo(player, opening.currentEntity().getId(), opening.currentNpcName());
                state = new NPCDialogueState.Opening(scene, opening.currentEntity(), opening.currentNpcName());
            }
            case NPCDialogueState.Responded responded -> {
                scene.sendTo(player, responded.currentEntity().getId(), responded.currentNpcName());
                state = new NPCDialogueState.Opening(scene, responded.currentEntity(), responded.currentNpcName());
            }
            case null -> {
                throw new IllegalStateException("首次发送，请先调用 openDialogue(NPCDialogueScene scene, Entity entity)");
            }
        }
    }

    public void openDialogue(NPCDialogueScene scene, Entity entity) {
        this.openDialogue(scene, entity, entity.getNameTag());
    }

    public void openDialogue(NPCDialogueScene scene, Entity entity, String name) {
        if (scene == null) {
            return;
        }
        SynapseAPI.getInstance().getLogger().trace("玩家(" + player.getName() + ") 尝试打开对话框(" + scene.getSceneName() + ")" );

        switch (this.state) {
            case NPCDialogueState.Opening opening -> {
                if (entity != opening.currentEntity()) {
                    closeDialogue();
                }
                scene.sendTo(player, entity.getId(), name);
                this.state = new NPCDialogueState.Opening(scene, entity, name);
            }
            case NPCDialogueState.Responded responded -> {
                if (entity != responded.currentEntity()) {
                    closeDialogue();
                }
                scene.sendTo(player, entity.getId(), name);
                this.state = new NPCDialogueState.Opening(scene, entity, name);
            }
            case null -> {
                scene.sendTo(player, entity.getId(), name);
                this.state = new NPCDialogueState.Opening(scene, entity, name);
            }
        }
    }

    public void closeDialogue() {
        if (state == null) {
            return;
        }

        this.state.currentScene().close(player, state.currentEntity().getId(), state.currentNpcName());
        this.state = null;
    }

    public boolean onDialogueResponse(String sceneName, int buttonId) {
        SynapseAPI.getInstance().getLogger().trace("玩家(" + player.getName() + ") 回复对话框(" + sceneName + ")" );
        switch (state) {
            case NPCDialogueState.Opening opening -> {
                if (!opening.currentScene().getSceneName().equals(sceneName)) {
                    SynapseAPI.getInstance().getLogger().warning("玩家(" + player.getName() + ") 回复对话框(" + opening.currentScene().getSceneName() + ") 的SceneName不正确(" + sceneName + ")");
                    this.state = null;
                    return false;
                }
                NPCDialogueButton button = opening.currentScene().getButton(buttonId);
                if (button == null) {
                    SynapseAPI.getInstance().getLogger().warning("玩家(" + player.getName() + ") 回复对话框(" + opening.currentScene().getSceneName() + ") 的按钮索引越界(" + buttonId + ")");
                    this.state = null;
                    return false;
                }
                this.state = new NPCDialogueState.Responded(opening.currentScene(), opening.currentEntity(), opening.currentNpcName());

                if (button.getClickCallback() != null) {
                    button.getClickCallback().accept(this);
                }
                if (button.isForceCloseOnClick()) {
                    closeDialogue();
                } else {
                    // 如果下一tick仍未打开新对话框，则自动关闭
                    SynapseAPI.getInstance().getServer().getScheduler().scheduleTask(SynapseAPI.getInstance(), () -> {
                        switch (this.state) {
                            case NPCDialogueState.Responded responded -> {
                                if (responded.currentScene().getSceneName().equals(sceneName)) {
                                    closeDialogue();
                                }
                            }
                            case null, default -> {}
                        }
                    });
                }
                return true;
            }
            case NPCDialogueState.Responded responded -> {
                SynapseAPI.getInstance().getLogger().warning("玩家(" + player.getName() + ") 回复对话框(" + responded.currentScene().getSceneName() + ") 已经回复过了，不能重复回复");
                this.state = null;
                return false;
            }
            case null -> {
                SynapseAPI.getInstance().getLogger().warning("玩家(" + player.getName() + ") 回复对话框(" + sceneName + ") 不存在" );
                this.state = null;
                return false;
            }
        }
    }

    public boolean onDialogueOpening(String sceneName) {
        SynapseAPI.getInstance().getLogger().trace("玩家(" + player.getName() + ") 打开对话框(" + sceneName + ")" );
        switch (state) {
            case NPCDialogueState.Opening opening -> {
                return true;
            }
            case NPCDialogueState.Responded responded -> {
                SynapseAPI.getInstance().getLogger().warning("玩家(" + player.getName() + ") 打开对话框(" + sceneName + ") 但已打开其他对话框(" + responded.currentScene().getSceneName() + ")");
                this.state = null;
                return false;
            }
            case null -> {
                SynapseAPI.getInstance().getLogger().warning("玩家(" + player.getName() + ") 打开对话框(" + sceneName + ") 不存在" );
                this.state = null;
                return false;
            }
        }
    }

    public boolean onDialogueClosing(String sceneName) {
        SynapseAPI.getInstance().getLogger().trace("玩家(" + player.getName() + ") 关闭对话框(" + sceneName + ")" );
        switch (state) {
            case NPCDialogueState.Opening opening -> {
                if (opening.currentScene().getSceneName().equals(sceneName)) {
                    this.state = null;
                }
                return true;
            }
            case NPCDialogueState.Responded responded -> {
                if (responded.currentScene().getSceneName().equals(sceneName)) {
                    this.state = null;
                }
                return true;
            }
            case null -> {
                return true;
            }
        }
    }
}
