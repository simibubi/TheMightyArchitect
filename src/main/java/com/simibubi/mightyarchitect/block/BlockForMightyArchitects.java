package com.simibubi.mightyarchitect.block;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class BlockForMightyArchitects extends Block {

	public BlockForMightyArchitects(String name, Material materialIn) {
		super(materialIn);
		if (materialIn == Material.AIR) {
			this.setRegistryName(name).setUnlocalizedName(TheMightyArchitect.ID + "." + name);
			
		} else {
			this.setRegistryName(name).setUnlocalizedName(TheMightyArchitect.ID + "." + name).setCreativeTab(TheMightyArchitect.creativeTab);
		}
	}
	
	public void initItemModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}

}
