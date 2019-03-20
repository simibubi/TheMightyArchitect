package com.simibubi.mightyarchitect;

import org.apache.logging.log4j.Logger;

import com.simibubi.mightyarchitect.block.AllBlocks;
import com.simibubi.mightyarchitect.gui.GuiHandler;
import com.simibubi.mightyarchitect.item.AllItems;
import com.simibubi.mightyarchitect.networking.PacketInstantPrint;
import com.simibubi.mightyarchitect.networking.PacketInstantPrint.PacketHandlerInstantPrint;
import com.simibubi.mightyarchitect.networking.PacketNbt;
import com.simibubi.mightyarchitect.networking.PacketNbt.PacketHandlerNbt;
import com.simibubi.mightyarchitect.networking.PacketPlaceSign;
import com.simibubi.mightyarchitect.networking.PacketPlaceSign.PacketHandlerPlaceSign;
import com.simibubi.mightyarchitect.networking.PacketSender;
import com.simibubi.mightyarchitect.proxy.IProxy;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber
@Mod(modid = TheMightyArchitect.ID, name = TheMightyArchitect.NAME, version = TheMightyArchitect.VERSION, useMetadata = true)
public class TheMightyArchitect {

    public static final String ID = "mightyarchitect";
    public static final String NAME = "The Mighty Architect";
    public static final String VERSION = "0.1.3";

    @Mod.Instance
    public static TheMightyArchitect instance;
    
    @SidedProxy(clientSide="com.simibubi.mightyarchitect.proxy.CombinedClientProxy",
            	serverSide="com.simibubi.mightyarchitect.proxy.DedicatedServerProxy")
    	public static IProxy proxy;
    

    public static Logger logger;
    
    public static CreativeTabs creativeTab = new MightyArchitectCreativeTab(CreativeTabs.getNextID(), "mightyArchitectTab");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    	proxy.init(event);
    	NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
      	PacketSender.INSTANCE.registerMessage(PacketHandlerNbt.class, PacketNbt.class, 0, Side.SERVER);
      	PacketSender.INSTANCE.registerMessage(PacketHandlerInstantPrint.class, PacketInstantPrint.class, 1, Side.SERVER);
      	PacketSender.INSTANCE.registerMessage(PacketHandlerPlaceSign.class, PacketPlaceSign.class, 2, Side.SERVER);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	proxy.postInit(event);
    }
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
    	AllItems.registerAll(event.getRegistry());
		AllBlocks.registerAllItemBlocks(event.getRegistry());
    }
    
    @SubscribeEvent
	public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
    }
    
    @SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
    	AllBlocks.registerAll(event.getRegistry());
	}
    
	@SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
		AllItems.initModels();
		AllBlocks.initModels();	
    }
    
    
}