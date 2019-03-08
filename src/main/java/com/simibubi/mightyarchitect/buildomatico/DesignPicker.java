package com.simibubi.mightyarchitect.buildomatico;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Cuboid;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Design;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Design.Style;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Design.Type;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Sketch;

import net.minecraft.util.math.BlockPos;

public class DesignPicker {

	private Map<Character, DesignCache> styleGroups;
	
	public Sketch pickDesigns(GroundPlan groundPlan) {
		styleGroups = new HashMap<>();
		styleGroups.put('A', new DesignCache());
		styleGroups.put('B', new DesignCache());
		styleGroups.put('C', new DesignCache());
		styleGroups.put('D', new DesignCache());
		
		Sketch sketch = composeAround(groundPlan);
		sketch.setContext(groundPlan.getContext());
		return sketch;
	}
	
	private Sketch composeAround(GroundPlan groundPlan) {
		Sketch sketch = new Sketch();
		
		for (int layer = 0; layer < groundPlan.layerCount; layer++) {
			for (Cuboid c : groundPlan.getCuboidsOnLayer(layer)) {
				
				DesignCache cache = new DesignCache(true);
				if (c.styleGroup >= 'A' && c.styleGroup <= 'D') {
					cache = styleGroups.get(c.styleGroup);	
				}
				
				BlockPos size = new BlockPos(c.width, c.height, c.length);
				BlockPos start = new BlockPos(c.x, c.y, c.z);
				DesignHelper.addCuboid(cache, c.isSecondary() ? sketch.secondary : sketch.primary, c.style, start, size);
				if (c.isTop()) {
					DesignHelper.addDoubleRoof(cache, c.isSecondary() ? sketch.secondary : sketch.primary, start.up(c.height), size);					
				}
			}
		}

		groundPlan.clearInsides();
		sketch.interior = groundPlan.getClearing();
		
		return sketch;
	}
	
	class DesignCache {
		Map<Vector<Integer>, Design> designs;
		boolean random;
		
		public DesignCache() {
			designs = new HashMap<>();
		}
		
		public DesignCache(boolean random) {
			this();
			this.random = random;
		}
		
		public boolean isRandom() {
			return random;
		}
		
		public Design pick(Style weight, Type type, int height) {
			return pick(weight, type, 0, height);
		}
		
		public Design pick(Style weight, Type type, int width, int height) {
			Vector<Integer> vector = new Vector<>();
			vector.add(weight.ordinal());
			vector.add(type.ordinal());
			vector.add(width);
			vector.add(height);
			
			if (designs.containsKey(vector)) {
				return designs.get(vector);
			} else {
				Design design = DesignStorage.findRandomFor(weight, type, width, height);
				designs.put(vector, design);
				return design;
			}
		}
	}

}
