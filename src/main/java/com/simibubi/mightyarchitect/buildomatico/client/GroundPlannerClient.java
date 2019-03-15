package com.simibubi.mightyarchitect.buildomatico.client;

import com.simibubi.mightyarchitect.buildomatico.client.tools.ImAToolForGroundPlanning;
import com.simibubi.mightyarchitect.buildomatico.client.tools.RoomTool;
import com.simibubi.mightyarchitect.buildomatico.helpful.RaycastHelper;
import com.simibubi.mightyarchitect.buildomatico.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TextComponentString;

public class GroundPlannerClient {

	private static Minecraft mc;
	private static GroundPlannerClient instance;

	private boolean active;
	private BlockPos anchor;
	private GroundPlan groundPlan;
	private GroundPlanRenderer renderer;

	private ImAToolForGroundPlanning activeTool;

	public GroundPlannerClient(DesignTheme theme) {
		groundPlan = new GroundPlan(theme);
		renderer = new GroundPlanRenderer(mc);
		activeTool = new RoomTool(this);
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
		String message = activeTool.handleRightClick();

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
		EntityPlayerSP player = mc.player;

		RayTraceResult trace = RaycastHelper.rayTraceRange(player.world, player, 75);
		if (trace != null && trace.typeOfHit == Type.BLOCK) {

			BlockPos hit = trace.getBlockPos();
			if (trace.sideHit.getAxis() == Axis.Y)
				hit = hit.offset(trace.sideHit);

			if (anchor == null)
				activeTool.updateSelection(hit);
			else
				activeTool.updateSelection(hit.subtract(anchor));

		} else {
			activeTool.updateSelection(null);
		}
	}

	public void render() {
		TessellatorHelper.prepareForDrawing();
		activeTool.render();
		renderer.renderGroundPlan(groundPlan, anchor);
		TessellatorHelper.cleanUpAfterDrawing();
	}

}
