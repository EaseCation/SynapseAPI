package org.itxtech.synapseapi.multiprotocol.protocol12610.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.EasingType;
import org.itxtech.synapseapi.multiprotocol.common.environment.*;

/**
 * Syncs Attribute Layers to the Client.
 */
@ToString
public class ClientboundAttributeLayerSyncPacket12610 extends Packet12610 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_ATTRIBUTE_LAYER_SYNC_PACKET;

    public SyncAttributeLayerAction action;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();

        SyncAttributeLayerActionType type = action.getType();
        putUnsignedVarInt(type.ordinal());
        switch (type) {
            case UPDATE_ATTRIBUTE_LAYERS -> {
                putArray(((UpdateAttributeLayersAction) action).layers, (stream, layer) -> {
                    stream.putString(layer.name);
                    stream.putVarInt(layer.dimension);
                    putAttributeLayerSettings(stream, layer.settings);
                    stream.putArray(layer.attributes, ClientboundAttributeLayerSyncPacket12610::putEnvironmentAttribute);
                });
            }
            case UPDATE_ATTRIBUTE_LAYER_SETTINGS -> {
                UpdateAttributeLayerSettingsAction update = (UpdateAttributeLayerSettingsAction) action;
                putString(update.name);
                putVarInt(update.dimension);
                putAttributeLayerSettings(this, update.settings);
            }
            case UPDATE_ENVIRONMENT_ATTRIBUTES -> {
                UpdateEnvironmentAttributesAction update = (UpdateEnvironmentAttributesAction) action;
                putString(update.name);
                putVarInt(update.dimension);
                putArray(update.attributes, ClientboundAttributeLayerSyncPacket12610::putEnvironmentAttribute);
            }
            case REMOVE_ENVIRONMENT_ATTRIBUTES -> {
                RemoveEnvironmentAttributesAction remove = (RemoveEnvironmentAttributesAction) action;
                putString(remove.name);
                putVarInt(remove.dimension);
                putArray(remove.attributes, BinaryStream::putString);

            }
        }
    }

    private static void putAttributeLayerSettings(BinaryStream stream, AttributeLayerSettings settings) {
        stream.putLInt(settings.priority);

        WeightData weight = settings.weight;
        WeightDataType type = weight.getType();
        stream.putUnsignedVarInt(type.ordinal());
        switch (type) {
            case FLOAT -> stream.putLFloat(((FloatWeightData) weight).value());
            case STRING -> stream.putString(((StringWeightData) weight).value());
        }

        stream.putBoolean(settings.enabled);
        stream.putBoolean(settings.transitionsPaused);
    }

    private static void putEnvironmentAttribute(BinaryStream stream, EnvironmentAttribute attribute) {
        stream.putString(attribute.name);
        stream.putOptional(attribute.fromAttribute, ClientboundAttributeLayerSyncPacket12610::putAttributeValue);
        putAttributeValue(stream, attribute.attribute);
        stream.putOptional(attribute.toAttribute, ClientboundAttributeLayerSyncPacket12610::putAttributeValue);
        stream.putLInt(attribute.currentTransitionTicks);
        stream.putLInt(attribute.totalTransitionTicks);
        stream.putEnum(attribute.easing, EasingType::getName);
    }

    private static void putAttributeValue(BinaryStream stream, AttributeValue data) {
        AttributeValueType type = data.getType();
        stream.putUnsignedVarInt(type.ordinal());
        switch (type) {
            case BOOLEAN -> {
                BooleanAttributeValue bool = (BooleanAttributeValue) data;
                stream.putBoolean(bool.value);
                stream.putEnum(bool.operation, BooleanAttributeValue.Operation::getName);
            }
            case FLOAT -> {
                FloatAttributeValue number = (FloatAttributeValue) data;
                stream.putLFloat(number.value);
                stream.putEnum(number.operation, FloatAttributeValue.Operation::getName);
                stream.putOptional(number.constraintMin, BinaryStream::putLFloat);
                stream.putOptional(number.constraintMax, BinaryStream::putLFloat);
            }
            case COLOR -> {
                ColorAttributeValue color = (ColorAttributeValue) data;

                ColorData value = color.value;
                ColorDataType colorType = value.getType();
                stream.putUnsignedVarInt(colorType.ordinal());
                switch (colorType) {
                    case STRING -> stream.putString(((StringColorData) value).value());
                    case ARRAY -> {
                        ArrayColorData arrayColor = (ArrayColorData) value;
                        stream.putLInt(arrayColor.r());
                        stream.putLInt(arrayColor.g());
                        stream.putLInt(arrayColor.b());
                        stream.putLInt(arrayColor.a());
                    }
                }

                stream.putEnum(color.operation, ColorAttributeValue.Operation::getName);
            }
        }
    }
}
