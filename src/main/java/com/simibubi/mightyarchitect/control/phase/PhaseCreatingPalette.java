package com.simibubi.mightyarchitect.control.phase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.Schematic;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.foundation.utility.RaycastHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.ForgeMod;

public class PhaseCreatingPalette extends PhaseBase implements IDrawBlockHighlights {

	private PaletteDefinition palette;
	private BlockPos center;
	private Map<BlockPos, Palette> grid;
	private boolean[] changed;

	@Override
	public void whenEntered() {

		Schematic model = getModel();
		ClientWorld world = minecraft.level;
		changed = new boolean[16];

		palette = model.getCreatedPalette();
		center = world.getHeightmapPos(Heightmap.Type.WORLD_SURFACE, minecraft.player.blockPosition());
		grid = new HashMap<>();

		for (int i = 0; i < 16; i++) {
			BlockPos pos = positionFromIndex(i);
			grid.put(pos, Palette.values()[i]);
			if (!world.isEmptyBlock(pos) && palette.get(Palette.values()[i]) != world.getBlockState(pos)) {
				palette.put(Palette.values()[i], world.getBlockState(pos));
				changed[i] = true;
			}
		}

		model.updatePalettePreview();
		MightyClient.renderer.display(getModel());
	}

	@Override
	public void update() {
		for (int i = 0; i < 16; i++) {
			BlockPos pos = positionFromIndex(i);

			// Handle changes
			if (minecraft.level.isEmptyBlock(pos)) {
				PaletteDefinition paletteDef =
					getModel().isEditingPrimary() ? getModel().getPrimary() : getModel().getSecondary();
				Palette key = grid.get(pos);

				if (paletteDef.get(key) != palette.get(key)) {
					palette.put(key, paletteDef.get(key));
					changed[i] = false;
					notifyChange();
				}

				continue;
			}

			BlockState state = minecraft.level.getBlockState(pos);
			if (state.getBlock() instanceof TrapDoorBlock)
				state = state.setValue(TrapDoorBlock.OPEN, true);

			if (palette.get(Palette.values()[i]) != state) {
				palette.put(grid.get(pos), state);
				changed[i] = true;
				notifyChange();
			}
		}
	}

	@Override
	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		// Blocks
		for (int i = 0; i < 16; i++) {
			BlockState state = palette.get(Palette.values()[i]);

			if (state == null)
				continue;
			if (changed[i])
				continue;

			ms.pushPose();
			BlockPos translate = positionFromIndex(i);
			ms.translate(translate.getX(), translate.getY(), translate.getZ());
			ms.translate(1 / 32f, 1 / 32f, 1 / 32f);
			ms.scale(15 / 16f, 15 / 16f, 15 / 16f);
			minecraft.getBlockRenderer()
				.renderBlock(state, ms, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
			ms.popPose();
		}
	}

	@Override
	public void whenExited() {
		getModel().stopPalettePreview();
		MightyClient.renderer.setActive(false);
	}

	protected void notifyChange() {
		getModel().updatePalettePreview();
		minecraft.player.displayClientMessage(new StringTextComponent("Updating Preview..."), true);
		MightyClient.renderer.update();
	}

	static final Object textKey = new Object();

	@Override
	public void tickHighlightOutlines() {
		BlockPos targetBlock = null;

		RayTraceResult raytrace = RaycastHelper.rayTraceRange(minecraft.level, minecraft.player,
			minecraft.player.getAttributeValue(ForgeMod.REACH_DISTANCE.get()));
		if (raytrace != null && raytrace.getType() == Type.BLOCK) {
			targetBlock = new BlockPos(raytrace.getLocation());
			if (grid.containsKey(targetBlock))
				sendStatusMessage(grid.get(targetBlock)
					.getDisplayName());
		}

		for (int i = 0; i < 16; i++) {
			BlockPos pos = positionFromIndex(i);

			// Render Outline
			boolean s = targetBlock != null && pos.equals(targetBlock);
			boolean b = changed[i];
			MightyClient.outliner.showAABB("pallete" + i, new AxisAlignedBB(pos))
				.lineWidth(b || s ? 1 / 16f : 1 / 32f)
				.colored(s ? 0x8888ff : b ? 0x6666ff : 0xbbbbbb);
		}

	}

	private BlockPos positionFromIndex(int index) {
		return center.east(-3 + (index % 4) * 2)
			.south(-3 + (index / 4) * 2);
	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("The Ghost blocks show the individual materials used in this build.",
			"Modify the palette by placing blocks into the marked areas. You do not have to fill all the gaps.",
			"Once finished, make sure to save it. [F]");
	}

}
