package com.simibubi.mightyarchitect.control.design;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.design.partials.Corner;
import com.simibubi.mightyarchitect.control.design.partials.Design;
import com.simibubi.mightyarchitect.control.design.partials.Facade;
import com.simibubi.mightyarchitect.control.design.partials.FlatRoof;
import com.simibubi.mightyarchitect.control.design.partials.Roof;
import com.simibubi.mightyarchitect.control.design.partials.Tower;
import com.simibubi.mightyarchitect.control.design.partials.Trim;
import com.simibubi.mightyarchitect.control.design.partials.Wall;

public enum DesignType {

	WALL("wall", "Wall", new Wall()), FACADE("facade", "Facade", new Facade()),
	CORNER("corner", "Corner", new Corner()), TOWER("tower", "Tower", new Tower()), TRIM("trim", "Trim", new Trim()),
	ROOF("roof", "Roof", new Roof()),
	FLAT_ROOF("flatroof", "Flat Roof", new FlatRoof()),
	NONE("none", "Don't use", null);

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
		case FLAT_ROOF:
			return "Margin";
		case TOWER:
			return "Tower Radius";
		case WALL:
			return "Expandability";
		default:
			return "";
		}
	}

	public boolean hasSizeData() {
		return this == TOWER || this == ROOF || this == FLAT_ROOF;
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
