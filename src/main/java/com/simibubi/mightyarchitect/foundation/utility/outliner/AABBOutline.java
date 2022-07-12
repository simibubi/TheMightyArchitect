package com.simibubi.mightyarchitect.foundation.utility.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.mightyarchitect.foundation.RenderTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AABBOutline extends Outline {

	protected AABB bb;

	public AABBOutline(AABB bb) {
		this.setBounds(bb);
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffer) {
		renderBB(ms, buffer, bb);
	}

	public void renderBB(PoseStack ms, MultiBufferSource buffer, AABB bb) {
		Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();
		boolean noCull = bb.contains(projectedView);
		bb = bb.inflate(noCull ? -1 / 128d : 1 / 128d);
		noCull |= params.disableCull;

		Vec3 xyz = new Vec3(bb.minX, bb.minY, bb.minZ);
		Vec3 Xyz = new Vec3(bb.maxX, bb.minY, bb.minZ);
		Vec3 xYz = new Vec3(bb.minX, bb.maxY, bb.minZ);
		Vec3 XYz = new Vec3(bb.maxX, bb.maxY, bb.minZ);
		Vec3 xyZ = new Vec3(bb.minX, bb.minY, bb.maxZ);
		Vec3 XyZ = new Vec3(bb.maxX, bb.minY, bb.maxZ);
		Vec3 xYZ = new Vec3(bb.minX, bb.maxY, bb.maxZ);
		Vec3 XYZ = new Vec3(bb.maxX, bb.maxY, bb.maxZ);

		Vec3 start = xyz;
		renderAACuboidLine(ms, buffer, start, Xyz);
		renderAACuboidLine(ms, buffer, start, xYz);
		renderAACuboidLine(ms, buffer, start, xyZ);

		start = XyZ;
		renderAACuboidLine(ms, buffer, start, xyZ);
		renderAACuboidLine(ms, buffer, start, XYZ);
		renderAACuboidLine(ms, buffer, start, Xyz);

		start = XYz;
		renderAACuboidLine(ms, buffer, start, xYz);
		renderAACuboidLine(ms, buffer, start, Xyz);
		renderAACuboidLine(ms, buffer, start, XYZ);

		start = xYZ;
		renderAACuboidLine(ms, buffer, start, XYZ);
		renderAACuboidLine(ms, buffer, start, xyZ);
		renderAACuboidLine(ms, buffer, start, xYz);

		renderFace(ms, buffer, Direction.NORTH, xYz, XYz, Xyz, xyz, noCull);
		renderFace(ms, buffer, Direction.SOUTH, XYZ, xYZ, xyZ, XyZ, noCull);
		renderFace(ms, buffer, Direction.EAST, XYz, XYZ, XyZ, Xyz, noCull);
		renderFace(ms, buffer, Direction.WEST, xYZ, xYz, xyz, xyZ, noCull);
		renderFace(ms, buffer, Direction.UP, xYZ, XYZ, XYz, xYz, noCull);
		renderFace(ms, buffer, Direction.DOWN, xyz, Xyz, XyZ, xyZ, noCull);

	}

	protected void renderFace(PoseStack ms, MultiBufferSource buffer, Direction direction, Vec3 p1, Vec3 p2, Vec3 p3,
		Vec3 p4, boolean noCull) {
		if (!params.faceTexture.isPresent())
			return;
		if (params.isFaceHidden(direction))
			return;

		ResourceLocation faceTexture = params.faceTexture.get()
			.getLocation();
		float alphaBefore = params.alpha;
		params.alpha =
			(direction == params.getHighlightedFace() && params.hightlightedFaceTexture.isPresent()) ? 1 : alphaBefore;

		// LogManager.getLogger().info(faceTexture.getPath() + " " + params.alpha + " "
		// + alphaBefore);

		VertexConsumer builder = buffer.getBuffer(RenderTypes.getOutlineTranslucent(faceTexture, !noCull));

		Axis axis = direction.getAxis();
		Vec3 uDiff = p2.subtract(p1);
		Vec3 vDiff = p4.subtract(p1);
		float maxU = (float) Math.abs(axis == Axis.X ? uDiff.z : uDiff.x);
		float maxV = (float) Math.abs(axis == Axis.Y ? vDiff.z : vDiff.y);

		putQuadUV(ms, builder, p1, p2, p3, p4, 0, 0, maxU, maxV, Direction.UP, true);

		params.alpha = alphaBefore;
	}

	public void setBounds(AABB bb) {
		this.bb = bb;
	}

}
