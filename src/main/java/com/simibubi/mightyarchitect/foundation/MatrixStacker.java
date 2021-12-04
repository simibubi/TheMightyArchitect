package com.simibubi.mightyarchitect.foundation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.mightyarchitect.foundation.utility.AngleHelper;
import com.simibubi.mightyarchitect.foundation.utility.VecHelper;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

public class MatrixStacker {

	static Vector3d center = VecHelper.getCenterOf(BlockPos.ZERO);
	static MatrixStacker instance;

	MatrixStack ms;

	public static MatrixStacker of(MatrixStack ms) {
		if (instance == null)
			instance = new MatrixStacker();
		instance.ms = ms;
		return instance;
	}

	public MatrixStacker rotateX(double angle) {
		return multiply(Vector3f.XP, angle);
	}

	public MatrixStacker rotateY(double angle) {
		return multiply(Vector3f.YP, angle);
	}

	public MatrixStacker rotateZ(double angle) {
		return multiply(Vector3f.ZP, angle);
	}

	public MatrixStacker rotateRadians(double angleRoll, double angleYaw, double anglePitch) {
		rotateX(AngleHelper.deg(angleRoll));
		rotateY(AngleHelper.deg(angleYaw));
		rotateZ(AngleHelper.deg(anglePitch));
		return this;
	}

	public MatrixStacker centre() {
		return translate(center);
	}

	public MatrixStacker unCentre() {
		return translateBack(center);
	}

	public MatrixStacker translate(Vector3i vec) {
		ms.translate(vec.getX(), vec.getY(), vec.getZ());
		return this;
	}

	public MatrixStacker translate(Vector3d vec) {
		ms.translate(vec.x, vec.y, vec.z);
		return this;
	}

	public MatrixStacker translateBack(Vector3d vec) {
		ms.translate(-vec.x, -vec.y, -vec.z);
		return this;
	}

	public MatrixStacker nudge(int id) {
		long randomBits = (long) id * 493286711L;
		randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
		float xNudge = (((float) (randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float yNudge = (((float) (randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float zNudge = (((float) (randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		ms.translate(xNudge, yNudge, zNudge);
		return this;
	}

	private MatrixStacker multiply(Vector3f axis, double angle) {
		ms.mulPose(axis.rotationDegrees((float) angle));
		return this;
	}

}
