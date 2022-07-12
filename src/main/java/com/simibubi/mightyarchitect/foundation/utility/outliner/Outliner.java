package com.simibubi.mightyarchitect.foundation.utility.outliner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.foundation.utility.outliner.LineOutline.ChasingLineOutline;
import com.simibubi.mightyarchitect.foundation.utility.outliner.LineOutline.EndChasingLineOutline;
import com.simibubi.mightyarchitect.foundation.utility.outliner.Outline.OutlineParams;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Outliner {

	Map<Object, OutlineEntry> outlines;

	// Facade

	public OutlineParams showLine(Object slot, Vec3 start, Vec3 end) {
		if (!outlines.containsKey(slot)) {
			LineOutline outline = new LineOutline();
			outlines.put(slot, new OutlineEntry(outline));
		}
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		((LineOutline) entry.outline).set(start, end);
		return entry.outline.getParams();
	}
	
	public OutlineParams chaseLine(Object slot, Vec3 start, Vec3 end) {
		if (!outlines.containsKey(slot)) {
			ChasingLineOutline outline = new ChasingLineOutline();
			outline.set(start, end);
			outlines.put(slot, new OutlineEntry(outline));
		}
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		((ChasingLineOutline) entry.outline).target(start, end);
		return entry.outline.getParams();
	}
	
	public OutlineParams chaseText(Object slot, Vec3 location, String text) {
		if (!outlines.containsKey(slot)) {
			OutlinedText outline = new OutlinedText();
			outline.set(location);
			outlines.put(slot, new OutlineEntry(outline));
		}
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		OutlinedText outlinedText = (OutlinedText) entry.outline;
		outlinedText.target(location);
		outlinedText.setText(text);
		return entry.outline.getParams();
	}

	public OutlineParams endChasingLine(Object slot, Vec3 start, Vec3 end, float chasingProgress) {
		if (!outlines.containsKey(slot)) {
			EndChasingLineOutline outline = new EndChasingLineOutline();
			outlines.put(slot, new OutlineEntry(outline));
		}
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		((EndChasingLineOutline) entry.outline).setProgress(chasingProgress)
			.set(start, end);
		return entry.outline.getParams();
	}

	public OutlineParams showAABB(Object slot, AABB bb) {
		createAABBOutlineIfMissing(slot, bb);
		ChasingAABBOutline outline = getAndRefreshAABB(slot);
		outline.prevBB = outline.targetBB = bb;
		return outline.getParams();
	}

	public OutlineParams chaseAABB(Object slot, AABB bb) {
		createAABBOutlineIfMissing(slot, bb);
		ChasingAABBOutline outline = getAndRefreshAABB(slot);
		outline.targetBB = bb;
		return outline.getParams();
	}

	public OutlineParams showCluster(Object slot, Iterable<BlockPos> selection) {
		BlockClusterOutline outline = new BlockClusterOutline(selection);
		OutlineEntry entry = new OutlineEntry(outline);
		outlines.put(slot, entry);
		return entry.getOutline()
			.getParams();
	}
	
	public OutlineParams show(Object slot, Outline outline) {
		if (!outlines.containsKey(slot)) 
			outlines.put(slot, new OutlineEntry(outline));
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		return entry.outline.getParams();
	}

	public void keep(Object slot) {
		if (outlines.containsKey(slot))
			outlines.get(slot).ticksTillRemoval = 1;
	}

	public void remove(Object slot) {
		outlines.remove(slot);
	}

	public Optional<OutlineParams> edit(Object slot) {
		keep(slot);
		if (outlines.containsKey(slot))
			return Optional.of(outlines.get(slot)
				.getOutline()
				.getParams());
		return Optional.empty();
	}

	// Utility

	private void createAABBOutlineIfMissing(Object slot, AABB bb) {
		if (!outlines.containsKey(slot)) {
			ChasingAABBOutline outline = new ChasingAABBOutline(bb);
			outlines.put(slot, new OutlineEntry(outline));
		}
	}

	private ChasingAABBOutline getAndRefreshAABB(Object slot) {
		OutlineEntry entry = outlines.get(slot);
		entry.ticksTillRemoval = 1;
		return (ChasingAABBOutline) entry.getOutline();
	}

	// Maintenance

	public Outliner() {
		outlines = new HashMap<>();
	}

	public void tickOutlines() {
		Set<Object> toClear = new HashSet<>();

		outlines.forEach((key, entry) -> {
			entry.ticksTillRemoval--;
			entry.getOutline()
				.tick();
			if (entry.isAlive())
				return;
			toClear.add(key);
		});

		toClear.forEach(outlines::remove);
	}

	public void renderOutlines(PoseStack ms, MultiBufferSource buffer) {
		outlines.forEach((key, entry) -> {
			Outline outline = entry.getOutline();
			//outline.params.alpha = 1;
			if (entry.ticksTillRemoval < 0) {

				int prevTicks = entry.ticksTillRemoval + 1;
				float fadeticks = (float) entry.outline.params.getFadeTicks();
				float lastAlpha = prevTicks >= 0 ? 1 : 1 + (prevTicks / fadeticks);
				float currentAlpha = 1 + (entry.ticksTillRemoval / fadeticks);
				float alpha = Mth.lerp(Minecraft.getInstance()
					.getFrameTime(), lastAlpha, currentAlpha);

				outline.params.alpha = alpha * alpha * alpha;
				if (outline.params.alpha < 1 / 8f)
					return;
			}
			outline.render(ms, buffer);
		});
	}

	private class OutlineEntry {

		private Outline outline;
		private int ticksTillRemoval;

		public OutlineEntry(Outline outline) {
			this.outline = outline;
			ticksTillRemoval = 1;
		}

		public boolean isAlive() {
			return ticksTillRemoval >= -outline.params.getFadeTicks();
		}

		public Outline getOutline() {
			return outline;
		}

	}

}
