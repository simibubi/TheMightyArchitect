package com.simibubi.mightyarchitect.networking;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SetHotbarItemPacket {
	
	private int slot;
	private ItemStack stack;

	public SetHotbarItemPacket(int slot, ItemStack stack) {
		this.slot = slot;
		this.stack = stack;
	}
	
	public SetHotbarItemPacket(FriendlyByteBuf buffer) {
		this(buffer.readInt(), buffer.readItem());
	}

	public void toBytes(FriendlyByteBuf buffer) {
		buffer.writeInt(slot);
		buffer.writeItem(stack);
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayer player = context.get().getSender();
			if (!player.isCreative())
				return;

			player.getInventory().setItem(slot, stack);
			//player.setSlot(slot, stack);
			player.inventoryMenu.broadcastChanges();
		});
	}
	
}
