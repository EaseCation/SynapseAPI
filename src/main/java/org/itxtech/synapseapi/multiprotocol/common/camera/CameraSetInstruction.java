package org.itxtech.synapseapi.multiprotocol.common.camera;

import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraSetInstruction {

    public CameraPreset preset;
    @Nullable
    public CameraEase ease;

    @Nullable
    public Vector3f pos;
    @Nullable
    public Vector2f rot;
    @Nullable
    public Vector3f facing;
    /**
     * @since 1.21.20
     */
    @Nullable
    public Vector2f viewOffset;
    /**
     * @since 1.21.40
     */
    @Nullable
    public Vector3f entityOffset;
    @Nullable
    public Boolean defaultPreset;
    /**
     * @since 1.21.90
     */
    public boolean removeIgnoreStartingValuesComponent;
}
