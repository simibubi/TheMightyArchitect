package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.List;

public class Facade extends Wall {

	public Facade(List<String> definition) {
		super(definition);
	}

	@Override
	public Type getType() {
		return Type.FACADE;
	}
	
}
