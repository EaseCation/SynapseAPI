package org.itxtech.synapseapi.multiprotocol.utils;

import org.itxtech.synapseapi.multiprotocol.common.Experiments.Experiment;

public final class VanillaExperiments {
    // Creators Technical Experiments

    /**
     * Create custom biomes and change world generation. (Custom Biomes)
     * @since 1.16.100
     */
    public static final Experiment DATA_DRIVEN_BIOMES = new Experiment("data_driven_biomes", true);
    /**
     * Add data-driven block and item technology to customize block shape, rotation, damage and more. (Holiday Creator Features)
     * @deprecated 1.21.20 (removed)
     * @since 1.16.100
     */
    public static final Experiment DATA_DRIVEN_ITEMS = new Experiment("data_driven_items", true);
    /**
     * preview internal.
     * @since -beta
     */
    public static final Experiment DATA_DRIVEN_VANILLA_BLOCKS_AND_ITEMS = new Experiment("data_driven_vanilla_blocks_and_items", true);
    /**
     * Use "-beta" versions of API modules in add-on packs (e.g., @minecraft/server 1.1.0-beta). (Beta APIs)
     * @since 1.16.210
     */
    public static final Experiment GAMETEST = new Experiment("gametest", true);
    /**
     * Includes adjustable fog parameters. (Upcoming Creator Features)
     * deprecated 1.19.50 entity properties
     * deprecated 1.21.20 volume area (removed)
     * @since 1.17.0
     */
    public static final Experiment UPCOMING_CREATOR_FEATURES = new Experiment("upcoming_creator_features", true);
    /**
     * Container for Molang experimental queries and language features that aren't tied to other experimental toggles. (Molang Features)
     * @deprecated 1.20.70 (removed)
     * @since 1.17.30
     */
    public static final Experiment EXPERIMENTAL_MOLANG_FEATURES = new Experiment("experimental_molang_features", true);
    /**
     * Experimental Cameras.
     * deprecated 1.20.30
     * @since 1.20.0
     * @deprecated 1.21.0
     */
    public static final Experiment CAMERAS = new Experiment("cameras", true);
    /**
     * Enable the deferred rendering pipeline. Requires a PBR-enables resource pack and compatible hardware. (Render Dragon Features for Creators)
     * @since 1.20.30-beta
     */
    public static final Experiment DEFERRED_TECHNICAL_PREVIEW = new Experiment("deferred_technical_preview", true);
    /**
     * Contains new third person over the shoulder follow_orbit camera preset. (Creator Cameras: New Third Person Presets)
     * @since 1.21.20
     * @deprecated 1.21.70
     */
    public static final Experiment THIRD_PERSON_CAMERAS = new Experiment("third_person_cameras", true);
    /**
     * Enables behavior for the existing free camera to target an entity. (Creator Cameras: Focus Target Camera)
     * @since 1.21.20
     * @deprecated 1.21.60
     */
    public static final Experiment FOCUS_TARGET_CAMERA = new Experiment("focus_target_camera", true);
    /**
     * Player's aim assist system to interact with elements in the world while using custom cameras. (Aim Assist)
     * @since 1.21.50
     * @deprecated 1.21.70
     */
    public static final Experiment CAMERA_AIM_ASSIST = new Experiment("camera_aim_assist", true);
    /**
     * Loads Jigsaw Structures from the behavior pack worldgen folder. (Data-Driven Jigsaw Structures)
     * @since 1.21.50
     */
    public static final Experiment JIGSAW_STRUCTURES = new Experiment("jigsaw_structures", true);
    /**
     * Enables the use of the latest custom camera features. (Experimental Creator Camera Features)
     * @since 1.21.70
     */
    public static final Experiment EXPERIMENTAL_CREATOR_CAMERAS = new Experiment("experimental_creator_cameras", true);

    // Gameplay Experiments

    /**
     * Spectator Mode.
     * deprecated 1.19.50
     * @since 1.18.31
     */
    public static final Experiment SPECTATOR_MODE = new Experiment("spectator_mode", true);
    /**
     * Short Sneaking and Crawling.
     * deprecated 1.20.10 short sneaking
     * since 1.20.10 crawling
     * deprecated 1.20.30 crawling
     * deprecated 1.20.30
     * @since 1.19.80
     */
    public static final Experiment SHORT_SNEAKING = new Experiment("short_sneaking", true);
    /**
     * Contains updated trades for villages for the purpose of rebalancing. (Villager Trade Rebalancing)
     * @since 1.20.30
     */
    public static final Experiment VILLAGER_TRADES_REBALANCE = new Experiment("villager_trades_rebalance", true);

    private VanillaExperiments() {
    }
}
