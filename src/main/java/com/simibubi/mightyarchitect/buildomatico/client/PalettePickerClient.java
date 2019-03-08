package com.simibubi.mightyarchitect.buildomatico.client;

import com.simibubi.mightyarchitect.buildomatico.PaletteDefinition;
import com.simibubi.mightyarchitect.buildomatico.model.schematic.Schematic;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Sketch;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@EventBusSubscriber
public class PalettePickerClient {

	private static PalettePickerClient instance;
	private static boolean guiNextTick;
	
	private PaletteDefinition primary;
	private PaletteDefinition secondary;
	private Schematic schematic;
	

	public static void initWithDefault() {
		instance = new PalettePickerClient();
		instance.primary = PaletteDefinition.defaultPalette();
		instance.secondary = PaletteDefinition.defaultPalette();
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (guiNextTick) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiPalettePicker());
			guiNextTick = false;
		}
	}
	
	public static void openGui() {
		guiNextTick = true;
	}
	
	public static boolean isPresent() {
		return instance != null;
	}

	public static void reset() {
		instance = null;
	}

	public static PalettePickerClient getInstance() {
		return instance;
	}

	public static Schematic providePalette(Sketch sketch) {
		if (!isPresent())
			initWithDefault();
		Schematic schematic = new Schematic(sketch, instance.primary, instance.secondary);
		instance.schematic = schematic;
		return schematic;
	}

	public PaletteDefinition getPrimary() {
		return primary;
	}

	public void setPrimary(PaletteDefinition primary) {
		this.primary = primary;
		schematic.swapPrimaryPalette(primary);
	}

	public PaletteDefinition getSecondary() {
		return secondary;
	}

	public void setSecondary(PaletteDefinition secondary) {
		this.secondary = secondary;
		schematic.swapSecondaryPalette(secondary);
	}
	
	public Schematic setSketch(Sketch sketch) {
		schematic.swapSketch(sketch);
		return schematic;
	}
	
	public Schematic getSchematic() {
		return schematic;
	}

}
