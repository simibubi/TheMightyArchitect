package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.List;

public class Corner extends Design {

	public Corner(List<String> definition) {
		super(definition);
	}
	
	@Override
	public Type getType() {
		return Type.CORNER;
	}

}
