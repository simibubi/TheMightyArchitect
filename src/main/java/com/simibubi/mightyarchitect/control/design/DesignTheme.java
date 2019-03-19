package com.simibubi.mightyarchitect.control.design;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.design.partials.Design;

public enum DesignTheme {

	Medieval("medieval", "Medieval", 
			withLayers(
					DesignLayer.Foundation, 
					DesignLayer.Regular, 
					DesignLayer.Open, 
					DesignLayer.Independent
					),
			withTypes(
					DesignType.WALL, 
					DesignType.CORNER,
					DesignType.ROOF,
					DesignType.TOWER,
					DesignType.FACADE,
					DesignType.FLAT_ROOF,
					DesignType.TOWER_FLAT_ROOF,
					DesignType.TOWER_ROOF
					), 
			new StandardDesignPicker()
			),

	Modern("modern", "Modern", 
			withLayers(
					DesignLayer.Regular, 
					DesignLayer.Independent
					),
			withTypes(
					DesignType.WALL, 
					DesignType.CORNER,
					DesignType.TRIM,
					DesignType.ROOF
					), 
			new StandardDesignPicker()
			);
	

	private String filePath;
	private String displayName;
	private IPickDesigns designPicker;

	private List<DesignLayer> layers;
	private List<DesignType> types;
	private Map<DesignLayer, Map<DesignType, Set<Design>>> designs;

	private DesignTheme(String filePath, String displayName, List<DesignLayer> layers, List<DesignType> types,
			IPickDesigns designPicker) {
		this.filePath = filePath;
		this.displayName = displayName;
		this.designPicker = designPicker;
		this.layers = layers;
		this.types = types;
		this.designPicker.setTheme(this);
	}

	private static List<DesignLayer> withLayers(DesignLayer... designLayers) {
		return ImmutableList.copyOf(designLayers);
	}

	private static List<DesignType> withTypes(DesignType... designtypes) {
		return ImmutableList.copyOf(designtypes);
	}

	public String getFilePath() {
		return filePath;
	}

	public String getDisplayName() {
		return displayName;
	}

	public IPickDesigns getDesignPicker() {
		return designPicker;
	}

	public List<DesignLayer> getLayers() {
		return layers;
	}

	public List<DesignType> getTypes() {
		return types;
	}

	public Set<Design> getDesigns(DesignLayer designLayer, DesignType designType) {
		if (designs == null) {
			designs = DesignResourceLoader.loadDesignsForTheme(this);
		}

		if (!getLayers().contains(designLayer))
			fail(getDisplayName() + " theme does not support " + designLayer.getDisplayName() + " layers.");
		if (!getTypes().contains(designType))
			fail(getDisplayName() + " theme does not support the type " + designLayer.getDisplayName() + ".");

		if (designs.containsKey(designLayer)) {
			Map<DesignType, Set<Design>> typeMap = designs.get(designLayer);

			if (typeMap.containsKey(designType)) {
				return typeMap.get(designType);
			}
		}

		return new HashSet<>();
	}

	private void fail(String message) {
		throw new IllegalArgumentException(message);
	}

	public void clearDesigns() {
		designs = null;
	}

}
