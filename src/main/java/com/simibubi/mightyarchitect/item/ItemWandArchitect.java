package com.simibubi.mightyarchitect.item;

import com.simibubi.mightyarchitect.AllBlocks;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.gui.GuiDesignExporter;
import com.simibubi.mightyarchitect.gui.GuiOpener;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ItemWandArchitect extends Item {

	public ItemWandArchitect(Properties properties) {
		super(properties.maxStackSize(1).rarity(Rarity.RARE));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		World world = context.getWorld();

		if (!world.isRemote)
			return ActionResultType.SUCCESS;

		if (player.isSneaking()) {
			PhaseEditTheme.resetVisualization();
			return ActionResultType.SUCCESS;
		}

		BlockPos anchor = context.getPos();
		BlockState blockState = world.getBlockState(anchor);

		if (AllBlocks.DESIGN_ANCHOR.typeOf(blockState)) {
			if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
				return ActionResultType.FAIL;

			String name = DesignExporter.exportDesign(world, anchor);
			if (!name.isEmpty()) {
				player.sendStatusMessage(new StringTextComponent(name), true);
			}

		} else {
			if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
				return ActionResultType.FAIL;
			GuiOpener.open(new GuiDesignExporter());
		}

		player.getCooldownTracker().setCooldown(this, 5);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (worldIn.isRemote) {
			if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
				return super.onItemRightClick(worldIn, playerIn, handIn);

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
