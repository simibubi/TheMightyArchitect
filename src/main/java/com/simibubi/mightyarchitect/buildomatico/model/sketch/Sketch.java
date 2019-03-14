package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.simibubi.mightyarchitect.buildomatico.Palette;
import com.simibubi.mightyarchitect.buildomatico.model.context.Context;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Design.DesignInstance;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class Sketch {

	public List<DesignInstance> primary;
	public List<DesignInstance> secondary;
	public List<Cuboid> interior;

	private Context context;

	public Sketch() {
		primary = new LinkedList<>();
		secondary = new LinkedList<>();
		interior = new LinkedList<>();
	}

	public Vector<Map<BlockPos, PaletteBlockInfo>> assemble() {
		Vector<Map<BlockPos, PaletteBlockInfo>> assembled = new Vector<>(2);
		Map<BlockPos, PaletteBlockInfo> blocksPrimary = new HashMap<>();
		Map<BlockPos, PaletteBlockInfo> blocksSecondary = new HashMap<>();

		for (DesignInstance design : secondary)
			design.getBlocks(blocksSecondary);
		for (DesignInstance design : primary)
			design.getBlocks(blocksPrimary);

		clean(blocksPrimary);
		clean(blocksSecondary);
		
		addFloors(blocksPrimary, blocksSecondary);

		assembled.addElement(blocksPrimary);
		assembled.addElement(blocksSecondary);
		return assembled;
	}

	private void clean(Map<BlockPos, PaletteBlockInfo> blocks) {
		List<BlockPos> toRemove = new LinkedList<>();
		for (BlockPos pos : blocks.keySet()) {
			if (blocks.get(pos).palette == Palette.CLEAR) {
				toRemove.add(pos);
			} else {
				for (Cuboid c : interior) {
					if (c.contains(pos))
						toRemove.add(pos);
				}
			}

		}
		toRemove.forEach(e -> blocks.remove(e));
		toRemove.clear();
	}

	private void addFloors(Map<BlockPos, PaletteBlockInfo> primary, Map<BlockPos, PaletteBlockInfo> secondary) {
		for (Cuboid cuboid : interior) {
			List<Cuboid> checked = new LinkedList<>();
			
			interior.forEach(other -> {
				if (other == cuboid)
					return;
				if (!other.intersects(cuboid))
					return;
				if (other.width * other.length > cuboid.width * cuboid.length)
					return;
				checked.add(other);
			});

			int y = cuboid.height - 1;
			PaletteBlockInfo paletteBlockInfo = new PaletteBlockInfo(Palette.FLOOR, EnumFacing.UP);
			Map<BlockPos, PaletteBlockInfo> blocks = cuboid.isSecondary()? secondary : primary;
			
			for (int x = 0; x < cuboid.width; x++) {
				for (int z = 0; z < cuboid.length; z++) {
					boolean contained = false;
					BlockPos pos = cuboid.getOrigin().add(x, y, z);
					
					for (Cuboid other : checked) {
						if (other.contains(pos)) {
							contained = true;
							break;
						}
					}
					
					if (contained) {
						continue;
					}
					
					blocks.put(pos, paletteBlockInfo);
				}
			}

		}
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

}
