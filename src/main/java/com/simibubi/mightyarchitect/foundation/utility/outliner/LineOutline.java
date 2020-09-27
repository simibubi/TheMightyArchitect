package com.simibubi.mightyarchitect.foundation.utility.outliner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.mightyarchitect.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class LineOutline extends Outline {

	protected Vec3d start = Vec3d.ZERO;
	protected Vec3d end = Vec3d.ZERO;

	public LineOutline set(Vec3d start, Vec3d end) {
		this.start = start;
		this.end = end;
		return this;
	}

	@Override
	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		renderCuboidLine(ms, buffer, start, end);
	}

	public static class ChasingLineOutline extends LineOutline {

		protected Vec3d prevStart = Vec3d.ZERO;
		protected Vec3d prevEnd = Vec3d.ZERO;
		protected Vec3d targetStart = Vec3d.ZERO;
		protected Vec3d targetEnd = Vec3d.ZERO;

		public ChasingLineOutline target(Vec3d start, Vec3d end) {
			if (end.distanceTo(targetStart) + start.distanceTo(targetEnd) < end.distanceTo(targetEnd)
				+ start.distanceTo(targetStart)) {
				this.targetEnd = start;
				this.targetStart = end;
				return this;
			}
			this.targetStart = start;
			this.targetEnd = end;
			return this;
		}

		@Override
		public LineOutline set(Vec3d start, Vec3d end) {
			prevEnd = end;
			prevStart = start;
			return super.set(start, end);
		}
		
		@Override
		public void tick() {
			prevStart = start;
			prevEnd = end;
			start = VecHelper.lerp(start, targetStart, .5f);
			end = VecHelper.lerp(end, targetEnd, .5f);
		}

		@Override
		public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
			float pt = Minecraft.getInstance()
				.getRenderPartialTicks();
			renderCuboidLine(ms, buffer, VecHelper.lerp(prevStart, start, pt), VecHelper.lerp(prevEnd, end, pt));
		}

	}

	public static class EndChasingLineOutline extends LineOutline {

		float prevProgress = 0;
		float progress = 0;

		@Override
		public void tick() {}

		public EndChasingLineOutline setProgress(float progress) {
			prevProgress = this.progress;
			this.progress = progress;
			return this;
		}

		@Override
		public LineOutline set(Vec3d start, Vec3d end) {
			if (!end.equals(this.end))
				super.set(start, end);
			return this;
		}

		@Override
		public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
			float pt = Minecraft.getInstance()
				.getRenderPartialTicks();
			float distanceToTarget = 1 - MathHelper.lerp(pt, prevProgress, progress);
			Vec3d start = end.add(this.start.subtract(end)
				.scale(distanceToTarget));
			renderCuboidLine(ms, buffer, start, end);
		}

	}

}
