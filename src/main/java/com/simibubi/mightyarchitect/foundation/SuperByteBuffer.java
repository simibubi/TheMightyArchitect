package com.simibubi.mightyarchitect.foundation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.mojang.blaze3d.vertex.BufferBuilder.DrawState;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class SuperByteBuffer {

	public interface IVertexLighter {
		public int getPackedLight(float x, float y, float z);
	}

	protected ByteBuffer template;
	protected int formatSize;

	// Vertex Position
	private PoseStack transforms;

	// Vertex Texture Coords
	private boolean shouldShiftUV;
	private SpriteShiftEntry spriteShift;
	private float uTarget, vTarget;

	// Vertex Lighting
	private boolean shouldLight;
	private int packedLightCoords;
	private Matrix4f lightTransform;

	// Vertex Coloring
	private boolean shouldColor;
	private int r, g, b, a;
	private float sheetSize;

	public SuperByteBuffer(RenderedBuffer renderedBuffer) {
		DrawState drawState = renderedBuffer.drawState();
		
		formatSize = drawState.format()
			.getVertexSize();
		int size = drawState.vertexCount() * formatSize;
		
		ByteBuffer byteBuffer = renderedBuffer.vertexBuffer();
		byteBuffer.order(ByteOrder.nativeOrder()); // Vanilla bug, endianness does not carry over into sliced buffers
		template = ByteBuffer.allocateDirect(size)
			.order(ByteOrder.nativeOrder());
		template.order(byteBuffer.order());
		template.limit(byteBuffer.limit());
		template.put(byteBuffer);
		template.rewind();

		transforms = new PoseStack();
	}

	public static float getUnInterpolatedU(TextureAtlasSprite sprite, float u) {
		float f = sprite.getU1() - sprite.getU0();
		return (u - sprite.getU0()) / f * 16.0F;
	}

	public static float getUnInterpolatedV(TextureAtlasSprite sprite, float v) {
		float f = sprite.getV1() - sprite.getV0();
		return (v - sprite.getV0()) / f * 16.0F;
	}

	public void renderInto(PoseStack input, VertexConsumer builder) {
		ByteBuffer buffer = template;
		if (buffer.limit() == 0)
			return;
		buffer.rewind();

		Matrix4f t = input.last()
			.pose()
			.copy();
		Matrix4f localTransforms = transforms.last()
			.pose();
		t.multiply(localTransforms);

		for (int i = 0; i < vertexCount(buffer); i++) {
			float x = getX(buffer, i);
			float y = getY(buffer, i);
			float z = getZ(buffer, i);

			Vector4f pos = new Vector4f(x, y, z, 1F);
			Vector4f lightPos = new Vector4f(x, y, z, 1F);
			pos.transform(t);
			lightPos.transform(localTransforms);

			builder.vertex(pos.x(), pos.y(), pos.z());

			byte r = getR(buffer, i);
			byte g = getG(buffer, i);
			byte b = getB(buffer, i);
			byte a = getA(buffer, i);

			if (shouldColor) {
				float lum = (r < 0 ? 255 + r : r) / 256f;
				builder.color((int) (this.r * lum), (int) (this.g * lum), (int) (this.b * lum), this.a);
			} else
				builder.color(r, g, b, a);

			float u = getU(buffer, i);
			float v = getV(buffer, i);

			if (shouldShiftUV) {
				float targetU = spriteShift.getTarget()
					.getU((getUnInterpolatedU(spriteShift.getOriginal(), u) / sheetSize) + uTarget * 16);
				float targetV = spriteShift.getTarget()
					.getV((getUnInterpolatedV(spriteShift.getOriginal(), v) / sheetSize) + vTarget * 16);
				builder.uv(targetU, targetV);
			} else
				builder.uv(u, v);

			if (shouldLight) {
				int light = packedLightCoords;
				if (lightTransform != null) {
					lightPos.transform(lightTransform);
					light = getLight(Minecraft.getInstance().level, lightPos);
				}
				builder.uv2(light);
			} else
				builder.uv2(getLight(buffer, i));

			builder.normal(getNX(buffer, i), getNY(buffer, i), getNZ(buffer, i))
				.endVertex();
		}

		transforms = new PoseStack();
		shouldShiftUV = false;
		shouldColor = false;
		shouldLight = false;
	}

	public SuperByteBuffer translate(double x, double y, double z) {
		return translate((float) x, (float) y, (float) z);
	}

	public SuperByteBuffer translate(float x, float y, float z) {
		transforms.translate(x, y, z);
		return this;
	}

	public SuperByteBuffer rotate(Direction axis, float radians) {
		if (radians == 0)
			return this;
		transforms.mulPose(axis.step()
			.rotation(radians));
		return this;
	}

	public SuperByteBuffer rotateCentered(Direction axis, float radians) {
		return translate(.5f, .5f, .5f).rotate(axis, radians)
			.translate(-.5f, -.5f, -.5f);
	}

	public SuperByteBuffer shiftUV(SpriteShiftEntry entry) {
		shouldShiftUV = true;
		spriteShift = entry;
		uTarget = 0;
		vTarget = 0;
		sheetSize = 1;
		return this;
	}

	public SuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
		shouldShiftUV = true;
		spriteShift = entry;
		this.uTarget = uTarget;
		this.vTarget = vTarget;
		this.sheetSize = sheetSize;
		return this;
	}

	public SuperByteBuffer light(int packedLightCoords) {
		shouldLight = true;
		lightTransform = null;
		this.packedLightCoords = packedLightCoords;
		return this;
	}

	public SuperByteBuffer light(Matrix4f lightTransform) {
		shouldLight = true;
		this.lightTransform = lightTransform;
		return this;
	}

	public SuperByteBuffer color(int color) {
		shouldColor = true;
		r = ((color >> 16) & 0xFF);
		g = ((color >> 8) & 0xFF);
		b = (color & 0xFF);
		a = 255;
		return this;
	}

	protected int vertexCount(ByteBuffer buffer) {
		return buffer.limit() / formatSize;
	}

	protected int getBufferPosition(int vertexIndex) {
		return vertexIndex * formatSize;
	}

	protected float getX(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index));
	}

	protected float getY(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 4);
	}

	protected float getZ(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 8);
	}

	protected byte getR(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 12);
	}

	protected byte getG(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 13);
	}

	protected byte getB(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 14);
	}

	protected byte getA(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 15);
	}

	protected float getU(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 16);
	}

	protected float getV(ByteBuffer buffer, int index) {
		return buffer.getFloat(getBufferPosition(index) + 20);
	}

	protected int getLight(ByteBuffer buffer, int index) {
		return buffer.getInt(getBufferPosition(index) + 24);
	}

	protected byte getNX(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 28);
	}

	protected byte getNY(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 29);
	}

	protected byte getNZ(ByteBuffer buffer, int index) {
		return buffer.get(getBufferPosition(index) + 30);
	}

	private static int getLight(Level world, Vector4f lightPos) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		float sky = 0, block = 0;
		float offset = 1 / 8f;
		for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset)
			for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset)
				for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
					pos.set(lightPos.x() + xOffset, lightPos.y() + yOffset, lightPos.z() + zOffset);
					sky += world.getBrightness(LightLayer.SKY, pos) / 8f;
					block += world.getBrightness(LightLayer.BLOCK, pos) / 8f;
				}

		return ((int) sky) << 20 | ((int) block) << 4;
	}

}
