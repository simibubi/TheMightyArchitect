package com.simibubi.mightyarchitect.networking;

import java.util.function.Supplier;

import net.minecraft.block.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PlaceSignPacket {
	
	public String text1;
	public String text2;
	public BlockPos position;

	public PlaceSignPacket() {
	}
	
	public PlaceSignPacket(String textLine1, String textLine2, BlockPos position) {
		this.text1 = textLine1;
		this.text2 = textLine2;
		this.position = position;
	}
	
	public PlaceSignPacket(PacketBuffer buffer) {
		this(buffer.readUtf(128), buffer.readUtf(128), buffer.readBlockPos());
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeUtf(text1);
		buffer.writeUtf(text2);
		buffer.writeBlockPos(position);
	}
	
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			World entityWorld = context.get().getSender().getCommandSenderWorld();
			entityWorld.setBlockAndUpdate(position, Blocks.SPRUCE_SIGN.defaultBlockState());
			SignTileEntity sign = (SignTileEntity) entityWorld.getBlockEntity(position);
			sign.setMessage(0, new StringTextComponent(text1));
			sign.setMessage(1, new StringTextComponent(text2));
		});
	}
	
}
