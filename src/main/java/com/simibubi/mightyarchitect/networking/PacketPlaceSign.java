package com.simibubi.mightyarchitect.networking;

import java.util.function.Supplier;

import net.minecraft.block.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketPlaceSign {
	
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
	
	public PacketPlaceSign(PacketBuffer buffer) {
		this(buffer.readString(), buffer.readString(), buffer.readBlockPos());
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeString(text1);
		buffer.writeString(text2);
		buffer.writeBlockPos(position);
	}
	
	public void handle(Supplier<Context> context) {
		context.get().enqueueWork(() -> {
			World entityWorld = context.get().getSender().getEntityWorld();
			entityWorld.setBlockState(position, Blocks.SPRUCE_SIGN.getDefaultState());
			SignTileEntity sign = (SignTileEntity) entityWorld.getTileEntity(position);
			sign.signText[0] = new StringTextComponent(text1);
			sign.signText[1] = new StringTextComponent(text2);
		});
	}
	
}
