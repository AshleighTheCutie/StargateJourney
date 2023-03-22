package net.povstalec.sgjourney.stargate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.povstalec.sgjourney.StargateJourney;
import net.povstalec.sgjourney.block_entities.stargate.AbstractStargateEntity;
import net.povstalec.sgjourney.data.StargateNetwork;
import net.povstalec.sgjourney.data.Universe;
import net.povstalec.sgjourney.init.TagInit;

public class Dialing
{
	private static final String EMPTY = StargateJourney.EMPTY;
	
	public static AbstractStargateEntity dialStargate(Level level, int[] address)
	{
		switch(address.length)
		{
		case 6:
			return get7ChevronStargate(level, address);
		case 7:
			return get8ChevronStargate(level, address);
		case 8:
			return get9ChevronStargate(level, address);
		default:
			return null;
		}
	}
	
	private static AbstractStargateEntity get7ChevronStargate(Level level, int[] address)
	{
		String addressString = Addressing.addressIntArrayToString(address);
		
		// List of Galaxies the dialing Dimension is located in
		ListTag galaxies = Universe.get(level).getGalaxiesFromDimension(level.dimension().location().toString());
		
		if(galaxies.isEmpty())
		{
			StargateJourney.LOGGER.info("Local Dimension is not in any galaxy");
			return null;
		}
		
		String solarSystem = EMPTY;
		
		for(int i = 0; i < galaxies.size(); i++)
		{
			String galaxy = galaxies.getCompound(i).getAllKeys().iterator().next();
			
			solarSystem = Universe.get(level).getSolarSystemInGalaxy(galaxy, addressString);
			
			if(!solarSystem.equals(EMPTY))
				break;
		}
		
		if(solarSystem.equals(EMPTY))
		{
			StargateJourney.LOGGER.info("Invalid Address");
			return null;
		}
		
		return getStargate(level, solarSystem);
	}
	
	private static AbstractStargateEntity get8ChevronStargate(Level level, int[] address)
	{
		String addressString = Addressing.addressIntArrayToString(address);
		String solarSystem = Universe.get(level).getSolarSystemFromExtragalacticAddress(addressString);
		
		if(solarSystem.equals(EMPTY))
			return null;
		
		return getStargate(level, solarSystem);
	}
	
	private static AbstractStargateEntity getStargate(Level level, String systemID)
	{
		String currentSystem = Universe.get(level).getSolarSystemFromDimension(level.dimension().location().toString());
		
		if(systemID.equals(currentSystem))
		{
			StargateJourney.LOGGER.info("Cannot dial system the Stargate is in");
			return null;
		}
		
		MinecraftServer server = level.getServer();
		
		CompoundTag solarSystem = StargateNetwork.get(server).getSolarSystem(systemID);
		
		if(solarSystem.isEmpty())
		{
			ListTag dimensionList = Universe.get(server).getDimensionsFromSolarSystem(systemID);
			
			if(dimensionList.isEmpty())
			{
				StargateJourney.LOGGER.info("The Solar System " + systemID + " has no dimensions");
				return null;
			}
			
			// Cycles through the list of Dimensions in the Solar System
			for(int i = 0; i < dimensionList.size(); i++)
			{
				Level targetLevel = server.getLevel(stringToDimension(dimensionList.getString(i)));
				findStargates(targetLevel);
			}
			
			solarSystem = StargateNetwork.get(server).getSolarSystem(systemID);
			if(solarSystem.isEmpty())
			{
				StargateJourney.LOGGER.info("No Stargates have been registered to " + systemID);
				return null;
			}
			
			solarSystem = StargateNetwork.get(server).getSolarSystem(systemID);
		}
		
		return getPreferredStargate(server, solarSystem);
	}
	
	private static void findStargates(Level level)
	{
		StargateJourney.LOGGER.info("Attempting to locate the Stargate Structure in " + level.dimension().location().toString());

		//Nearest Structure that potentially has a Stargate
		BlockPos blockpos = ((ServerLevel) level).findNearestMapStructure(TagInit.Structures.HAS_STARGATE, new BlockPos(0, 0, 0), 150, false);
		if(blockpos == null)
		{
			StargateJourney.LOGGER.info("Stargate Structure not found");
			return;
		}
		//Map of Block Entities that might contain a Stargate
		List<AbstractStargateEntity> stargates = new ArrayList<AbstractStargateEntity>();
		
		for(int x = -2; x <= 2; x++)
		{
			for(int z = -2; z <= 2; z++)
			{
				ChunkAccess chunk = level.getChunk(blockpos.east(16 * x).south(16 * z));
				Set<BlockPos> positions = chunk.getBlockEntitiesPos();
				
				positions.stream().forEach(pos ->
				{
					if(level.getBlockEntity(pos) instanceof AbstractStargateEntity stargate)
						stargates.add(stargate);
				});
			}
		}
		
		if(stargates.isEmpty())
		{
			StargateJourney.LOGGER.info("No Stargates found in Stargate Structure");
			return;
		}
		
		stargates.stream().forEach(stargate -> stargate.onLoad());
		return;
	}
	
	public static AbstractStargateEntity getStargateFromID(MinecraftServer server, String id)
	{
		CompoundTag stargateList = StargateNetwork.get(server).getStargates();
		
		if(!stargateList.contains(id))
		{
			StargateJourney.LOGGER.info("Invalid Address");
			return null;
		}
		
		BlockPos pos = intArrayToBlockPos(stargateList.getCompound(id).getIntArray("Coordinates"));
		
		if(server.getLevel(stringToDimension(stargateList.getCompound(id).getString("Dimension"))).getBlockEntity(pos) instanceof AbstractStargateEntity stargate && stargate.canConnect())
		{
			StargateJourney.LOGGER.info("Getting Stargate from 9 Chevron address " + stargate);
			return stargate;
		}
		StargateJourney.LOGGER.info("Failed to get Stargate from 9 Chevron address");
		return null;
	}
	
	public static AbstractStargateEntity get9ChevronStargate(Level level, int[] address)
	{
		String id = Addressing.addressIntArrayToString(address);
		return getStargateFromID(level.getServer(), id);
	}
	
	public static ResourceKey<Level> stringToDimension(String dimensionString)
	{
		String[] split = dimensionString.split(":");
		return ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "dimension")), new ResourceLocation(split[0], split[1]));
	}
	
	public static BlockPos intArrayToBlockPos(int[] coordinates)
	{
		return new BlockPos(coordinates[0], coordinates[1], coordinates[2]);
	}
	
	private static AbstractStargateEntity getPreferredStargate(MinecraftServer server, CompoundTag solarSystem)
	{
		while(!solarSystem.isEmpty())
		{
			String preferredStargate = StargateNetwork.get(server).getPreferredStargate(solarSystem);
			
			if(!preferredStargate.equals(EMPTY))
			{
				CompoundTag stargateInfo = solarSystem.getCompound(preferredStargate);
				
				int[] coordinates = stargateInfo.getIntArray("Coordinates");
				BlockPos pos = intArrayToBlockPos(coordinates);
				ResourceKey<Level> dimension = stringToDimension(stargateInfo.getString("Dimension"));
				ServerLevel targetLevel = server.getLevel(dimension);
				
				if(targetLevel.getBlockEntity(pos) instanceof AbstractStargateEntity stargate && stargate.canConnect())
					return stargate;
			}
			solarSystem.remove(preferredStargate);
			System.out.println("Removed " + preferredStargate);
			System.out.println(solarSystem);
		}
		StargateJourney.LOGGER.info("Could not find a suitable Stargate");
		
		return null;
	}
}
