package com.simibubi.mightyarchitect.item;

import com.simibubi.mightyarchitect.AllBlocks;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.gui.DesignExporterScreen;
import com.simibubi.mightyarchitect.gui.ScreenHelper;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import net.minecraft.world.item.Item.Properties;

public class ArchitectWandItem extends Item {

	public ArchitectWandItem(Properties properties) {
		super(properties.stacksTo(1).rarity(Rarity.RARE));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level world = context.getLevel();

		if (!world.isClientSide)
			return InteractionResult.SUCCESS;

		if (player.isShiftKeyDown()) {
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
				openGui();
			});
			return InteractionResult.SUCCESS;
		}

		BlockPos anchor = context.getClickedPos();
		BlockState blockState = world.getBlockState(anchor);

		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			handleUseOnDesignAnchor(player, world, anchor, blockState);
		});

		player.getCooldowns().addCooldown(this, 5);
		return InteractionResult.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void resetVisualization() {
		PhaseEditTheme.resetVisualization();
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void handleUseOnDesignAnchor(Player player, Level world, BlockPos anchor, BlockState blockState) {
		if (AllBlocks.DESIGN_ANCHOR.typeOf(blockState)) {
			if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
				return;

			String name = DesignExporter.exportDesign(world, anchor);
			if (!name.isEmpty()) {
				player.displayClientMessage(new TextComponent(name), true);
			}

		} else {
			if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
				return;
			DistExecutor.runWhenOn(Dist.CLIENT, () -> this::resetVisualization);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (worldIn.isClientSide) {
			DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
				handleRightClick(worldIn, playerIn, handIn);
			});
			playerIn.getCooldowns().addCooldown(this, 5);
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void handleRightClick(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
			return;

		if (playerIn.isShiftKeyDown()) {
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
