package com.simibubi.mightyarchitect.control.design;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.control.design.DesignPicker.RoomDesignMapping;
import com.simibubi.mightyarchitect.control.design.partials.Design;
import com.simibubi.mightyarchitect.foundation.utility.DesignHelper;

public class TemporaryDesignCache {
	Map<Vector<Integer>, Design> designs;
	Map<Room, RoomDesignMapping> roomDesigns;
	Map<Stack, Design> roofDesigns;
	Random random;

	public TemporaryDesignCache(Map<Room, RoomDesignMapping> roomDesigns, Map<Stack, Design> roofDesigns, int seed) {
		designs = new HashMap<>();
		this.roomDesigns = roomDesigns;
		this.roofDesigns = roofDesigns;
		random = new Random(seed);
	}
	
	public boolean hasCachedRoom(Room room) {
		return roomDesigns.containsKey(room);
	}
	
	public boolean hasCachedRoof(Stack stack) {
		return roofDesigns.containsKey(stack);
	}
	
	public RoomDesignMapping getCachedRoom(Room room) {
		return roomDesigns.get(room);
	}
	
	public Design getCachedRoof(Stack stack) {
		return roofDesigns.get(stack);
	}
	
	public void cacheRoom(Room room, RoomDesignMapping value) {
		roomDesigns.put(room, value);
	}
	
	public void cacheRoof(Stack stack, Design roof) {
		roofDesigns.put(stack, roof);
	}

	public Design find(DesignQuery query) {
		Vector<Integer> key = query.asCacheKey();
		if (designs.containsKey(key)) {
			return designs.get(key);
		} else {
			Design design = DesignHelper.pickRandom(query, random);
			designs.put(key, design);
			return design;
		}
	}
	
	public Random getRandom() {
		return random;
	}
	
}
