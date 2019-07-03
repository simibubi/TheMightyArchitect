package com.simibubi.mightyarchitect;

import org.apache.logging.log4j.Logger;

import com.simibubi.mightyarchitect.networking.Packets;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
	public static final String VERSION = "0.3.0";

	public static TheMightyArchitect instance;
	public static KeyBinding COMPOSE;
	public static Logger logger;

	public static ItemGroup creativeTab = new MightyArchitectItemGroup();

	public TheMightyArchitect() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
    }
	
	private void clientInit(FMLClientSetupEvent event) {
		AllItems.initColorHandlers();

		COMPOSE = new KeyBinding("Start composing", 103, "The Mighty Architect");
		ClientRegistry.registerKeyBinding(COMPOSE);
	}

	private void init(final FMLCommonSetupEvent event) {
//		NetworkRegistry.registerGuiHandler(instance, new GuiHandler());
		Packets.registerPackets();
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		AllItems.registerItems(event.getRegistry());
		AllBlocks.registerItemBlocks(event.getRegistry());
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		AllBlocks.registerBlocks(event.getRegistry());
	}

	@SubscribeEvent
	public static void onEntityConstructing(EntityConstructing event) {
		if (event.getEntity() == null)
			return;

		if (event.getEntity().world == null)
			return;

		if (!event.getEntity().world.isRemote)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (event.getEntity() instanceof PlayerEntity && mc.isSingleplayer()) {
			mc.ingameGUI.getChatGUI().printChatMessage(new StringTextComponent("The Mighty Architect v" + VERSION));
			mc.ingameGUI.getChatGUI().printChatMessage(new StringTextComponent(
					"Press [" + COMPOSE.getKey().getTranslationKey() + "] to start a Build."));
		}
	}

}