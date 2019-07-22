package com.simibubi.mightyarchitect.control.phase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.mightyarchitect.control.Schematic;
import com.simibubi.mightyarchitect.control.SchematicHologram;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.control.helpful.TessellatorTextures;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;

import net.minecraft.block.BlockState;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.model.data.EmptyModelData;

public class PhaseCreatingPalette extends PhaseBase implements IDrawBlockHighlights {

	private PaletteDefinition palette;
	private BlockPos center;
	private Map<BlockPos, Palette> grid;
	private boolean[] changed;

	@Override
	public void whenEntered() {

		Schematic model = getModel();
		ClientWorld world = minecraft.world;
		changed = new boolean[16];

		palette = model.getCreatedPalette();
		center = world.getHeight(Heightmap.Type.WORLD_SURFACE, minecraft.player.getPosition());
		grid = new HashMap<>();

		for (int i = 0; i < 16; i++) {
			BlockPos pos = positionFromIndex(i);
			grid.put(pos, Palette.values()[i]);
			if (!world.isAirBlock(pos) && palette.get(Palette.values()[i]) != world.getBlockState(pos)) {
				palette.put(Palette.values()[i], world.getBlockState(pos));
				changed[i] = true;
			}
		}

		model.updatePalettePreview();
		SchematicHologram.display(getModel());
	}

	@Override
	public void update() {
		for (int i = 0; i < 16; i++) {
			BlockPos pos = positionFromIndex(i);

			if (minecraft.world.isAirBlock(pos)) {
				PaletteDefinition paletteDef = getModel().isEditingPrimary() ? getModel().getPrimary()
						: getModel().getSecondary();
				Palette key = grid.get(pos);

				if (paletteDef.get(key) != palette.get(key)) {
					palette.put(key, paletteDef.get(key));
					changed[i] = false;
					notifyChange();
				}

				continue;
			}

			BlockState state = minecraft.world.getBlockState(pos);
			if (state.getBlock() instanceof TrapDoorBlock)
				state = state.with(TrapDoorBlock.OPEN, true);

			if (palette.get(Palette.values()[i]) != state) {
				palette.put(grid.get(pos), state);
				changed[i] = true;
				notifyChange();
			}
		}
	}

	@Override
	public void render() {
		TessellatorHelper.prepareForDrawing();
		
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		// Blocks
		for (int i = 0; i < 16; i++) {
			BlockState state = palette.get(Palette.values()[i]);
			
			if (state == null)
				continue;
			if (changed[i])
				continue;

			GlStateManager.pushMatrix();
			BlockPos translate = positionFromIndex(i);
			minecraft.getBlockRendererDispatcher().renderBlock(state, translate, minecraft.world, bufferBuilder,
					minecraft.world.rand, EmptyModelData.INSTANCE);
			GlStateManager.popMatrix();
		}
		Tessellator.getInstance().draw();
		
		// Changed frames
		TessellatorTextures.PaletteChanged.bind();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for (int i = 0; i < 16; i++) {
			if (!changed[i])
				continue;

			TessellatorHelper.cube(bufferBuilder, positionFromIndex(i), new BlockPos(1, 1, 1), 1 / 32d, true, true);
		}
		Tessellator.getInstance().draw();

		// Unchanged frames
		TessellatorTextures.PaletteUnchanged.bind();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for (int i = 0; i < 16; i++) {
			if (changed[i])
				continue;

			TessellatorHelper.cube(bufferBuilder, positionFromIndex(i), new BlockPos(1, 1, 1), 1 / 32d, true, true);
		}
		Tessellator.getInstance().draw();

		TessellatorHelper.cleanUpAfterDrawing();
		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
	}

	@Override
	public void whenExited() {
		getModel().stopPalettePreview();
		SchematicHologram.reset();
	}

	protected void notifyChange() {
		getModel().updatePalettePreview();
		minecraft.player.sendStatusMessage(new StringTextComponent("Updating Preview..."), true);
		SchematicHologram.getInstance().schematicChanged();
	}

	@Override
	public void onBlockHighlight(DrawBlockHighlightEvent event) {
		RayTraceResult raytrace = event.getTarget();
		if (raytrace != null && raytrace.getType() == Type.BLOCK) {
			BlockPos targetBlock = new BlockPos(raytrace.getHitVec());

			if (grid.containsKey(targetBlock)) {
				TessellatorHelper.prepareForDrawing();
				TessellatorHelper.drawString(grid.get(targetBlock).getDisplayName(), targetBlock.getX() + 0.5f,
						targetBlock.up().getY() + 0.3f, targetBlock.getZ() + 0.5f, false, true);
				TessellatorHelper.cleanUpAfterDrawing();
			}

		}
	}

	private BlockPos positionFromIndex(int index) {
		return center.east(-3 + (index % 4) * 2).south(-3 + (index / 4) * 2);
	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("The Ghost blocks show the individual materials used in this build.",
				"Modify the palette by placing blocks into the marked areas. You do not have to fill all the gaps.",
				"Once finished, make sure to save it. [F]");
	}

}
