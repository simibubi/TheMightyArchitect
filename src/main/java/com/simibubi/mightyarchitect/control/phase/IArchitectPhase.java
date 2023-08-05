package com.simibubi.mightyarchitect.control.phase;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

public interface IArchitectPhase {

	public void whenEntered();

	public void update();

	default void render(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {}

	public void whenExited();

	public List<String> getToolTip();

	public void onClick(int button);

	public void onKey(int key, boolean released);

	public boolean onScroll(int amount);

}
