package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.*;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CameraInstruction implements Cloneable {
    @Nullable
    public CameraSetInstruction set;
    @Nullable
    public Boolean clear;
    @Nullable
    public CameraFadeInstruction fade;
    /**
     * @since 1.21.20
     */
    @Nullable
    public CameraTargetInstruction target;
    /**
     * @since 1.21.20
     */
    @Nullable
    public Boolean removeTarget;
    /**
     * @since 1.21.100
     */
    @Nullable
    public CameraFovInstruction fieldOfView;
    /**
     * @since 1.21.120
     */
    @Nullable
    public CameraSplineInstruction spline;
    /**
     * @since 1.21.120
     */
    @Nullable
    public CameraAttachToEntityInstruction attachToEntity;
    /**
     * @since 1.21.120
     */
    @Nullable
    public Boolean detachFromEntity;

    public CameraInstruction(CameraSetInstruction set) {
        this.set = set;
    }

    public static CameraInstruction clear() {
        CameraInstruction instruction = new CameraInstruction();
        instruction.clear = true;
        return instruction;
    }

    public CameraInstruction(CameraFadeInstruction fade) {
        this.fade = fade;
    }

    public CameraInstruction(CameraTargetInstruction target) {
        this.target = target;
    }

    public static CameraInstruction removeTarget() {
        CameraInstruction instruction = new CameraInstruction();
        instruction.removeTarget = true;
        return instruction;
    }

    public CameraInstruction(CameraFovInstruction fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    public CameraInstruction(CameraSplineInstruction spline) {
        this.spline = spline;
    }

    public CameraInstruction(CameraAttachToEntityInstruction attachToEntity) {
        this.attachToEntity = attachToEntity;
    }

    public static CameraInstruction detachFromEntity() {
        CameraInstruction instruction = new CameraInstruction();
        instruction.detachFromEntity = true;
        return instruction;
    }

    @Override
    public CameraInstruction clone() {
        try {
            return (CameraInstruction) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
