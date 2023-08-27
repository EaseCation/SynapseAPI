package org.itxtech.synapseapi.multiprotocol.protocol11920.protocol;

import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.entity.attribute.AttributeModifier;
import cn.nukkit.entity.attribute.AttributeModifier.Operation;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.UpdateAttributesPacket;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.Set;

@ToString
public class UpdateAttributesPacket11920 extends Packet11920 {

    public static final int NETWORK_ID = ProtocolInfo.UPDATE_ATTRIBUTES_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public Attribute[] entries;
    public long entityId;
    public long frame;

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();

        this.putEntityRuntimeId(this.entityId);

        if (this.entries == null) {
            this.putUnsignedVarInt(0);
        } else {
            this.putUnsignedVarInt(this.entries.length);
            for (Attribute entry : this.entries) {
                this.putLFloat(entry.getMinValue());
                this.putLFloat(entry.getMaxValue());
                this.putLFloat(entry.getValue());
                this.putLFloat(entry.getDefaultValue());
                this.putString(entry.getName());

                Set<AttributeModifier> addModifiers = entry.getModifiers(Operation.ADDITION);
                Set<AttributeModifier> mulBaseModifiers = entry.getModifiers(Operation.MULTIPLY_BASE);
                Set<AttributeModifier> mulTotalModifiers = entry.getModifiers(Operation.MULTIPLY_TOTAL);
                Set<AttributeModifier> capModifiers = entry.getModifiers(Operation.CAP);
                this.putUnsignedVarInt(addModifiers.size() + mulBaseModifiers.size() + mulTotalModifiers.size() + capModifiers.size());
                this.writeAttributeModifiers(addModifiers);
                this.writeAttributeModifiers(mulBaseModifiers);
                this.writeAttributeModifiers(mulTotalModifiers);
                this.writeAttributeModifiers(capModifiers);
            }
        }

        this.putUnsignedVarLong(this.frame);
    }

    private void writeAttributeModifiers(Set<AttributeModifier> modifiers) {
        for (AttributeModifier modifier : modifiers) {
            this.putString(modifier.getId().toString());
            this.putString(modifier.getName());
            this.putLFloat(modifier.getAmount());
            this.putLInt(modifier.getOperation().ordinal());
            this.putLInt(modifier.getOperand().ordinal());
            this.putBoolean(modifier.isSerializable());
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, UpdateAttributesPacket.class);

        UpdateAttributesPacket packet = (UpdateAttributesPacket) pk;
        this.entries = packet.entries;
        this.entityId = packet.entityId;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return UpdateAttributesPacket.class;
    }
}
