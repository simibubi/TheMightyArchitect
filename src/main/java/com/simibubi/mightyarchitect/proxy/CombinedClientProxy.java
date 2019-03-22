package com.simibubi.mightyarchitect.proxy;

import org.lwjgl.input.Keyboard;

import com.simibubi.mightyarchitect.item.AllItems;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class CombinedClientProxy implements IProxy {
	
	public static KeyBinding COMPOSE;

	@Override
	public void preInit(FMLPreInitializationEvent event) {
	}

	@Override
	public void init(FMLInitializationEvent event) {
		AllItems.initColorHandlers();
		
		COMPOSE = new KeyBinding("Start composing", Keyboard.KEY_G, "The Mighty Architect");
		ClientRegistry.registerKeyBinding(COMPOSE);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
	}
	
}
