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
	Select(new SelectionTool(), "Select", GuiResources.ICON_TARGET),
	
	RerollAll(new RerollTool(), "Reroll All", GuiResources.ICON_TOOL_REROLL),
	RerollTarget(new RerollTargetTool(), "Reroll Target", GuiResources.ICON_TOOL_REROLL_TARGET),
	LayerStyle(new LayerStyleTool(), "Wall Style", GuiResources.ICON_LAYER_OPEN),
	CopyDesign(new CopyDesignTool(), "Reuse Patterns", GuiResources.ICON_3x3),
	Palette(new PalettePainterTool(), "Palette", GuiResources.ICON_TOOL_PALETTE);
	
	private IComposerTool tool;
	private String displayName;
	private GuiResources icon;
	
	private Tools(IComposerTool tool, String name, GuiResources icon) {
		this.tool = tool;
		this.displayName = name;
		this.icon = icon;
	}
	
	public IComposerTool getTool() {
		return tool;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public GuiResources getIcon() {
		return icon;
	}
	
	public static List<Tools> getGroundPlanningTools() {
		List<Tools> tools = new ArrayList<>();
		Collections.addAll(tools, Room, Cylinder, Height, MoveReshape, Stack, Roof, Select);
		return tools;
	}
	
	public static List<Tools> getWallDecorationTools() {
		List<Tools> tools = new ArrayList<>();
		Collections.addAll(tools, RerollAll, RerollTarget, CopyDesign, LayerStyle, Palette);
		return tools;
	}
	
}
