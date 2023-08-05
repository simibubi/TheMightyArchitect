package com.simibubi.mightyarchitect.block;

import com.simibubi.mightyarchitect.AllItems;
import com.simibubi.mightyarchitect.control.design.DesignSlice.DesignSliceTrait;
import com.simibubi.mightyarchitect.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class SliceMarkerBlock extends Block {

	public static final BooleanProperty compass = BooleanProperty.create("compass");
	public static final EnumProperty<DesignSliceTrait> VARIANT =
		EnumProperty.<DesignSliceTrait>create("variant", DesignSliceTrait.class);

	public SliceMarkerBlock() {
		super(Properties.copy(Blocks.STONE));
		this.registerDefaultState(defaultBlockState().setValue(VARIANT, DesignSliceTrait.Standard));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(compass, VARIANT);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (context.getLevel()
			.getBlockState(context.getClickedPos()
				.below())
			.getBlock() == this)
			return defaultBlockState().setValue(compass, false);
		return defaultBlockState().setValue(compass, true);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (hit.getDirection()
			.getAxis() == Axis.Y)
			return InteractionResult.PASS;
		if (AllItems.ARCHITECT_WAND.typeOf(player.getItemInHand(handIn)))
			return InteractionResult.PASS;
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;

		DesignSliceTrait currentTrait = state.getValue(VARIANT);
		DesignSliceTrait newTrait = currentTrait.cycle(player.isShiftKeyDown() ? -1 : 1);
		worldIn.setBlockAndUpdate(pos, state.setValue(VARIANT, newTrait));
		Lang.text(newTrait.getDescription())
			.sendStatus(player);

		return InteractionResult.SUCCESS;
	}

}
