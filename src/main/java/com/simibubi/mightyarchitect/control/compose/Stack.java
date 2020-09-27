package com.simibubi.mightyarchitect.control.compose;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.simibubi.mightyarchitect.AllSpecialTextures;
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
		theme = ArchitectManager.getModel()
			.getGroundPlan().theme;

		if (room.designLayer == DesignLayer.None) {
			room.designLayer = theme.getLayers()
				.contains(DesignLayer.Foundation) ? DesignLayer.Foundation : DesignLayer.Regular;
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
		insertNewAt(rooms.size(), false);
	}

	@Nullable
	public Room insertNewAt(int index, boolean exactCopy) {
		if (rooms.size() >= ThemeStatistics.MAX_FLOORS)
			return null;

		Room reference = rooms.get(Math.max(0, index - 1));
		Room newRoom = reference.stack(exactCopy);
		if (reference.designLayer == DesignLayer.Foundation)
			newRoom.designLayer = DesignLayer.Regular;
		if (!exactCopy) {
			int defaultHeightForFloor = theme.getDefaultHeightForFloor(rooms.size());
			newRoom.height = defaultHeightForFloor != -1 ? defaultHeightForFloor
				: Math.max(reference.height, Math.min(4, theme.getMaxFloorHeight()));
		}

		rooms.add(index, newRoom);
		forEachAbove(newRoom, r -> r.move(0, newRoom.height, 0));
		return newRoom;
	}

	public void removeRoom(Room room) {
		if (room == highest()) {
			decrease();
			return;
		}
		forEachAbove(room, r -> r.move(0, -room.height, 0));
		rooms.remove(room);
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
		rooms.subList(rooms.indexOf(anchor) + 1, rooms.size())
			.forEach(action);
	}

	public void forRoomAndEachAbove(Room anchor, Consumer<? super Room> action) {
		rooms.subList(rooms.indexOf(anchor), rooms.size())
			.forEach(action);
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

	public AllSpecialTextures getTextureOf(Room room) {
		switch (room.designLayer) {
		case Foundation:
			return AllSpecialTextures.FOUNDATION;
		case None:
		case Open:
		case Regular:
		case Roofing:
		case Special:
		default:
			return AllSpecialTextures.NORMAL;
		}
	}

}
