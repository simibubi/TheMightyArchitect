package com.simibubi.mightyarchitect.control.compose;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.mightyarchitect.control.design.DesignLayer;

import net.minecraft.util.math.BlockPos;

public class Stack {

	public static final int MaxHeight = 5;
	
	private List<Room> rooms;
	
	public Stack(Room room) {
		rooms = new ArrayList<>();
		
		if (room.designLayer == DesignLayer.None)
			room.designLayer = DesignLayer.Foundation;
		
		rooms.add(room);
	}
	
	public Room lowest() {
		if (rooms.isEmpty())
			return null;
		
		return rooms.get(0);
	}
	
	public Room highest() {
		if (rooms.isEmpty())
			return null;
		
		return rooms.get(rooms.size() - 1);
	}
	
	public void increase() {
		if (rooms.size() < MaxHeight) {
			Room newRoom = highest().stack();
			if (highest().designLayer == DesignLayer.Foundation) {
				newRoom.designLayer = DesignLayer.Regular;
				newRoom.height = Math.max(highest().height, 4);
			}
			rooms.add(newRoom);
		}
	}
	
	public void decrease() {
		if (!rooms.isEmpty()) 
			rooms.remove(highest());
	}
	
	public void forEachAbove(Room anchor, Consumer<? super Room> action) {
		rooms.subList(rooms.indexOf(anchor) + 1, rooms.size()).forEach(action);
	}

	public void forRoomAndEachAbove(Room anchor, Consumer<? super Room> action) {
		rooms.subList(rooms.indexOf(anchor), rooms.size()).forEach(action);
	}
	
	public void forEach(Consumer<? super Room> action) {
		rooms.forEach(action);
	}
	
	public Room getRoomAtPos(BlockPos localPos) {
		for (Room room : rooms)
			if (room.contains(localPos))
				return room;
		return null;
	}
	
	public List<Room> getRooms() {
		return rooms;
	}
	
	public int floors() {
		return rooms.size();
	}
	
	public int getMaxFacadeWidth() {
		return 35;
	}
	
	
}
