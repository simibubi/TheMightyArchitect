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
import com.simibubi.mightyarchitect.control.design.partials.TowerFlatRoof;
import com.simibubi.mightyarchitect.control.design.partials.TowerRoof;
import com.simibubi.mightyarchitect.control.design.partials.Trim;
import com.simibubi.mightyarchitect.control.design.partials.Wall;

public enum DesignType {

	WALL("wall", "Wall", new Wall()), 
	FACADE("facade", "Facade", new Facade()), 
	CORNER("corner", "Corner", new Corner()), 
	TOWER("tower", "Tower", new Tower()), 
	TRIM("trim", "Trim", new Trim()), 
	ROOF("roof", "Gable Roof", new Roof()), 
	FLAT_ROOF("flatroof", "Flat Roof", new FlatRoof()), 
	TOWER_ROOF("towerroof", "Conical Roof", new TowerRoof()), 
	TOWER_FLAT_ROOF("towerflatroof", "Flat Tower Roof", new TowerFlatRoof()), 
	
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
		case TOWER_FLAT_ROOF:
		case TOWER_ROOF:
			return "Tower Radius";
		case WALL:
			return "Size Behaviour";
		default:
			return "";
		}
	}

	public boolean hasSizeData() {
		switch (this) {
		case FLAT_ROOF:
		case ROOF:
		case TOWER:
		case TOWER_FLAT_ROOF:
		case TOWER_ROOF:
			return true;
		default:
			return false;
		}
	}
	
	public int getMaxSize() {
		switch (this) {
		case ROOF:
			return ThemeStatistics.MAX_ROOF_SPAN;
		case FLAT_ROOF:
			return ThemeStatistics.MAX_MARGIN;
		case TOWER:
		case TOWER_FLAT_ROOF:
		case TOWER_ROOF:
			return ThemeStatistics.MAX_TOWER_RADIUS;
		default:
			return 0;
		}
	}
	
	public int getMinSize() {
		switch (this) {
		case ROOF:
			return ThemeStatistics.MIN_ROOF_SPAN;
		case FLAT_ROOF:
			return ThemeStatistics.MIN_MARGIN;
		case TOWER:
		case TOWER_FLAT_ROOF:
		case TOWER_ROOF:
			return ThemeStatistics.MIN_TOWER_RADIUS;
		default:
			return 0;
		}
	}

	public boolean hasSubtypes() {
		switch (this) {
		case WALL:
			return true;
		default:
			return false;
		}
	}

	public List<String> getSubtypeOptions() {
		switch (this) {
		case WALL:
			List<String> list = new ArrayList<>();
			ImmutableList.copyOf(Wall.ExpandBehaviour.values()).forEach(value -> list.add(value.name()));
			return list;
		default:
			return Collections.emptyList();
		}
	}
	
	public static List<DesignType> defaults() {
		return ImmutableList.of(WALL, FACADE, CORNER);
	}
	
	public static List<DesignType> roofTypes() {
		return ImmutableList.of(ROOF, FLAT_ROOF, TOWER_FLAT_ROOF, TOWER_ROOF);
	}

}
