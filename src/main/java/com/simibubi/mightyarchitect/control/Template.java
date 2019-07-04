//package com.simibubi.mightyarchitect.control;
//
//import java.util.Iterator;
//import java.util.List;
//
//import javax.annotation.Nullable;
//
//import com.google.common.collect.Lists;
//import com.mojang.datafixers.util.Pair;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.Blocks;
//import net.minecraft.block.ILiquidContainer;
//import net.minecraft.fluid.IFluidState;
//import net.minecraft.inventory.IClearable;
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.nbt.IntNBT;
//import net.minecraft.nbt.ListNBT;
//import net.minecraft.nbt.NBTUtil;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.Direction;
//import net.minecraft.util.Mirror;
//import net.minecraft.util.ObjectIntIdentityMap;
//import net.minecraft.util.Rotation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.util.math.shapes.BitSetVoxelShapePart;
//import net.minecraft.util.math.shapes.VoxelShapePart;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.World;
//import net.minecraft.world.gen.feature.template.PlacementSettings;
//import net.minecraft.world.gen.structure.template.BlockRotationProcessor;
//
//public class Template {
//
//	/** blocks in the structure */
//	private final List<Template.BlockInfo> blocks = Lists.<Template.BlockInfo>newArrayList();
//	/** size of the structure */
//	private BlockPos size = BlockPos.ZERO;
//	/** The author of this template. */
//	private String author = "?";
//
//	public BlockPos getSize() {
//		return this.size;
//	}
//
//	public void setSize(BlockPos size) {
//		this.size = size;
//	}
//
//	public void setAuthor(String authorIn) {
//		this.author = authorIn;
//	}
//
//	public String getAuthor() {
//		return this.author;
//	}
//
//	public void putBlock(BlockPos pos, BlockState state) {
//		blocks.add(new BlockInfo(pos, state, null));
//	}
//
//	public List<Template.BlockInfo> getBlocks() {
//		return blocks;
//	}
//
//	public CompoundNBT writeToNBT(CompoundNBT nbt) {
//		Template.BasicPalette template$basicpalette = new Template.BasicPalette();
//		ListNBT nbttaglist = new ListNBT();
//
//		for (Template.BlockInfo template$blockinfo : this.blocks) {
//			CompoundNBT nbttagcompound = new CompoundNBT();
//			nbttagcompound.put("pos", this.writeInts(template$blockinfo.pos.getX(), template$blockinfo.pos.getY(),
//					template$blockinfo.pos.getZ()));
//			nbttagcompound.setInteger("state", template$basicpalette.idFor(template$blockinfo.blockState));
//
//			if (template$blockinfo.tileentityData != null) {
//				nbttagcompound.put("nbt", template$blockinfo.tileentityData);
//			}
//
//			nbttaglist.add(nbttagcompound);
//		}
//
//		ListNBT nbttaglist2 = new ListNBT();
//
//		for (BlockState iblockstate : template$basicpalette) {
//			nbttaglist2.add(NBTUtil.writeBlockState(iblockstate));
//		}
//
//		nbt.put("palette", nbttaglist2);
//		nbt.put("blocks", nbttaglist);
//		nbt.put("size", this.writeInts(this.size.getX(), this.size.getY(), this.size.getZ()));
//		nbt.putString("author", this.author);
//		nbt.putInt("DataVersion", 1343);
//		return nbt;
//	}
//
//	public void read(CompoundNBT compound) {
//		this.blocks.clear();
//		ListNBT nbttaglist = compound.getList("size", 3);
//		this.size = new BlockPos(nbttaglist.getInt(0), nbttaglist.getInt(1), nbttaglist.getInt(2));
//		this.author = compound.getString("author");
//		Template.BasicPalette template$basicpalette = new Template.BasicPalette();
//		ListNBT nbttaglist1 = compound.getList("palette", 10);
//
//		for (int i = 0; i < nbttaglist1.size(); ++i) {
//			template$basicpalette.addMapping(NBTUtil.readBlockState(nbttaglist1.getCompoundTagAt(i)), i);
//		}
//
//		ListNBT nbttaglist3 = compound.getList("blocks", 10);
//
//		for (int j = 0; j < nbttaglist3.size(); ++j) {
//			CompoundNBT nbttagcompound = nbttaglist3.getCompound(j);
//			ListNBT nbttaglist2 = nbttagcompound.getList("pos", 3);
//			BlockPos blockpos = new BlockPos(nbttaglist2.getInt(0), nbttaglist2.getInt(1), nbttaglist2.getInt(2));
//			BlockState iblockstate = template$basicpalette.stateFor(nbttagcompound.getInt("state"));
//			CompoundNBT nbttagcompound1;
//
//			if (nbttagcompound.contains("nbt")) {
//				nbttagcompound1 = nbttagcompound.getCompound("nbt");
//			} else {
//				nbttagcompound1 = null;
//			}
//
//			this.blocks.add(new Template.BlockInfo(blockpos, iblockstate, nbttagcompound1));
//		}
//
//	}
//
//	static class BasicPalette implements Iterable<BlockState> {
//		public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.getDefaultState();
//		final ObjectIntIdentityMap<BlockState> ids;
//		private int lastId;
//
//		private BasicPalette() {
//			this.ids = new ObjectIntIdentityMap<BlockState>(16);
//		}
//
//		public int idFor(BlockState state) {
//			int i = this.ids.get(state);
//
//			if (i == -1) {
//				i = this.lastId++;
//				this.ids.put(state, i);
//			}
//
//			return i;
//		}
//
//		@Nullable
//		public BlockState stateFor(int id) {
//			BlockState iblockstate = this.ids.getByValue(id);
//			return iblockstate == null ? DEFAULT_BLOCK_STATE : iblockstate;
//		}
//
//		public Iterator<BlockState> iterator() {
//			return this.ids.iterator();
//		}
//
//		public void addMapping(BlockState p_189956_1_, int p_189956_2_) {
//			this.ids.put(p_189956_1_, p_189956_2_);
//		}
//	}
//
//	public static class BlockInfo {
//		/** the position the block is to be generated to */
//		public final BlockPos pos;
//		/** The type of block in this particular spot in the structure. */
//		public final BlockState blockState;
//		/** NBT data for the tileentity */
//		public final CompoundNBT tileentityData;
//
//		public BlockInfo(BlockPos posIn, BlockState stateIn, @Nullable CompoundNBT compoundIn) {
//			this.pos = posIn;
//			this.blockState = stateIn;
//			this.tileentityData = compoundIn;
//		}
//	}
//
//	private ListNBT writeInts(int... values) {
//		ListNBT nbttaglist = new ListNBT();
//
//		for (int i : values) {
//			nbttaglist.appendTag(new IntNBT(i));
//		}
//
//		return nbttaglist;
//	}
//
//	/**
//	 * This takes the data stored in this instance and puts them into the world.
//	 */
//	public void addBlocksToWorld(World worldIn, BlockPos pos, PlacementSettings placementIn) {
//		this.addBlocksToWorld(worldIn, pos, new BlockRotationProcessor(pos, placementIn), placementIn, 2);
//	}
//
//	/**
//	 * This takes the data stored in this instance and puts them into the world.
//	 */
//	public void addBlocksToWorld(World worldIn, BlockPos pos, PlacementSettings placementIn, int flags) {
//		this.addBlocksToWorld(worldIn, pos, new BlockRotationProcessor(pos, placementIn), placementIn, flags);
//	}
//
//	public boolean addBlocksToWorld(IWorld worldIn, BlockPos pos, PlacementSettings placementIn, int flags) {
//		if (this.blocks.isEmpty()) {
//			return false;
//		} else {
//			List<Template.BlockInfo> list = placementIn.func_204764_a(this.blocks, pos);
//			if ((!list.isEmpty() || !placementIn.getIgnoreEntities() && !this.entities.isEmpty())
//					&& this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
//				MutableBoundingBox mutableboundingbox = placementIn.getBoundingBox();
//				List<BlockPos> list1 = Lists.newArrayListWithCapacity(placementIn.func_204763_l() ? list.size() : 0);
//				List<Pair<BlockPos, CompoundNBT>> list2 = Lists.newArrayListWithCapacity(list.size());
//				int i = Integer.MAX_VALUE;
//				int j = Integer.MAX_VALUE;
//				int k = Integer.MAX_VALUE;
//				int l = Integer.MIN_VALUE;
//				int i1 = Integer.MIN_VALUE;
//				int j1 = Integer.MIN_VALUE;
//
//				for (Template.BlockInfo template$blockinfo : func_215387_a(worldIn, pos, placementIn, list)) {
//					BlockPos blockpos = template$blockinfo.pos;
//					if (mutableboundingbox == null || mutableboundingbox.isVecInside(blockpos)) {
//						IFluidState ifluidstate = placementIn.func_204763_l() ? worldIn.getFluidState(blockpos) : null;
//						BlockState blockstate = template$blockinfo.state.mirror(placementIn.getMirror())
//								.rotate(placementIn.getRotation());
//						if (template$blockinfo.nbt != null) {
//							TileEntity tileentity = worldIn.getTileEntity(blockpos);
//							IClearable.clearObj(tileentity);
//							worldIn.setBlockState(blockpos, Blocks.BARRIER.getDefaultState(), 20);
//						}
//
//						if (worldIn.setBlockState(blockpos, blockstate, flags)) {
//							i = Math.min(i, blockpos.getX());
//							j = Math.min(j, blockpos.getY());
//							k = Math.min(k, blockpos.getZ());
//							l = Math.max(l, blockpos.getX());
//							i1 = Math.max(i1, blockpos.getY());
//							j1 = Math.max(j1, blockpos.getZ());
//							list2.add(Pair.of(blockpos, template$blockinfo.nbt));
//							if (template$blockinfo.nbt != null) {
//								TileEntity tileentity1 = worldIn.getTileEntity(blockpos);
//								if (tileentity1 != null) {
//									template$blockinfo.nbt.putInt("x", blockpos.getX());
//									template$blockinfo.nbt.putInt("y", blockpos.getY());
//									template$blockinfo.nbt.putInt("z", blockpos.getZ());
//									tileentity1.read(template$blockinfo.nbt);
//									tileentity1.mirror(placementIn.getMirror());
//									tileentity1.rotate(placementIn.getRotation());
//								}
//							}
//
//							if (ifluidstate != null && blockstate.getBlock() instanceof ILiquidContainer) {
//								((ILiquidContainer) blockstate.getBlock()).receiveFluid(worldIn, blockpos, blockstate,
//										ifluidstate);
//								if (!ifluidstate.isSource()) {
//									list1.add(blockpos);
//								}
//							}
//						}
//					}
//				}
//
//				boolean flag = true;
//				Direction[] adirection = new Direction[] { Direction.UP, Direction.NORTH, Direction.EAST,
//						Direction.SOUTH, Direction.WEST };
//
//				while (flag && !list1.isEmpty()) {
//					flag = false;
//					Iterator<BlockPos> iterator = list1.iterator();
//
//					while (iterator.hasNext()) {
//						BlockPos blockpos2 = iterator.next();
//						BlockPos blockpos3 = blockpos2;
//						IFluidState ifluidstate2 = worldIn.getFluidState(blockpos2);
//
//						for (int k1 = 0; k1 < adirection.length && !ifluidstate2.isSource(); ++k1) {
//							BlockPos blockpos1 = blockpos3.offset(adirection[k1]);
//							IFluidState ifluidstate1 = worldIn.getFluidState(blockpos1);
//							if (ifluidstate1.func_215679_a(worldIn, blockpos1) > ifluidstate2.func_215679_a(worldIn,
//									blockpos3) || ifluidstate1.isSource() && !ifluidstate2.isSource()) {
//								ifluidstate2 = ifluidstate1;
//								blockpos3 = blockpos1;
//							}
//						}
//
//						if (ifluidstate2.isSource()) {
//							BlockState blockstate2 = worldIn.getBlockState(blockpos2);
//							Block block = blockstate2.getBlock();
//							if (block instanceof ILiquidContainer) {
//								((ILiquidContainer) block).receiveFluid(worldIn, blockpos2, blockstate2, ifluidstate2);
//								flag = true;
//								iterator.remove();
//							}
//						}
//					}
//				}
//
//				if (i <= l) {
//					if (!placementIn.func_215218_i()) {
//						VoxelShapePart voxelshapepart = new BitSetVoxelShapePart(l - i + 1, i1 - j + 1, j1 - k + 1);
//						int l1 = i;
//						int i2 = j;
//						int j2 = k;
//
//						for (Pair<BlockPos, CompoundNBT> pair1 : list2) {
//							BlockPos blockpos5 = pair1.getFirst();
//							voxelshapepart.setFilled(blockpos5.getX() - l1, blockpos5.getY() - i2,
//									blockpos5.getZ() - j2, true, true);
//						}
//
//						func_222857_a(worldIn, flags, voxelshapepart, l1, i2, j2);
//					}
//
//					for (Pair<BlockPos, CompoundNBT> pair : list2) {
//						BlockPos blockpos4 = pair.getFirst();
//						if (!placementIn.func_215218_i()) {
//							BlockState blockstate1 = worldIn.getBlockState(blockpos4);
//							BlockState blockstate3 = Block.getValidBlockForPosition(blockstate1, worldIn, blockpos4);
//							if (blockstate1 != blockstate3) {
//								worldIn.setBlockState(blockpos4, blockstate3, flags & -2 | 16);
//							}
//
//							worldIn.notifyNeighbors(blockpos4, blockstate3.getBlock());
//						}
//
//						if (pair.getSecond() != null) {
//							TileEntity tileentity2 = worldIn.getTileEntity(blockpos4);
//							if (tileentity2 != null) {
//								tileentity2.markDirty();
//							}
//						}
//					}
//				}
//
//				if (!placementIn.getIgnoreEntities()) {
//					this.func_207668_a(worldIn, pos, placementIn.getMirror(), placementIn.getRotation(),
//							placementIn.func_207664_d(), mutableboundingbox);
//				}
//
//				return true;
//			} else {
//				return false;
//			}
//		}
//	}
//
//	private static BlockPos transformedBlockPos(BlockPos pos, Mirror mirrorIn, Rotation rotationIn) {
//		int i = pos.getX();
//		int j = pos.getY();
//		int k = pos.getZ();
//		boolean flag = true;
//
//		switch (mirrorIn) {
//		case LEFT_RIGHT:
//			k = -k;
//			break;
//		case FRONT_BACK:
//			i = -i;
//			break;
//		default:
//			flag = false;
//		}
//
//		switch (rotationIn) {
//		case COUNTERCLOCKWISE_90:
//			return new BlockPos(k, j, -i);
//		case CLOCKWISE_90:
//			return new BlockPos(-k, j, i);
//		case CLOCKWISE_180:
//			return new BlockPos(-i, j, -k);
//		default:
//			return flag ? new BlockPos(i, j, k) : pos;
//		}
//	}
//
//	public static BlockPos transformedBlockPos(PlacementSettings placementIn, BlockPos pos) {
//		return transformedBlockPos(pos, placementIn.getMirror(), placementIn.getRotation());
//	}
//
//}
