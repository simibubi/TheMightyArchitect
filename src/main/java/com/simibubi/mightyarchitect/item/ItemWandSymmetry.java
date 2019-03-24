package com.simibubi.mightyarchitect.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.mightyarchitect.gui.GuiOpener;
import com.simibubi.mightyarchitect.gui.GuiWandSymmetry;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryCrossPlane;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryElement;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryEmptySlot;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryPlane;
import com.simibubi.mightyarchitect.networking.PacketSender;
import com.simibubi.mightyarchitect.networking.PacketSymmetryEffect;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemWandSymmetry extends ItemForMightyArchitects {

	public static final String $SYMMETRY = "symmetry";
	private static final String $ENABLE = "enable";

	public ItemWandSymmetry(String name) {
		super(name);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		if (!worldIn.isRemote && hand == EnumHand.MAIN_HAND) {
			ItemStack wand = player.getHeldItem(hand);
			checkNBT(wand);
			NBTTagCompound compound = wand.getTagCompound().getCompoundTag($SYMMETRY);
			pos = pos.offset(facing);
			SymmetryElement previousElement = SymmetryElement.fromNBT(compound);

			if (player.isSneaking()) {
				if (!(previousElement instanceof SymmetryEmptySlot))
					wand.getTagCompound().setBoolean($ENABLE, !isEnabled(wand));
				return EnumActionResult.SUCCESS;
			}

			wand.getTagCompound().setBoolean($ENABLE, true);
			Vec3d pos3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
			SymmetryElement newElement = new SymmetryPlane(pos3d);

			if (previousElement instanceof SymmetryEmptySlot) {
				newElement.setOrientation((player.getHorizontalFacing() == EnumFacing.NORTH
						|| player.getHorizontalFacing() == EnumFacing.SOUTH) ? SymmetryPlane.Align.XY.ordinal()
								: SymmetryPlane.Align.YZ.ordinal());
				newElement.enable = true;
				player.sendStatusMessage(new TextComponentString("New Plane created"), true);
				wand.getTagCompound().setBoolean($ENABLE, true);

			} else {
				previousElement.setPosition(pos3d);

				if (previousElement instanceof SymmetryPlane) {
					previousElement.setOrientation((player.getHorizontalFacing() == EnumFacing.NORTH
							|| player.getHorizontalFacing() == EnumFacing.SOUTH) ? SymmetryPlane.Align.XY.ordinal()
									: SymmetryPlane.Align.YZ.ordinal());
				}

				if (previousElement instanceof SymmetryCrossPlane) {
					float rotation = player.getRotationYawHead();
					float abs = Math.abs(rotation % 90);
					boolean diagonal = abs > 22 && abs < 45 + 22;
					previousElement.setOrientation(
							diagonal ? SymmetryCrossPlane.Align.D.ordinal() : SymmetryCrossPlane.Align.Y.ordinal());
				}

				newElement = previousElement;
			}

			compound = newElement.writeToNbt();
			wand.getTagCompound().setTag($SYMMETRY, compound);

			player.setHeldItem(hand, wand);
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		GuiOpener.open(new GuiWandSymmetry(playerIn.getHeldItem(handIn)));
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public int getItemStackLimit() {
		return 1;
	}

	private static void checkNBT(ItemStack wand) {
		if (!wand.hasTagCompound() || !wand.getTagCompound().hasKey($SYMMETRY)) {
			wand.setTagCompound(new NBTTagCompound());
			wand.getTagCompound().setTag($SYMMETRY, new SymmetryEmptySlot(new Vec3d(0, 0, 0)).writeToNbt());
			wand.getTagCompound().setBoolean($ENABLE, false);
		}
	}

	public static boolean isEnabled(ItemStack stack) {
		checkNBT(stack);
		return stack.getTagCompound().getBoolean($ENABLE);
	}

	public static SymmetryElement getMirror(ItemStack stack) {
		checkNBT(stack);
		return SymmetryElement.fromNBT((NBTTagCompound) stack.getTagCompound().getCompoundTag($SYMMETRY));
	}

	public static void apply(World world, ItemStack wand, EntityPlayer player, BlockPos pos, IBlockState block) {
		checkNBT(wand);
		if (!isEnabled(wand))
			return;

		Map<BlockPos, IBlockState> blockSet = new HashMap<>();
		blockSet.put(pos, block);
		SymmetryElement symmetry = SymmetryElement
				.fromNBT((NBTTagCompound) wand.getTagCompound().getCompoundTag($SYMMETRY));

		Vec3d mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(new Vec3d(pos)) > 50)
			return;

		symmetry.process(blockSet);
		
		BlockPos to = new BlockPos(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();

		targets.add(pos);
		for (BlockPos position : blockSet.keySet()) {
			if (world.mayPlace(block.getBlock(), position, true, EnumFacing.DOWN, player)) {
				world.setBlockState(position, blockSet.get(position));
				targets.add(position);				
			}
		}
		
		PacketSender.INSTANCE.sendToAll(new PacketSymmetryEffect(to, targets));

	}

	public static void remove(World world, ItemStack wand, EntityPlayer player, BlockPos pos) {
		IBlockState air = Blocks.AIR.getDefaultState();
		checkNBT(wand);
		if (!isEnabled(wand))
			return;

		Map<BlockPos, IBlockState> blockSet = new HashMap<>();
		blockSet.put(pos, air);
		SymmetryElement symmetry = SymmetryElement
				.fromNBT((NBTTagCompound) wand.getTagCompound().getCompoundTag($SYMMETRY));

		Vec3d mirrorPos = symmetry.getPosition();
		if (mirrorPos.distanceTo(new Vec3d(pos)) > 50)
			return;

		symmetry.process(blockSet);

		BlockPos to = new BlockPos(mirrorPos);
		List<BlockPos> targets = new ArrayList<>();
		
		targets.add(pos);
		for (BlockPos position : blockSet.keySet()) {
			targets.add(position);
			world.setBlockToAir(position);
		}
		
		PacketSender.INSTANCE.sendToAll(new PacketSymmetryEffect(to, targets));
	}
	
}
