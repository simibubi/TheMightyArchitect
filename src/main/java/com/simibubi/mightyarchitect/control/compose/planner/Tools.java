package com.simibubi.mightyarchitect.control.compose.planner;

public enum Tools {

	Room(new RoomTool(), "Room Tool"),
	Select(new SelectionTool(), "Selection Tool"),
	Cylinder(new CylinderRoomTool(), "Cylinder Tool");
	
	private ImAToolForGroundPlanning tool;
	private String displayName;
	
	private Tools(ImAToolForGroundPlanning tool, String name) {
		this.tool = tool;
		this.displayName = name;
	}
	
	public ImAToolForGroundPlanning getTool() {
		return tool;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public Tools next() {
		return values()[(this.ordinal() + 1) % values().length];
	}
	
	public Tools previous() {
		return values()[(this.ordinal() - 1 + values().length) % values().length];
	}

}
