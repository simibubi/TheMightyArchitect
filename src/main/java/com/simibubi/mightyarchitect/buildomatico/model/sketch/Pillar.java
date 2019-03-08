package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.List;

public class Pillar extends Design {

	public Pillar(List<String> definition) {
		super(definition);
	}
	
	@Override
	public Type getType() {
		return Type.PILLAR;
	}
	

}
