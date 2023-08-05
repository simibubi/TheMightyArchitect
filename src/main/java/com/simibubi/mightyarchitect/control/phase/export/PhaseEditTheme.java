package com.simibubi.mightyarchitect.control.phase.export;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.AllSpecialTextures;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.phase.PhaseBase;
import com.simibubi.mightyarchitect.foundation.utility.BuildingHelper;
import com.simibubi.mightyarchitect.foundation.utility.outliner.AABBOutline;
import com.simibubi.mightyarchitect.foundation.utility.outliner.BlockClusterOutline;
import com.simibubi.mightyarchitect.foundation.utility.outliner.Outline;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class PhaseEditTheme extends PhaseBase {

	public static Cuboid selectedDesign;
	public static Outline effectiveSelectedDesign;
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
		tickOutlines();
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

	static final Object effectiveDesignKey = new Object();
	static final Object textKey1 = new Object();
	static final Object textKey2 = new Object();
	static final Object textKey3 = new Object();
	static final Object textKey4 = new Object();

	private void tickOutlines() {
		if (selectedDesign == null)
			return;

		MightyClient.outliner.chaseAABB("editThemeSelection", selectedDesign.toAABB());

		if (effectiveSelectedDesign != null)
			MightyClient.outliner.show(effectiveDesignKey, effectiveSelectedDesign)
				.withFaceTexture(AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f);

		Cuboid selection = selectedDesign;
		float hw = selection.width / 2f;
		float hl = selection.length / 2f;

		chaseText(textKey1, selection.x + hw, selection.y + .5f, selection.z - 1, backText);
		chaseText(textKey2, selection.x + hw, selection.y + .5f, selection.z + selection.length + 1, frontText);
		chaseText(textKey3, selection.x + selection.width + 1, selection.y + .5f, selection.z + hl, rightText);
		chaseText(textKey4, selection.x - 1, selection.y + .5f, selection.z + hl, leftText);
	}

	private void chaseText(Object key, float x, float y, float z, String text) {
		MightyClient.outliner.chaseText(key, new Vec3(x, y, z), text)
			.disableLineNormals()
			.colored(0xffffff);
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
		MightyClient.outliner.remove(effectiveDesignKey);

		selectedDesign = bounds;
		effectiveSelectedDesign = null;
		if (selectedDesign == null)
			return;

		Function<Cuboid, Outline> outlineFunc = c -> new AABBOutline(c.toAABB());
		switch (DesignExporter.type) {
		case CORNER:
			effectiveSelectedDesign = outlineFunc.apply(new Cuboid(selectedDesign.getOrigin(), 1, effectiveHeight, 1));
			break;
		case WALL:
		case FACADE:
			effectiveSelectedDesign =
				outlineFunc.apply(new Cuboid(selectedDesign.getOrigin(), selectedDesign.width, effectiveHeight, 1));
			break;
		case FLAT_ROOF:
			int margin = DesignExporter.designParameter;
			effectiveSelectedDesign = outlineFunc.apply(new Cuboid(selectedDesign.getOrigin()
				.east(margin),
				selectedDesign.getSize()
					.offset(-margin, 0, -margin)));
			break;
		case ROOF:
			int span = DesignExporter.designParameter;
			margin = (selectedDesign.width - span) / 2;
			effectiveSelectedDesign = outlineFunc.apply(new Cuboid(selectedDesign.getOrigin()
				.offset(margin, 0, 0), selectedDesign.width - 2 * margin, selectedDesign.height, 3));
			break;
		case TOWER:
		case TOWER_FLAT_ROOF:
		case TOWER_ROOF:
			int radius = DesignExporter.designParameter;
			margin = (selectedDesign.width - (radius * 2 + 1)) / 2;
			BlockPos center = selectedDesign.getCenter()
				.below(selectedDesign.height / 2);
			List<BlockPos> cylinderSet = new ArrayList<>();
			for (int i = 0; i < effectiveHeight; i++) {
				final int offset = i;
				BuildingHelper.getCircle(center, radius)
					.forEach(pos -> cylinderSet.add(pos.above(offset)));
			}
			effectiveSelectedDesign = new BlockClusterOutline(cylinderSet);
			break;
		default:
			break;
		}
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
