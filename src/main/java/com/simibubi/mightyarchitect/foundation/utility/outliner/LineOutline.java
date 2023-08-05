package com.simibubi.mightyarchitect.foundation.utility.outliner;

import org.joml.Vector3d;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.mightyarchitect.foundation.RenderTypes;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

public class LineOutline extends Outline {

	protected final Vector3d start = new Vector3d(0, 0, 0);
	protected final Vector3d end = new Vector3d(0, 0, 0);

	public LineOutline set(Vector3d start, Vector3d end) {
		this.start.set(start.x, start.y, start.z);
		this.end.set(end.x, end.y, end.z);
		return this;
	}

	public LineOutline set(Vec3 start, Vec3 end) {
		this.start.set(start.x, start.y, start.z);
		this.end.set(end.x, end.y, end.z);
		return this;
	}

	protected void renderInner(PoseStack ms, VertexConsumer consumer, Vec3 camera, float pt, float width,
		Vector4f color, int lightmap, boolean disableNormals) {
		bufferCuboidLine(ms, consumer, camera, start, end, width, color, lightmap, disableNormals);
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffer, Vec3 camera, float pt) {
		float width = params.getLineWidth();
		if (width == 0)
			return;

		VertexConsumer consumer = buffer.getBuffer(RenderTypes.getOutlineSolid());
		params.loadColor(colorTemp);
		Vector4f color = colorTemp;
		int lightmap = params.lightmap;
		boolean disableLineNormals = params.disableLineNormals;
		renderInner(ms, consumer, camera, pt, width, color, lightmap, disableLineNormals);
	}

}
