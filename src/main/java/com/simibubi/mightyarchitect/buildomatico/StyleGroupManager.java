package com.simibubi.mightyarchitect.buildomatico;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.buildomatico.helpful.DesignHelper;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Design;

public class StyleGroupManager {
	private Map<Character, StyleGroupDesignProvider> styleGroups;
	
	public StyleGroupManager() {
		styleGroups = new HashMap<>();
		
		ImmutableList.of('A', 'B', 'C', 'D').forEach(styleGroup -> {
			styleGroups.put(styleGroup, new StyleGroupDesignCache());			
		});
		styleGroups.put('U', new RandomDesignProvider());
	}
	
	public StyleGroupDesignProvider getStyleGroup(char group) {
		return styleGroups.get(group);
	}
	
	public static abstract class StyleGroupDesignProvider {
		public abstract Design find(DesignQuery query);
	}
	
	public static class RandomDesignProvider extends StyleGroupDesignProvider {
		@Override
		public Design find(DesignQuery query) {
			return DesignHelper.pickRandom(query);
		}
		
	}
	
	public static class StyleGroupDesignCache extends StyleGroupDesignProvider {
		Map<Vector<Integer>, Design> designs;
		boolean random;

		public StyleGroupDesignCache() {
			designs = new HashMap<>();
		}

		@Override
		public Design find(DesignQuery query) {
			Vector<Integer> key = query.asCacheKey();
			if (designs.containsKey(key)) {
				return designs.get(key);
			} else {
				Design design = DesignHelper.pickRandom(query);
				designs.put(key, design);
				return design;
			}
		}
	}
	
}
