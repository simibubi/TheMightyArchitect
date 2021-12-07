package com.simibubi.mightyarchitect.foundation.utility.outliner;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.foundation.RenderTypes;
import com.simibubi.mightyarchitect.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.math.Matrix4f;
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
	public void render(PoseStack ms, MultiBufferSource buffer) {
		if (text == null)
			return;
		
		Minecraft mc = Minecraft.getInstance();
		float pt = mc.getFrameTime();
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
		Vec3 v1 = new Vec3(-f + 2, -scaleMod * (h - 1), 0);
		Vec3 v2 = new Vec3(-f + 2, scaleMod, 0);
		Vec3 v3 = new Vec3(f - 2, scaleMod, 0);
		Vec3 v4 = new Vec3(f - 2, -scaleMod * (h - 1), 0);

		ms.pushPose();
		ms.scale(-scaleMod, 1, scaleMod);
		ms.translate(0, 0, .5f);

		putQuadUV(ms, buffer.getBuffer(RenderTypes.getOutlineSolid()), v1, v2, v3, v4,0, 0, 1, 1, null, true);

		ms.popPose();

		ms.scale(scaleMod, 1, 1);
		ms.translate(0, -2 * scaleMod, 0);
		renderCuboidLine(ms, buffer, v4, v1);
		ms.popPose();

		ms.pushPose();
		ms.scale(-scaleMod, -scaleMod, scaleMod);
		//mc.font.drawInBatch(text, f, 0, params.color, false, matrix4f, buffer, true, 0, 0xF000F0);
		mc.font.draw(ms, text, f, 0, params.color);
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
