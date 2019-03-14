package com.simibubi.mightyarchitect.buildomatico.model.groundPlan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.mightyarchitect.buildomatico.model.context.Context;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignLayer;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;

public class GroundPlan {

	public static final int MAX_LAYERS = 5;

	public Context context;
	public DesignTheme theme;

	private List<List<Room>> layers;
	private List<Room> all;
	private List<Room> interior;
	
	public GroundPlan(DesignTheme theme) {
		this.theme = theme;
		layers = new ArrayList<>();
		interior = new LinkedList<>();
		all = new LinkedList<>();
		
		for (int i = 0; i < MAX_LAYERS; i++)
			layers.add(new LinkedList<>());
	}

	public List<Room> getRoomsOnLayer(int layer) {
		if (layer < layers.size()) {
			return layers.get(layer);
		}
		return new LinkedList<>();
	}

	public List<Room> getInterior() {
		interior.clear();
		for (int i = 0; i < MAX_LAYERS; i++) {
			for (Room room : layers.get(i)) {
				if (room.designLayer.isExterior())
					continue;
				interior.add(room.getInterior());
			}
		}
		return interior;
	}

	public void add(Room room, int layer) {
		if (layer < layers.size()) {
			layers.get(layer).add(room);
			room.layer = layer;
			if (room.designLayer == DesignLayer.None)
				room.designLayer = (layer == 0) ? DesignLayer.Foundation : DesignLayer.Regular;
		}
		all.add(room);
	}

	public List<Room> getAll() {
		return all;
	}

}
