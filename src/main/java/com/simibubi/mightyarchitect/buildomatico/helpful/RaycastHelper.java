package com.simibubi.mightyarchitect.buildomatico.helpful;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RaycastHelper {

	public static RayTraceResult rayTraceRange(World worldIn, EntityPlayer playerIn, double range) {
		Vec3d origin = getTraceOrigin(playerIn);
		Vec3d target = getTraceTarget(playerIn, range, origin);
		return worldIn.rayTraceBlocks(origin, target, false, true, false);
	}

	public static PredicateTraceResult rayTraceUntil(EntityPlayer playerIn, double range, Predicate<BlockPos> predicate) {
		Vec3d origin = getTraceOrigin(playerIn);
		Vec3d target = getTraceTarget(playerIn, range, origin);
		return rayTraceUntil(origin, target, predicate);
	}

	private static Vec3d getTraceTarget(EntityPlayer playerIn, double range, Vec3d origin) {
		float f = playerIn.rotationPitch;
		float f1 = playerIn.rotationYaw;
		float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-f * 0.017453292F);
		float f5 = MathHelper.sin(-f * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d3 = range;
		Vec3d vec3d1 = origin.addVector((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);
		return vec3d1;
	}

	private static Vec3d getTraceOrigin(EntityPlayer playerIn) {
		double d0 = playerIn.posX;
		double d1 = playerIn.posY + (double) playerIn.getEyeHeight();
		double d2 = playerIn.posZ;
		Vec3d vec3d = new Vec3d(d0, d1, d2);
		return vec3d;
	}

	private static PredicateTraceResult rayTraceUntil(Vec3d start, Vec3d end, Predicate<BlockPos> predicate) {
		if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z))
			return null;
		if (Double.isNaN(end.x) || Double.isNaN(end.y) || Double.isNaN(end.z))
			return null;

		int dx = MathHelper.floor(end.x);
		int dy = MathHelper.floor(end.y);
		int dz = MathHelper.floor(end.z);
		int x = MathHelper.floor(start.x);
		int y = MathHelper.floor(start.y);
		int z = MathHelper.floor(start.z);

		BlockPos currentPos = new BlockPos(x, y, z);

		if (predicate.test(currentPos))
			return new PredicateTraceResult(currentPos, EnumFacing.getFacingFromVector(dx - x, dy - y, dz - z));

		int remainingDistance = 200;

		while (remainingDistance-- >= 0) {
			if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
				return null;
			}

			if (x == dx && y == dy && z == dz) {
				return new PredicateTraceResult();
			}

			boolean flag2 = true;
			boolean flag = true;
			boolean flag1 = true;
			double d0 = 999.0D;
			double d1 = 999.0D;
			double d2 = 999.0D;

			if (dx > x) {
				d0 = (double) x + 1.0D;
			} else if (dx < x) {
				d0 = (double) x + 0.0D;
			} else {
				flag2 = false;
			}

			if (dy > y) {
				d1 = (double) y + 1.0D;
			} else if (dy < y) {
				d1 = (double) y + 0.0D;
			} else {
				flag = false;
			}

			if (dz > z) {
				d2 = (double) z + 1.0D;
			} else if (dz < z) {
				d2 = (double) z + 0.0D;
			} else {
				flag1 = false;
			}

			double d3 = 999.0D;
			double d4 = 999.0D;
			double d5 = 999.0D;
			double d6 = end.x - start.x;
			double d7 = end.y - start.y;
			double d8 = end.z - start.z;

			if (flag2) {
				d3 = (d0 - start.x) / d6;
			}

			if (flag) {
				d4 = (d1 - start.y) / d7;
			}

			if (flag1) {
				d5 = (d2 - start.z) / d8;
			}

			if (d3 == -0.0D) {
				d3 = -1.0E-4D;
			}

			if (d4 == -0.0D) {
				d4 = -1.0E-4D;
			}

			if (d5 == -0.0D) {
				d5 = -1.0E-4D;
			}

			EnumFacing enumfacing;

			if (d3 < d4 && d3 < d5) {
				enumfacing = dx > x ? EnumFacing.WEST : EnumFacing.EAST;
				start = new Vec3d(d0, start.y + d7 * d3, start.z + d8 * d3);
			} else if (d4 < d5) {
				enumfacing = dy > y ? EnumFacing.DOWN : EnumFacing.UP;
				start = new Vec3d(start.x + d6 * d4, d1, start.z + d8 * d4);
			} else {
				enumfacing = dz > z ? EnumFacing.NORTH : EnumFacing.SOUTH;
				start = new Vec3d(start.x + d6 * d5, start.y + d7 * d5, d2);
			}

			x = MathHelper.floor(start.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
			y = MathHelper.floor(start.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
			z = MathHelper.floor(start.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
			currentPos = new BlockPos(x, y, z);

			if (predicate.test(currentPos))
				return new PredicateTraceResult(currentPos, enumfacing);
		}

		return new PredicateTraceResult();
	}

	public static class PredicateTraceResult {
		private BlockPos pos;
		private EnumFacing facing;

		public PredicateTraceResult(BlockPos pos, EnumFacing facing) {
			this.pos = pos;
			this.facing = facing;
		}
		
		public PredicateTraceResult() {
			// missed, no result
		}

		public EnumFacing getFacing() {
			return facing;
		}

		public BlockPos getPos() {
			return pos;
		}
		
		public boolean missed() {
			return this.pos == null;
		}
	}

}
