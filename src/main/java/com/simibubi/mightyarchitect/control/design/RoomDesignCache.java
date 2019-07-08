package com.simibubi.mightyarchitect.control.design;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.mightyarchitect.control.compose.Room;
import com.simibubi.mightyarchitect.control.compose.Stack;
import com.simibubi.mightyarchitect.control.design.partials.Design;

public class RoomDesignCache {
	
	private Map<Room, Design> cachedDesigns;
	
	public RoomDesignCache() {
		cachedDesigns = new HashMap<>();
	}

	public void rerollAll() {
		cachedDesigns.clear();
	}
	
	public void rerollRoom(Room room) {
		if (cachedDesigns.containsKey(room))
			cachedDesigns.remove(room);
	}
	
	public void rerollStack(Stack stack) {
		stack.forEach(this::rerollRoom);
	}

}
