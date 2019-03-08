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

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

}
