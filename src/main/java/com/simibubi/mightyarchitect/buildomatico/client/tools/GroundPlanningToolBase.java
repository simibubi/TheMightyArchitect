package com.simibubi.mightyarchitect.buildomatico.client.tools;

import com.simibubi.mightyarchitect.buildomatico.ArchitectManager;
import com.simibubi.mightyarchitect.buildomatico.helpful.RaycastHelper;
import com.simibubi.mightyarchitect.buildomatico.model.Schematic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public abstract class GroundPlanningToolBase implements ImAToolForGroundPlanning {

	protected Schematic model;
	protected BlockPos selectedPosition;
	
	public void init() {
		model = ArchitectManager.getModel();
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

			if (model.getAnchor() == null)
				selectedPosition = hit;
			else
				selectedPosition = hit.subtract(model.getAnchor());

		} else {
			selectedPosition = null;
		}
		
	}
	
	@Override
	public String handleRightClick() {
		if (selectedPosition == null)
			return null;
		
		if (model.getAnchor() == null) {
			model.setAnchor(selectedPosition);
			selectedPosition = BlockPos.ORIGIN;
		}
		
		return null;
	}
	
	@Override
	public void handleKey(int key) {
	}
	
}
