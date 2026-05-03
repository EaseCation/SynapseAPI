package org.itxtech.synapseapi.multiprotocol.common.block;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VoxelShape {
    /**
     * Number of cells along the X axis.
     */
    public int xSize;
    /**
     * Number of cells along the Y axis.
     */
    public int ySize;
    /**
     * Number of cells along the Z axis.
     */
    public int zSize;
    /**
     * Solid/empty state per cell.
     */
    public byte[] storage;

    /**
     * Cell boundaries along the X axis.
     */
    public float[] xCoordinates;
    /**
     * Cell boundaries along the Y axis.
     */
    public float[] yCoordinates;
    /**
     * Cell boundaries along the Z axis.
     */
    public float[] zCoordinates;
}
