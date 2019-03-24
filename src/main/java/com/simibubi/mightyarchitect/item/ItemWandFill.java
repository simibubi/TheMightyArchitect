package com.simibubi.mightyarchitect.item;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.BlockColored;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemWandFill extends ItemForMightyArchitects {

	public ItemWandFill(String name) {
		super(name);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

//		if (worldIn.isRemote) {
//			return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
//		}

		BlockPos start = pos.offset(facing);
		Set<BlockPos> filledBlocks = new HashSet<>();
		Set<BlockPos> checked = new HashSet<>();
		List<BlockPos> toCheck = new LinkedList<>();
		toCheck.add(start);
		int limit = 10000;
		
		checked.add(start);						
		for (int i = 0; i <= limit; i++) {
			if (i == limit)
				return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
			if (toCheck.isEmpty())
				break;
			
			BlockPos checkedPos = toCheck.get(0);
			
			if (worldIn.getBlockState(checkedPos).getBlock().isReplaceable(worldIn, checkedPos)) {
				for (EnumFacing offset : EnumFacing.values()) {
					if (facing.getAxis() == offset.getAxis())
						continue;
					BlockPos newCheckedPos = checkedPos.offset(offset);
					if (!checked.contains(newCheckedPos)) {
						toCheck.add(newCheckedPos);
						checked.add(newCheckedPos);						
					}
				}
				
				filledBlocks.add(checkedPos);
			}
			
			toCheck.remove(checkedPos);
			
		}

		for (BlockPos position : filledBlocks)
			worldIn.setBlockState(position,
					Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.RED));

		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

}
