package com.simibubi.mightyarchitect.buildomatico.client;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.buildomatico.Palette;
import com.simibubi.mightyarchitect.buildomatico.PaletteDefinition;
import com.simibubi.mightyarchitect.buildomatico.helpful.TesselatorTextures;
import com.simibubi.mightyarchitect.buildomatico.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.buildomatico.model.schematic.Schematic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class PaletteCreatorClient {

	private static PaletteCreatorClient instance;
	private PaletteDefinition palette;
	private BlockPos center;
	private Schematic target;
	private boolean primary;
	private Map<BlockPos, Palette> grid;

	public static void startCreating(BlockPos center, Schematic target, boolean primary) {
		instance = new PaletteCreatorClient();
		instance.palette = PaletteDefinition.defaultPalette().clone();
		instance.center = center;
		instance.target = target;
		instance.primary = primary;
		instance.grid = new HashMap<>();
		for (int i = 0; i < 16; i++) {
			BlockPos pos = instance.positionFromIndex(i);
			instance.grid.put(pos, Palette.values()[i]);
			instance.palette.put(Palette.values()[i], Minecraft.getMinecraft().world.getBlockState(pos));
		}
		notifyChange();
	}

	public static boolean isPresent() {
		return instance != null;
	}

	public static void reset() {
		instance = null;
	}
	
	public boolean isPrimary() {
		return primary;
	}

	public static PaletteDefinition finish() {
		PaletteDefinition palette = instance.palette;
		reset();
		return palette;
	}

	public static PaletteCreatorClient getInstance() {
		return instance;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void render(RenderWorldLastEvent event) {
		if (isPresent()) {
			TessellatorHelper.prepareForDrawing();
			TesselatorTextures.Trim.bind();
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			for (int i = 0; i < 16; i++) {
				TessellatorHelper.walls(bufferBuilder, instance.positionFromIndex(i), new BlockPos(1, 1, 1), 0.125,
						false, true);
			}

			Tessellator.getInstance().draw();
			TessellatorHelper.cleanUpAfterDrawing();
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		}
	}

	@SubscribeEvent
	public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
		if (isPresent()) {
			if (instance.grid.containsKey(event.getPos())) {
				instance.palette.put(instance.grid.get(event.getPos()), event.getPlacedBlock());
				notifyChange();
			}
		}
	}
	
	@SubscribeEvent
	public static void onBlockBroken(BlockEvent.BreakEvent event) {
		if (isPresent()) {
			if (instance.grid.containsKey(event.getPos())) {
				instance.palette.put(instance.grid.get(event.getPos()), Blocks.AIR.getDefaultState());
				notifyChange();
			}
		}		
	}
		
	@SubscribeEvent
	public static void onBlockHighlight(DrawBlockHighlightEvent event) {
		if (isPresent()) {
			RayTraceResult raytrace = event.getTarget();
			if (raytrace != null && raytrace.typeOfHit == Type.BLOCK) {
				BlockPos targetBlock = raytrace.getBlockPos().offset(raytrace.sideHit);
				if (instance.grid.containsKey(targetBlock)) {
					TessellatorHelper.prepareForDrawing();
					TessellatorHelper.drawString(instance.grid.get(targetBlock).getDisplayName(),
							targetBlock.getX() + 0.5f, targetBlock.up().getY(), targetBlock.getZ() + 0.5f, false, true);
					TessellatorHelper.cleanUpAfterDrawing();
				}
			}
		}
	}

	private static void notifyChange() {
		if (instance.primary) {
			instance.target.swapPrimaryPalette(instance.palette);
		} else {
			instance.target.swapSecondaryPalette(instance.palette);
		}
		SchematicHologram.getInstance().schematicChanged();
	}

	private BlockPos positionFromIndex(int index) {
		return getInstance().center.east(-3 + (index % 4) * 2).south(-3 + (index / 4) * 2);
	}

}
