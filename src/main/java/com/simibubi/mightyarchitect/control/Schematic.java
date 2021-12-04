package com.simibubi.mightyarchitect.control;

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
import com.simibubi.mightyarchitect.networking.InstantPrintPacket;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;

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

	public TemplateBlockAccess getMaterializedSketch() {
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
			primaryPalette = groundPlan.theme.getDefaultPalette()
				.clone();
			secondaryPalette = groundPlan.theme.getDefaultSecondaryPalette()
				.clone();
		}

		materializeSketch(primaryPalette, secondaryPalette);
	}

	private void materializeSketch(PaletteDefinition primary, PaletteDefinition secondary) {
		bounds = null;

		HashMap<BlockPos, BlockState> blockMap = new HashMap<>();
		assembledSketch.get(0)
			.forEach((pos, paletteInfo) -> {
				BlockState state = primary.get(paletteInfo);
				blockMap.put(pos, state);
				checkBounds(pos);
			});
		assembledSketch.get(1)
			.forEach((pos, paletteInfo) -> {
				if (!assembledSketch.get(0)
					.containsKey(pos)
					|| !assembledSketch.get(0)
						.get(pos).palette.isPrefferedOver(paletteInfo.palette)) {
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

		BlockPos maxPos = bounds.getOrigin()
			.offset(bounds.getSize());
		if (x >= maxPos.getX())
			bounds.width = x - bounds.x + 1;
		if (y >= maxPos.getY())
			bounds.height = y - bounds.y + 1;
		if (z >= maxPos.getZ())
			bounds.length = z - bounds.z + 1;
	}

	public Template writeToTemplate() {
		final Template template = new Template();
		template.setAuthor(Minecraft.getInstance().player.getName()
			.getString());

		materializedSketch.localMode(true);
		template.fillFromWorld(materializedSketch, materializedSketch.getBounds()
			.getOrigin(),
			materializedSketch.getBounds()
				.getSize(),
			false, null);
		materializedSketch.localMode(false);

		return template;
	}

	public List<InstantPrintPacket> getPackets() {
		return InstantPrintPacket.sendSchematic(materializedSketch.getBlockMap(), anchor);
	}

	public boolean isEditingPrimary() {
		return editingPrimary;
	}

	public DesignTheme getTheme() {
		return groundPlan.theme;
	}

	public boolean isEmpty() {
		return groundPlan == null;
	}

}
