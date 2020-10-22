package com.simibubi.mightyarchitect.foundation.utility.outliner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.mightyarchitect.foundation.RenderTypes;
import com.simibubi.mightyarchitect.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class OutlinedText extends Outline {

	private String text;
	Vector3d targetLocation;
	Vector3d location;
	Vector3d prevLocation;

	public OutlinedText() {
		setText("");
		targetLocation = Vector3d.ZERO;
		location = Vector3d.ZERO;
		prevLocation = Vector3d.ZERO;
	}

	public void set(Vector3d location) {
		prevLocation = this.location = location;
	}

	public void target(Vector3d location) {
		targetLocation = location;
	}

	@Override
	public void tick() {
		super.tick();
		prevLocation = location;
		location = VecHelper.lerp(location, targetLocation, .5f);
	}

	@Override
	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		if (text == null)
			return;
		
		Minecraft mc = Minecraft.getInstance();
		float pt = mc.getRenderPartialTicks();
		Vector3d vec = VecHelper.lerp(prevLocation, location, pt);
		EntityRendererManager renderManager = mc.getRenderManager();
		FontRenderer fontrenderer = renderManager.getFontRenderer();
		float stringLength = fontrenderer.getStringWidth(text);

		ms.push();
		ms.translate(vec.x, vec.y, vec.z);
		ms.multiply(renderManager.getRotation());

//		if (scalesUp) {
		double distance = mc.player.getEyePosition(mc.getRenderPartialTicks())
			.squareDistanceTo(vec);
		float scale = (float) (distance / 512f);
		ms.scale(2 + scale, 2 + scale, 2 + scale);
//		}	

		float scaleMod = 0.025F;
		float f = -stringLength / 2;
		float h = fontrenderer.FONT_HEIGHT;

		ms.push();
		Vector3d v1 = new Vector3d(-f + 2, -scaleMod * (h - 1), 0);
		Vector3d v2 = new Vector3d(-f + 2, scaleMod, 0);
		Vector3d v3 = new Vector3d(f - 2, scaleMod, 0);
		Vector3d v4 = new Vector3d(f - 2, -scaleMod * (h - 1), 0);

		ms.push();
		ms.scale(-scaleMod, 1, scaleMod);
		ms.translate(0, 0, .5f);
		if (params.faceRgb != null)
			putQuadUVColor(ms, buffer.getBuffer(RenderTypes.getOutlineSolid()), v1, v2, v3, v4, params.faceRgb, 0, 0, 1,
				1, null);
		ms.pop();

		ms.scale(scaleMod, 1, 1);
		ms.translate(0, -2 * scaleMod, 0);
		renderCuboidLine(ms, buffer, v4, v1);
		ms.pop();

		ms.push();
		ms.scale(-scaleMod, -scaleMod, scaleMod);
		Matrix4f matrix4f = ms.peek()
			.getModel();
		fontrenderer.draw(text, f, 0, params.color, false, matrix4f, buffer, true, 0, 0xF000F0);
		ms.pop();

		ms.pop();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
