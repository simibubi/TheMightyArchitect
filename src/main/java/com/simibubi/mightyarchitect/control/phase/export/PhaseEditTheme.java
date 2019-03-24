package com.simibubi.mightyarchitect.control.phase.export;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.helpful.BuildingHelper;
import com.simibubi.mightyarchitect.control.helpful.TesselatorTextures;
import com.simibubi.mightyarchitect.control.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.control.phase.PhaseBase;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class PhaseEditTheme extends PhaseBase {

	public static Cuboid selectedDesign;
	public static Cuboid effectiveSelectedDesign;
	public static int effectiveHeight;

	public String frontText;
	public String rightText;
	public String backText;
	public String leftText;

	private DesignType lastType;

	@Override
	public void whenEntered() {
		selectedDesign = null;
		effectiveSelectedDesign = null;
		frontText = null;
		rightText = null;
		leftText = null;
		backText = null;
		lastType = null;
		effectiveHeight = 0;
	}

	@Override
	public void update() {
		if (lastType == DesignExporter.type)
			return;

		frontText = null;
		rightText = null;
		leftText = null;
		backText = null;

		lastType = DesignExporter.type;

		switch (lastType) {
		case CORNER:
			frontText = "Facade";
			rightText = "Side Facade";
			break;
		case FACADE:
		case WALL:
			frontText = "Facade";
			backText = "Back";
			break;
		case FLAT_ROOF:
			frontText = "Front";
			rightText = "Back";
			backText = "Back";
			leftText = "Front";
			break;
		case ROOF:
			frontText = "Facade";
			leftText = "Side";
			rightText = "Side";
			backText = "Back";
			break;
		case TOWER:
		case TOWER_FLAT_ROOF:
		case TOWER_ROOF:
			break;
		default:
			break;
		}

	}

	@Override
	public void render() {
		if (selectedDesign == null)
			return;
		TessellatorHelper.prepareForDrawing();
		TesselatorTextures.Selection.bind();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		Cuboid selection = selectedDesign;

		TessellatorHelper.walls(bufferBuilder, selection.getOrigin(), selection.getSize().down(selection.height - 1),
				1 / 16f, false, true);
		Tessellator.getInstance().draw();
		
		bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
		GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
		RenderGlobal.drawBoundingBox(bufferBuilder, selection.x - 1/8f, selection.y + 1/16f, selection.z - 1/8f, selection.x + selection.width + 1/8f, selection.y + selection.height + 1/16f, selection.z + selection.length + 1/8f, 1, 1, 1, 0.6f);
		Tessellator.getInstance().draw();
		GlStateManager.enableTexture2D();
		GlStateManager.depthMask(true);
		
		if (effectiveSelectedDesign != null) {
			TesselatorTextures.Exporter.bind();
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			
			if (DesignExporter.type == DesignType.TOWER || DesignExporter.type == DesignType.TOWER_FLAT_ROOF || DesignExporter.type == DesignType.TOWER_ROOF) {
				int radius = DesignExporter.designParameter;
				BlockPos center = selection.getCenter().down(selection.height / 2);
				for (BlockPos pos : BuildingHelper.getCircle(center, radius)) {
					TessellatorHelper.cube(bufferBuilder, pos, BlockPos.ORIGIN.add(1, effectiveSelectedDesign.height, 1),
							1 / 32d, true, false);	
				}
			} else {
				Cuboid effectiveSelection = effectiveSelectedDesign;
				TessellatorHelper.cube(bufferBuilder, effectiveSelection.getOrigin(), effectiveSelection.getSize(),
						1 / 32d, true, false);				
			}
			Tessellator.getInstance().draw();			
		}

		if (backText != null)
			TessellatorHelper.drawString(backText, selection.x + selection.width / 2f, selection.y + .5f,
					selection.z - 1, false, false);

		if (frontText != null)
			TessellatorHelper.drawString(frontText, selection.x + selection.width / 2f, selection.y + .5f,
					selection.z + selection.length + 1, false, false);

		if (rightText != null)
			TessellatorHelper.drawString(rightText, selection.x + selection.width + 1, selection.y + .5f,
					selection.z + selection.length / 2f, false, false);

		if (leftText != null)
			TessellatorHelper.drawString(leftText, selection.x - 1, selection.y + .5f,
					selection.z + selection.length / 2f, false, false);

		TessellatorHelper.cleanUpAfterDrawing();
	}

	@Override
	public void whenExited() {

	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("Right click the origin marker of your designs to scan them.",
				"Don't forget to pick the correct traits (type, layer, size) for the next design!");
	}

	public static void setVisualization(Cuboid bounds) {
		selectedDesign = bounds;
		effectiveSelectedDesign = null;
		
		if (selectedDesign == null)
			return;

		switch (DesignExporter.type) {
		case CORNER:
			effectiveSelectedDesign = new Cuboid(selectedDesign.getOrigin(), 1, selectedDesign.height, 1);
			break;
		case WALL:
		case FACADE:
			effectiveSelectedDesign = new Cuboid(selectedDesign.getOrigin(), selectedDesign.width,
					selectedDesign.height, 1);
			break;
		case FLAT_ROOF:
			int margin = DesignExporter.designParameter;
			effectiveSelectedDesign = new Cuboid(selectedDesign.getOrigin().east(margin),
					selectedDesign.getSize().add(-margin, 0, -margin));
			break;
		case ROOF:
			int span = DesignExporter.designParameter;
			margin = (selectedDesign.width - span) /2;
			effectiveSelectedDesign = new Cuboid(selectedDesign.getOrigin().add(margin, 0, 0),
					selectedDesign.width - 2 * margin, selectedDesign.height, 3);
			break;
		case TOWER:
		case TOWER_FLAT_ROOF:
		case TOWER_ROOF:
			int radius = DesignExporter.designParameter;
			margin = (selectedDesign.width - (radius * 2 + 1)) / 2;
			effectiveSelectedDesign = new Cuboid(selectedDesign.getOrigin().add(margin, 0, margin),
					selectedDesign.getSize().add(-2 * margin, 0, -2 * margin));
			break;
		default:
			break;
		}
		
		effectiveSelectedDesign.height = effectiveHeight;
	}

	public static boolean isVisualizing() {
		return selectedDesign != null;
	}

	public static void resetVisualization() {
		selectedDesign = null;
		effectiveSelectedDesign = null;
		effectiveHeight = 0;
	}

}
