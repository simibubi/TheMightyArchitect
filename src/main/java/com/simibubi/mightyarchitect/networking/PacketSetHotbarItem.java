package com.simibubi.mightyarchitect.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetHotbarItem implements IMessage {
	
	private int slot;
	private ItemStack stack;

	public PacketSetHotbarItem() {
	}
	
	public PacketSetHotbarItem(int slot, ItemStack stack) {
		this.slot = slot;
		this.stack = stack;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		slot = buf.readInt();
		stack = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(slot);
		ByteBufUtils.writeItemStack(buf, stack);
	}

	public static class PacketHandlerSetHotbarItem implements IMessageHandler<PacketSetHotbarItem, IMessage>{

		@Override
		public IMessage onMessage(PacketSetHotbarItem message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			if (!player.isCreative())
				return null;
			
			player.replaceItemInInventory(message.slot, message.stack);
			player.inventoryContainer.detectAndSendChanges();
			
			//no response
			return null;
		}
		
	}
	
}
