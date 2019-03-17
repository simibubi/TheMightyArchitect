package com.simibubi.mightyarchitect.buildomatico.client.tools;

public enum AllTools {

	Room(new RoomTool()),
	Select(new SelectionTool()),
	Cylinder(new CylinderRoomTool());
	
	private ImAToolForGroundPlanning tool;
	
	private AllTools(ImAToolForGroundPlanning tool) {
		this.tool = tool;
	}
	
	public ImAToolForGroundPlanning getTool() {
		return tool;
	}
	
	public AllTools next() {
		return values()[(this.ordinal() + 1) % values().length];
	}
	
	public AllTools previous() {
		return values()[(this.ordinal() - 1 + values().length) % values().length];
	}

}
