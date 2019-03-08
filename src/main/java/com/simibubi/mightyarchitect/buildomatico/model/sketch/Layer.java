package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import com.simibubi.mightyarchitect.buildomatico.Palette;

import net.minecraft.util.math.Vec3i;

public class Layer {

	public enum Type {
		STANDARD, NOT_SUPPORTIVE, CLONE_ONCE, CLONE, OPTIONAL, MASK_BELOW
	}
	
	public Type type;
	public Vec3i size;
	public Palette[][] blocks;
	
	public Layer(Vec3i size, String definition, String content) {
		this.size = new Vec3i(size.getX(), 1, size.getZ());
		blocks = new Palette[size.getZ()][size.getX()];
		type = Type.valueOf(definition.split(" ")[1]);
		String[] strips = content.split(",");
		for (int z = 0; z < strips.length; z++) {
			String strip = strips[z];
			for (int x = 0; x < strip.length(); x++) {
				char charAt = strip.charAt(x);
				if (charAt != ' ')
					blocks[z][x] = Palette.getByChar(charAt);
			}
		}
	}
	
}
