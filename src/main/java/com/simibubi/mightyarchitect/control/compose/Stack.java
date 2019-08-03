package com.simibubi.mightyarchitect.control.compose;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.design.ThemeStatistics;

import net.minecraft.util.math.BlockPos;

public class Stack {

	protected List<Room> rooms;
	protected DesignTheme theme;

	public Stack(Room room) {
		rooms = new ArrayList<>();
		theme = ArchitectManager.getModel().getGroundPlan().theme;

		if (room.designLayer == DesignLayer.None) {
			room.designLayer = theme.getLayers().contains(DesignLayer.Foundation) ? DesignLayer.Foundation
					: DesignLayer.Regular;
		}

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
		if (rooms.size() < ThemeStatistics.MAX_FLOORS) {
			Room newRoom = highest().stack();
			if (highest().designLayer == DesignLayer.Foundation) {
				newRoom.designLayer = DesignLayer.Regular;
			}
			int defaultHeightForFloor = theme.getDefaultHeightForFloor(rooms.size());

			if (defaultHeightForFloor != -1)
				newRoom.height = defaultHeightForFloor;
			else
				newRoom.height = Math.max(highest().height, Math.min(4, theme.getMaxFloorHeight()));
			rooms.add(newRoom);
		}
	}

	public void decrease() {
		if (!rooms.isEmpty()) {
			if (rooms.size() > 1) {
				rooms.get(rooms.size() - 2).roofType = highest().roofType;
				rooms.get(rooms.size() - 2).quadFacadeRoof = highest().quadFacadeRoof;
			}
			rooms.remove(highest());
		}
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
		return ThemeStatistics.MAX_ROOF_SPAN;
	}

	public int getMinWidth() {
		return theme.getStatistics().MinRoomLength;
	}

	public DesignType getRoofType() {
		return highest().roofType;
	}

}
