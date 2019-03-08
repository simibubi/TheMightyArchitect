package com.simibubi.mightyarchitect.item;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.gui.Guis;
import com.simibubi.mightyarchitect.symmetry.SymmetryElement;
import com.simibubi.mightyarchitect.symmetry.SymmetryEmptySlot;
import com.simibubi.mightyarchitect.symmetry.SymmetryPlane;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemWandSymmetry extends ItemForMightyArchitects {

	public static final String $SYMMETRY_ELEMS = "elements";
	private static final String $ENABLE = "enable";
	
	public ItemWandSymmetry(String name) {
		super(name);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		if (!worldIn.isRemote) {
			ItemStack wand = player.getHeldItem(hand);
			checkNBT(wand);
			if (player.isSneaking()) {
				NBTTagList elements = wand.getTagCompound().getTagList($SYMMETRY_ELEMS, 10);
				
				// NEW ELEMENT
				for (int i = 0; i < elements.tagCount(); i++) {
					if (SymmetryElement.fromNBT((NBTTagCompound) elements.get(i)) instanceof SymmetryEmptySlot) {
						pos = pos.offset(facing);
						SymmetryElement symmetryPlane = new SymmetryPlane(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
						symmetryPlane.setOrientation((player.getHorizontalFacing() == EnumFacing.NORTH || player.getHorizontalFacing() == EnumFacing.SOUTH)? SymmetryPlane.Align.XY.ordinal() : SymmetryPlane.Align.YZ.ordinal());
						symmetryPlane.enable = true;
						elements.set(i, symmetryPlane.writeToNbt());
						player.sendStatusMessage(new TextComponentString("New Plane created"), true);
						wand.getTagCompound().setBoolean($ENABLE, true);
						player.setHeldItem(hand, wand);
						return EnumActionResult.SUCCESS;
					}
				}
				
				player.sendStatusMessage(new TextComponentString("No Space left! Delete a Mirror first"), true);
				return EnumActionResult.FAIL;
				
			} else {
				
				
			}
		} else {
			if (!player.isSneaking()) {
				player.openGui(TheMightyArchitect.instance, Guis.SYMMETRY_WAND.id, worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public int getItemStackLimit() {
		return 1;
	}
	
	private static void checkNBT(ItemStack wand) {
		if (!wand.hasTagCompound() || !wand.getTagCompound().hasKey($SYMMETRY_ELEMS)) {
			wand.setTagCompound(new NBTTagCompound());
			NBTTagList elements = new NBTTagList();
			elements.appendTag(new SymmetryEmptySlot(new Vec3d(0, 0, 0)).writeToNbt());
			elements.appendTag(new SymmetryEmptySlot(new Vec3d(0, 0, 0)).writeToNbt());
			elements.appendTag(new SymmetryEmptySlot(new Vec3d(0, 0, 0)).writeToNbt());
			wand.getTagCompound().setTag($SYMMETRY_ELEMS, elements);
			wand.getTagCompound().setBoolean($ENABLE, false);
		}
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		return isEnabled(stack);
	}
	
	public static boolean isEnabled(ItemStack stack) {
		checkNBT(stack);
		return stack.getTagCompound().getBoolean($ENABLE);
	}
	
	public static List<SymmetryElement> getMirrors(ItemStack stack) {
		List<SymmetryElement> result = new LinkedList<>();
		checkNBT(stack);
		NBTTagList elements = stack.getTagCompound().getTagList($SYMMETRY_ELEMS, 10);
		for (int i = 0; i < elements.tagCount(); i++) {
			result.add(SymmetryElement.fromNBT((NBTTagCompound) elements.get(i)));
		}
		return result;
	}
	
	public static void apply(World world, ItemStack wand, EntityPlayer player, BlockPos pos, IBlockState block) {
		Map<BlockPos, IBlockState> blockSet = new HashMap<>();
		blockSet.put(pos, block);
		checkNBT(wand);
		
		NBTTagList elements = wand.getTagCompound().getTagList($SYMMETRY_ELEMS, 10);
		
		for (int i = 0; i < elements.tagCount(); i++) {
			SymmetryElement.fromNBT((NBTTagCompound) elements.get(i)).process(blockSet);
		}
		
		for (BlockPos position : blockSet.keySet()) {
			if (world.mayPlace(block.getBlock(), position, true, EnumFacing.DOWN, player))
				world.setBlockState(position, blockSet.get(position));
		}
		
	}

	public static void remove(World world, ItemStack wand, EntityPlayer player, BlockPos pos) {
		Map<BlockPos, IBlockState> blockSet = new HashMap<>();
		IBlockState air = Blocks.AIR.getDefaultState();
		blockSet.put(pos, air);
		checkNBT(wand);
		
		NBTTagList elements = wand.getTagCompound().getTagList($SYMMETRY_ELEMS, 10);
		
		for (int i = 0; i < elements.tagCount(); i++) {
			SymmetryElement.fromNBT((NBTTagCompound) elements.get(i)).process(blockSet);
		}
		
		for (BlockPos position : blockSet.keySet()) {
			world.setBlockToAir(position);
		}
		
	}
}
