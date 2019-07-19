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
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class SliceMarkerBlock extends Block {

	public static final BooleanProperty compass = BooleanProperty.create("compass");
	public static final EnumProperty<DesignSliceTrait> VARIANT = EnumProperty.<DesignSliceTrait>create("variant",
			DesignSliceTrait.class);

	public SliceMarkerBlock() {
		super(Properties.create(Material.ROCK));
		this.setDefaultState(getDefaultState().with(VARIANT, DesignSliceTrait.Standard));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(compass, VARIANT);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		if (context.getWorld().getBlockState(context.getPos().down()).getBlock() == this)
			return getDefaultState().with(compass, false);
		return getDefaultState().with(compass, true);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (hit.getFace().getAxis() == Axis.Y)
			return false;
		if (AllItems.ARCHITECT_WAND.typeOf(player.getHeldItem(handIn)))
			return false;
		if (worldIn.isRemote)
			return true;

		DesignSliceTrait currentTrait = state.get(VARIANT);
		DesignSliceTrait newTrait = currentTrait.cycle(player.isSneaking() ? -1 : 1);
		worldIn.setBlockState(pos, state.with(VARIANT, newTrait));
		player.sendStatusMessage(new StringTextComponent(newTrait.getDescription()), true);

		return true;
	}

}
