package com.simibubi.mightyarchitect.networking;

import java.util.function.Supplier;

import com.simibubi.mightyarchitect.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PlaceSignPacket {

	public String text1;
	public String text2;
	public BlockPos position;

	public PlaceSignPacket() {}

	public PlaceSignPacket(String textLine1, String textLine2, BlockPos position) {
		this.text1 = textLine1;
		this.text2 = textLine2;
		this.position = position;
	}

	public PlaceSignPacket(FriendlyByteBuf buffer) {
		this(buffer.readUtf(128), buffer.readUtf(128), buffer.readBlockPos());
	}

	public void toBytes(FriendlyByteBuf buffer) {
		buffer.writeUtf(text1);
		buffer.writeUtf(text2);
		buffer.writeBlockPos(position);
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get()
			.enqueueWork(() -> {
				Level entityWorld = context.get()
					.getSender()
					.getCommandSenderWorld();
				entityWorld.setBlockAndUpdate(position, Blocks.SPRUCE_SIGN.defaultBlockState());
				SignBlockEntity sign = (SignBlockEntity) entityWorld.getBlockEntity(position);
				sign.setMessage(0, Lang.text(text1)
					.component());
				sign.setMessage(1, Lang.text(text2)
					.component());
			});
	}

}
