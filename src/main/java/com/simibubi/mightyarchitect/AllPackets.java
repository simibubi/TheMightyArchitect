package com.simibubi.mightyarchitect;

import com.simibubi.mightyarchitect.networking.InstantPrintPacket;
import com.simibubi.mightyarchitect.networking.PlaceSignPacket;
import com.simibubi.mightyarchitect.networking.SetHotbarItemPacket;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class AllPackets {

	public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(TheMightyArchitect.ID, "simple_channel"), () -> "1", v -> v.equals("1"), v -> v.equals("1"));

	public static void registerPackets() {
		int i = 0;

		channel.registerMessage(i++, InstantPrintPacket.class, InstantPrintPacket::toBytes, InstantPrintPacket::new,
				InstantPrintPacket::handle);
		channel.registerMessage(i++, PlaceSignPacket.class, PlaceSignPacket::toBytes, PlaceSignPacket::new,
				PlaceSignPacket::handle);
		channel.registerMessage(i++, SetHotbarItemPacket.class, SetHotbarItemPacket::toBytes, SetHotbarItemPacket::new,
				SetHotbarItemPacket::handle);
	}

}
