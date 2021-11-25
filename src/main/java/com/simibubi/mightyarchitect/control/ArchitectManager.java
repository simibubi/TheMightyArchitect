package com.simibubi.mightyarchitect.control;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.mightyarchitect.AllPackets;
import com.simibubi.mightyarchitect.MightyClient;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.ThemeStorage;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.control.palette.PaletteStorage;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.control.phase.IArchitectPhase;
import com.simibubi.mightyarchitect.control.phase.IDrawBlockHighlights;
import com.simibubi.mightyarchitect.control.phase.IRenderGameOverlay;
import com.simibubi.mightyarchitect.foundation.utility.FilesHelper;
import com.simibubi.mightyarchitect.foundation.utility.Keyboard;
import com.simibubi.mightyarchitect.gui.ArchitectMenuScreen;
import com.simibubi.mightyarchitect.gui.DesignExporterScreen;
import com.simibubi.mightyarchitect.gui.PalettePickerScreen;
import com.simibubi.mightyarchitect.gui.ScreenHelper;
import com.simibubi.mightyarchitect.gui.TextInputPromptScreen;
import com.simibubi.mightyarchitect.gui.ThemeSettingsScreen;
import com.simibubi.mightyarchitect.networking.InstantPrintPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ArchitectManager {

	private static ArchitectPhases phase = ArchitectPhases.Empty;
	private static Schematic model = new Schematic();
	private static ArchitectMenuScreen menu = new ArchitectMenuScreen();

	public static boolean testRun = false;

	// Commands

	public static void compose() {
		enterPhase(ArchitectPhases.Composing);
	}

	public static void compose(DesignTheme theme) {
		if (getModel().isEmpty())
			getModel().setGroundPlan(new GroundPlan(theme));
		enterPhase(ArchitectPhases.Composing);
	}

	public static void pauseCompose() {
		status(I18n.format("mightyarchitect.manager.composer_paused"));
	}

	public static void unload() {
		if (!model.isEmpty())
			model.getTheme()
				.getDesignPicker()
				.reset();

		enterPhase(ArchitectPhases.Empty);
		resetSchematic();

		if (testRun) {
			testRun = false;
			editTheme(DesignExporter.theme);
			return;
		}

		menu.setVisible(false);
	}

	public static void design() {
		GroundPlan groundPlan = model.getGroundPlan();

		if (groundPlan.isEmpty()) {
			status(I18n.format("mightyarchitect.manager.design_empty"));
			return;
		}

		model.setSketch(groundPlan.theme.getDesignPicker()
			.assembleSketch(groundPlan, model.seed));
		enterPhase(ArchitectPhases.Previewing);
	}

	public static void reAssemble() {
		GroundPlan groundPlan = model.getGroundPlan();
		model.setSketch(groundPlan.theme.getDesignPicker()
			.assembleSketch(groundPlan, model.seed));
		MightyClient.renderer.update();
	}

	public static void createPalette(boolean primary) {
		getModel().startCreatingNewPalette(primary);
		enterPhase(ArchitectPhases.CreatingPalette);
	}

	public static void finishPalette(String name) {
		if (name.isEmpty())
			name = I18n.format("mightyarchitect.manager.palette_name_default");

		PaletteDefinition palette = getModel().getCreatedPalette();
		palette.setName(name);
		PaletteStorage.exportPalette(palette);
		PaletteStorage.loadAllPalettes();

		getModel().applyCreatedPalette();
		status(I18n.format("mightyarchitect.manager.palette_saved"));
		enterPhase(ArchitectPhases.Previewing);
	}

	public static void print() {
		if (getModel().getSketch() == null)
			return;

		Minecraft mc = Minecraft.getInstance();

		if (mc.isSingleplayer()) {
			for (InstantPrintPacket packet : getModel().getPackets())
				AllPackets.channel.sendToServer(packet);
			MightyClient.renderer.setActive(false);
			status(I18n.format("mightyarchitect.manager.print_success"));
			unload();
			return;
		}

		enterPhase(ArchitectPhases.PrintingToMultiplayer);
	}

	public static void writeToFile(String name) {
		if (getModel().getSketch() == null)
			return;

		if (name.isEmpty())
			name = I18n.format("mightyarchitect.manager.build_name_default");

		String folderPath = "schematics";

		FilesHelper.createFolderIfMissing(folderPath);
		String filename = FilesHelper.findFirstValidFilename(name, folderPath, "nbt");
		String filepath = folderPath + "/" + filename;

		OutputStream outputStream = null;
		try {
			outputStream = Files.newOutputStream(Paths.get(filepath), StandardOpenOption.CREATE);
			CompoundNBT nbttagcompound = getModel().writeToTemplate()
				.writeToNBT(new CompoundNBT());
			CompressedStreamTools.writeCompressed(nbttagcompound, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null)
				IOUtils.closeQuietly(outputStream);
		}
		status(I18n.format("mightyarchitect.manager.build_saved_as", filepath));

		BlockPos pos = model.getAnchor()
			.add(((TemplateBlockAccess) model.getMaterializedSketch()).getBounds()
				.getOrigin());
		TranslationTextComponent component = new TranslationTextComponent(
				"mightyarchitect.manager.deploy_schematic_location",
				TextFormatting.BLUE + "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]"
		);
		Minecraft.getInstance().player.sendStatusMessage(component, false);
		unload();
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

		ScreenHelper.open(new PalettePickerScreen());
	}

	public static void pickScanPalette() {
		ScreenHelper.open(new PalettePickerScreen(true));
	}

	public static void manageThemes() {
		enterPhase(ArchitectPhases.ManagingThemes);
	}

	public static void createTheme() {
		TextInputPromptScreen gui = new TextInputPromptScreen(result -> {
			DesignExporter.setTheme(ThemeStorage.createTheme(result));
			ScreenHelper.open(new ThemeSettingsScreen());
		}, result -> {
		});
		gui.setButtonTextConfirm(I18n.format("mightyarchitect.manager.theme_create"));
		gui.setButtonTextAbort(I18n.format("mightyarchitect.manager.theme_cancel"));
		gui.setTitle(I18n.format("mightyarchitect.manager.theme_set_title"));

		ScreenHelper.open(gui);
	}

	public static void editTheme(DesignTheme theme) {
		DesignExporter.setTheme(theme);
		enterPhase(ArchitectPhases.EditingThemes);
	}

	public static void changeExportedDesign() {
		ScreenHelper.open(new DesignExporterScreen());
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
		if (Minecraft.getInstance().world == null) {
			if (!inPhase(ArchitectPhases.Paused) && !model.isEmpty())
				enterPhase(ArchitectPhases.Paused);
			return;
		}

		phase.getPhaseHandler()
			.update();
		menu.onClientTick();

	}

	@SubscribeEvent
	public static void onMouseScrolled(MouseScrollEvent event) {
		if (Minecraft.getInstance().currentScreen != null)
			return;
		if (phase.getPhaseHandler()
			.onScroll((int) Math.signum(event.getScrollDelta())))
			event.setCanceled(true);
	}

	public static void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		if (Minecraft.getInstance().world != null)
			phase.getPhaseHandler()
				.render(ms, buffer);
	}

	@SubscribeEvent
	public static void onClick(MouseInputEvent event) {
		if (Minecraft.getInstance().currentScreen != null)
			return;
		if (event.getAction() != Keyboard.PRESS)
			return;
		phase.getPhaseHandler()
			.onClick(event.getButton());
	}

	@SubscribeEvent
	public static void onKeyTyped(KeyInputEvent event) {
		if (event.getKey() == GLFW.GLFW_KEY_ESCAPE && event.getAction() == Keyboard.PRESS) {
			if (inPhase(ArchitectPhases.Composing) || inPhase(ArchitectPhases.Previewing)) {
				enterPhase(ArchitectPhases.Paused);
				menu.setVisible(false);
			}
			return;
		}
		if (Minecraft.getInstance().currentScreen != null)
			return;
		if (MightyClient.COMPOSE.isPressed()) {
			if (!menu.isFocused())
				openMenu();
			return;
		}

		boolean released = event.getAction() == Keyboard.RELEASE;
		phase.getPhaseHandler()
			.onKey(event.getKey(), released);
	}

	public static void openMenu() {
		menu.updateContents();
		ScreenHelper.open(menu);
		menu.setFocused(true);
		menu.setVisible(true);
		return;
	}

	public static void tickBlockHighlightOutlines() {
		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		if (phaseHandler instanceof IDrawBlockHighlights)
			((IDrawBlockHighlights) phaseHandler).tickHighlightOutlines();
	}

	@SubscribeEvent
	public static void onDrawGameOverlay(RenderGameOverlayEvent.Pre event) {
		if (event.getType() != ElementType.HOTBAR)
			return;

		IArchitectPhase phaseHandler = phase.getPhaseHandler();
		if (phaseHandler instanceof IRenderGameOverlay) {
			((IRenderGameOverlay) phaseHandler).renderGameOverlay(event);
		}

		menu.drawPassive();
		RenderSystem.enableBlend();
		RenderSystem.enableTexture();
	}

	@SubscribeEvent
	public static void onItemRightClick(PlayerInteractEvent.RightClickBlock event) {}

	public static void resetSchematic() {
		model = new Schematic();
	}

}
