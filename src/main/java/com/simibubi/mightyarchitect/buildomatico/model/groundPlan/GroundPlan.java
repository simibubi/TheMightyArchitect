package com.simibubi.mightyarchitect.buildomatico.model.groundPlan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.mightyarchitect.buildomatico.model.context.Context;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;

import net.minecraft.util.math.BlockPos;

public class GroundPlan {

	public static final int MAX_LAYERS = 5;

	public Context context;
	public DesignTheme theme;
	private List<Stack> stacks;
	private List<Room> interior;
	
	public GroundPlan(DesignTheme theme) {
		this.theme = theme;
		stacks = new ArrayList<>();
		interior = new LinkedList<>();
	}

	public List<Room> getInterior() {
		interior.clear();
		forEachRoom(room -> {
			
			if (room.designLayer.isExterior())
				return;
			
			interior.add(room.getInterior());
			
		});
		return interior;
	}

	public Stack startStack(Room room) {
		Stack stack = new Stack(room);
		stacks.add(stack);
		return stack;
	}
	
	public Stack getStackAtPos(BlockPos globalPos) {		
		BlockPos localPos = globalPos.subtract(context.getAnchor());
		for (Stack stack : stacks) {
			Room room = stack.getRoomAtPos(localPos);
			if (room != null)
				return stack;
		}
		return null;
	}
	
	public Room getRoomAtPos(BlockPos globalPos) {
		BlockPos localPos = globalPos.subtract(context.getAnchor());
		for (Stack stack : stacks) {
			Room room = stack.getRoomAtPos(localPos);
			if (room != null)
				return room;
		}
		return null;
	}
	
	public boolean isEmpty() {
		return stacks.isEmpty();
	}
	
	public void remove(Stack stack) {
		stacks.remove(stack);
	}
	
	public void forEachStack(Consumer<? super Stack> action) {
		stacks.forEach(action);
	}
	
	public void forEachRoom(Consumer<? super Room> action) {
		stacks.forEach(stack -> stack.forEach(action));
	}

}
