package com.simibubi.mightyarchitect.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketInstantPrint implements IMessage {

	private BunchOfBlocks blocks;

	public PacketInstantPrint() {
	}

	public PacketInstantPrint(BunchOfBlocks blocks) {
		this.blocks = blocks;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		Map<BlockPos, IBlockState> blocks = new HashMap<>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			NBTTagCompound blockTag = ByteBufUtils.readTag(buf);
			NBTTagCompound posTag = ByteBufUtils.readTag(buf);
			blocks.put(NBTUtil.getPosFromTag(posTag), NBTUtil.readBlockState(blockTag));
		}
		this.blocks = new BunchOfBlocks(blocks);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(blocks.size);
		blocks.blocks.forEach((pos, state) -> {
			ByteBufUtils.writeTag(buf, NBTUtil.writeBlockState(new NBTTagCompound(), state));
			ByteBufUtils.writeTag(buf, NBTUtil.createPosTag(pos));
		});
	}
	
	public static List<PacketInstantPrint> sendSchematic(Map<BlockPos, IBlockState> blockMap, BlockPos anchor) {
		List<PacketInstantPrint> packets = new LinkedList<>();
		
		Map<BlockPos, IBlockState> currentMap = new HashMap<>(BunchOfBlocks.MAX_SIZE);
		List<BlockPos> posList = new ArrayList<>(blockMap.keySet());
		
		for (int i = 0; i < blockMap.size(); i++) {
			if (currentMap.size() >= BunchOfBlocks.MAX_SIZE) {
				packets.add(new PacketInstantPrint(new BunchOfBlocks(currentMap)));
				currentMap = new HashMap<>(BunchOfBlocks.MAX_SIZE);
			}
			currentMap.put(posList.get(i).add(anchor), blockMap.get(posList.get(i)));
		}
		packets.add(new PacketInstantPrint(new BunchOfBlocks(currentMap)));
		
		return packets;
	}
	
	static class BunchOfBlocks {
		static final int MAX_SIZE = 32;
		Map<BlockPos, IBlockState> blocks;
		int size;
		
		public BunchOfBlocks(Map<BlockPos, IBlockState> blocks) {
			this.blocks = blocks;
			this.size = blocks.size();
		}
		
	}

	public static class PacketHandlerInstantPrint implements IMessageHandler<PacketInstantPrint, IMessage> {

		@Override
		public IMessage onMessage(PacketInstantPrint message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> {
				message.blocks.blocks.forEach((pos, state) -> {
					player.getEntityWorld().setBlockState(pos, state);
				});
			});

			// no response
			return null;
		}

	}

}
