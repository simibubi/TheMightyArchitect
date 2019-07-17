package com.simibubi.mightyarchitect.networking;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Packets {

	public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(TheMightyArchitect.ID, "simple_channel"), () -> "1", v -> v.equals("1"), v -> v.equals("1"));

	public static void registerPackets() {
		int i = 0;

		channel.registerMessage(i++, PacketInstantPrint.class, PacketInstantPrint::toBytes, PacketInstantPrint::new,
				PacketInstantPrint::handle);
		channel.registerMessage(i++, PacketPlaceSign.class, PacketPlaceSign::toBytes, PacketPlaceSign::new,
				PacketPlaceSign::handle);
		channel.registerMessage(i++, PacketSetHotbarItem.class, PacketSetHotbarItem::toBytes, PacketSetHotbarItem::new,
				PacketSetHotbarItem::handle);
	}

}
