package com.simibubi.mightyarchitect.buildomatico.client;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.buildomatico.TessellatorHelper;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class GroundPlanRenderer {

	private static final ResourceLocation blueprintShaderLocation = new ResourceLocation(TheMightyArchitect.ID,
			"shaders/post/blueprint.json");
	private static final ResourceLocation heavyTexture = new ResourceLocation(TheMightyArchitect.ID,
			"textures/blocks/markers/heavy.png");
	private static final ResourceLocation lightTexture = new ResourceLocation(TheMightyArchitect.ID,
			"textures/blocks/markers/light.png");
	private static final ResourceLocation innerTexture = new ResourceLocation(TheMightyArchitect.ID,
			"textures/blocks/markers/inner.png");
	static final ResourceLocation trimTexture = new ResourceLocation(TheMightyArchitect.ID,
			"textures/blocks/markers/trim.png");
	
	public static void updateShader(boolean active) {
		Minecraft mc = Minecraft.getMinecraft();
		if (active && !pencilShaderActive(mc)) 
			mc.entityRenderer.loadShader(blueprintShaderLocation);
		if (!active && pencilShaderActive(mc)) 
			mc.entityRenderer.stopUseShader();
	}
	
	private static boolean pencilShaderActive(Minecraft mc) {
		return mc.entityRenderer.isShaderActive()
				&& mc.entityRenderer.getShaderGroup().getShaderGroupName().equals(blueprintShaderLocation.toString());
	}
	
	private Minecraft mc;
	
	public GroundPlanRenderer(Minecraft mc) {
		this.mc = mc;
	}
		
	public void renderSelection(BlockPos selectedPos, BlockPos firstPos, BlockPos anchor) {
		if (selectedPos != null) {
			mc.getTextureManager().bindTexture(trimTexture);
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			TessellatorHelper.walls(bufferBuilder, selectedPos, new BlockPos(1, 1, 1), 0.125, false, true);

			if (firstPos != null) {
				BlockPos actualFirstPos = anchor.add(firstPos);
				BlockPos size = selectedPos.subtract(actualFirstPos);
				Cuboid selection = new Cuboid(actualFirstPos, size.getX(), 1, size.getZ());
				selection.width += 1;
				selection.length += 1;
				TessellatorHelper.walls(bufferBuilder, selection.getOrigin(),
						selection.getSize(), -0.125, false, true);
				Tessellator.getInstance().draw();

				TessellatorHelper.drawString("" + selection.width, selection.x + selection.width / 2f,
						selection.y + .5f, selection.z - 1, true, false);

				TessellatorHelper.drawString("" + selection.width, selection.x + selection.width / 2f,
						selection.y + .5f, selection.z + selection.length + 1, true, false);

				TessellatorHelper.drawString("" + selection.length, selection.x + selection.width + 1,
						selection.y + .5f, selection.z + selection.length / 2f, true, false);

				TessellatorHelper.drawString("" + selection.length, selection.x - 1, selection.y + .5f,
						selection.z + selection.length / 2f, true, false);

			} else {
				Tessellator.getInstance().draw();
			}

		}
	}

	public void renderGroundPlan(GroundPlan groundPlan, BlockPos anchor) {
		if (groundPlan != null && anchor != null) {
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			ResourceLocation currentTexture = null;
			mc.getTextureManager().bindTexture(innerTexture);

			for (Cuboid c : groundPlan.getAll()) {

				ResourceLocation newTexture = (c.layer == 0) ? heavyTexture : lightTexture;
				if (newTexture != currentTexture) {
					Tessellator.getInstance().draw();
					mc.getTextureManager().bindTexture(newTexture);
					bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
					currentTexture = newTexture;
				}
			}

			Tessellator.getInstance().draw();
			mc.getTextureManager().bindTexture(trimTexture);
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			for (Cuboid c : groundPlan.getAll()) {
				BlockPos pos = c.getOrigin().add(anchor);
				TessellatorHelper.walls(bufferBuilder, pos, new BlockPos(c.width, 1, c.length), 0.125, false, true);
				if (c.isTop())
					TessellatorHelper.walls(bufferBuilder, pos.add(0, c.height, 0), new BlockPos(c.width, 1, c.length),
							0.125, false, true);
			}

			Tessellator.getInstance().draw();
			mc.getTextureManager().bindTexture(innerTexture);
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			for (Cuboid c : groundPlan.getAll()) {
				BlockPos pos = c.getOrigin().add(anchor);
				TessellatorHelper.walls(bufferBuilder, pos, c.getSize(), -0.250, true, false);
			}

			Tessellator.getInstance().draw();
		}
	}

	

}
