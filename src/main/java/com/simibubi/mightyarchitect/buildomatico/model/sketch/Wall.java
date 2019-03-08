package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.List;
import java.util.Map;

import net.minecraft.util.math.BlockPos;

public class Wall extends Design {

	public boolean repeatable;
	public boolean override;
	public int zShift;

	public Wall(List<String> definition) {
		super(definition);
		String[] keyWords = definition.get(0).split(" ");
		zShift = (keyWords.length >= 5) ? Integer.parseInt(keyWords[4]) : 0;
		Type type = Type.valueOf(keyWords[2]);
		if (type == Type.WALL_REPEAT) {
			repeatable = true;
		} else if (type == Type.WALL_REPEAT_OVERRIDE) {
			repeatable = true;
			override = true;
		}
	}

	@Override
	public Type getType() {
		return Type.WALL;
	}

	@Override
	public String toString() {
		return super.toString() + "\nZSHIFT " + zShift;
	}

	@Override
	public boolean fitsHorizontally(int width) {
		if (repeatable) {
			if (override) {
				return (width % (this.defaultWidth - 1)) == 1;
			}
			return (width % this.defaultWidth) == 0;
		}
		return super.fitsHorizontally(width);
	}

	@Override
	public void getBlocks(DesignInstance instance, Map<BlockPos, PaletteBlockInfo> blocks) {
		if (repeatable) {
			int instances = override ? (instance.width - 1) / (defaultWidth - 1) : instance.width / defaultWidth;
			int multiplierWidth = (override? defaultWidth - 1 : defaultWidth);
			for (int i = 0; i < instances; i++) {
				BlockPos shift = new BlockPos(i * multiplierWidth, 0, -zShift);
				super.getBlocksShifted(instance, blocks, shift);			
			}
		} else {
			super.getBlocksShifted(instance, blocks, new BlockPos(0, 0, -zShift));			
		}
	}

}
