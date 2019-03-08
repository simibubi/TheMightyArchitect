package com.simibubi.mightyarchitect.buildomatico.model.context;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Context {
	
	private BlockPos anchor;
	private EntityPlayer owner;
	
	public Context(BlockPos anchor, EntityPlayer owner) {
		this.anchor = anchor;
		this.owner = owner;
	}
	
	public BlockPos getAnchor() {
		return anchor;
	}
	
	public EntityPlayer getOwner() {
		return owner;
	}
	
	public void writeToNBT(NBTTagCompound compound) {
		if (compound != null) {
			NBTTagCompound contextTag = new NBTTagCompound();
			contextTag.setTag("Anchor", NBTUtil.createPosTag(anchor));
			NBTTagCompound profile = new NBTTagCompound();
			NBTUtil.writeGameProfile(profile, owner.getGameProfile());
			contextTag.setTag("Owner", profile);
			compound.setTag("Context", contextTag);
		}
		
	}
	
	public static Context readFromNBT(World world, NBTTagCompound compound) {
		if (compound != null && compound.hasKey("Context")) {
			NBTTagCompound contextTag = compound.getCompoundTag("Context");
			BlockPos anchor = NBTUtil.getPosFromTag(contextTag.getCompoundTag("Anchor"));
			GameProfile owner = NBTUtil.readGameProfileFromNBT(contextTag.getCompoundTag("Owner"));
			return new Context(anchor, world.getPlayerEntityByUUID(owner.getId()));
		}
		return null;
	}

}
