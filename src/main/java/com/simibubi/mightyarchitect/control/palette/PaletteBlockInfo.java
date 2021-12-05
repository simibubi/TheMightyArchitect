package com.simibubi.mightyarchitect.control.palette;

import net.minecraft.world.level.block.state.BlockState;

public class PaletteBlockInfo {
	
	public Palette palette;
	public BlockOrientation initial;
	public BlockOrientation afterPosition;
	
	public boolean mirrorX;
	public boolean mirrorZ;
	public boolean forceAxis;
	
	public PaletteBlockInfo(Palette palette, BlockOrientation orientation) {
		this.palette = palette;
		this.initial = orientation;
	}

	public BlockState apply(BlockState state) {
		return afterPosition.apply(initial.apply(state, forceAxis), false);
	}
	
}