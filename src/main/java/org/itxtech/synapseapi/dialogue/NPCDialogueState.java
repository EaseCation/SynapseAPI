package org.itxtech.synapseapi.dialogue;

import cn.nukkit.entity.Entity;

public sealed interface NPCDialogueState {
    record Opening(NPCDialogueScene currentScene, Entity currentEntity, String currentNpcName) implements NPCDialogueState { }
    record Responded(NPCDialogueScene currentScene, Entity currentEntity, String currentNpcName) implements NPCDialogueState { }

    NPCDialogueScene currentScene();

    Entity currentEntity();

    String currentNpcName();
}
