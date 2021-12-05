package com.simibubi.mightyarchitect;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public enum AllSpecialTextures {

	BLANK("blank.png"),
	CHECKERED("checkerboard.png"),
	THIN_CHECKERED("thin_checkerboard.png"),
	HIGHLIGHT_CHECKERED("highlighted_checkerboard.png"),
	SELECTION("selection.png"),
	
	FOUNDATION("foundation.png"),
	NORMAL("normal.png"),
	TOWER_FOUNDATION("tower_foundation.png"),
	TOWER_NORMAL("tower_normal.png"),
	
    Room("inner.png"),
    RoomTransparent("inner_transparent.png"),
    SelectedRoom("inner_selected.png"),
    SuperSelectedRoom("inner_super_selected.png"),
    Selection("select.png"),
    Exporter("exporter.png"),
    
    PaletteUnchanged("palette_unchanged.png"),
    PaletteChanged("palette_changed.png"),
    
    Trim("trim.png");

    private ResourceLocation location;

    private AllSpecialTextures(String filename) {
        location = new ResourceLocation(TheMightyArchitect.ID,
                "textures/block/marker/" + filename);
    }

    public void bind() {
	    RenderSystem.setShaderTexture(0, location);
    }
    
    public ResourceLocation getLocation() {
		return location;
	}

}