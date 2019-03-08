package com.simibubi.mightyarchitect;

import com.simibubi.mightyarchitect.item.AllItems;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class MightyArchitectCreativeTab extends CreativeTabs
{
    public MightyArchitectCreativeTab(int index, String label)
    {
        super(index, label);
    }

    @SideOnly(Side.CLIENT)
    public ItemStack getTabIconItem()
    {
        return new ItemStack(AllItems.blueprint_filled);
    }
}
