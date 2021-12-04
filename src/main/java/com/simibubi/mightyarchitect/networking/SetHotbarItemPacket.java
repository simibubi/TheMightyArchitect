package com.simibubi.mightyarchitect.networking;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SetHotbarItemPacket {
	
	private int slot;
	private ItemStack stack;

	public SetHotbarItemPacket(int slot, ItemStack stack) {
		this.slot = slot;
		this.stack = stack;
	}
	
	public SetHotbarItemPacket(PacketBuffer buffer) {
		this(buffer.readInt(), buffer.readItem());
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeInt(slot);
		buffer.writeItem(stack);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (!player.isCreative())
				return;
			
			player.setSlot(slot, stack);
			player.inventoryMenu.broadcastChanges();
		});
	}
	
}
