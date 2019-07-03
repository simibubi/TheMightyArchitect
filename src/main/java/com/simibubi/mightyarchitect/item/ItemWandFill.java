package com.simibubi.mightyarchitect.item;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class ItemWandFill extends Item {

	public ItemWandFill(Properties properties) {
		super(properties.maxStackSize(1));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		BlockPos start = context.getPos().offset(context.getFace());
		Set<BlockPos> filledBlocks = new HashSet<>();
		Set<BlockPos> checked = new HashSet<>();
		List<BlockPos> toCheck = new LinkedList<>();
		toCheck.add(start);
		int limit = 10000;

		checked.add(start);
		for (int i = 0; i <= limit; i++) {
			if (i == limit)
				return super.onItemUse(context);
			if (toCheck.isEmpty())
				break;

			BlockPos checkedPos = toCheck.get(0);

			if (context.getWorld().getBlockState(checkedPos).isReplaceable(new BlockItemUseContext(context))) {
				for (Direction offset : Direction.values()) {
					if (context.getFace().getAxis() == offset.getAxis())
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
			context.getWorld().setBlockState(position, Blocks.RED_STAINED_GLASS.getDefaultState());
		return super.onItemUse(context);
	}

}
