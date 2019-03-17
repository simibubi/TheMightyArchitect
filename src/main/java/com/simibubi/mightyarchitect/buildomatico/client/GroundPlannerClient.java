package com.simibubi.mightyarchitect.buildomatico.client;

import com.simibubi.mightyarchitect.buildomatico.client.tools.AllTools;
import com.simibubi.mightyarchitect.buildomatico.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class GroundPlannerClient {

	private static Minecraft mc;
	private static GroundPlannerClient instance;

	private boolean active;
	private BlockPos anchor;
	private GroundPlan groundPlan;
	private GroundPlanRenderer renderer;

	private AllTools activeTool;

	public GroundPlannerClient(DesignTheme theme) {
		groundPlan = new GroundPlan(theme);
		renderer = new GroundPlanRenderer();
		setActiveTool(AllTools.Room);
		activeTool.getTool().init(this);
	}

	public static boolean isActive() {
		return instance != null && instance.active;
	}

	public static boolean isPresent() {
		return instance != null;
	}

	public static GroundPlannerClient getInstance() {
		return instance;
	}

	public static void startComposing(DesignTheme theme) {
		mc = Minecraft.getMinecraft();
		instance = new GroundPlannerClient(theme);
		instance.active = true;
	}

	public static void reset() {
		instance = null;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void handleRightClick() {
		String message = getActiveTool().getTool().handleRightClick();

		if (message != null)
			mc.player.sendStatusMessage(new TextComponentString(message), true);
	}

	public GroundPlan getGroundPlan() {
		return groundPlan;
	}

	public BlockPos getAnchor() {
		return anchor;
	}

	public void setAnchor(BlockPos anchor) {
		this.anchor = anchor;
	}

	public void update() {
		activeTool.getTool().updateSelection();
	}

	public void render() {
		TessellatorHelper.prepareForDrawing();
		renderer.renderGroundPlan(groundPlan, anchor);
		getActiveTool().getTool().render();
		TessellatorHelper.cleanUpAfterDrawing();
	}

	public AllTools getActiveTool() {
		return activeTool;
	}

	public void setActiveTool(AllTools activeTool) {
		this.activeTool = activeTool;
	}
	
	public void cycleTool(boolean forward) {
		setActiveTool(forward ? activeTool.next() : activeTool.previous());
		activeTool.getTool().init(this);
	}

}
