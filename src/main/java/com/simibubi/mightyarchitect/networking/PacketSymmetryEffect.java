package com.simibubi.mightyarchitect.networking;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.mightyarchitect.item.SymmetryHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSymmetryEffect implements IMessage {

	private BlockPos mirror;
	private List<BlockPos> positions;

	public PacketSymmetryEffect() {
	}
	
	public PacketSymmetryEffect(BlockPos mirror, List<BlockPos> positions) {
		this.mirror = mirror;
		this.positions = positions;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		mirror = NBTUtil.getPosFromTag(ByteBufUtils.readTag(buf));
		int amt = buf.readInt();
		positions = new ArrayList<>(amt);
		for (int i = 0; i < amt; i++) {
			positions.add(NBTUtil.getPosFromTag(ByteBufUtils.readTag(buf)));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, NBTUtil.createPosTag(mirror));
		buf.writeInt(positions.size());
		for (BlockPos blockPos : positions) {			
			ByteBufUtils.writeTag(buf, NBTUtil.createPosTag(blockPos));
		}
	}

	public static class PacketHandlerSymmetryEffect implements IMessageHandler<PacketSymmetryEffect, IMessage>{

		@Override
		public IMessage onMessage(PacketSymmetryEffect message, MessageContext ctx) {
			if (Minecraft.getMinecraft().player.getPositionVector().distanceTo(new Vec3d(message.mirror)) > 100)
				return null;
			
			for (BlockPos to : message.positions) 
				SymmetryHandler.drawEffect(message.mirror, to);
			//no response
			return null;
		}
		
	}

}
