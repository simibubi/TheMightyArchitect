package com.simibubi.mightyarchitect.control.design;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.design.partials.Design.DesignInstance;
import com.simibubi.mightyarchitect.control.palette.BlockOrientation;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteBlockInfo;

import net.minecraft.util.math.BlockPos;

public class Sketch {

	public List<DesignInstance> primary;
	public List<DesignInstance> secondary;
	public List<Room> interior;

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

		clean(blocksPrimary, blocksSecondary);
		addFloors(blocksPrimary, blocksSecondary);

		assembled.addElement(blocksPrimary);
		assembled.addElement(blocksSecondary);
		return assembled;
	}

	private void clean(Map<BlockPos, PaletteBlockInfo> blocks, Map<BlockPos, PaletteBlockInfo> blocks2) {
		Set<BlockPos> toRemove = new HashSet<>();

		for (Map<BlockPos, PaletteBlockInfo> paletteLayer : ImmutableList.of(blocks, blocks2)) {
			for (BlockPos pos : paletteLayer.keySet()) {
				
				if (paletteLayer.get(pos).palette == Palette.CLEAR) {
					toRemove.add(pos);
				} else {
					for (Room room : interior) {
						if (room.designLayer.isExterior())
							continue;
						if (room.contains(pos))
							toRemove.add(pos);
					}
				}
			}
		}
		
		toRemove.forEach(e -> {
			blocks.remove(e);
			blocks2.remove(e);
		});
	}

	private void addFloors(Map<BlockPos, PaletteBlockInfo> primary, Map<BlockPos, PaletteBlockInfo> secondary) {
		for (Room cuboid : interior) {
			
			boolean trimAbove = false;
			for (Room trim : interior) {
				if (trimAbove)
					continue;
				if (trim.height > 1)
					continue;
				if (trim.y != cuboid.y + cuboid.height)
					continue;
				if (trim.x <= cuboid.x && trim.z <= cuboid.z && trim.x + trim.width >= cuboid.x + cuboid.width && trim.z + trim.length >= cuboid.z + cuboid.length)
					trimAbove = true;				
			}
			if (trimAbove)
				continue;
			
			List<Room> checked = new LinkedList<>();
			
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
			PaletteBlockInfo paletteBlockInfo = new PaletteBlockInfo(Palette.FLOOR, BlockOrientation.NONE);
			paletteBlockInfo.afterPosition = BlockOrientation.TOP_UP;
			Map<BlockPos, PaletteBlockInfo> blocks = cuboid.secondaryPalette ? secondary : primary;
			
			for (int x = 0; x < cuboid.width; x++) {
				for (int z = 0; z < cuboid.length; z++) {
					boolean contained = false;
					BlockPos pos = cuboid.getOrigin().offset(x, y, z);
					
					for (Room other : checked) {
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

}
