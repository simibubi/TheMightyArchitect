package com.simibubi.mightyarchitect.foundation.utility.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LineOutline extends Outline {

	protected Vec3 start = Vec3.ZERO;
	protected Vec3 end = Vec3.ZERO;

	public LineOutline set(Vec3 start, Vec3 end) {
		this.start = start;
		this.end = end;
		return this;
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffer) {
		renderCuboidLine(ms, buffer, start, end);
	}

	public static class ChasingLineOutline extends LineOutline {

		protected Vec3 prevStart = Vec3.ZERO;
		protected Vec3 prevEnd = Vec3.ZERO;
		protected Vec3 targetStart = Vec3.ZERO;
		protected Vec3 targetEnd = Vec3.ZERO;

		public ChasingLineOutline target(Vec3 start, Vec3 end) {
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
		public LineOutline set(Vec3 start, Vec3 end) {
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
		public void render(PoseStack ms, MultiBufferSource buffer) {
			float pt = Minecraft.getInstance()
				.getFrameTime();
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
		public LineOutline set(Vec3 start, Vec3 end) {
			if (!end.equals(this.end))
				super.set(start, end);
			return this;
		}

		@Override
		public void render(PoseStack ms, MultiBufferSource buffer) {
			float pt = Minecraft.getInstance()
				.getFrameTime();
			float distanceToTarget = 1 - Mth.lerp(pt, prevProgress, progress);
			Vec3 start = end.add(this.start.subtract(end)
				.scale(distanceToTarget));
			renderCuboidLine(ms, buffer, start, end);
		}

	}

}
