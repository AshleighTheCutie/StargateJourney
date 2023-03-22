package net.povstalec.sgjourney.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.povstalec.sgjourney.init.BlockEntityInit;

public class ClassicDHDEntity extends AbstractDHDEntity
{
	public ClassicDHDEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityInit.CLASSIC_DHD.get(), pos, state);
	}
	
	@Override
	public boolean isCorrectSide(Direction side)
	{
		return side == Direction.DOWN;
	}
}
