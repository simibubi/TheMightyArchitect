package com.simibubi.mightyarchitect.buildomatico.client.tools;

import com.simibubi.mightyarchitect.buildomatico.client.GroundPlannerClient;
import com.simibubi.mightyarchitect.buildomatico.helpful.RaycastHelper;
import com.simibubi.mightyarchitect.buildomatico.model.context.Context;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public abstract class GroundPlanningToolBase implements ImAToolForGroundPlanning {

	protected GroundPlannerClient planner;
	protected BlockPos selectedPosition;
	
	public void init(GroundPlannerClient planner) {
		this.planner = planner;
		this.selectedPosition = BlockPos.ORIGIN;		
	}
	
	@Override
	public void updateSelection() {
		EntityPlayerSP player = Minecraft.getMinecraft().player;

		RayTraceResult trace = RaycastHelper.rayTraceRange(player.world, player, 75);
		if (trace != null && trace.typeOfHit == Type.BLOCK) {

			BlockPos hit = trace.getBlockPos();
			if (trace.sideHit.getAxis() == Axis.Y)
				hit = hit.offset(trace.sideHit);

			if (planner.getAnchor() == null)
				selectedPosition = hit;
			else
				selectedPosition = hit.subtract(planner.getAnchor());

		} else {
			selectedPosition = null;
		}
		
	}
	
	@Override
	public String handleRightClick() {
		if (selectedPosition == null)
			return null;
		
		if (planner.getAnchor() == null) {
			planner.setAnchor(selectedPosition);
			planner.getGroundPlan().context = new Context(selectedPosition, Minecraft.getMinecraft().player);
			selectedPosition = BlockPos.ORIGIN;
		}
		
		return null;
	}
	
	@Override
	public void handleKey(int key) {
	}
	
}
