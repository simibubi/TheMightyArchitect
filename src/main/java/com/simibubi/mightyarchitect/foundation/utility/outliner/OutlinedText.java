package com.simibubi.mightyarchitect.foundation.utility.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.simibubi.mightyarchitect.foundation.RenderTypes;
import com.simibubi.mightyarchitect.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;

public class OutlinedText extends Outline {

	private String text;
	Vec3 targetLocation;
	Vec3 location;
	Vec3 prevLocation;

	public OutlinedText() {
		setText("");
		targetLocation = Vec3.ZERO;
		location = Vec3.ZERO;
		prevLocation = Vec3.ZERO;
	}

	public void set(Vec3 location) {
		prevLocation = this.location = location;
	}

	public void target(Vec3 location) {
		targetLocation = location;
	}

	@Override
	public void tick() {
		super.tick();
		prevLocation = location;
		location = VecHelper.lerp(location, targetLocation, .5f);
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource buffer, Vec3 camera, float pt) {
		if (text == null)
			return;

		Minecraft mc = Minecraft.getInstance();
		Vec3 vec = VecHelper.lerp(prevLocation, location, pt);
		EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
		float stringLength = mc.font.width(text);

		ms.pushPose();
		ms.translate(vec.x, vec.y, vec.z);
		ms.mulPose(renderManager.cameraOrientation());

//		if (scalesUp) {
		double distance = mc.player.getEyePosition(mc.getFrameTime())
			.distanceToSqr(vec);
		float scale = (float) (distance / 512f);
		ms.scale(2 + scale, 2 + scale, 2 + scale);
//		}	

		float scaleMod = 0.025F;
		float f = -stringLength / 2;
		float h = mc.font.lineHeight;

		ms.pushPose();

		ms.pushPose();
		ms.scale(-scaleMod, 1, scaleMod);
		ms.translate(0, 0, .5f);

		VertexConsumer consumer = buffer.getBuffer(RenderTypes.getOutlineSolid());
		params.loadColor(colorTemp);
		int lightmap = params.lightmap;
		boolean disableLineNormals = params.disableLineNormals;

		{
			Vector3f v1 = new Vector3f(-f + 2, -scaleMod * (h - 1), 0);
			Vector3f v2 = new Vector3f(-f + 2, scaleMod, 0);
			Vector3f v3 = new Vector3f(f - 2, scaleMod, 0);
			Vector3f v4 = new Vector3f(f - 2, -scaleMod * (h - 1), 0);
			bufferQuad(ms.last(), consumer, v1, v2, v3, v4, colorTemp, 0, 0, 1, 1, lightmap, Vector3f.YP);
		}

		ms.popPose();

		ms.scale(scaleMod, 1, 1);
		ms.translate(0, -2 * scaleMod, 0);

		{
			Vector3d v1 = new Vector3d(-f + 2, -scaleMod * (h - 1), 0);
			Vector3d v4 = new Vector3d(f - 2, -scaleMod * (h - 1), 0);
			bufferCuboidLine(ms, consumer, camera, v4, v1, params.getLineWidth(), colorTemp, lightmap,
				disableLineNormals);
		}

		ms.popPose();

		ms.pushPose();
		ms.scale(-scaleMod, -scaleMod, scaleMod);
		mc.font.draw(ms, text, f, 0, params.rgb.getRGB());
		ms.popPose();

		ms.popPose();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
