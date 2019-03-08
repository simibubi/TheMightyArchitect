package com.simibubi.mightyarchitect.item;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class ItemForMightyArchitects extends Item {

	public ItemForMightyArchitects(String name) {
		this.setRegistryName(name).setUnlocalizedName(TheMightyArchitect.ID + "." + name).setCreativeTab(TheMightyArchitect.creativeTab);
	}
	
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}
	
}
