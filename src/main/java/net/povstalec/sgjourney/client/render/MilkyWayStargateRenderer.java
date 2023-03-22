package net.povstalec.sgjourney.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.povstalec.sgjourney.block_entities.stargate.MilkyWayStargateEntity;
import net.povstalec.sgjourney.blocks.stargate.MilkyWayStargateBlock;
import net.povstalec.sgjourney.client.Layers;
import net.povstalec.sgjourney.client.models.WormholeModel;
import net.povstalec.sgjourney.config.ClientStargateConfig;
import net.povstalec.sgjourney.client.models.MilkyWayStargateModel;

@OnlyIn(Dist.CLIENT)
public class MilkyWayStargateRenderer extends AbstractStargateRenderer implements BlockEntityRenderer<MilkyWayStargateEntity>
{
	protected static final int r = ClientStargateConfig.milky_way_r.get();
	protected static final int g = ClientStargateConfig.milky_way_g.get();
	protected static final int b = ClientStargateConfig.milky_way_b.get();
	
	protected final WormholeModel wormholeModel;
	protected final MilkyWayStargateModel stargateModel;
	
	public MilkyWayStargateRenderer(BlockEntityRendererProvider.Context context)
	{
		super(context);
		this.wormholeModel = new WormholeModel(context.bakeLayer(Layers.EVENT_HORIZON_LAYER), r, g, b);
		this.stargateModel = new MilkyWayStargateModel(
				context.bakeLayer(Layers.MILKY_WAY_RING_LAYER), 
				context.bakeLayer(Layers.MILKY_WAY_SYMBOL_RING_LAYER), 
				context.bakeLayer(Layers.MILKY_WAY_DIVIDER_LAYER), 
				context.bakeLayer(Layers.MILKY_WAY_CHEVRON_LAYER));
	}
	
	@Override
	public void render(MilkyWayStargateEntity stargate, float partialTick, PoseStack stack,
			MultiBufferSource source, int combinedLight, int combinedOverlay)
	{
		BlockState blockstate = stargate.getBlockState();
		float facing = blockstate.getValue(MilkyWayStargateBlock.FACING).toYRot();
        stack.pushPose();
		stack.translate(0.5D, 3.5D, 0.5D);
        stack.mulPose(Axis.YP.rotationDegrees(-facing));
		
        this.stargateModel.setRotation(stargate.getRotation(partialTick));
        this.stargateModel.renderStargate(stargate, partialTick, stack, source, combinedLight, combinedOverlay);
        
        if(stargate.isConnected())
	    	this.wormholeModel.renderEventHorizon(stack, source, combinedLight, combinedOverlay, stargate.getTickCount());
	    
	    stack.popPose();
	}
	
	@Override
	public int getViewDistance()
	{
		return 128;
	}
	
}
