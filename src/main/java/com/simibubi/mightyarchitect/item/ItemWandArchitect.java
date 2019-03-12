package com.simibubi.mightyarchitect.item;

import com.simibubi.mightyarchitect.block.AllBlocks;
import com.simibubi.mightyarchitect.buildomatico.DesignExporter;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;
import com.simibubi.mightyarchitect.gui.GuiDesignExporter;
import com.simibubi.mightyarchitect.gui.GuiOpener;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemWandArchitect extends ItemForMightyArchitects {

	public ItemWandArchitect(String name) {
		super(name);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos anchor, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			IBlockState blockState = worldIn.getBlockState(anchor);

			if (blockState.getBlock() == AllBlocks.slice_marker) {
				ItemStack heldItem = player.getHeldItem(hand);
				if (heldItem.hasTagCompound()) {
					String name = DesignExporter.exportDesign(worldIn, anchor, heldItem);
					if (!name.isEmpty()) {
						player.sendMessage(new TextComponentString("Exported new Design: " + name));
					}					
				}
			} else if (blockState.getBlock() == Blocks.DIAMOND_BLOCK) {
				for (DesignTheme theme : DesignTheme.values()) {
					theme.clearDesigns();
				}
				player.sendMessage(new TextComponentString("Reloading desings..."));
			} else {
				GuiOpener.open(new GuiDesignExporter());
			}
		}
		
		player.getCooldownTracker().setCooldown(this, 10);
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

}
