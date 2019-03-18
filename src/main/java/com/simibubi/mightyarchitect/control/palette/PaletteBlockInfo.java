package com.simibubi.mightyarchitect.control.palette;

import net.minecraft.util.EnumFacing;

public class PaletteBlockInfo {
	public Palette palette;
	public EnumFacing facing;
	
	public PaletteBlockInfo(Palette palette, EnumFacing facing) {
		this.palette = palette;
		this.facing = facing;
	}
	
}