package org.itxtech.synapseapi.multiprotocol.common;

public interface PlayerAuthInputFlags {
    /**
     * Pressing the "fly up" key when using touch. (including while swimming)
     */
    int ASCEND = 0;
    /**
     * Pressing the "fly down" key when using touch. (including while swimming)
     */
    int DESCEND = 1;
    /**
     * Pressing (and optionally holding) the jump key (while not flying).
     */
    int NORTH_JUMP = 2;
    /**
     * Pressing (and optionally holding) the jump key (including while flying).
     */
    int JUMP_DOWN = 3;
    /**
     * Pressing (and optionally holding) the sprint key (typically the CTRL key). Does not include double-pressing the forward key.
     */
    int SPRINT_DOWN = 4;
    /**
     * Pressing (and optionally holding) the fly/swim button ONCE when in flight/swim mode when using touch. This has no obvious use.
     */
    int CHANGE_HEIGHT = 5;
    /**
     * Pressing (and optionally holding) the jump key (including while flying), and also auto-jumping.
     */
    int JUMPING = 6;
    /**
     * Auto-swimming upwards while pressing forwards with auto-jump enabled.
     */
    int AUTO_JUMPING_IN_WATER = 7;
    /**
     * Sneaking, and pressing the "fly down" key or "sneak" key (including while flying).
     */
    int SNEAKING = 8;
    /**
     * Pressing (and optionally holding) the sneak key (including while flying). This includes when the sneak button is toggled ON with touch controls.
     */
    int SNEAK_DOWN = 9;
    /**
     * Pressing the forward key (typically W on keyboard).
     */
    int UP = 10;
    /**
     * Pressing the backward key (typically S on keyboard).
     */
    int DOWN = 11;
    /**
     * Pressing the left key (typically A on keyboard).
     */
    int LEFT = 12;
    /**
     * Pressing the right key (typically D on keyboard).
     */
    int RIGHT = 13;
    /**
     * Pressing the ↖ key on touch.
     */
    int UP_LEFT = 14;
    /**
     * Pressing the ↗ key on touch.
     */
    int UP_RIGHT = 15;
    /**
     * Client wants to go upwards. Sent when Ascend or Jump is pressed, irrespective of whether flight is enabled.
     */
    int WANT_UP = 16;
    /**
     * Client wants to go downwards. Sent when Descend or Sneak is pressed, irrespective of whether flight is enabled.
     */
    int WANT_DOWN = 17;
    /**
     * Same as "want up" but slow. Only usable with controllers at the time of writing. Triggered by pressing the right joystick by default.
     */
    int WANT_DOWN_SLOW = 18;
    /**
     * Same as "want down" but slow. Only usable with controllers at the time of writing. Not bound to any control by default.
     */
    int WANT_UP_SLOW = 19;
    /**
     * Unclear usage, during testing it was only seen in conjunction with SPRINT_DOWN. NOT sent while actually sprinting.
     */
    int SPRINTING = 20;
    /**
     * Ascending scaffolding. Note that this is NOT sent when climbing ladders.
     */
    int ASCEND_BLOCK = 21;
    /**
     * Descending scaffolding.
     */
    int DESCEND_BLOCK = 22;
    /**
     * Toggling the sneak button on touch when the button enters the "enabled" state.
     */
    int SNEAK_TOGGLE_DOWN = 23;
    /**
     * Unclear use. Sent continually on touch controls, irrespective of whether the player is actually sneaking or not.
     */
    int PERSIST_SNEAK = 24;
    int START_SPRINTING = 25;
    int STOP_SPRINTING = 26;
    int START_SNEAKING = 27;
    int STOP_SNEAKING = 28;
    int START_SWIMMING = 29;
    int STOP_SWIMMING = 30;
    /**
     * Initiating a new jump. Sent every time the client leaves the ground due to jumping, including auto jumps.
     */
    int START_JUMPING = 31;
    int START_GLIDING = 32;
    int STOP_GLIDING = 33;
    int PERFORM_ITEM_INTERACTION = 34;
    int PERFORM_BLOCK_ACTIONS = 35;
    int PERFORM_ITEM_STACK_REQUEST = 36;
    /**
     * @since 1.19.60
     */
    int HANDLED_TELEPORT = 37;
    /**
     * @since 1.19.70
     */
    int EMOTING = 38;
    /**
     * @since 1.20.10
     */
    int MISSED_SWING = 39;
    /**
     * @since 1.20.10
     */
    int START_CRAWLING = 40;
    /**
     * @since 1.20.10
     */
    int STOP_CRAWLING = 41;
    /**
     * @since 1.20.30
     */
    int START_FLYING = 42;
    /**
     * @since 1.20.30
     */
    int STOP_FLYING = 43;
    /**
     * @since 1.20.40
     */
    int ACK_ENTITY_DATA = 44;
    /**
     * @since 1.20.60
     */
    int IN_CLIENT_PREDICTED_IN_VEHICLE = 45;
    /**
     * @since 1.20.70
     */
    int PADDLE_LEFT = 46;
    /**
     * @since 1.20.70
     */
    int PADDLE_RIGHT = 47;
}
