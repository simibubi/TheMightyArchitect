package com.simibubi.mightyarchitect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.simibubi.mightyarchitect.gui.Keyboard;
import com.simibubi.mightyarchitect.networking.Packets;

import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@EventBusSubscriber(bus = Bus.FORGE)
@Mod(TheMightyArchitect.ID)
public class TheMightyArchitect {

	public static final String ID = "mightyarchitect";
	public static final String NAME = "The Mighty Architect";
	public static final String VERSION = "0.4.1";

	public static TheMightyArchitect instance;

	@OnlyIn(value = Dist.CLIENT)
	public static KeyBinding COMPOSE;
	
	@OnlyIn(value = Dist.CLIENT)
	public static KeyBinding TOOL_MENU;
	
	public static Logger logger = LogManager.getLogger();

	public TheMightyArchitect() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::clientInit);
		modEventBus.addListener(this::init);
	}

	private void clientInit(FMLClientSetupEvent event) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			AllItems.initColorHandlers();

			COMPOSE = new KeyBinding("Start composing", Keyboard.G, NAME);
			TOOL_MENU = new KeyBinding("Tool Menu (Hold)", Keyboard.LALT, NAME);
			ClientRegistry.registerKeyBinding(COMPOSE);
			ClientRegistry.registerKeyBinding(TOOL_MENU);
			ScrollFixer.init();
		});
	}

	private void init(final FMLCommonSetupEvent event) {
		Packets.registerPackets();
	}

	@EventBusSubscriber(bus = Bus.MOD)
	public static class RegistryListener {

		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event) {
			AllItems.registerItems(event.getRegistry());
			AllBlocks.registerItemBlocks(event.getRegistry());
		}

		@SubscribeEvent
		public static void registerBlocks(RegistryEvent.Register<Block> event) {
			AllBlocks.registerBlocks(event.getRegistry());
		}
	}

}