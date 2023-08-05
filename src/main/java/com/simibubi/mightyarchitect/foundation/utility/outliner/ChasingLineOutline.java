package com.simibubi.mightyarchitect.foundation.utility.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector4f;

import net.minecraft.world.phys.Vec3;

public class ChasingLineOutline extends LineOutline {

	protected final Vector3d startPrevious = new Vector3d(0, 0, 0);
	protected final Vector3d endPrevious = new Vector3d(0, 0, 0);

	protected final Vector3d startLerped = new Vector3d(0, 0, 0);
	protected final Vector3d endLerped = new Vector3d(0, 0, 0);

	protected final Vector3d startTarget = new Vector3d(0, 0, 0);
	protected final Vector3d endTarget = new Vector3d(0, 0, 0);

	public ChasingLineOutline target(Vec3 start, Vec3 end) {
		if (end.distanceToSqr(startTarget.x, startTarget.y, startTarget.z) + start.distanceToSqr(endTarget.x,
			endTarget.y, endTarget.z) < start.distanceToSqr(startTarget.x, startTarget.y, startTarget.z)
				+ end.distanceToSqr(endTarget.x, endTarget.y, endTarget.z)) {
			this.endTarget.set(start.x, start.y, start.z);
			this.startTarget.set(end.x, end.y, end.z);
			return this;
		}
		this.startTarget.set(start.x, start.y, start.z);
		this.endTarget.set(end.x, end.y, end.z);
		return this;
	}

	@Override
	public LineOutline set(Vec3 start, Vec3 end) {
		this.startTarget.set(start.x, start.y, start.z);
		this.endTarget.set(end.x, end.y, end.z);
		this.startPrevious.set(start.x, start.y, start.z);
		this.endPrevious.set(end.x, end.y, end.z);
		return super.set(start, end);
	}

	@Override
	public void tick() {
		startPrevious.set(start);
		endPrevious.set(end);
		setAsLerp(start, startTarget, 0.5f, start);
		setAsLerp(end, endTarget, 0.5f, end);
	}

	protected void renderInner(PoseStack ms, VertexConsumer consumer, Vec3 camera, float pt, float width,
		Vector4f color, int lightmap, boolean disableNormals) {
		bufferCuboidLine(ms, consumer, camera, setAsLerp(startPrevious, start, pt, startLerped),
			setAsLerp(endPrevious, end, pt, endLerped), width, color, lightmap, disableNormals);
	}

	protected Vector3d setAsLerp(Vector3d from, Vector3d to, float delta, Vector3d write) {
		double x = (to.x - from.x) * delta + from.x;
		double y = (to.y - from.y) * delta + from.y;
		double z = (to.z - from.z) * delta + from.z;
		write.set(x, y, z);
		return write;
	}

}