package com.simibubi.mightyarchitect.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

public enum ScreenResources {
	
	// Inventories
	PLAYER_INVENTORY("player_inventory.png", 176, 108),
	COMPOSER("composer.png", 256, 58),
	PALETTES("palette_picker.png", 256, 236),
	EXPORTER("design_exporter.png", 200, 111),
	THEME_EDITOR("theme_editor.png", 200, 187),
	WAND_SYMMETRY("wand_symmetry.png", 207, 58),

	// Widgets
	PALETTE_BUTTON("palette_picker.png", 0, 236, 20, 20),
	TEXT_INPUT("widgets.png", 0, 28, 194, 47),
	BUTTON("widgets.png", 18, 18),
	BUTTON_HOVER("widgets.png", 18, 0, 18, 18),
	BUTTON_DOWN("widgets.png", 36, 0, 18, 18),
	INDICATOR("widgets.png", 0, 18, 18, 5),
	INDICATOR_WHITE("widgets.png", 18, 18, 18, 5),
	INDICATOR_GREEN("widgets.png", 0, 23, 18, 5),
	INDICATOR_YELLOW("widgets.png", 18, 23, 18, 5),
	INDICATOR_RED("widgets.png", 36, 23, 18, 5),
	GRAY("background.png", 0, 0, 16, 16),
	
	SCROLLBAR_AXIS("widgets.png", 224, 0, 32, 256),
	SCROLLBAR_CAP("widgets.png", 0, 87, 40, 6),
	SCROLLBAR_INDICATOR("widgets.png", 0, 75, 40, 12),
	SCROLLBAR_BACKGROUND("widgets.png", 0, 93, 40, 16),
	
	// Icons
	ICON_NONE("icons.png", 16, 16, 16, 16),
	ICON_ADD("icons.png", 16, 16),
	ICON_TRASH("icons.png", 16, 0, 16, 16),
	ICON_3x3("icons.png", 32, 0, 16, 16),
	ICON_TARGET("icons.png", 48, 0, 16, 16),
	ICON_CONFIRM("icons.png", 0, 16, 16, 16),
	
	ICON_NORMAL_ROOF("icons.png", 32, 16, 16, 16),
	ICON_FLAT_ROOF("icons.png", 48, 16, 16, 16),
	ICON_NO_ROOF("icons.png", 0, 32, 16, 16),
	
	ICON_TOWER_NO_ROOF("icons.png", 16, 32, 16, 16),
	ICON_TOWER_ROOF("icons.png", 32, 32, 16, 16),
	ICON_TOWER_FLAT_ROOF("icons.png", 48, 32, 16, 16),
	
	ICON_LAYER_REGULAR("icons.png", 0, 48, 16, 16),
	ICON_LAYER_OPEN("icons.png", 16, 48, 16, 16),
	ICON_LAYER_FOUNDATION("icons.png", 32, 48, 16, 16),
	ICON_LAYER_SPECIAL("icons.png", 48, 48, 16, 16),
	
	ICON_TOOL_RESHAPE("icons.png", 0, 64, 16, 16),
	ICON_TOOL_ROOM("icons.png", 16, 64, 16, 16),
	ICON_TOOL_TOWER("icons.png", 32, 64, 16, 16),
	ICON_TOOL_STACK("icons.png", 48, 64, 16, 16),
	
	ICON_TOOL_HEIGHT("icons.png", 0, 80, 16, 16),
	ICON_TOOL_REROLL("icons.png", 16, 80, 16, 16),
	ICON_TOOL_REROLL_TARGET("icons.png", 32, 80, 16, 16),
	ICON_TOOL_PALETTE("icons.png", 48, 80, 16, 16),
	
	ICON_FOLDER("icons.png", 0, 96, 16, 16),
	ICON_REFRESH("icons.png", 16, 96, 16, 16);
	
	public static final int FONT_COLOR = 0x373F5A;
	
	public final ResourceLocation location;
	public int width, height;
	public int startX, startY;
	
	private ScreenResources(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}
	
	private ScreenResources(String location, int startX, int startY, int width, int height) {
		this.location = new ResourceLocation(TheMightyArchitect.ID, "textures/gui/" + location);
		this.width = width; this.height = height;
		this.startX = startX; this.startY = startY;
	}
	
	public void draw(PoseStack ms, GuiComponent screen, int i, int j) {
		bind();
		screen.blit(ms, i, j, startX, startY, width, height);
	}

	public void bind() {
		RenderSystem.setShaderTexture(0, location);
	}

}
