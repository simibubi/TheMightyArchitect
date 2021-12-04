package com.simibubi.mightyarchitect.block;

import com.simibubi.mightyarchitect.AllItems;
import com.simibubi.mightyarchitect.control.design.DesignSlice.DesignSliceTrait;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class SliceMarkerBlock extends Block {

	public static final BooleanProperty compass = BooleanProperty.create("compass");
	public static final EnumProperty<DesignSliceTrait> VARIANT = EnumProperty.<DesignSliceTrait>create("variant",
			DesignSliceTrait.class);

	public SliceMarkerBlock() {
		super(Properties.of(Material.STONE));
		this.registerDefaultState(defaultBlockState().setValue(VARIANT, DesignSliceTrait.Standard));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(compass, VARIANT);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		if (context.getLevel().getBlockState(context.getClickedPos().below()).getBlock() == this)
			return defaultBlockState().setValue(compass, false);
		return defaultBlockState().setValue(compass, true);
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (hit.getDirection().getAxis() == Axis.Y)
			return ActionResultType.PASS;
		if (AllItems.ARCHITECT_WAND.typeOf(player.getItemInHand(handIn)))
			return ActionResultType.PASS;
		if (worldIn.isClientSide)
			return ActionResultType.SUCCESS;

		DesignSliceTrait currentTrait = state.getValue(VARIANT);
		DesignSliceTrait newTrait = currentTrait.cycle(player.isShiftKeyDown() ? -1 : 1);
		worldIn.setBlockAndUpdate(pos, state.setValue(VARIANT, newTrait));
		player.displayClientMessage(new StringTextComponent(newTrait.getDescription()), true);

		return ActionResultType.SUCCESS;
	}

}
