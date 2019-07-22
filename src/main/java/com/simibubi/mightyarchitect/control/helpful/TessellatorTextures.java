package com.simibubi.mightyarchitect.control.helpful;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public enum TessellatorTextures {

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

    private TessellatorTextures(String filename) {
        location = new ResourceLocation(TheMightyArchitect.ID,
                "textures/block/marker/" + filename);
    }

    public void bind() {
        Minecraft.getInstance().getTextureManager().bindTexture(location);
    }

}