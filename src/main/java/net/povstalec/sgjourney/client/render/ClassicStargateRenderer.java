package net.povstalec.sgjourney.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.povstalec.sgjourney.block_entities.stargate.ClassicStargateEntity;
import net.povstalec.sgjourney.blocks.stargate.ClassicStargateBlock;
import net.povstalec.sgjourney.client.Layers;
import net.povstalec.sgjourney.client.models.ClassicStargateModel;
import net.povstalec.sgjourney.client.models.WormholeModel;
import net.povstalec.sgjourney.config.ClientStargateConfig;

@OnlyIn(Dist.CLIENT)
public class ClassicStargateRenderer extends AbstractStargateRenderer implements BlockEntityRenderer<ClassicStargateEntity>
{
	protected static final int r = ClientStargateConfig.classic_r.get();
	protected static final int g = ClientStargateConfig.classic_g.get();
	protected static final int b = ClientStargateConfig.classic_b.get();
	
	protected final WormholeModel wormholeModel;
	protected final ClassicStargateModel stargateModel;
	
	public ClassicStargateRenderer(BlockEntityRendererProvider.Context context)
	{
		super(context);
		this.wormholeModel = new WormholeModel(context.bakeLayer(Layers.EVENT_HORIZON_LAYER), r, g, b);
		this.stargateModel = new ClassicStargateModel(
				context.bakeLayer(Layers.CLASSIC_OUTER_RING_LAYER), 
				context.bakeLayer(Layers.CLASSIC_INNER_RING_LAYER), 
				context.bakeLayer(Layers.CLASSIC_CHEVRON_LAYER));
	}
	
	@Override
	public void render(ClassicStargateEntity stargate, float partialTick, PoseStack stack,
			MultiBufferSource source, int combinedLight, int combinedOverlay)
	{
		BlockState blockstate = stargate.getBlockState();
		float facing = blockstate.getValue(ClassicStargateBlock.FACING).toYRot();
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
