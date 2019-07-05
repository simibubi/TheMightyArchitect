package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.mightyarchitect.gui.GuiResources;

public enum Tools {

	Room(new RoomTool(), "+ Cuboid", GuiResources.ICON_TOOL_ROOM),
	Cylinder(new CylinderRoomTool(), "+ Cylinder", GuiResources.ICON_TOOL_TOWER),
	MoveReshape(new MoveReshapeTool(), "Adjust (XZ)", GuiResources.ICON_TOOL_RESHAPE),
	Height(new HeightTool(), "Adjust (Y)", GuiResources.ICON_TOOL_HEIGHT),
	Stack(new StackTool(), "Floors", GuiResources.ICON_TOOL_STACK),
	Roof(new RoofTool(), "Roof Style", GuiResources.ICON_NORMAL_ROOF),
	Select(new SelectionTool(), "Select", GuiResources.ICON_TARGET);
	
	private ImAToolForGroundPlanning tool;
	private String displayName;
	private GuiResources icon;
	
	private Tools(ImAToolForGroundPlanning tool, String name, GuiResources icon) {
		this.tool = tool;
		this.displayName = name;
		this.icon = icon;
	}
	
	public ImAToolForGroundPlanning getTool() {
		return tool;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public GuiResources getIcon() {
		return icon;
	}
	
	public Tools next() {
		return values()[(this.ordinal() + 1) % values().length];
	}
	
	public Tools previous() {
		return values()[(this.ordinal() - 1 + values().length) % values().length];
	}
	
	public static List<Tools> getGroundPlanningTools() {
		List<Tools> tools = new ArrayList<>();
		Collections.addAll(tools, Room, Cylinder, Height, MoveReshape, Stack, Roof, Select);
		return tools;
	}
	
}
