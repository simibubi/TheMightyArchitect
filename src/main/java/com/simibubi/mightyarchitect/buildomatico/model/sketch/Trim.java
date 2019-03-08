package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.List;

public class Trim extends Design {

	public Trim(List<String> definition) {
		super(definition);
	}

	@Override
	public Type getType() {
		return Type.TRIM;
	}
	
}
