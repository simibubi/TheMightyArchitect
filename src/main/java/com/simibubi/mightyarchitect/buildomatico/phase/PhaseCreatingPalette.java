package com.simibubi.mightyarchitect.buildomatico.phase;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.buildomatico.Palette;
import com.simibubi.mightyarchitect.buildomatico.PaletteDefinition;
import com.simibubi.mightyarchitect.buildomatico.client.SchematicHologram;
import com.simibubi.mightyarchitect.buildomatico.helpful.TesselatorTextures;
import com.simibubi.mightyarchitect.buildomatico.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.buildomatico.model.Schematic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;

public class PhaseCreatingPalette extends PhaseBase implements IListenForBlockEvents, IDrawBlockHighlights {

	private PaletteDefinition palette;
	private BlockPos center;
	private Map<BlockPos, Palette> grid;

	@Override
	public void whenEntered() {
		
		Schematic model = getModel();
		WorldClient world = minecraft.world;

		palette = model.getCreatedPalette();
		center = world.getHeight(minecraft.player.getPosition());
		grid = new HashMap<>();
		
		for (int i = 0; i < 16; i++) {
			BlockPos pos = positionFromIndex(i);
			grid.put(pos, Palette.values()[i]);
			if (!world.isAirBlock(pos))
				palette.put(Palette.values()[i], world.getBlockState(pos));
		}
		
		model.updatePalettePreview();
		SchematicHologram.display(getModel());
	}

	@Override
	public void update() {
	}

	@Override
	public void render() {
		TessellatorHelper.prepareForDrawing();
		TesselatorTextures.Trim.bind();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		for (int i = 0; i < 16; i++) {
			TessellatorHelper.walls(bufferBuilder, positionFromIndex(i), new BlockPos(1, 1, 1), 0.125,
					false, true);
		}

		Tessellator.getInstance().draw();
		TessellatorHelper.cleanUpAfterDrawing();
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	}

	@Override
	public void whenExited() {
		getModel().stopPalettePreview();
		SchematicHologram.reset();
	}

	@Override
	public void onBlockPlaced(PlaceEvent event) {
		if (grid.containsKey(event.getPos())) {
			palette.put(grid.get(event.getPos()), event.getPlacedBlock());
			notifyChange();
		}
	}

	protected void notifyChange() {
		getModel().updatePalettePreview();
		SchematicHologram.getInstance().schematicChanged();
	}

	@Override
	public void onBlockBroken(BreakEvent event) {
		if (grid.containsKey(event.getPos())) {
			palette.put(grid.get(event.getPos()), Blocks.AIR.getDefaultState());
			notifyChange();
		}
	}

	@Override
	public void onBlockHighlight(DrawBlockHighlightEvent event) {
		RayTraceResult raytrace = event.getTarget();
		if (raytrace != null && raytrace.typeOfHit == Type.BLOCK) {
			BlockPos targetBlock = raytrace.getBlockPos().offset(raytrace.sideHit);
			
			if (grid.containsKey(targetBlock)) {
				TessellatorHelper.prepareForDrawing();
				TessellatorHelper.drawString(grid.get(targetBlock).getDisplayName(),
						targetBlock.getX() + 0.5f, targetBlock.up().getY(), targetBlock.getZ() + 0.5f, false, true);
				TessellatorHelper.cleanUpAfterDrawing();
			}
			
		}
	}
	
	private BlockPos positionFromIndex(int index) {
		return center.east(-3 + (index % 4) * 2).south(-3 + (index / 4) * 2);
	}
	
}
