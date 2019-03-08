package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.List;

public class Feature extends Design {

	public Feature(List<String> definition) {
		super(definition);
	}
	
	@Override
	public Type getType() {
		return Type.FEATURE;
	}

}
