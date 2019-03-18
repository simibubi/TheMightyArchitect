package com.simibubi.mightyarchitect.gui;

public enum Guis {

	COMPOSER("composer"),
	PALETTE("palette");
	
	public int id;
	public String name;
	private Guis(String name) {
		this.id = ordinal();
		this.name = name;
	}
	
	public static Guis getById(int id) {
		if (id >= Guis.values().length)
			throw new IllegalArgumentException("Theres no Gui with ID " + id);
		return Guis.values()[id];
	}
	
}
