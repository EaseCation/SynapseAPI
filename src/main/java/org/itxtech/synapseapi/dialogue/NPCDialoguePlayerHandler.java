package org.itxtech.synapseapi.dialogue;

import cn.nukkit.entity.Entity;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapsePlayer;

import javax.annotation.Nullable;

public class NPCDialoguePlayerHandler {

    private final SynapsePlayer player;
    @Nullable
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

    private boolean isCurrentState(@Nullable NPCDialogueState expected) {
        return !player.isMainThreadInputEnabled() || player.isAcceptingInputPackets()
                && player.getNpcDialoguePlayerHandler() == this && this.state == expected;
    }

    public void openDialogue(NPCDialogueScene scene) {
        @Nullable NPCDialogueState previous = this.state;
        if (scene == null || !isCurrentState(previous)) {
            return;
        }
        SynapseAPI.getInstance().getLogger().trace("玩家(" + player.getName() + ") 尝试打开对话框(" + scene.getSceneName() + ")" );

        if (previous == null) {
            throw new IllegalStateException("首次发送，请先调用 openDialogue(NPCDialogueScene scene, Entity entity)");
        }
        scene.sendTo(player, previous.currentEntity().getId(), previous.currentNpcName());
        if (isCurrentState(previous)) {
            this.state = new NPCDialogueState.Opening(scene, previous.currentEntity(), previous.currentNpcName());
        }
    }

    public void openDialogue(NPCDialogueScene scene, Entity entity) {
        this.openDialogue(scene, entity, entity.getNameTag());
    }

    public void openDialogue(NPCDialogueScene scene, Entity entity, String name) {
        @Nullable NPCDialogueState previous = this.state;
        if (scene == null || !isCurrentState(previous)) {
            return;
        }
        SynapseAPI.getInstance().getLogger().trace("玩家(" + player.getName() + ") 尝试打开对话框(" + scene.getSceneName() + ")" );

        if (previous != null && entity != previous.currentEntity()) {
            closeDialogue();
            if (!isCurrentState(null)) {
                return;
            }
            previous = this.state;
        }
        scene.sendTo(player, entity.getId(), name);
        if (isCurrentState(previous)) {
            this.state = new NPCDialogueState.Opening(scene, entity, name);
        }
    }

    /**
     * 无缝切换到新NPC的对话，不关闭当前对话框
     * 用于多NPC轮流对话场景
     */
    public void openDialogueSeamless(NPCDialogueScene scene, Entity entity, String name) {
        @Nullable NPCDialogueState previous = this.state;
        if (scene == null || !isCurrentState(previous)) {
            return;
        }
        SynapseAPI.getInstance().getLogger().trace("玩家(" + player.getName() + ") 无缝切换对话框(" + scene.getSceneName() + ")");
        scene.sendTo(player, entity.getId(), name);
        if (isCurrentState(previous)) {
            this.state = new NPCDialogueState.Opening(scene, entity, name);
        }
    }

    public void closeDialogue() {
        @Nullable NPCDialogueState closing = this.state;
        if (closing == null || !isCurrentState(closing)) {
            return;
        }

        closing.currentScene().close(player, closing.currentEntity().getId(), closing.currentNpcName());
        if (isCurrentState(closing)) {
            this.state = null;
        }
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
                NPCDialogueState.Responded response = new NPCDialogueState.Responded(opening.currentScene(), opening.currentEntity(), opening.currentNpcName());
                this.state = response;

                if (button.getClickCallback() != null) {
                    button.getClickCallback().accept(this);
                }
                // 回调可以退休会话或替换 handler，原响应已消费但不能继续旧来源的工作。
                if (!isCurrentState(this.state)) {
                    return true;
                }
                if (button.isForceCloseOnClick()) {
                    closeDialogue();
                } else {
                    // 如果下一tick仍未打开新对话框，则自动关闭
                    SynapseAPI.getInstance().getServer().getScheduler().scheduleTask(SynapseAPI.getInstance(), () -> {
                        // 延期关闭只归属本次响应，不能影响后来同场景的响应或退休会话。
                        if (!isCurrentState(response)) {
                            return;
                        }
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
