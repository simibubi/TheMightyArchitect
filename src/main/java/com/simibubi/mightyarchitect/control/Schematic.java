package com.simibubi.mightyarchitect.control;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.Sketch;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.networking.PacketInstantPrint;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class Schematic {

	private BlockPos anchor;
	private GroundPlan groundPlan;
	private PaletteDefinition primaryPalette;
	private PaletteDefinition secondaryPalette;

	private Sketch sketch;
	private Vector<Map<BlockPos, PaletteBlockInfo>> assembledSketch;
	private TemplateBlockAccess materializedSketch;
	private Cuboid bounds;

	private PaletteDefinition editedPalette;
	private boolean editingPrimary;
	public int seed;

	public Schematic() {
		seed = new Random().nextInt(100000);
	}

	public void setGroundPlan(GroundPlan groundPlan) {
		this.groundPlan = groundPlan;
	}

	public void setAnchor(BlockPos anchor) {
		this.anchor = anchor;
	}

	public void swapPrimaryPalette(PaletteDefinition newPalette) {
		this.primaryPalette = newPalette;
		materializeSketch();
	}

	public void swapSecondaryPalette(PaletteDefinition newPalette) {
		this.secondaryPalette = newPalette;
		materializeSketch();
	}

	public void setSketch(Sketch newSketch) {
		this.sketch = newSketch;
		assembleSketch();
		materializeSketch();
	}

	public Sketch getSketch() {
		return sketch;
	}

	public GroundPlan getGroundPlan() {
		return groundPlan;
	}

	public BlockPos getAnchor() {
		return anchor;
	}

	public PaletteDefinition getPrimary() {
		return primaryPalette;
	}

	public PaletteDefinition getSecondary() {
		return secondaryPalette;
	}

	public IBlockReader getMaterializedSketch() {
		return materializedSketch;
	}

	public void assembleSketch() {
		assembledSketch = sketch.assemble();
	}

	public Cuboid getLocalBounds() {
		return bounds;
	}

	public Cuboid getGlobalBounds() {
		Cuboid clone = bounds.clone();
		clone.move(anchor.getX(), anchor.getY(), anchor.getZ());
		return clone;
	}

	public void startCreatingNewPalette(boolean primary) {
		editedPalette = (primary ? primaryPalette : secondaryPalette).clone();
		editedPalette.setName("");
		editingPrimary = primary;
	}

	public PaletteDefinition getCreatedPalette() {
		return editedPalette;
	}

	public void updatePalettePreview() {
		if (isEditingPrimary())
			materializeSketch(editedPalette, secondaryPalette);
		else
			materializeSketch(primaryPalette, editedPalette);
	}

	public void stopPalettePreview() {
		materializeSketch();
	}

	public void applyCreatedPalette() {
		if (isEditingPrimary())
			primaryPalette = editedPalette;
		else
			secondaryPalette = editedPalette;
		materializeSketch();
	}

	public void materializeSketch() {
		if (primaryPalette == null) {
			PaletteDefinition defaultPalette = groundPlan.theme.getDefaultPalette();
			primaryPalette = defaultPalette.clone();
			secondaryPalette = defaultPalette.clone();
		}
		
		materializeSketch(primaryPalette, secondaryPalette);
	}

	private void materializeSketch(PaletteDefinition primary, PaletteDefinition secondary) {
		bounds = null;

		HashMap<BlockPos, BlockState> blockMap = new HashMap<>();
		assembledSketch.get(0).forEach((pos, paletteInfo) -> {
			BlockState state = primary.get(paletteInfo);
			blockMap.put(pos, state);
			checkBounds(pos);
		});
		assembledSketch.get(1).forEach((pos, paletteInfo) -> {
			if (!assembledSketch.get(0).containsKey(pos)
					|| !assembledSketch.get(0).get(pos).palette.isPrefferedOver(paletteInfo.palette)) {
				BlockState state = secondary.get(paletteInfo);
				blockMap.put(pos, state);
				checkBounds(pos);
			}
		});

		materializedSketch = new TemplateBlockAccess(blockMap, bounds, anchor);
	}

	private void checkBounds(BlockPos pos) {
		if (bounds == null)
			bounds = new Room(pos, BlockPos.ZERO);

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if (x < bounds.x) {
			bounds.width += bounds.x - x;
			bounds.x = x;
		}
		if (y < bounds.y) {
			bounds.height += bounds.y - y;
			bounds.y = y;
		}
		if (z < bounds.z) {
			bounds.length += bounds.z - z;
			bounds.z = z;
		}

		BlockPos maxPos = bounds.getOrigin().add(bounds.getSize());
		if (x >= maxPos.getX())
			bounds.width = x - bounds.x + 1;
		if (y >= maxPos.getY())
			bounds.height = y - bounds.y + 1;
		if (z >= maxPos.getZ())
			bounds.length = z - bounds.z + 1;
	}

	public Template writeToTemplate() {
		final Template template = new Template();
		template.setAuthor(Minecraft.getInstance().player.getName().getFormattedText());

		try {
			Field fBlocks = ObfuscationReflectionHelper.findField(Template.class, "field_204769_a");
			Field fSize = ObfuscationReflectionHelper.findField(Template.class, "field_186272_c");
			
			Object objectBlocks = fBlocks.get(template);
			fSize.set(template, bounds.getSize());
			@SuppressWarnings("unchecked")
			List<List<BlockInfo>> blocks = (List<List<Template.BlockInfo>>) objectBlocks;
			
			List<BlockInfo> added = new ArrayList<>();
			
			materializedSketch.getBlockMap()
			.forEach((pos, state) -> added.add(new BlockInfo(pos.subtract(bounds.getOrigin()), state, new CompoundNBT())));
			blocks.add(added);
			
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return template;
	}

	public List<PacketInstantPrint> getPackets() {
		return PacketInstantPrint.sendSchematic(materializedSketch.getBlockMap(), anchor);
	}

	public boolean isEditingPrimary() {
		return editingPrimary;
	}
	
	public DesignTheme getTheme() {
		return groundPlan.theme;
	}

}
