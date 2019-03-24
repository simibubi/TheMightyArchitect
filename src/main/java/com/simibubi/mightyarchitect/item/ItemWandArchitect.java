package com.simibubi.mightyarchitect.item;

import com.simibubi.mightyarchitect.block.AllBlocks;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.gui.GuiDesignExporter;
import com.simibubi.mightyarchitect.gui.GuiOpener;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
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

			if (player.isSneaking()) {
				PhaseEditTheme.resetVisualization();
				return EnumActionResult.SUCCESS;
			}

			IBlockState blockState = worldIn.getBlockState(anchor);

			if (blockState.getBlock() == AllBlocks.design_anchor) {
				if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
					return EnumActionResult.FAIL;

				String name = DesignExporter.exportDesign(worldIn, anchor);
				if (!name.isEmpty()) {
					player.sendMessage(new TextComponentString(name));
				}

			} else {
				if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
					return EnumActionResult.FAIL;
				GuiOpener.open(new GuiDesignExporter());
			}
		}

		player.getCooldownTracker().setCooldown(this, 5);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (worldIn.isRemote) {
			if (playerIn.isSneaking()) {
				PhaseEditTheme.resetVisualization();

			} else {
				GuiOpener.open(new GuiDesignExporter());
			}
			playerIn.getCooldownTracker().setCooldown(this, 5);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

}
