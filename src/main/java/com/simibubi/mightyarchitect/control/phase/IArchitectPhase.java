package com.simibubi.mightyarchitect.control.phase;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;

public interface IArchitectPhase {

	public void whenEntered();
	public void update();
	public void render(MatrixStack ms, IRenderTypeBuffer buffer);
	public void whenExited();
	
	public List<String> getToolTip();
	
	public void onClick(int button);
	public void onKey(int key, boolean released);
	public boolean onScroll(int amount);
	
}
