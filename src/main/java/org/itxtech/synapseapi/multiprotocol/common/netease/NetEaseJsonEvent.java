package org.itxtech.synapseapi.multiprotocol.common.netease;

import cn.nukkit.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventName", visible = true, defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddContainerMix.class, name = "ADD_CONTAINER_MIX"),
        @JsonSubTypes.Type(value = AddPlayerBannedItem.class, name = "ADD_PLAYER_BANNED_ITEM"),
        @JsonSubTypes.Type(value = AddPotionMix.class, name = "ADD_POTION_MIX"),
        @JsonSubTypes.Type(value = CanPlayerAttack.class, name = "CAN_PLAYER_ATTACK"),
        @JsonSubTypes.Type(value = CanPlayerJump.class, name = "CAN_PLAYER_JUMP"),
        @JsonSubTypes.Type(value = CanPlayerMove.class, name = "CAN_PLAYER_MOVE"),
        @JsonSubTypes.Type(value = ControlEntityAi.class, name = "CONTROL_ENTITY_AI"),
        @JsonSubTypes.Type(value = CustomAppearanceUuid.class, name = "CUSTOMAPPEARANCE_UUID"),
        @JsonSubTypes.Type(value = DisableContainers.class, name = "DISABLE_CONTAINERS"),
        @JsonSubTypes.Type(value = DisableDropItem.class, name = "DISABLE_DROPITEM"),
        @JsonSubTypes.Type(value = DisableGravityInLiquid.class, name = "DISABLE_GRAVITY_IN_LIQUID"),
        @JsonSubTypes.Type(value = DisableHunger.class, name = "DISABLE_HUNGER"),
        @JsonSubTypes.Type(value = DisableSolidify.class, name = "DISABLE_SOLIDIFY"),
        @JsonSubTypes.Type(value = EnablePlayerKeepInventory.class, name = "ENABLE_PLAYER_KEEP_INVENTORY"),
        @JsonSubTypes.Type(value = FurnaceLit.class, name = "FURNACE_LIT"),
        @JsonSubTypes.Type(value = LockDifficulty.class, name = "LOCK_DIFFICULTY"),
        @JsonSubTypes.Type(value = LockGameRulesInfo.class, name = "LOCK_GAME_RULES_INFO"),
        @JsonSubTypes.Type(value = LockGameType.class, name = "LOCK_GAME_TYPE"),
        @JsonSubTypes.Type(value = OnOpenStore.class, name = "ON_OPEN_STORE"),
        @JsonSubTypes.Type(value = OnPlayerDeath.class, name = "ON_PLAYER_DEATH"),
        @JsonSubTypes.Type(value = PlayRidingAnimation.class, name = "PLAY_RIDING_ANIMATION"),
        @JsonSubTypes.Type(value = RemotePlayerGameType.class, name = "RemotePlayerGameType"),
        @JsonSubTypes.Type(value = SetBlockDict.class, name = "SET_BLOCK_DICT"),
        @JsonSubTypes.Type(value = SetEntityAttackSpeed.class, name = "SET_ENTITY_ATTACK_SPEED"),
        @JsonSubTypes.Type(value = SetEntityGravity.class, name = "SET_ENTITY_GRAVITY"),
        @JsonSubTypes.Type(value = SetHealthLevel.class, name = "SET_HEALTH_LEVEL"),
        @JsonSubTypes.Type(value = SetHealthTick.class, name = "SET_HEALTH_TICK"),
        @JsonSubTypes.Type(value = SetHurtsShader.class, name = "SET_HURTSHADER"),
        @JsonSubTypes.Type(value = SetJumpPower.class, name = "SET_JUMP_POWER"),
        @JsonSubTypes.Type(value = SetLevelGravity.class, name = "SET_LEVEL_GRAVITY"),
        @JsonSubTypes.Type(value = SetLockPassenger.class, name = "SET_LOCK_PASSENGER"),
        @JsonSubTypes.Type(value = SetMaxAutoStep.class, name = "SET_MAX_AUTO_STEP"),
        @JsonSubTypes.Type(value = SetNameTagInfo.class, name = "SetNameTagInfo"),
        @JsonSubTypes.Type(value = SetNaturalRegen.class, name = "SET_NATURAL_REGEN"),
        @JsonSubTypes.Type(value = SetNaturalStarve.class, name = "SET_NATURAL_STARVE"),
        @JsonSubTypes.Type(value = SetPassengerIndex.class, name = "SET_PASSENGER_INDEX"),
        @JsonSubTypes.Type(value = SetPlayerPosition.class, name = "SET_PLAYER_POSITION"),
        @JsonSubTypes.Type(value = SetPlayerReadyPosDelta.class, name = "SET_PLAYER_READY_POS_DELTA"),
        @JsonSubTypes.Type(value = SetPlayerSize.class, name = "SET_PLAYER_SIZE"),
        @JsonSubTypes.Type(value = SetPlayerSizeAabb.class, name = "SET_PLAYER_SIZE_AABB"),
        @JsonSubTypes.Type(value = SetShearsDestorySpeed.class, name = "SET_SHEARS_DESTORY_SPEED"),
        @JsonSubTypes.Type(value = SetSpecificPassengerIndex.class, name = "SET_SPECIFIC_PASSENGER_INDEX"),
        @JsonSubTypes.Type(value = SetStarveLevel.class, name = "SET_STARVE_LEVEL"),
        @JsonSubTypes.Type(value = SetVipNameTagInfo.class, name = "SetVipNameTagInfo"),
        @JsonSubTypes.Type(value = SyncFishingLineColor.class, name = "SYNC_FISHING_LINE_COLOR"),
        @JsonSubTypes.Type(value = SyncFishingLineMax.class, name = "SYNC_FISHING_LINE_MAX"),
        @JsonSubTypes.Type(value = SyncOwnerId.class, name = "SYNC_OWNER_ID"),
        @JsonSubTypes.Type(value = UpdateBannedItem.class, name = "UPDATE_BANNED_ITEM"),
        @JsonSubTypes.Type(value = UpdatePassenger.class, name = "UPDATE_PASSENGER")
})
@JsonPropertyOrder("eventName")
public interface NetEaseJsonEvent {
    String getEventName();

    default String toJson() {
        try {
            return JsonUtil.TRUSTED_JSON_MAPPER.writeValueAsString(this);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize NetEase JSON event", e);
        }
    }

    default byte[] toJsonAsBytes() {
        try {
            return JsonUtil.TRUSTED_JSON_MAPPER.writeValueAsBytes(this);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize NetEase JSON event", e);
        }
    }
}
