package com.simibubi.mightyarchitect.buildomatico.model.schematic;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.simibubi.mightyarchitect.buildomatico.PaletteDefinition;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Room;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.PaletteBlockInfo;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Sketch;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class Schematic {

	private Sketch sketch;
	private Vector<Map<BlockPos, PaletteBlockInfo>> assembledSketch;
	private Map<BlockPos, IBlockState> materializedSketch;
	private IBlockAccess schematicBlockAccess;

	private Cuboid bounds;

	private PaletteDefinition primary;
	private PaletteDefinition secondary;

	public Schematic(Sketch sketch, PaletteDefinition primary, PaletteDefinition secondary) {
		this.sketch = sketch;
		this.primary = primary;
		this.secondary = secondary;
		assembleSketch();
		materializeSketch();
	}

	public void swapPrimaryPalette(PaletteDefinition newPalette) {
		this.primary = newPalette;
		materializeSketch();
	}

	public void swapSecondaryPalette(PaletteDefinition newPalette) {
		this.secondary = newPalette;
		materializeSketch();
	}

	public void swapPalettes(PaletteDefinition primary, PaletteDefinition secondary) {
		this.primary = primary;
		this.secondary = secondary;
		materializeSketch();
	}

	public void swapSketch(Sketch newSketch) {
		this.sketch = newSketch;
		assembleSketch();
		materializeSketch();
	}

	public Sketch getSketch() {
		return sketch;
	}

	public PaletteDefinition getPrimary() {
		return primary;
	}

	public PaletteDefinition getSecondary() {
		return secondary;
	}

	public IBlockAccess getSchematicBlockAccess() {
		return schematicBlockAccess;
	}

	public BlockPos getBuildingPosition() {
		return sketch.getContext().getAnchor();
	}

	public Vector<Map<BlockPos, PaletteBlockInfo>> getAssembledSketch() {
		return assembledSketch;
	}

	public Map<BlockPos, IBlockState> getMaterializedSketch() {
		return materializedSketch;
	}

	private void assembleSketch() {
		assembledSketch = sketch.assemble();
	}

	public Cuboid getLocalBounds() {
		return bounds;
	}

	public Cuboid getGlobalBounds() {
		BlockPos anchor = getBuildingPosition();
		Cuboid clone = bounds.clone();
		clone.move(anchor.getX(), anchor.getY(), anchor.getZ());
		return clone;
	}

	private void materializeSketch() {
		bounds = null;

		materializedSketch = new HashMap<>();
		assembledSketch.get(0).forEach((pos, paletteInfo) -> {
			IBlockState state = primary.get(paletteInfo.palette, paletteInfo.facing);
			materializedSketch.put(pos, state);
			checkBounds(pos);
		});
		assembledSketch.get(1).forEach((pos, paletteInfo) -> {
			if (!assembledSketch.get(0).containsKey(pos)
					|| !assembledSketch.get(0).get(pos).palette.isPrefferedOver(paletteInfo.palette)) {
				IBlockState state = secondary.get(paletteInfo.palette, paletteInfo.facing);
				materializedSketch.put(pos, state);
				checkBounds(pos);
			}
		});

		schematicBlockAccess = new SchematicBlockAccess(materializedSketch, bounds, getBuildingPosition());
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

		template.setAuthor(sketch.getContext().getOwner().getName());
		template.setSize(bounds.getSize());
		materializedSketch.forEach((pos, state) -> {
			template.putBlock(pos.subtract(bounds.getOrigin()), state);
		});

		return template;
	}

}
