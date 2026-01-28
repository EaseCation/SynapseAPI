package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import lombok.ToString;

/**
 * Sent from the client to the server IF ProfilerLite is enabled AND the creator toggle for
 */
@ToString
public class ServerboundDiagnosticsPacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.SERVERBOUND_DIAGNOSTICS_PACKET;

    public static final int CATEGORY_UNKNOWN = 0;
    public static final int CATEGORY_INVALID_SIZE_UNKNOWN = 1;
    public static final int CATEGORY_ACTOR = 2;
    public static final int CATEGORY_ACTOR_ANIMATION = 3;
    public static final int CATEGORY_ACTOR_RENDERING = 4;
    public static final int CATEGORY_BALANCER = 5;
    public static final int CATEGORY_BLOCK_TICKING_QUEUES = 6;
    public static final int CATEGORY_BIOME_STORAGE = 7;
    public static final int CATEGORY_CEREAL = 8;
    public static final int CATEGORY_CIRCUIT_SYSTEM = 9;
    public static final int CATEGORY_CLIENT = 10;
    public static final int CATEGORY_COMMANDS = 11;
    public static final int CATEGORY_DB_STORAGE = 12;
    public static final int CATEGORY_DEBUG = 13;
    public static final int CATEGORY_DOCUMENTATION = 14;
    public static final int CATEGORY_ECS_SYSTEMS = 15;
    public static final int CATEGORY_FMOD = 16;
    public static final int CATEGORY_FONTS = 17;
    public static final int CATEGORY_IMGUI = 18;
    public static final int CATEGORY_INPUT = 19;
    public static final int CATEGORY_JSON_UI = 20;
    public static final int CATEGORY_JSON_UI_CONTROL_FACTORY_JSON = 21;
    public static final int CATEGORY_JSON_UI_CONTROL_TREE = 22;
    public static final int CATEGORY_JSON_UI_CONTROL_TREE_CONTROL_ELEMENT = 23;
    public static final int CATEGORY_JSON_UI_CONTROL_TREE_POPULATE_DATA_BINDING = 24;
    public static final int CATEGORY_JSON_UI_CONTROL_TREE_POPULATE_FOCUS = 25;
    public static final int CATEGORY_JSON_UI_CONTROL_TREE_POPULATE_LAYOUT = 26;
    public static final int CATEGORY_JSON_UI_CONTROL_TREE_POPULATE_OTHER = 27;
    public static final int CATEGORY_JSON_UI_CONTROL_TREE_POPULATE_SPRITE = 28;
    public static final int CATEGORY_JSON_UI_CONTROL_TREE_POPULATE_TEXT = 29;
    public static final int CATEGORY_JSON_UI_CONTROL_TREE_POPULATE_TTS = 30;
    public static final int CATEGORY_JSON_UI_CONTROL_TREE_VISIBILITY = 31;
    public static final int CATEGORY_JSON_UI_CREATE_UI = 32;
    public static final int CATEGORY_JSON_UI_DEFS = 33;
    public static final int CATEGORY_JSON_UI_LAYOUT_MANAGER = 34;
    public static final int CATEGORY_JSON_UI_LAYOUT_MANAGER_REMOVE_DEPENDENCIES = 35;
    public static final int CATEGORY_JSON_UI_LAYOUT_MANAGER_INIT_VARIABLE = 36;
    public static final int CATEGORY_LANGUAGES = 37;
    public static final int CATEGORY_LEVEL = 38;
    public static final int CATEGORY_LEVEL_STRUCTURES = 39;
    public static final int CATEGORY_LEVEL_CHUNK = 40;
    public static final int CATEGORY_LEVEL_CHUNK_GEN = 41;
    public static final int CATEGORY_LEVEL_CHUNK_GEN_THREAD_LOCAL = 42;
    public static final int CATEGORY_NETWORK = 43;
    public static final int CATEGORY_MARKETPLACE = 44;
    public static final int CATEGORY_MATERIAL_DRAGON_COMPILED_DEFINITION = 45;
    public static final int CATEGORY_MATERIAL_DRAGON_MATERIAL = 46;
    public static final int CATEGORY_MATERIAL_DRAGON_RESOURCE = 47;
    public static final int CATEGORY_MATERIAL_DRAGON_UNIFORM_MAP = 48;
    public static final int CATEGORY_MATERIAL_RENDER_MATERIAL = 49;
    public static final int CATEGORY_MATERIAL_RENDER_MATERIAL_GROUP = 50;
    public static final int CATEGORY_MATERIAL_VARIATION_MANAGER = 51;
    public static final int CATEGORY_MOLANG = 52;
    public static final int CATEGORY_ORE_UI = 53;
    public static final int CATEGORY_PERSONA = 54;
    public static final int CATEGORY_PLAYER = 55;
    public static final int CATEGORY_RENDER_CHUNK = 56;
    public static final int CATEGORY_RENDER_CHUNK_INDEX_BUFFER = 57;
    public static final int CATEGORY_RENDER_CHUNK_VERTEX_BUFFER = 58;
    public static final int CATEGORY_RENDERING = 59;
    public static final int CATEGORY_RENDERING_LIBRARY = 60;
    public static final int CATEGORY_REQUEST_LOG = 61;
    public static final int CATEGORY_RESOURCE_PACKS = 62;
    public static final int CATEGORY_SOUND = 63;
    public static final int CATEGORY_SUB_CHUNK_BIOME_DATA = 64;
    public static final int CATEGORY_SUB_CHUNK_BLOCK_DATA = 65;
    public static final int CATEGORY_SUB_CHUNK_LIGHT_DATA = 66;
    public static final int CATEGORY_TEXTURES = 67;
    public static final int CATEGORY_VR = 68;
    public static final int CATEGORY_WEATHER_RENDERER = 69;
    public static final int CATEGORY_WORLD_GENERATOR = 70;
    public static final int CATEGORY_TASKS = 71;
    public static final int CATEGORY_TEST = 72;
    public static final int CATEGORY_SCRIPTING = 73;
    public static final int CATEGORY_SCRIPTING_RUNTIME = 74;
    public static final int CATEGORY_SCRIPTING_CONTEXT = 75;
    public static final int CATEGORY_SCRIPTING_CONTEXT_BINDINGS_MC = 76;
    public static final int CATEGORY_SCRIPTING_CONTEXT_BINDINGS_GT = 77;
    public static final int CATEGORY_SCRIPTING_CONTEXT_RUN = 78;
    public static final int CATEGORY_DATA_DRIVEN_UI = 79;
    public static final int CATEGORY_DATA_DRIVEN_UI_DEFS = 80;

    public float avgFps;
    public float avgServerSimTickTimeMs;
    public float avgClientSimTickTimeMs;
    public float avgBeginFrameTimeMs;
    public float avgInputTimeMs;
    public float avgRenderTimeMs;
    public float avgEndFrameTimeMs;
    public float avgRemainderTimePercent;
    public float avgUnaccountedTimePercent;
    /// Pair<MemoryCategory, CurrentBytes>
    public IntLongPair[] memoryCategoryValues;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        avgFps = getLFloat();
        avgServerSimTickTimeMs = getLFloat();
        avgClientSimTickTimeMs = getLFloat();
        avgBeginFrameTimeMs = getLFloat();
        avgInputTimeMs = getLFloat();
        avgRenderTimeMs = getLFloat();
        avgEndFrameTimeMs = getLFloat();
        avgRemainderTimePercent = getLFloat();
        avgUnaccountedTimePercent = getLFloat();
        memoryCategoryValues = getArray(new IntLongPair[0], stream -> IntLongPair.of(stream.getByte(), stream.getLLong()));
    }

    @Override
    public void encode() {
    }
}
