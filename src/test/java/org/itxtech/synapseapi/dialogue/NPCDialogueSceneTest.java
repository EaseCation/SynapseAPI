package org.itxtech.synapseapi.dialogue;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NPCDialogueSceneTest {
    @Test
    void javaClientUsesWhiteForDarkDialogueColors() {
        assertEquals("\u00a7fBlack \u00a7fGray \u00a7fDark \u00a76Gold \u00a7lBold",
            NPCDialogueScene.formatTextForClient("\u00a70Black \u00a77Gray \u00a78Dark \u00a76Gold \u00a7lBold", true));
    }

    @Test
    void bedrockClientKeepsOriginalDialogueColors() {
        String input = "\u00a70Black \u00a77Gray \u00a78Dark";
        assertEquals(input, NPCDialogueScene.formatTextForClient(input, false));
    }

    @Test
    void emptyDialogueValuesRemainUnchanged() {
        assertEquals("", NPCDialogueScene.formatTextForClient("", true));
        assertNull(NPCDialogueScene.formatTextForClient(null, true));
    }

    @Test
    void javaClientFormattingAlsoCoversButtonPayload() {
        NPCDialogueButton button = new NPCDialogueButton("\u00a78Continue").setText("\u00a77Details");
        JsonObject json = button.toJsonObject(text -> NPCDialogueScene.formatTextForClient(text, true));
        assertEquals("\u00a7fContinue", json.get("button_name").getAsString());
        assertEquals("\u00a7fDetails", json.get("text").getAsString());
        assertEquals("\u00a7fDetails", json.getAsJsonArray("data").get(0).getAsJsonObject().get("cmd_line").getAsString());
    }
}
