package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.block.Block;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.SequencedHashSet;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.biome.*;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;

import java.util.Map;
import java.util.Map.Entry;

@ToString
public class BiomeDefinitionListPacket12630 extends Packet12630 {
    public static final int NETWORK_ID = ProtocolInfo.BIOME_DEFINITION_LIST_PACKET;

    public Map<String, BiomeDefinitionData> biomes;
    private final SequencedHashSet<String> strings = new SequencedHashSet<>();

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();

        this.putUnsignedVarInt(biomes.size());
        for (Entry<String, BiomeDefinitionData> entry : biomes.entrySet()) {
            this.putLShort(strings.getOrAdd(entry.getKey()));

            BiomeDefinitionData definition = entry.getValue();
            this.putLShort(definition.id != null ? definition.id : -1);
            this.putLFloat(definition.temperature);
            this.putLFloat(definition.downfall);
            this.putLFloat(definition.foliageSnow);
            this.putLFloat(definition.depth);
            this.putLFloat(definition.scale);
            this.putLInt(definition.mapWaterColorARGB);
            this.putBoolean(definition.rain);

            if (neteaseMode) {
                this.putLInt(definition.dimension);
                this.putString(definition.vanilla);
            }

            this.putOptional(definition.tags, (stream, tags) -> {
                putUnsignedVarInt(tags.size());
                for (String tag : tags) {
                    stream.putLShort(strings.getOrAdd(tag));
                }
            });

            this.putOptional(definition.chunkGenData, (stream, chunkGenData) -> {
                stream.putOptional(chunkGenData.climate, (bs, climate) -> {
                    bs.putLFloat(climate.temperature);
                    bs.putLFloat(climate.downfall);
                    bs.putLFloat(climate.minSnowAccumulation);
                    bs.putLFloat(climate.maxSnowAccumulation);
                });

                stream.putOptional(chunkGenData.consolidatedFeatures, (bs, consolidatedFeatures) -> {
                    bs.putUnsignedVarInt(consolidatedFeatures.size());
                    for (BiomeConsolidatedFeatureData consolidatedFeature : consolidatedFeatures) {
                        bs.putUnsignedVarInt(consolidatedFeature.scatter.coordinates.size());
                        for (BiomeCoordinateData coordinate : consolidatedFeature.scatter.coordinates) {
                            bs.putVarInt(coordinate.minValueType.getId());
                            bs.putLShort(strings.getOrAdd(coordinate.minValue));
                            bs.putVarInt(coordinate.maxValueType.getId());
                            bs.putLShort(strings.getOrAdd(coordinate.maxValue));
                            bs.putLInt(coordinate.gridOffset);
                            bs.putLInt(coordinate.gridStepSize);
                            bs.putVarInt(coordinate.distribution.ordinal());
                        }

                        bs.putVarInt(consolidatedFeature.scatter.evalOrder.ordinal());
                        bs.putVarInt(consolidatedFeature.scatter.chancePercentType.getId());
                        bs.putLShort(strings.getOrAdd(consolidatedFeature.scatter.chancePercent));
                        bs.putLInt(consolidatedFeature.scatter.chanceNumerator);
                        bs.putLInt(consolidatedFeature.scatter.chanceDenominator);
                        bs.putVarInt(consolidatedFeature.scatter.iterationsType.getId());
                        bs.putLShort(strings.getOrAdd(consolidatedFeature.scatter.iterations));

                        bs.putLShort(strings.getOrAdd(consolidatedFeature.feature));
                        bs.putLShort(strings.getOrAdd(consolidatedFeature.identifier));
                        bs.putLShort(strings.getOrAdd(consolidatedFeature.pass));
                        bs.putBoolean(consolidatedFeature.canUseInternal);
                    }
                });

                stream.putOptional(chunkGenData.mountainParams, (bs, mountainParams) -> {
                    putBlockNetId(mountainParams.steepBlock);
                    bs.putBoolean(mountainParams.northSlopes);
                    bs.putBoolean(mountainParams.southSlopes);
                    bs.putBoolean(mountainParams.westSlopes);
                    bs.putBoolean(mountainParams.eastSlopes);
                    bs.putBoolean(mountainParams.topSlideEnabled);
                });

                stream.putOptional(chunkGenData.surfaceMaterialAdjustments, (bs, surfaceMaterialAdjustments) -> {
                    bs.putUnsignedVarInt(surfaceMaterialAdjustments.size());
                    for (BiomeSurfaceMaterialAdjustmentData element : surfaceMaterialAdjustments) {
                        bs.putLFloat(element.noiseFreqScale);
                        bs.putLFloat(element.noiseLowerBound);
                        bs.putLFloat(element.noiseUpperBound);
                        bs.putVarInt(element.minHeightType.getId());
                        bs.putLShort(strings.getOrAdd(element.minHeight));
                        bs.putVarInt(element.maxHeightType.getId());
                        bs.putLShort(strings.getOrAdd(element.maxHeight));
                        putBiomeSurfaceMaterialData(element.adjustedMaterials);
                    }
                });

                stream.putOptional(chunkGenData.overworldGenRules, (bs, overworldGenRules) -> {
                    bs.putUnsignedVarInt(overworldGenRules.hillsTransformations.size());
                    for (BiomeWeightedData data : overworldGenRules.hillsTransformations) {
                        putBiomeWeightedData(data);
                    }

                    bs.putUnsignedVarInt(overworldGenRules.mutateTransformations.size());
                    for (BiomeWeightedData data : overworldGenRules.mutateTransformations) {
                        putBiomeWeightedData(data);
                    }

                    bs.putUnsignedVarInt(overworldGenRules.riverTransformations.size());
                    for (BiomeWeightedData data : overworldGenRules.riverTransformations) {
                        putBiomeWeightedData(data);
                    }

                    bs.putUnsignedVarInt(overworldGenRules.shoreTransformations.size());
                    for (BiomeWeightedData data : overworldGenRules.shoreTransformations) {
                        putBiomeWeightedData(data);
                    }

                    bs.putUnsignedVarInt(overworldGenRules.preHillsEdge.size());
                    for (BiomeConditionalTransformationData data : overworldGenRules.preHillsEdge) {
                        putBiomeConditionalTransformationData(data);
                    }

                    bs.putUnsignedVarInt(overworldGenRules.postShoreEdge.size());
                    for (BiomeConditionalTransformationData data : overworldGenRules.postShoreEdge) {
                        putBiomeConditionalTransformationData(data);
                    }

                    bs.putUnsignedVarInt(overworldGenRules.climate.size());
                    for (BiomeWeightedTemperatureData data : overworldGenRules.climate) {
                        bs.putVarInt(data.temperatureCategory.ordinal());
                        bs.putLInt(data.weight);
                    }
                });

                stream.putOptional(chunkGenData.multinoiseGenRules, (bs, multinoiseGenRules) -> {
                    bs.putLFloat(multinoiseGenRules.temperature);
                    bs.putLFloat(multinoiseGenRules.humidity);
                    bs.putLFloat(multinoiseGenRules.altitude);
                    bs.putLFloat(multinoiseGenRules.weirdness);
                    bs.putLFloat(multinoiseGenRules.weight);
                });

                stream.putOptional(chunkGenData.legacyPreHillsEdge, (bs, legacyPreHillsEdge) -> {
                    bs.putUnsignedVarInt(legacyPreHillsEdge.size());
                    for (BiomeConditionalTransformationData data : legacyPreHillsEdge) {
                        putBiomeConditionalTransformationData(data);
                    }
                });

                stream.putOptional(chunkGenData.replaceBiomes, (bs, replaceBiomes) -> {
                    bs.putUnsignedVarInt(replaceBiomes.size());
                    for (BiomeReplacementData data : replaceBiomes) {
                        bs.putLShort(strings.getOrAdd(data.replacementBiome));
                        bs.putLShort(strings.getOrAdd(data.dimension));
                        bs.putUnsignedVarInt(data.targetBiomes.size());
                        for (String targetBiome : data.targetBiomes) {
                            bs.putLShort(strings.getOrAdd(targetBiome));
                        }
                        bs.putLFloat(data.amount);
                        bs.putLFloat(data.noiseFrequencyScale);
                        bs.putLInt(data.replacementIndex);
                    }
                });

                stream.putOptional(chunkGenData.villageType, (bs, type) -> bs.putByte(type.ordinal()));

                stream.putOptional(chunkGenData.surfaceBuilder, (bs, surfaceBuilder) -> putBiomeSurfaceBuilderData(surfaceBuilder));
                stream.putOptional(chunkGenData.subSurfaceBuilder, (bs, subSurfaceBuilder) -> putBiomeSurfaceBuilderData(subSurfaceBuilder));
            });
        }

        this.putUnsignedVarInt(strings.size());
        for (String string : strings) {
            this.putString(string);
        }
    }

    protected void putBlockNetId(Block block) {
        this.putLInt(AdvancedGlobalBlockPalette.getOrCreateRuntimeId((AbstractProtocol) helper.getProtocol(), neteaseMode, block.getId(), block.getDamage()));
    }

    protected void putBiomeSurfaceMaterialData(BiomeSurfaceMaterialData data) {
        this.putBlockNetId(data.topBlock);
        this.putBlockNetId(data.midBlock);
        this.putBlockNetId(data.seaFloorBlock);
        this.putBlockNetId(data.foundationBlock);
        this.putBlockNetId(data.seaBlock);
        this.putLInt(data.seaFloorDepth);
    }

    protected void putBiomeWeightedData(BiomeWeightedData data) {
        this.putLShort(strings.getOrAdd(data.biomeIdentifier));
        this.putLInt(data.weight);
    }

    protected void putBiomeConditionalTransformationData(BiomeConditionalTransformationData data) {
        this.putUnsignedVarInt(data.transformsInto.size());
        for (BiomeWeightedData d : data.transformsInto) {
            this.putBiomeWeightedData(d);
        }

        this.putLShort(strings.getOrAdd(data.conditionJson));
        this.putLInt(data.minPassingNeighbors);
    }

    protected void putBiomeSurfaceBuilderData(BiomeSurfaceBuilderData data) {
        this.putOptional(data.surfaceMaterials, (bs, surfaceMaterials) -> putBiomeSurfaceMaterialData(surfaceMaterials));

        this.putBoolean(data.hasDefaultOverworldSurface);
        this.putBoolean(data.hasSwampSurface);
        this.putBoolean(data.hasFrozenOceanSurface);
        this.putBoolean(data.hasTheEndSurface);

        this.putOptional(data.mesaSurface, (bs, mesaSurface) -> {
            this.putBlockNetId(mesaSurface.clayMaterial);
            this.putBlockNetId(mesaSurface.hardClayMaterial);
            bs.putBoolean(mesaSurface.brycePillars);
            bs.putBoolean(mesaSurface.hasForest);
        });

        this.putOptional(data.cappedSurface, (bs, cappedSurface) -> {
            bs.putUnsignedVarInt(cappedSurface.floorBlocks.size());
            for (Block block : cappedSurface.floorBlocks) {
                this.putBlockNetId(block);
            }

            bs.putUnsignedVarInt(cappedSurface.ceilingBlocks.size());
            for (Block block : cappedSurface.ceilingBlocks) {
                this.putBlockNetId(block);
            }

            bs.putOptional(cappedSurface.seaBlock, (s, block) -> this.putBlockNetId(block));
            bs.putOptional(cappedSurface.foundationBlock, (s, block) -> this.putBlockNetId(block));
            bs.putOptional(cappedSurface.beachBlock, (s, block) -> this.putBlockNetId(block));
        });

        this.putOptional(data.noiseGradientSurface, (bs, noiseGradientSurface) -> {
            bs.putUnsignedVarInt(noiseGradientSurface.nonReplaceableBlocks.size());
            for (Block block : noiseGradientSurface.nonReplaceableBlocks) {
                this.putBlockNetId(block);
            }

            bs.putUnsignedVarInt(noiseGradientSurface.gradientBlocks.size());
            for (SerializedNoiseBlockSpecifier specifier : noiseGradientSurface.gradientBlocks) {
                bs.putString(specifier.noise);
                bs.putLFloat(specifier.threshold);
                bs.putVector2f(specifier.range);
                this.putBlockNetId(specifier.block);
            }

            bs.putString(noiseGradientSurface.noiseSeedString);

            bs.putLInt(noiseGradientSurface.firstOctave);

            bs.putUnsignedVarInt(noiseGradientSurface.amplitudes.size());
            for (float value : noiseGradientSurface.amplitudes) {
                bs.putLFloat(value);
            }
        });
    }
}
