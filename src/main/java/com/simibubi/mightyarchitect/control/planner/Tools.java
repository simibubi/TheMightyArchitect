package com.simibubi.mightyarchitect.control.planner;

public enum Tools {

	Room(new RoomTool()),
	Select(new SelectionTool()),
	Cylinder(new CylinderRoomTool());
	
	private ImAToolForGroundPlanning tool;
	
	private Tools(ImAToolForGroundPlanning tool) {
		this.tool = tool;
	}
	
	public ImAToolForGroundPlanning getTool() {
		return tool;
	}
	
	public Tools next() {
		return values()[(this.ordinal() + 1) % values().length];
	}
	
	public Tools previous() {
		return values()[(this.ordinal() - 1 + values().length) % values().length];
	}

}
