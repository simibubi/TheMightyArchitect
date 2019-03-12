package com.simibubi.mightyarchitect.buildomatico.model.groundPlan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.mightyarchitect.buildomatico.model.context.Context;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignLayer;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;

public class GroundPlan {

	public int layerCount;
	private List<List<Cuboid>> layers;
	private List<Cuboid> all;
	private List<Cuboid> clearing;
	private Context context;
	private DesignTheme theme;

	public GroundPlan() {
		layers = new ArrayList<>();
		clearing = new LinkedList<>();
		all = new LinkedList<>();
		layerCount = 5;
		for (int i = 0; i < layerCount; i++)
			layers.add(new LinkedList<>());
		
		theme = DesignTheme.Medieval; // TODO: make dynamic
	}

	public List<Cuboid> getCuboidsOnLayer(int layer) {
		if (layer < layers.size()) {
			return layers.get(layer);
		}
		return new LinkedList<>();
	}

	public List<Cuboid> getRoomSpaceCuboids() {
		clearing.clear();
		for (int i = 0; i < layerCount; i++) {
			for (Cuboid cuboid : layers.get(i)) {
				clearing.add(cuboid.getClearing());
			}
		}
		return clearing;
	}

	public void add(Cuboid cuboid, int layer) {
		if (layer < layers.size()) {
			layers.get(layer).add(cuboid);
			cuboid.layer = layer;
			if (cuboid.designLayer == DesignLayer.None)
				cuboid.designLayer = (layer == 0) ? DesignLayer.Foundation : DesignLayer.Regular;
		}
		all.add(cuboid);
	}

	public List<Cuboid> getAll() {
		return all;
	}

	public boolean canCuboidAttachTo(Cuboid attach, Cuboid attachTo, EnumFacing side, int shift) {
		Cuboid clone = attach.clone();
		clone.moveToAttach(attachTo, side, shift);
		for (Cuboid c : all) {
			if (c.intersects(clone))
				return false;
		}
		return true;
	}

	public static GroundPlan readFromNBT(NBTTagCompound tagCompoundIn) {
		GroundPlan compound = new GroundPlan();
		if (tagCompoundIn != null && tagCompoundIn.hasKey("CuboidCompound")) {
			NBTTagCompound tagCompound = tagCompoundIn.getCompoundTag("CuboidCompound");

			NBTTagList cuboids = tagCompound.getTagList("Cuboids", 10);
			cuboids.forEach(nbtCuboid -> {
				Cuboid cuboid = Cuboid.readFromNBT((NBTTagCompound) nbtCuboid);
				int layer = ((NBTTagCompound) nbtCuboid).getInteger("Layer");
				compound.add(cuboid, layer);
			});

			NBTTagCompound attachedIndices = tagCompound.getCompoundTag("AttachedIndices");
			for (int index = 0; index < compound.all.size(); index++) {
				if (attachedIndices.hasKey("" + index)) {
					NBTTagCompound attached = attachedIndices.getCompoundTag("" + index);
					for (EnumFacing facing : EnumFacing.VALUES) {
						if (attached.hasKey(facing.name())) {
							for (NBTBase attachedIndex : attached.getTagList(facing.name(), 3)) {
								compound.all.get(index).putAttached(facing,
										compound.all.get(((NBTTagInt) attachedIndex).getInt()));
							}
						}
					}
				}
			}
		}
		return compound;
	}

	public void writeToNBT(NBTTagCompound tagCompound) {
		NBTTagCompound compound = new NBTTagCompound();

		NBTTagList tagListCuboids = new NBTTagList();
		NBTTagCompound tagAttachedCuboids = new NBTTagCompound();

		for (int index = 0; index < all.size(); index++) {
			Cuboid cuboid = all.get(index);

			// Cuboid
			NBTTagCompound cuboidNBT = cuboid.asNBTCompound();
			cuboidNBT.setInteger("Index", index);
			cuboidNBT.setInteger("Layer", cuboid.layer);
			tagListCuboids.appendTag(cuboidNBT);

			// Track attached indices
			NBTTagCompound tagAttached = new NBTTagCompound();
			for (EnumFacing facing : EnumFacing.VALUES) {
				NBTTagList tagListAttached = new NBTTagList();
				for (Cuboid attached : cuboid.getAttached(facing))
					tagListAttached.appendTag(new NBTTagInt(all.indexOf(attached)));
				if (!tagListAttached.hasNoTags()) {
					tagAttached.setTag(facing.name(), tagListAttached);
				}
			}
			if (!tagAttached.hasNoTags())
				tagAttachedCuboids.setTag("" + index, tagAttached);

		}
		compound.setTag("Cuboids", tagListCuboids);
		compound.setTag("AttachedIndices", tagAttachedCuboids);

		tagCompound.setTag("CuboidCompound", compound);
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public DesignTheme getTheme() {
		return theme;
	}

}
