package com.simibubi.mightyarchitect.buildomatico;

public enum Palette {

	// modifiable
	HEAVY_PRIMARY("Main Foundation Walls", 5),
	HEAVY_SECONDARY("Alt. Foundation Walls", 5),
	HEAVY_WINDOW("Foundation Windows", 3),
	HEAVY_POST("Foundation Detailing", 2),
	
	INNER_PRIMARY("Main Walls", 5),
	INNER_SECONDARY("Alt. Walls", 5),
	INNER_DETAIL("Detailed Wall Edges", 5),
	WINDOW("Windows", 3),
	
	OUTER_THICK("Heavy Posts", 2),
	OUTER_THIN("Light Posts / Fences", 1),
	OUTER_SLAB("Detailing Slabs", 3),
	OUTER_FLAT("Detailing Panels", 2),
	
	ROOF_PRIMARY("Main Roofing", 5),
	ROOF_SECONDARY("Alt. Roofing", 5),
	ROOF_DETAIL("Detailed Roof Edges", 4),
	ROOF_SLAB("Main Roofing Slabs", 3),
	
	// dynamic
	ROOF_SLAB_TOP("", 3),
	
	CLEAR("", 10);
	
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
