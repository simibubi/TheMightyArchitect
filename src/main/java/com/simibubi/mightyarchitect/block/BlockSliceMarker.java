package com.simibubi.mightyarchitect.block;

import com.simibubi.mightyarchitect.control.design.DesignSlice.DesignSliceTrait;
import com.simibubi.mightyarchitect.item.AllItems;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockSliceMarker extends BlockForMightyArchitects {

	public static final PropertyEnum<DesignSliceTrait> VARIANT = PropertyEnum.<DesignSliceTrait>create("variant",
			DesignSliceTrait.class);

	public BlockSliceMarker(String name) {
		super(name, Material.ROCK);
		this.setHardness(2.0f);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, DesignSliceTrait.Standard));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (facing.getAxis() == Axis.Y)
			return false;
		if (playerIn.getHeldItem(hand).getItem() == AllItems.wand_architect)
			return false;
		if (worldIn.isRemote)
			return true;

		int meta = getMetaFromState(worldIn.getBlockState(pos));
		int max = DesignSliceTrait.values().length;
		if (!playerIn.isSneaking()) {
			meta = (meta + 1) % max;
		} else {
			meta = (meta - 1);
			if (meta < 0)
				meta = max - 1;
		}
		IBlockState stateFromMeta = getStateFromMeta(meta);
		worldIn.setBlockState(pos, stateFromMeta);
		playerIn.sendStatusMessage(new TextComponentString(
				((DesignSliceTrait) stateFromMeta.getProperties().get(VARIANT)).getDescription()), true);

		return true;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((DesignSliceTrait) state.getProperties().get(VARIANT)).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(VARIANT, DesignSliceTrait.values()[meta]);
	}

}
