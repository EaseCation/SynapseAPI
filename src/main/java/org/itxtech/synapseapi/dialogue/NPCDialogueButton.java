package org.itxtech.synapseapi.dialogue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NPCDialogueButton {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    public static class CmdLine {
        public String cmd_line;
        public int vmd_ver = CMD_VER;

        public CmdLine(String cmd_line) {
            this.cmd_line = cmd_line;
        }
    }

    // enables button
    public static final int MODE_BUTTON = 0;
    // those two are disable button, no idea what is this for
    public static final int MODE_ON_CLOSE = 1;
    public static final int MODE_ON_ENTER = 2;

    public static final int TYPE_URL = 0;  // works on only Minecraft Education Edition
    public static final int TYPE_COMMAND = 1;
    public static final int TYPE_INVALID = 2;

    public static final int CMD_VER = 17; // 17 in 1.18.0.2.0, and 12 in 1.16.0.2.0

    public String button_name;
    public String text = "";
    public List<CmdLine> data = new ArrayList<>();  // null when type is url
    public int mode = MODE_BUTTON;
    public int type = TYPE_COMMAND;
    private Consumer<NPCDialoguePlayerHandler> clickCallback = null;
    private boolean forceCloseOnClick = false;

    public NPCDialogueButton(String button_name) {
        this.button_name = button_name;
    }

    public String getButtonName() {
        return button_name;
    }

    public NPCDialogueButton setButtonName(String button_name) {
        this.button_name = button_name;
        return this;
    }

    public String getText() {
        return text;
    }

    public NPCDialogueButton setText(String text) {
        this.text = text;
        if (text == null || text.isEmpty()) {
            this.data = Collections.emptyList();
            return this;
        }
        this.data = Arrays.stream(text.split("\n")).map(CmdLine::new).collect(Collectors.toList());
        return this;
    }

    /**
     * This doesn't work on normal Minecraft: Bedrock Edition
     *
     * @param link String
     *
     * @return $this
     */
    public NPCDialogueButton setLink(String link) {
        this.text = link;
        this.type = TYPE_URL;
        this.data = null;
        return this;
    }

    public List<CmdLine> getData() {
        return data;
    }

    public NPCDialogueButton setData(List<CmdLine> data) {
        this.data = data;
        return this;
    }

    public int getMode() {
        return mode;
    }

    public NPCDialogueButton setMode(int mode) {
        this.mode = mode;
        return this;
    }

    public int getType() {
        return type;
    }

    public NPCDialogueButton setType(int type) {
        this.type = type;
        return this;
    }

    public NPCDialogueButton clickCallback(Consumer<NPCDialoguePlayerHandler> callback) {
        this.clickCallback = callback;
        return this;
    }

    public Consumer<NPCDialoguePlayerHandler> getClickCallback() {
        return clickCallback;
    }

    public NPCDialogueButton setForceCloseOnClick(boolean forceCloseOnClick) {
        this.forceCloseOnClick = forceCloseOnClick;
        return this;
    }

    public boolean isForceCloseOnClick() {
        return forceCloseOnClick;
    }

    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("button_name", button_name);
        obj.addProperty("text", text);
        obj.addProperty("mode", mode);
        obj.addProperty("type", type);
        obj.add("data", this.data != null ? GSON.toJsonTree(this.data) : JsonNull.INSTANCE);
        return obj;
    }
}
