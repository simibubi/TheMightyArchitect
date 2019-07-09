package com.simibubi.mightyarchitect.control;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.ThemeStorage;
import com.simibubi.mightyarchitect.control.helpful.FilesHelper;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.control.palette.PaletteStorage;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.control.phase.IArchitectPhase;
import com.simibubi.mightyarchitect.control.phase.IDrawBlockHighlights;
import com.simibubi.mightyarchitect.control.phase.IListenForBlockEvents;
import com.simibubi.mightyarchitect.control.phase.IRenderGameOverlay;
import com.simibubi.mightyarchitect.gui.GuiArchitectMenu;
import com.simibubi.mightyarchitect.gui.GuiDesignExporter;
import com.simibubi.mightyarchitect.gui.GuiEditTheme;
import com.simibubi.mightyarchitect.gui.GuiOpener;
import com.simibubi.mightyarchitect.gui.GuiPalettePicker;
import com.simibubi.mightyarchitect.gui.GuiTextPrompt;
import com.simibubi.mightyarchitect.gui.Keyboard;
import com.simibubi.mightyarchitect.networking.PacketInstantPrint;
import com.simibubi.mightyarchitect.networking.Packets;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ArchitectManager {

	private static ArchitectPhases phase = ArchitectPhases.Empty;
	private static Schematic model = new Schematic();
	private static GuiArchitectMenu menu = new GuiArchitectMenu();

	public static boolean testRun = false;

	// Commands

	public static void compose() {
		enterPhase(ArchitectPhases.Composing);
	}

	public static void compose(DesignTheme theme) {
		if (getModel().getGroundPlan() == null) {
			getModel().setGroundPlan(new GroundPlan(theme));
		}
		enterPhase(ArchitectPhases.Composing);
	}

	public static void pauseCompose() {
		status("Composer paused, use /compose to return.");
	}

	public static void unload() {
		model.getTheme().getDesignPicker().reset();
		enterPhase(ArchitectPhases.Empty);
		resetSchematic();

		if (testRun) {
			testRun = false;
			editTheme(DesignExporter.theme);
		} else {
			menu.setVisible(false);
		}
	}

	public static void design() {
		GroundPlan groundPlan = model.getGroundPlan();

		if (groundPlan.isEmpty()) {
			status("Draw some rooms before going to the next step!");
			return;
		}

		model.setSketch(groundPlan.theme.getDesignPicker().assembleSketch(groundPlan));
		enterPhase(ArchitectPhases.Previewing);
	}

	public static void reAssemble() {
		GroundPlan groundPlan = model.getGroundPlan();
		model.setSketch(groundPlan.theme.getDesignPicker().assembleSketch(groundPlan));
		SchematicHologram.getInstance().schematicChanged();
	}

	public static void createPalette(boolean primary) {
		getModel().startCreatingNewPalette(primary);
		enterPhase(ArchitectPhases.CreatingPalette);
	}

	public static void finishPalette(String name) {
		if (name.isEmpty())
			name = "My Palette";

		PaletteDefinition palette = getModel().getCreatedPalette();
		palette.setName(name);
		PaletteStorage.exportPalette(palette);
		PaletteStorage.loadAllPalettes();

		getModel().applyCreatedPalette();
		status("Your new palette has been saved.");
		enterPhase(ArchitectPhases.Previewing);
	}

	public static void print() {
		if (getModel().getSketch() == null)
			return;

		Minecraft mc = Minecraft.getInstance();

		if (mc.isSingleplayer()) {
			for (PacketInstantPrint packet : getModel().getPackets()) {
				Packets.channel.sendToServer(packet);
			}
			SchematicHologram.reset();
			status("Printed result into world.");
			unload();

		} else {
			enterPhase(ArchitectPhases.PrintingToMultiplayer);
		}
		
	}

	public static void writeToFile(String name) {
		if (getModel().getSketch() == null)
			return;

		if (name.isEmpty())
			name = "My Build";

		String folderPath = "schematics";

		FilesHelper.createFolderIfMissing(folderPath);
		String filename = FilesHelper.findFirstValidFilename(name, folderPath, "nbt");
		String filepath = folderPath + "/" + filename;

		OutputStream outputStream = null;
		try {
			outputStream = Files.newOutputStream(Paths.get(filepath), StandardOpenOption.CREATE);
			CompoundNBT nbttagcompound = getModel().writeToTemplate().writeToNBT(new CompoundNBT());
			CompressedStreamTools.writeCompressed(nbttagcompound, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null)
				IOUtils.closeQuietly(outputStream);
		}
		status("Saved as " + filepath);
	}

	public static void status(String message) {
		Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(message), true);
	}

	public static void pickPalette() {
		if (getModel().getSketch() == null)
			return;

		if (inPhase(ArchitectPhases.CreatingPalette)) {
			getModel().stopPalettePreview();
			enterPhase(ArchitectPhases.Previewing);
		}

		GuiOpener.open(new GuiPalettePicker());
	}

	public static void pickScanPalette() {
		GuiOpener.open(new GuiPalettePicker(true));
	}

	public static void manageThemes() {
		enterPhase(ArchitectPhases.ManagingThemes);
	}

	public static void createTheme() {
		GuiTextPrompt gui = new GuiTextPrompt(result -> {
			DesignExporter.setTheme(ThemeStorage.createTheme(result));
			GuiOpener.open(new GuiEditTheme());
		}, result -> {
		});
		gui.setButtonTextConfirm("Create");
		gui.setButtonTextAbort("Cancel");
		gui.setTitle("Enter a name for your Theme:");

		GuiOpener.open(gui);
	}

	public static void editTheme(DesignTheme theme) {
		DesignExporter.setTheme(theme);
		enterPhase(ArchitectPhases.EditingThemes);
	}

	public static void changeExportedDesign() {
		GuiOpener.open(new GuiDesignExporter());
	}

	// Phases

	public static boolean inPhase(ArchitectPhases phase) {
		return ArchitectManager.phase == phase;
	}

	public static void enterPhase(ArchitectPhases newPhase) {
		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		phaseHandler.whenExited();
		phaseHandler = newPhase.getPhaseHandler();
		phaseHandler.whenEntered();
		phase = newPhase;
		menu.updateContents();
	}

	public static Schematic getModel() {
		return model;
	}

	public static ArchitectPhases getPhase() {
		return phase;
	}

	// Events

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (Minecraft.getInstance().world != null) {
			phase.getPhaseHandler().update();
		}
		menu.onClientTick();

	}

	public static boolean onMouseScrolled(int delta) {
		return phase.getPhaseHandler().onScroll(delta);
	}

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		if (Minecraft.getInstance().world != null) {
			phase.getPhaseHandler().render();
		}
	}

	@SubscribeEvent
	public static void onClick(MouseInputEvent event) {
		if (event.getAction() != Keyboard.PRESS)
			return;
		phase.getPhaseHandler().onClick(event.getButton());
	}

	@SubscribeEvent
	public static void onKeyTyped(KeyInputEvent event) {
		if (TheMightyArchitect.COMPOSE.isPressed()) {
			if (menu.isFocused())
				return;

			menu.updateContents();
			GuiOpener.open(menu);
			menu.setFocused(true);
			menu.setVisible(true);
			return;
		}

		boolean released = event.getAction() == Keyboard.RELEASE;
		phase.getPhaseHandler().onKey(event.getKey(), released);
	}

	@SubscribeEvent
	public static void onBlockPlaced(EntityPlaceEvent event) {
		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		if (phaseHandler instanceof IListenForBlockEvents) {
			((IListenForBlockEvents) phaseHandler).onBlockPlaced(event.getPos(), event.getPlacedBlock());
		}

	}

	@SubscribeEvent
	public static void onBlockBroken(PlayerInteractEvent.LeftClickBlock event) {
		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		if (phaseHandler instanceof IListenForBlockEvents) {
			((IListenForBlockEvents) phaseHandler).onBlockBroken(event.getPos());
		}
	}

	@SubscribeEvent
	public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		if (phaseHandler instanceof IDrawBlockHighlights) {
			((IDrawBlockHighlights) phaseHandler).onBlockHighlight(event);
		}
	}

	@SubscribeEvent
	public static void onDrawGameOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.HOTBAR)
			return;

		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		if (phaseHandler instanceof IRenderGameOverlay) {
			((IRenderGameOverlay) phaseHandler).renderGameOverlay(event);
		}

		menu.drawPassive();
	}

	@SubscribeEvent
	public static void onItemRightClick(PlayerInteractEvent.RightClickBlock event) {
	}

	public static void resetSchematic() {
		model = new Schematic();
	}

}
