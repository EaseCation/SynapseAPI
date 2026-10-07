package org.itxtech.synapseapi.multiprotocol.common.netease;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventName", visible = true, defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LoginUid.class, name = "LOGIN_UID"),
        @JsonSubTypes.Type(value = DeliverLobbyGood.class, name = "DELIVER_LOBBY_GOOD"),
        @JsonSubTypes.Type(value = AddonUnmatch.class, name = "ADDON_UNMATCH")
})
public interface NetEaseJsonServerEvent extends NetEaseJsonEvent {
}
