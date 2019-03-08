package com.simibubi.mightyarchitect.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketItemNBT implements IMessage {
	
	public ItemStack stack;

	public PacketItemNBT() {
	}
	
	public PacketItemNBT(ItemStack stack) {
		this.stack = stack;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		stack = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeItemStack(buf, stack);
	}

	public static class SimiNbtPacketHandler implements IMessageHandler<PacketItemNBT, IMessage>{

		@Override
		public IMessage onMessage(PacketItemNBT message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			
			player.getServerWorld().addScheduledTask(() -> {
				ItemStack heldItem = player.getHeldItemMainhand();
				if (heldItem.getItem() == message.stack.getItem()) {
					heldItem.setTagCompound(message.stack.getTagCompound());
				}
			});
			
			//no response
			return null;
		}
		
	}
	
}
