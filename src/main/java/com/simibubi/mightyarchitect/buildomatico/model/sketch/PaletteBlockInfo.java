package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import com.simibubi.mightyarchitect.buildomatico.Palette;

import net.minecraft.util.EnumFacing;

public class PaletteBlockInfo {
	public Palette palette;
	public EnumFacing facing;
	
	public PaletteBlockInfo(Palette palette, EnumFacing facing) {
		this.palette = palette;
		this.facing = facing;
	}
	
}