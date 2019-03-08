package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.List;

public class Tower extends Design {

	public int radius;
	
	public Tower(List<String> definition) {
		super(definition);
		String[] keyWords = definition.get(0).split(" ");
		radius = Integer.parseInt(keyWords[4]);
	}

	@Override
	public Type getType() {
		return Type.TOWER;
	}
	

	@Override
	public String toString() {
		return super.toString() + "\nRADIUS " + radius;
	}
	
	@Override
	public boolean fitsHorizontally(int width) {
		return width == radius * 2 - 1;
	}

}
