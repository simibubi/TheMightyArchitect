package com.simibubi.mightyarchitect.control.palette;

public class PaletteBlockInfo {
	public Palette palette;
	public BlockOrientation orientation;
	public boolean mirrorX;
	public boolean mirrorZ;
	public boolean forceAxis;
	
	public PaletteBlockInfo(Palette palette, BlockOrientation orientation) {
		this.palette = palette;
		this.orientation = orientation;
	}
	
}