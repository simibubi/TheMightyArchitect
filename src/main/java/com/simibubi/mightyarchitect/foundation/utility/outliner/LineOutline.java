package com.simibubi.mightyarchitect.foundation.utility.outliner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.mightyarchitect.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class LineOutline extends Outline {

	protected Vector3d start = Vector3d.ZERO;
	protected Vector3d end = Vector3d.ZERO;

	public LineOutline set(Vector3d start, Vector3d end) {
		this.start = start;
		this.end = end;
		return this;
	}

	@Override
	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		renderCuboidLine(ms, buffer, start, end);
	}

	public static class ChasingLineOutline extends LineOutline {

		protected Vector3d prevStart = Vector3d.ZERO;
		protected Vector3d prevEnd = Vector3d.ZERO;
		protected Vector3d targetStart = Vector3d.ZERO;
		protected Vector3d targetEnd = Vector3d.ZERO;

		public ChasingLineOutline target(Vector3d start, Vector3d end) {
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
		public LineOutline set(Vector3d start, Vector3d end) {
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
		public LineOutline set(Vector3d start, Vector3d end) {
			if (!end.equals(this.end))
				super.set(start, end);
			return this;
		}

		@Override
		public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
			float pt = Minecraft.getInstance()
				.getRenderPartialTicks();
			float distanceToTarget = 1 - MathHelper.lerp(pt, prevProgress, progress);
			Vector3d start = end.add(this.start.subtract(end)
				.scale(distanceToTarget));
			renderCuboidLine(ms, buffer, start, end);
		}

	}

}
