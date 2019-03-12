package com.simibubi.mightyarchitect.buildomatico.model.sketch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

public enum DesignType {

	WALL("wall", "Wall", new Wall()), FACADE("facade", "Facade", new Facade()),
	CORNER("corner", "Corner", new Corner()), TOWER("tower", "Tower", new Tower()), TRIM("trim", "Trim", new Trim()),
	ROOF("roof", "Roof", new Roof());

	private String filePath;
	private String displayName;
	private Design design;

	private DesignType(String filePath, String displayName, Design design) {
		this.filePath = filePath;
		this.displayName = displayName;
		this.design = design;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Design getDesign() {
		return design;
	}

	public boolean hasAdditionalData() {
		return hasSizeData() || hasSubtypes();
	}

	public String getAdditionalDataName() {
		switch (this) {
		case ROOF:
			return "Roof Span";
		case TOWER:
			return "Tower Radius";
		case WALL:
			return "Expandability";
		default:
			return "";
		}
	}

	public boolean hasSizeData() {
		return this == TOWER || this == ROOF;
	}

	public boolean hasSubtypes() {
		return this == WALL;
	}

	public List<String> getSubtypeOptions() {
		if (this == WALL) {
			List<String> list = new ArrayList<>();
			ImmutableList.copyOf(Wall.ExpandBehaviour.values()).forEach(value -> list.add(value.name()));
			return list;
		}

		return Collections.emptyList();
	}

}
