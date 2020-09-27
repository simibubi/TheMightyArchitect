package com.simibubi.mightyarchitect.control.design;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.design.partials.Design;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

public class DesignTheme {

	private static final List<Integer> defaultHeightSequence = ImmutableList.of(2, 4);

	private String filePath;
	private String displayName;
	private String designer;
	private DesignPicker designPicker;
	private boolean imported;
	private PaletteDefinition defaultPalette;
	private PaletteDefinition defaultSecondaryPalette;
	private ThemeStatistics statistics;
	private int maxFloorHeight;
	private List<Integer> heightSequence;

	private List<DesignLayer> roomLayers;
	private List<DesignLayer> layers;
	private List<DesignType> types;
	private Map<DesignLayer, Map<DesignType, Set<Design>>> designs;

	public DesignTheme(String displayName, String designer) {
		this.designer = designer;
		this.displayName = displayName;
		this.designPicker = new DesignPicker();
		this.designPicker.setTheme(this);
		imported = false;
		maxFloorHeight = 10;
		heightSequence = defaultHeightSequence;
	}

	public DesignTheme withLayers(DesignLayer... designLayers) {
		layers = ImmutableList.copyOf(designLayers);
		updateRoomLayers();
		return this;
	}

	public DesignTheme withHeightSequence(List<Integer> seq) {
		this.heightSequence = seq;
		return this;
	}

	protected void updateRoomLayers() {
		roomLayers = new ArrayList<>();
		roomLayers.addAll(layers);
		roomLayers.remove(DesignLayer.Roofing);
	}

	public DesignTheme withTypes(DesignType... designtypes) {
		types = ImmutableList.copyOf(designtypes);
		return this;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getDefaultHeightForFloor(int floor) {
		return heightSequence.size() <= floor ? -1 : heightSequence.get(floor);
	}

	public DesignPicker getDesignPicker() {
		return designPicker;
	}

	public List<DesignLayer> getLayers() {
		return layers;
	}

	public List<DesignType> getTypes() {
		return types;
	}

	public boolean isImported() {
		return imported;
	}

	public Set<Design> getDesigns(DesignLayer designLayer, DesignType designType) {
		if (designs == null) {
			initDesigns();
		}

		if (designs.containsKey(designLayer)) {
			Map<DesignType, Set<Design>> typeMap = designs.get(designLayer);

			if (typeMap.containsKey(designType)) {
				return typeMap.get(designType);
			}
		}

		return new HashSet<>();
	}

	protected void initDesigns() {
		designs = DesignResourceLoader.loadDesignsForTheme(this);
		statistics = ThemeStatistics.evaluate(this);
	}

	public ThemeStatistics getStatistics() {
		if (designs == null) {
			initDesigns();
		}
		return statistics;
	}

	public void clearDesigns() {
		designs = null;
	}

	public void setDesigner(String designer) {
		this.designer = designer;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDesigner() {
		return designer;
	}

	public void setLayers(List<DesignLayer> layers) {
		this.layers = layers;
		updateRoomLayers();
	}

	public void setTypes(List<DesignType> types) {
		this.types = types;
	}

	public CompoundNBT asTagCompound() {
		CompoundNBT compound = new CompoundNBT();

		compound.putString("Name", getDisplayName());
		compound.putString("Designer", getDesigner());

		ListNBT layers = new ListNBT();
		ListNBT types = new ListNBT();

		this.layers.forEach(layer -> layers.add(StringNBT.of(layer.name())));
		this.types.forEach(type -> types.add(StringNBT.of(type.name())));

		compound.put("Layers", layers);
		compound.put("Types", types);
		compound.putInt("Maximum Room Height", maxFloorHeight);

		return compound;
	}

	public static DesignTheme fromNBT(CompoundNBT compound) {
		if (compound == null)
			return null;

		DesignTheme theme = new DesignTheme(compound.getString("Name"), compound.getString("Designer"));

		theme.layers = new ArrayList<>();
		theme.types = new ArrayList<>();

		if (compound.contains("Maximum Room Height"))
			theme.maxFloorHeight = compound.getInt("Maximum Room Height");

		compound.getList("Layers", 8).forEach(s -> theme.layers.add(DesignLayer.valueOf(((StringNBT) s).getString())));
		compound.getList("Types", 8).forEach(s -> theme.types.add(DesignType.valueOf(((StringNBT) s).getString())));

		theme.updateRoomLayers();
		return theme;
	}

	public void setImported(boolean imported) {
		this.imported = imported;
	}

	public PaletteDefinition getDefaultPalette() {
		return defaultPalette;
	}

	public void setDefaultPalette(PaletteDefinition defaultPalette) {
		this.defaultPalette = defaultPalette;
	}

	public PaletteDefinition getDefaultSecondaryPalette() {
		return defaultSecondaryPalette;
	}

	public void setDefaultSecondaryPalette(PaletteDefinition defaultSecondaryPalette) {
		this.defaultSecondaryPalette = defaultSecondaryPalette;
	}

	public List<DesignLayer> getRoomLayers() {
		return roomLayers;
	}

	public int getMaxFloorHeight() {
		return maxFloorHeight;
	}

	public void setMaxFloorHeight(int maxFloorHeight) {
		this.maxFloorHeight = maxFloorHeight;
	}

}
