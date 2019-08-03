package com.simibubi.mightyarchitect.item;

import com.simibubi.mightyarchitect.AllBlocks;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.gui.DesignExporterScreen;
import com.simibubi.mightyarchitect.gui.ScreenHelper;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ArchitectWandItem extends Item {

	public ArchitectWandItem(Properties properties) {
		super(properties.maxStackSize(1).rarity(Rarity.RARE));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		World world = context.getWorld();

		if (!world.isRemote)
			return ActionResultType.SUCCESS;

		if (player.isSneaking()) {
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
				openGui();
			});
			return ActionResultType.SUCCESS;
		}

		BlockPos anchor = context.getPos();
		BlockState blockState = world.getBlockState(anchor);

		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			handleUseOnDesignAnchor(player, world, anchor, blockState);
		});

		player.getCooldownTracker().setCooldown(this, 5);
		return ActionResultType.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void resetVisualization() {
		PhaseEditTheme.resetVisualization();
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void handleUseOnDesignAnchor(PlayerEntity player, World world, BlockPos anchor, BlockState blockState) {
		if (AllBlocks.DESIGN_ANCHOR.typeOf(blockState)) {
			if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
				return;

			String name = DesignExporter.exportDesign(world, anchor);
			if (!name.isEmpty()) {
				player.sendStatusMessage(new StringTextComponent(name), true);
			}

		} else {
			if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
				return;
			DistExecutor.runWhenOn(Dist.CLIENT, () -> this::resetVisualization);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (worldIn.isRemote) {
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
				handleRightClick(worldIn, playerIn, handIn);
			});
			playerIn.getCooldownTracker().setCooldown(this, 5);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void handleRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
			return;

		if (playerIn.isSneaking()) {
			openGui();

		} else {
			resetVisualization();
		}
	}
	
	@OnlyIn(value = Dist.CLIENT)
	private void openGui() {
		if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
			return;
		ScreenHelper.open(new DesignExporterScreen());
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

}
