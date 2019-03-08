package com.simibubi.mightyarchitect.networking;

import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class PacketSender {

	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(TheMightyArchitect.ID);
	
}
