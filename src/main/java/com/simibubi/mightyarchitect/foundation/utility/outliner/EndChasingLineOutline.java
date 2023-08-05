package com.simibubi.mightyarchitect.foundation.utility.outliner;

import org.joml.Vector3d;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EndChasingLineOutline extends LineOutline {
	protected float progress = 0;
	protected float prevProgress = 0;
	protected boolean lockStart;

	protected final Vector3d startTemp = new Vector3d(0, 0, 0);

	public EndChasingLineOutline(boolean lockStart) {
		this.lockStart = lockStart;
	}

	public EndChasingLineOutline setProgress(float progress) {
		prevProgress = this.progress;
		this.progress = progress;
		return this;
	}

	@Override
	protected void renderInner(PoseStack ms, VertexConsumer consumer, Vec3 camera, float pt, float width,
		Vector4f color, int lightmap, boolean disableNormals) {
		float distanceToTarget = Mth.lerp(pt, prevProgress, progress);

		Vector3d end;
		if (lockStart) {
			end = this.start;
		} else {
			end = this.end;
			distanceToTarget = 1 - distanceToTarget;
		}

		Vector3d start = this.startTemp;
		double x = (this.start.x - end.x) * distanceToTarget + end.x;
		double y = (this.start.y - end.y) * distanceToTarget + end.y;
		double z = (this.start.z - end.z) * distanceToTarget + end.z;
		start.set((float) x, (float) y, (float) z);
		bufferCuboidLine(ms, consumer, camera, start, end, width, color, lightmap, disableNormals);
	}
}