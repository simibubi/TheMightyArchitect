package com.simibubi.mightyarchitect.proxy;

import com.simibubi.mightyarchitect.buildomatico.client.command.BuildomaticoCommands;
import com.simibubi.mightyarchitect.item.AllItems;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class CombinedClientProxy implements IProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
	}

	@Override
	public void init(FMLInitializationEvent event) {
		AllItems.initColorHandlers();
		BuildomaticoCommands.init();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
	}
	
}
