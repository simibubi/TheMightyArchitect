package com.simibubi.mightyarchitect.networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class InstantPrintPacket {

	private BunchOfBlocks blocks;

	public InstantPrintPacket() {
	}

	public InstantPrintPacket(BunchOfBlocks blocks) {
		this.blocks = blocks;
	}

	public InstantPrintPacket(PacketBuffer buf) {
		Map<BlockPos, BlockState> blocks = new HashMap<>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			CompoundNBT blockTag = buf.readCompoundTag();
			BlockPos pos = buf.readBlockPos();
			blocks.put(pos, NBTUtil.readBlockState(blockTag));
		}
		this.blocks = new BunchOfBlocks(blocks);
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeInt(blocks.size);
		blocks.blocks.forEach((pos, state) -> {
			buf.writeCompoundTag(NBTUtil.writeBlockState(state));
			buf.writeBlockPos(pos);
		});
	}
	
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			blocks.blocks.forEach((pos, state) -> {
				ctx.getSender().getEntityWorld().setBlockState(pos, state, 3);
			});
		});
    }
	
	public static List<InstantPrintPacket> sendSchematic(Map<BlockPos, BlockState> blockMap, BlockPos anchor) {
		List<InstantPrintPacket> packets = new LinkedList<>();
		
		Map<BlockPos, BlockState> currentMap = new HashMap<>(BunchOfBlocks.MAX_SIZE);
		List<BlockPos> posList = new ArrayList<>(blockMap.keySet());
		
		for (int i = 0; i < blockMap.size(); i++) {
			if (currentMap.size() >= BunchOfBlocks.MAX_SIZE) {
				packets.add(new InstantPrintPacket(new BunchOfBlocks(currentMap)));
				currentMap = new HashMap<>(BunchOfBlocks.MAX_SIZE);
			}
			currentMap.put(posList.get(i).add(anchor), blockMap.get(posList.get(i)));
		}
		packets.add(new InstantPrintPacket(new BunchOfBlocks(currentMap)));
		
		return packets;
	}
	
	static class BunchOfBlocks {
		static final int MAX_SIZE = 32;
		Map<BlockPos, BlockState> blocks;
		int size;
		
		public BunchOfBlocks(Map<BlockPos, BlockState> blocks) {
			this.blocks = blocks;
			this.size = blocks.size();
		}
		
	}

}
