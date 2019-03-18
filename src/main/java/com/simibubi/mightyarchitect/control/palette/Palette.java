package com.simibubi.mightyarchitect.control.palette;

public enum Palette {

	// modifiable
	HEAVY_PRIMARY("Main Foundation Walls", 8),
	HEAVY_SECONDARY("Alt. Foundation Walls", 8),
	HEAVY_WINDOW("Foundation Windows", 3),
	HEAVY_POST("Foundation Detailing", 2),
	
	INNER_PRIMARY("Main Walls", 7),
	INNER_SECONDARY("Alt. Walls", 7),
	INNER_DETAIL("Detailed Wall Edges", 9),
	WINDOW("Windows", 6),
	
	OUTER_THICK("Heavy Posts", 2),
	OUTER_THIN("Light Posts / Fences", 1),
	OUTER_SLAB("Detailing Slabs", 3),
	OUTER_FLAT("Detailing Panels", 2),
	
	ROOF_PRIMARY("Main Roofing", 4),
	FLOOR("Flooring Material", 4),
	ROOF_DETAIL("Detailed Roof Edges", 5),
	ROOF_SLAB("Main Roofing Slabs", 3),
	
	// dynamic
	ROOF_SLAB_TOP("", 3),
	CLEAR("", 10),
	ROOF_SECONDARY("", 4); // old palettes dont crash
	
	private int priority;
	private String displayName;
	
	private Palette(String displayName, int priority) {
		this.displayName = displayName;
		this.priority = priority;
	}
	
	public boolean isPrefferedOver(Palette other) {
		return this.priority >= other.priority;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public static Palette getByChar(char letter) {
		return (values()[letter - 'A']);
	}
	
	public char asChar() {
		return (char) ('A' + ordinal());
	}
	
	
	
	
}
