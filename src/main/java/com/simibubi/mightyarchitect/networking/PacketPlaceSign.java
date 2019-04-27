package com.simibubi.mightyarchitect.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPlaceSign implements IMessage {
	
	public String text1;
	public String text2;
	public BlockPos position;

	public PacketPlaceSign() {
	}
	
	public PacketPlaceSign(String textLine1, String textLine2, BlockPos position) {
		this.text1 = textLine1;
		this.text2 = textLine2;
		this.position = position;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		text1 = ByteBufUtils.readUTF8String(buf);
		text2 = ByteBufUtils.readUTF8String(buf);
		position = NBTUtil.getPosFromTag(ByteBufUtils.readTag(buf));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, text1);
		ByteBufUtils.writeUTF8String(buf, text2);
		ByteBufUtils.writeTag(buf, NBTUtil.createPosTag(position));
	}

	public static class PacketHandlerPlaceSign implements IMessageHandler<PacketPlaceSign, IMessage>{

		@Override
		public IMessage onMessage(PacketPlaceSign message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			
			player.getServerWorld().addScheduledTask(() -> {
				player.world.setBlockState(message.position, Blocks.STANDING_SIGN.getDefaultState());
				TileEntitySign sign = (TileEntitySign) player.world.getTileEntity(message.position);
				sign.signText[0] = new TextComponentString(message.text1);
				sign.signText[1] = new TextComponentString(message.text2);
			});
			
			//no response
			return null;
		}
		
	}
	
}
