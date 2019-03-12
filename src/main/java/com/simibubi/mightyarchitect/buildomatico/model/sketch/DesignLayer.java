package com.simibubi.mightyarchitect.buildomatico.model.sketch;

public enum DesignLayer {
	
	Foundation("foundation", "Foundation"),
	Regular("regular", "Regular"),
	Open("open", "Open Arcs"),
	
	None("none", "None"),
	Independent("independent", "Independent");
	
	private String filePath;
	private String displayName;
	
	private DesignLayer(String filePath, String displayName) {
		this.filePath = filePath;
		this.displayName = displayName;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	
}
