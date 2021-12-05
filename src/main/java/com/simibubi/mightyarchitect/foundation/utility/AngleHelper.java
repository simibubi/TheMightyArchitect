package com.simibubi.mightyarchitect.foundation.utility;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import com.mojang.math.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AngleHelper {
	
	@OnlyIn(Dist.CLIENT)
	public static void applyRotation(Direction direction, PoseStack ms) {
		ms.mulPose(Vector3f.YP.rotationDegrees(AngleHelper.horizontalAngle(direction)));
		ms.mulPose(Vector3f.XP.rotationDegrees(AngleHelper.verticalAngle(direction)));
	}

	public static float horizontalAngle(Direction facing) {
		float angle = facing.toYRot();
		if (facing.getAxis() == Axis.X)
			angle = -angle;
		return angle;
	}

	public static float verticalAngle(Direction facing) {
		return facing == Direction.UP ? -90 : facing == Direction.DOWN ? 90 : 0;
	}

	public static float rad(double angle) {
		return (float) (angle / 180 * Math.PI);
	}

	public static float deg(double angle) {
		return (float) (angle * 180 / Math.PI);
	}

	public static float angleLerp(float pct, float current, float target) {
		return current + getShortestAngleDiff(current, target) * pct;
	}

	public static float getShortestAngleDiff(double current, double target) {
		current = current % 360;
		target = target % 360;
		return (float) (((((target - current) % 360) + 540) % 360) - 180);
	}

}
