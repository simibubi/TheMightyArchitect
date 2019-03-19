package com.simibubi.mightyarchitect.control.compose;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.mightyarchitect.control.design.DesignTheme;

import net.minecraft.util.math.BlockPos;

public class GroundPlan {

	public static final int MAX_LAYERS = 5;

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
		forEachStack(stack -> {
			if (stack instanceof CylinderStack)
				return;
			 
			stack.forEach(room -> {
				if (room.designLayer.isExterior())
					return;
				
				interior.add(room.getInterior());				
			});
		});
		return interior;
	}

	public void addStack(Stack stack) {
		stacks.add(stack);
	}
	
	public Stack getStackAtPos(BlockPos localPos) {		
		for (Stack stack : stacks) {
			Room room = stack.getRoomAtPos(localPos);
			if (room != null)
				return stack;
		}
		return null;
	}
	
	public Room getRoomAtPos(BlockPos localPos) {
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
