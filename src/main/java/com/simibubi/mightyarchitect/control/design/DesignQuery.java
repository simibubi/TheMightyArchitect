package com.simibubi.mightyarchitect.control.design;

import java.util.Vector;

public class DesignQuery {

	public DesignTheme theme;
	public DesignLayer layer;
	public DesignType type;

	public int desiredWidth;
	public int desiredHeight;

	public DesignQuery(DesignTheme theme, DesignLayer layer, DesignType type) {
		this.theme = theme;
		this.layer = layer;
		this.type = type;
		this.desiredWidth = 0;
		this.desiredHeight = 0;
	}
	
	public DesignQuery withWidth(int desiredWidth) {
		this.desiredWidth = desiredWidth;
		return this;
	}

	public DesignQuery withHeight(int desiredHeight) {
		this.desiredHeight = desiredHeight;
		return this;
	}
	
	public boolean isWidthIgnored() {
		return desiredWidth == 0;
	}
	
	public boolean isHeightIgnored() {
		return desiredHeight == 0;
	}

	public DesignQuery getClone() {
		return new DesignQuery(theme, layer, type).withWidth(desiredWidth).withHeight(desiredHeight);
	}
	
	public Vector<Integer> asCacheKey() {
		Vector<Integer> vector = new Vector<>(4);
		vector.add(layer.ordinal());
		vector.add(type.ordinal());
		vector.add(desiredWidth);
		vector.add(desiredHeight);		
		return vector;
	}

}
