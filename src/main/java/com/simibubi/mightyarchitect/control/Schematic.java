package com.simibubi.mightyarchitect.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.design.Sketch;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.networking.PacketInstantPrint;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

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

	public Schematic() {
		primaryPalette = PaletteDefinition.defaultPalette().clone();
		secondaryPalette = PaletteDefinition.defaultPalette().clone();
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

	public void swapPalettes(PaletteDefinition primary, PaletteDefinition secondary) {
		this.primaryPalette = primary;
		this.secondaryPalette = secondary;
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

	public IBlockAccess getMaterializedSketch() {
		return materializedSketch;
	}

	private void assembleSketch() {
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
		if (editingPrimary)
			materializeSketch(editedPalette, secondaryPalette);
		else
			materializeSketch(primaryPalette, editedPalette);
	}

	public void stopPalettePreview() {
		materializeSketch();
	}

	public void applyCreatedPalette() {
		if (editingPrimary)
			primaryPalette = editedPalette;
		else
			secondaryPalette = editedPalette;
		materializeSketch();
	}

	private void materializeSketch() {
		materializeSketch(primaryPalette, secondaryPalette);
	}

	private void materializeSketch(PaletteDefinition primary, PaletteDefinition secondary) {
		bounds = null;

		HashMap<BlockPos, IBlockState> blockMap = new HashMap<>();
		assembledSketch.get(0).forEach((pos, paletteInfo) -> {
			IBlockState state = primary.get(paletteInfo.palette, paletteInfo.facing);
			blockMap.put(pos, state);
			checkBounds(pos);
		});
		assembledSketch.get(1).forEach((pos, paletteInfo) -> {
			if (!assembledSketch.get(0).containsKey(pos)
					|| !assembledSketch.get(0).get(pos).palette.isPrefferedOver(paletteInfo.palette)) {
				IBlockState state = secondary.get(paletteInfo.palette, paletteInfo.facing);
				blockMap.put(pos, state);
				checkBounds(pos);
			}
		});

		materializedSketch = new TemplateBlockAccess(blockMap, bounds, anchor);
	}

	private void checkBounds(BlockPos pos) {
		if (bounds == null)
			bounds = new Room(pos, BlockPos.ORIGIN);

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

		template.setAuthor(Minecraft.getMinecraft().player.getName());
		template.setSize(bounds.getSize());
		materializedSketch.getBlockMap()
				.forEach((pos, state) -> template.putBlock(pos.subtract(bounds.getOrigin()), state));

		return template;
	}

	public List<PacketInstantPrint> getPackets() {
		return PacketInstantPrint.sendSchematic(materializedSketch.getBlockMap(), anchor);
	}

}
