package net.povstalec.sgjourney.common.structures;

import java.util.Optional;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.povstalec.sgjourney.common.config.CommonGenerationConfig;
import net.povstalec.sgjourney.common.init.StructureInit;
import net.povstalec.sgjourney.common.misc.SGJourneyJigsawPlacement;

//Structure class is mostly copy-pasted from https://github.com/TelepathicGrunt/StructureTutorialMod/blob/1.19.0-Forge-Jigsaw/src/main/java/com/telepathicgrunt/structuretutorial/StructureTutorialMain.java
public class StargateTemple extends Structure
{
    public static final Codec<StargateTemple> CODEC = RecordCodecBuilder.<StargateTemple>mapCodec(instance ->
            instance.group(StargateTemple.settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
            ).apply(instance, StargateTemple::new)).codec();

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public StargateTemple(Structure.StructureSettings config,
                         Holder<StructureTemplatePool> startPool,
                         Optional<ResourceLocation> startJigsawName,
                         int size,
                         HeightProvider startHeight,
                         Optional<Heightmap.Types> projectStartToHeightmap,
                         int maxDistanceFromCenter)
    {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }
    
    private static boolean extraSpawningChecks(Structure.GenerationContext context)
    {
        // Grabs the chunk position we are at
    	 ChunkPos chunkpos = context.chunkPos();
         Random random = new Random(context.seed() + 1);
         
         int xOffset = CommonGenerationConfig.stargate_generation_center_x_chunk_offset.get();
         int zOffset = CommonGenerationConfig.stargate_generation_center_z_chunk_offset.get();
         
         int xBound = CommonGenerationConfig.stargate_generation_x_bound.get();
         int zBound = CommonGenerationConfig.stargate_generation_z_bound.get();
         
         int chunkX = xBound <= 0 ? xOffset : xOffset + random.nextInt(-xBound, xBound + 1);
         int chunkZ = zBound <= 0 ? zOffset : zOffset + random.nextInt(-zBound, zBound + 1);
         
         if(chunkpos.x == chunkX && chunkpos.z == chunkZ)
         	return true;
         else
         	return false;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context)
    {
        if (!StargateTemple.extraSpawningChecks(context))
        {
            return Optional.empty();
        }
        int startY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

        // Turns the chunk coordinates into actual coordinates we can use. (Gets corner of that chunk)
        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

        Optional<Structure.GenerationStub> structurePiecesGenerator =
        		SGJourneyJigsawPlacement.addPieces(
                        context,
                        this.startPool,
                        this.startJigsawName,
                        this.size,
                        blockPos,
                        false,
                        this.projectStartToHeightmap,
                        this.maxDistanceFromCenter,
                        Rotation.COUNTERCLOCKWISE_90);
        
        return structurePiecesGenerator;
    }

    @Override
    public StructureType<?> type()
    {
        return StructureInit.STARGATE_TEMPLE.get(); // Helps the game know how to turn this structure back to json to save to chunks
    }
}
