package com.simibubi.mightyarchitect.control.compose.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.gui.ScreenResources;

public enum Tools {

	Room(new RoomTool(), "+ Cuboid", ScreenResources.ICON_TOOL_ROOM, ImmutableList.of(
			"Draw new Rooms with this Tool, Right-Click to select positions.",
			"Default   > Rooms are locked into being uneven",
			"Hold Ctrl > Rooms are forced to fit 5-wide wall designs",
			"Scroll to immediately grow or shrink the newly placed stack."
			)),
	Cylinder(new CylinderRoomTool(), "+ Cylinder", ScreenResources.ICON_TOOL_TOWER, ImmutableList.of(
			"Draw new Towers with this Tool, R-Click to select positions.",
			"Towers are Rooms with cylindrical wall designs.",
			"Min/Max Radius is governed by the Theme.",
			"Scroll to immediately grow or shrink the newly placed tower."
			)),
	MoveReshape(new MoveReshapeTool(), "Adjust (XZ)", ScreenResources.ICON_TOOL_RESHAPE, ImmutableList.of(
			"Point at Rooms and scroll to change their Position/Size.",
			"Default   > Shift selected Rooms around Horizontally",
			"Hold Ctrl > Grow/Shrink selected Rooms Horizontally",
			"Scaling is applied from the center."
			)),
	Height(new HeightTool(), "Adjust (Y)", ScreenResources.ICON_TOOL_HEIGHT, ImmutableList.of(
			"Point at Rooms and scroll to change their Position/Size.",
			"Default   > Move selected Stack Vertically",
			"Hold Ctrl > Grow/Shrink selected Room Vertically",
			"Max Height is governed by the Theme."
			)),
	Stack(new StackTool(), "Floors", ScreenResources.ICON_TOOL_STACK, ImmutableList.of(
			"Point at stacks and Scroll to modify their amount of Floors.",
			"Growing will clone the highest Room and add it on top.",
			"Reducing Floors to Zero deletes the stack."
			)),
	Roof(new RoofTool(), "Roof Style", ScreenResources.ICON_NORMAL_ROOF, ImmutableList.of(
			"Point at stacks and Scroll to cycle their Roof type.",
			"Available Roof types are governed by Theme limitations.",
			"Stacks will always generate a ceiling on the highest Floor."
			)),
	
	RerollAll(new RerollTool(), "Reroll All", ScreenResources.ICON_TOOL_REROLL, ImmutableList.of(
			"Scroll to cycle design choices for everything.",
			"Backtrack by scrolling in the opposite Direction."
			)),
	RerollTarget(new RerollTargetTool(), "Reroll Target", ScreenResources.ICON_TOOL_REROLL_TARGET, ImmutableList.of(
			"Cycle design choices for targeted rooms.",
			"Default   > Reroll the entire stack",
			"Hold Ctrl > Reroll a single room or its roof",
			"Select roofs at the surface of the higest room."
			)),
	LayerStyle(new LayerStyleTool(), "Wall Style", ScreenResources.ICON_LAYER_OPEN, ImmutableList.of(
			"Cycle the style Layer of the rooms pointed at.",
			"Style layers are seperated groups of designs.",			
			"Some layers have special Properties."			
			)),
	CopyDesign(new CopyDesignTool(), "Reuse Patterns", ScreenResources.ICON_3x3, ImmutableList.of(
			"Transfer design choices between components.",
			"Default   > Apply the copied design on the target.",
			"Hold Ctrl > Copy the component pointed at. [R-Click]",
			"This is limited by the flexibility of the desired design."
			)),
	Palette(new PalettePainterTool(), "Palette", ScreenResources.ICON_TOOL_PALETTE, ImmutableList.of(
			"Cycle the material palette for the selected rooms.",
			"Default   > Scroll to cycle palette for the stack.",
			"Hold Ctrl > Scroll to cycle palette for targeted room.",
			"Change palettes in the Architect's Menu ->"
			));
	
	private IComposerTool tool;
	private String displayName;
	private ScreenResources icon;
	private List<String> description;
	
	private Tools(IComposerTool tool, String name, ScreenResources icon, List<String> description) {
		this.tool = tool;
		this.displayName = name;
		this.icon = icon;
		this.description = description;
	}
	
	public IComposerTool getTool() {
		return tool;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public ScreenResources getIcon() {
		return icon;
	}
	
	public List<String> getDescription() {
		return description;
	}
	
	public static List<Tools> getGroundPlanningTools() {
		List<Tools> tools = new ArrayList<>();
		Collections.addAll(tools, Room, Cylinder, Height, MoveReshape, Stack, Roof);
		return tools;
	}
	
	public static List<Tools> getWallDecorationTools() {
		List<Tools> tools = new ArrayList<>();
		Collections.addAll(tools, RerollAll, RerollTarget, CopyDesign, LayerStyle, Palette);
		return tools;
	}
	
}
