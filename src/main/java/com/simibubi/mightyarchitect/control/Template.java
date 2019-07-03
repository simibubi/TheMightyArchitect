package com.simibubi.mightyarchitect.control;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.BlockRotationProcessor;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;

public class Template {

	/** blocks in the structure */
	private final List<Template.BlockInfo> blocks = Lists.<Template.BlockInfo>newArrayList();
	/** size of the structure */
	private BlockPos size = BlockPos.ORIGIN;
	/** The author of this template. */
	private String author = "?";

	public BlockPos getSize() {
		return this.size;
	}

	public void setSize(BlockPos size) {
		this.size = size;
	}

	public void setAuthor(String authorIn) {
		this.author = authorIn;
	}

	public String getAuthor() {
		return this.author;
	}

	public void putBlock(BlockPos pos, BlockState state) {
		blocks.add(new BlockInfo(pos, state, null));
	}

	public List<Template.BlockInfo> getBlocks() {
		return blocks;
	}

	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		Template.BasicPalette template$basicpalette = new Template.BasicPalette();
		NBTTagList nbttaglist = new NBTTagList();

		for (Template.BlockInfo template$blockinfo : this.blocks) {
			CompoundNBT nbttagcompound = new CompoundNBT();
			nbttagcompound.setTag("pos", this.writeInts(template$blockinfo.pos.getX(), template$blockinfo.pos.getY(),
					template$blockinfo.pos.getZ()));
			nbttagcompound.setInteger("state", template$basicpalette.idFor(template$blockinfo.blockState));

			if (template$blockinfo.tileentityData != null) {
				nbttagcompound.setTag("nbt", template$blockinfo.tileentityData);
			}

			nbttaglist.appendTag(nbttagcompound);
		}

		NBTTagList nbttaglist2 = new NBTTagList();

		for (BlockState iblockstate : template$basicpalette) {
			nbttaglist2.appendTag(NBTUtil.writeBlockState(new CompoundNBT(), iblockstate));
		}

		net.minecraftforge.fml.common.FMLCommonHandler.instance().getDataFixer().writeVersionData(nbt); // Moved
																										// up
																										// for
																										// MC
																										// updating
																										// reasons.
		nbt.setTag("palette", nbttaglist2);
		nbt.setTag("blocks", nbttaglist);
		nbt.setTag("size", this.writeInts(this.size.getX(), this.size.getY(), this.size.getZ()));
		nbt.setString("author", this.author);
		nbt.setInteger("DataVersion", 1343);
		return nbt;
	}

	public void read(CompoundNBT compound) {
		this.blocks.clear();
		NBTTagList nbttaglist = compound.getTagList("size", 3);
		this.size = new BlockPos(nbttaglist.getIntAt(0), nbttaglist.getIntAt(1), nbttaglist.getIntAt(2));
		this.author = compound.getString("author");
		Template.BasicPalette template$basicpalette = new Template.BasicPalette();
		NBTTagList nbttaglist1 = compound.getTagList("palette", 10);

		for (int i = 0; i < nbttaglist1.tagCount(); ++i) {
			template$basicpalette.addMapping(NBTUtil.readBlockState(nbttaglist1.getCompoundTagAt(i)), i);
		}

		NBTTagList nbttaglist3 = compound.getTagList("blocks", 10);

		for (int j = 0; j < nbttaglist3.tagCount(); ++j) {
			CompoundNBT nbttagcompound = nbttaglist3.getCompoundTagAt(j);
			NBTTagList nbttaglist2 = nbttagcompound.getTagList("pos", 3);
			BlockPos blockpos = new BlockPos(nbttaglist2.getIntAt(0), nbttaglist2.getIntAt(1), nbttaglist2.getIntAt(2));
			BlockState iblockstate = template$basicpalette.stateFor(nbttagcompound.getInteger("state"));
			CompoundNBT nbttagcompound1;

			if (nbttagcompound.hasKey("nbt")) {
				nbttagcompound1 = nbttagcompound.getCompoundTag("nbt");
			} else {
				nbttagcompound1 = null;
			}

			this.blocks.add(new Template.BlockInfo(blockpos, iblockstate, nbttagcompound1));
		}

	}

	static class BasicPalette implements Iterable<BlockState> {
		public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.getDefaultState();
		final ObjectIntIdentityMap<BlockState> ids;
		private int lastId;

		private BasicPalette() {
			this.ids = new ObjectIntIdentityMap<BlockState>(16);
		}

		public int idFor(BlockState state) {
			int i = this.ids.get(state);

			if (i == -1) {
				i = this.lastId++;
				this.ids.put(state, i);
			}

			return i;
		}

		@Nullable
		public BlockState stateFor(int id) {
			BlockState iblockstate = this.ids.getByValue(id);
			return iblockstate == null ? DEFAULT_BLOCK_STATE : iblockstate;
		}

		public Iterator<BlockState> iterator() {
			return this.ids.iterator();
		}

		public void addMapping(BlockState p_189956_1_, int p_189956_2_) {
			this.ids.put(p_189956_1_, p_189956_2_);
		}
	}

	public static class BlockInfo {
		/** the position the block is to be generated to */
		public final BlockPos pos;
		/** The type of block in this particular spot in the structure. */
		public final BlockState blockState;
		/** NBT data for the tileentity */
		public final CompoundNBT tileentityData;

		public BlockInfo(BlockPos posIn, BlockState stateIn, @Nullable CompoundNBT compoundIn) {
			this.pos = posIn;
			this.blockState = stateIn;
			this.tileentityData = compoundIn;
		}
	}

	private NBTTagList writeInts(int... values) {
		NBTTagList nbttaglist = new NBTTagList();

		for (int i : values) {
			nbttaglist.appendTag(new NBTTagInt(i));
		}

		return nbttaglist;
	}

	/**
	 * This takes the data stored in this instance and puts them into the world.
	 */
	public void addBlocksToWorld(World worldIn, BlockPos pos, PlacementSettings placementIn) {
		this.addBlocksToWorld(worldIn, pos, new BlockRotationProcessor(pos, placementIn), placementIn, 2);
	}

	/**
	 * This takes the data stored in this instance and puts them into the world.
	 */
	public void addBlocksToWorld(World worldIn, BlockPos pos, PlacementSettings placementIn, int flags) {
		this.addBlocksToWorld(worldIn, pos, new BlockRotationProcessor(pos, placementIn), placementIn, flags);
	}

	public void addBlocksToWorld(World worldIn, BlockPos origin, @Nullable ITemplateProcessor templateProcessor,
			PlacementSettings placementIn, int flags) {
		if (!this.blocks.isEmpty() && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
			Block block = placementIn.getReplacedBlock();
			StructureBoundingBox structureboundingbox = placementIn.getBoundingBox();

			for (Template.BlockInfo template$blockinfo : this.blocks) {
				BlockPos blockpos = transformedBlockPos(placementIn, template$blockinfo.pos).add(origin);
				// Forge: skip processing blocks outside BB to prevent cascading
				// worldgen issues
				if (structureboundingbox != null && !structureboundingbox.isVecInside(blockpos))
					continue;
				net.minecraft.world.gen.structure.template.Template.BlockInfo blockInfoVanilla = new net.minecraft.world.gen.structure.template.Template.BlockInfo(template$blockinfo.pos,
						template$blockinfo.blockState, template$blockinfo.tileentityData);
				net.minecraft.world.gen.structure.template.Template.BlockInfo blockInfoVanillaProcessed = templateProcessor != null
						? templateProcessor.processBlock(worldIn, blockpos, blockInfoVanilla)
						: blockInfoVanilla;
				Template.BlockInfo template$blockinfo1 = new BlockInfo(blockInfoVanillaProcessed.pos, blockInfoVanillaProcessed.blockState, blockInfoVanillaProcessed.tileentityData);
				if (template$blockinfo1 != null) {
					Block block1 = template$blockinfo1.blockState.getBlock();

					if ((block == null || block != block1)
							&& (!placementIn.getIgnoreStructureBlock() || block1 != Blocks.STRUCTURE_BLOCK)
							&& (structureboundingbox == null || structureboundingbox.isVecInside(blockpos))) {
						BlockState iblockstate = template$blockinfo1.blockState.withMirror(placementIn.getMirror());
						BlockState iblockstate1 = iblockstate.withRotation(placementIn.getRotation());

						if (template$blockinfo1.tileentityData != null) {
							TileEntity tileentity = worldIn.getTileEntity(blockpos);

							if (tileentity != null) {
								if (tileentity instanceof IInventory) {
									((IInventory) tileentity).clear();
								}

								worldIn.setBlockState(blockpos, Blocks.BARRIER.getDefaultState(), 4);
							}
						}

						if (worldIn.setBlockState(blockpos, iblockstate1, flags)
								&& template$blockinfo1.tileentityData != null) {
							TileEntity tileentity2 = worldIn.getTileEntity(blockpos);

							if (tileentity2 != null) {
								template$blockinfo1.tileentityData.setInteger("x", blockpos.getX());
								template$blockinfo1.tileentityData.setInteger("y", blockpos.getY());
								template$blockinfo1.tileentityData.setInteger("z", blockpos.getZ());
								tileentity2.readFromNBT(template$blockinfo1.tileentityData);
								tileentity2.mirror(placementIn.getMirror());
								tileentity2.rotate(placementIn.getRotation());
							}
						}
					}
				}
			}

			for (Template.BlockInfo template$blockinfo2 : this.blocks) {
				if (block == null || block != template$blockinfo2.blockState.getBlock()) {
					BlockPos blockpos1 = transformedBlockPos(placementIn, template$blockinfo2.pos).add(origin);

					if (structureboundingbox == null || structureboundingbox.isVecInside(blockpos1)) {
						worldIn.notifyNeighborsRespectDebug(blockpos1, template$blockinfo2.blockState.getBlock(),
								false);

						if (template$blockinfo2.tileentityData != null) {
							TileEntity tileentity1 = worldIn.getTileEntity(blockpos1);

							if (tileentity1 != null) {
								tileentity1.markDirty();
							}
						}
					}
				}
			}

		}
	}

	private static BlockPos transformedBlockPos(BlockPos pos, Mirror mirrorIn, Rotation rotationIn) {
		int i = pos.getX();
		int j = pos.getY();
		int k = pos.getZ();
		boolean flag = true;

		switch (mirrorIn) {
		case LEFT_RIGHT:
			k = -k;
			break;
		case FRONT_BACK:
			i = -i;
			break;
		default:
			flag = false;
		}

		switch (rotationIn) {
		case COUNTERCLOCKWISE_90:
			return new BlockPos(k, j, -i);
		case CLOCKWISE_90:
			return new BlockPos(-k, j, i);
		case CLOCKWISE_180:
			return new BlockPos(-i, j, -k);
		default:
			return flag ? new BlockPos(i, j, k) : pos;
		}
	}

	public static BlockPos transformedBlockPos(PlacementSettings placementIn, BlockPos pos) {
		return transformedBlockPos(pos, placementIn.getMirror(), placementIn.getRotation());
	}

}
