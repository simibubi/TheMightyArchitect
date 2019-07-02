package com.simibubi.mightyarchitect.networking;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketSetHotbarItem {
	
	private int slot;
	private ItemStack stack;

	public PacketSetHotbarItem(int slot, ItemStack stack) {
		this.slot = slot;
		this.stack = stack;
	}
	
	public PacketSetHotbarItem(PacketBuffer buffer) {
		this(buffer.readInt(), buffer.readItemStack());
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeInt(slot);
		buffer.writeItemStack(stack);
	}

	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			if (!player.isCreative())
				return;
			
			player.replaceItemInInventory(slot, stack);
			player.container.detectAndSendChanges();
		});
	}
	
}
