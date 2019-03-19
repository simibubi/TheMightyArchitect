package com.simibubi.mightyarchitect.control;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.control.compose.GroundPlan;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.helpful.FilesHelper;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.control.palette.PaletteStorage;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.control.phase.IArchitectPhase;
import com.simibubi.mightyarchitect.control.phase.IDrawBlockHighlights;
import com.simibubi.mightyarchitect.control.phase.IListenForBlockEvents;
import com.simibubi.mightyarchitect.gui.GuiOpener;
import com.simibubi.mightyarchitect.gui.GuiPalettePicker;
import com.simibubi.mightyarchitect.networking.PacketInstantPrint;
import com.simibubi.mightyarchitect.networking.PacketSender;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@EventBusSubscriber
public class ArchitectManager {

	private static IArchitectPhase phase = ArchitectPhases.Empty.getPhaseHandler();
	private static Schematic model = new Schematic();
	
	// Commands
	
	public static void compose() {
		if (inPhase(ArchitectPhases.Composing)) {
			status("Already composing, use /unload to reset progress.");
			return;
		}

		if (getModel().getGroundPlan() == null) {
			chatMarkerTop();
			tellrawpacked("Choose the Theme for your Build:");
			for (DesignTheme theme : DesignTheme.values()) {
				tellrawpacked("[", clickable(theme.getDisplayName(), "/compose " + theme.name()), "]");
			}
			chatMarkerBottom();
			return;
		}

		enterPhase(ArchitectPhases.Composing);
	}

	public static void compose(DesignTheme theme) {
		if (inPhase(ArchitectPhases.Composing)) {
			status("Already composing, use /unload to reset progress.");
			return;
		}

		if (getModel().getGroundPlan() == null) {
			getModel().setGroundPlan(new GroundPlan(theme));

			chatMarkerTop();
			tellrawpacked("The Mighty Architect v" + TheMightyArchitect.VERSION, " - Time for a new Build!");
			tellrawpacked("Once finished, type:        ", clickable("/design", "/design"), " to continue.");
			tellrawpacked("You can always exit with: ", clickable("/unload", "/unload"));
			chatMarkerBottom();
		}

		enterPhase(ArchitectPhases.Composing);
	}

	public static void pauseCompose() {
		// TODO
		status("Composer paused, use /compose to return.");
	}

	public static void unload() {
		enterPhase(ArchitectPhases.Empty);
		resetSchematic();

		chatMarkerTop();
		tellrawpacked("Progress has been reset.");
		tellrawpacked("Start a new Build: ", clickable("/compose", "/compose"));
		chatMarkerBottom();
	}

	public static void design() {
		GroundPlan groundPlan = model.getGroundPlan();

		if (groundPlan == null) {
			status("Use /compose to start your build.");
			return;
		}

		if (model.getSketch() == null) {
			status("Ground plan has been decorated.");

			chatMarkerTop();
			tellrawpacked("The walls have been decorated!");
			tellrawpacked("Pick the materials for your build:        ", clickable("/palette", "/palette"));
			tellrawpacked("Not a fan? You can always re-roll with:   ", clickable("/design", "/design"));
			tellrawpacked("For further tweaking on your Ground plan: ", clickable("/compose", "/compose"));
			tellrawpacked("");
			tellrawpacked("Save your Build to a file:                ", clickable("/saveSchematic", "/saveSchematic"));
			if (Minecraft.getMinecraft().isSingleplayer())
				tellrawpacked("Materialize your Build into the World:    ", clickable("/print", "/print"));
			chatMarkerBottom();

		} else {
			status("Re-rolled designs.");
		}

		model.setSketch(groundPlan.theme.getDesignPicker().assembleSketch(groundPlan));
		enterPhase(ArchitectPhases.Previewing);
	}

	public static void createPalette(boolean primary) {
		getModel().startCreatingNewPalette(primary);
		enterPhase(ArchitectPhases.CreatingPalette);

		chatMarkerTop();
		tellrawpacked("Creating a new custom palette! Fill the marked positions with your block choices.");
		tellrawpacked("Once finished, click here or type:   ", clickable("/palette save", "/palette save"));
		tellrawpacked("Return to the palette picker with:   ", clickable("/palette", "/palette"),
				" (Palette will be discarded)");
		chatMarkerBottom();
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

		if (!Minecraft.getMinecraft().isSingleplayer() || !Minecraft.getMinecraft().player.isCreative()) {
			status("Print is only available in creative singleplayer.");
			return;
		}

		for (PacketInstantPrint packet : getModel().getPackets()) {
			PacketSender.INSTANCE.sendToServer(packet);
		}

		SchematicHologram.reset();
		status("Printed result. Use /unload to start over.");
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
			NBTTagCompound nbttagcompound = getModel().writeToTemplate().writeToNBT(new NBTTagCompound());
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
		Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(message), true);
	}

	public static void chatMarkerTop() {
		tellrawpacked("+-----------------------------------------------+");
	}

	public static void chatMarkerBottom() {
		tellrawpacked("+-----------------------------------------------+");
	}

	public static String clickable(String message, String command) {
		return "{\"text\":\"" + message
				+ "\",\"color\":\"green\",\"underline\":\"true\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\""
				+ command + "\"}}";
	}

	public static void tellrawpacked(String... strings) {
		String result = "[";
		for (String string : strings) {
			boolean plaintext = !string.startsWith("{");
			if (plaintext) {
				result += "\"" + string + "\",";
			} else {
				result += string + ",";
			}
		}
		tellraw(result + "\"\"]");
	}

	public static void tellraw(String json) {
		ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(json);
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		try {
			player.sendMessage(TextComponentUtils.processComponent(player, itextcomponent, player));
		} catch (CommandException e) {
			e.printStackTrace();
		}
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
	
	// Phases
	
	public static void enterPhase(ArchitectPhases newPhase) {
		phase.whenExited();
		phase = newPhase.getPhaseHandler();
		phase.whenEntered();
	}
	
	public static Schematic getModel() {
		return model;
	}
	
	public static boolean inPhase(ArchitectPhases phaseIn) {
		return phase == phaseIn.getPhaseHandler();
	}
	
	// Events
	
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (Minecraft.getMinecraft().world != null) {
			phase.update();			
		}
	}

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		if (Minecraft.getMinecraft().world != null) {
			phase.render();
		}
	}

	@SubscribeEvent
	public static void onRightClick(MouseEvent event) {
		if (event.isButtonstate() && Mouse.isButtonDown(event.getButton())) {
			phase.onClick(event.getButton());
		}
	}
	
	@SubscribeEvent
	public static void onKeyTyped(KeyInputEvent event) {
		if (!Keyboard.getEventKeyState())
			return;
		
		phase.onKey(Keyboard.getEventKey());
	}
	

	@SubscribeEvent
	public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
		if (phase instanceof IListenForBlockEvents) {
			((IListenForBlockEvents) phase).onBlockPlaced(event);
		}
	}
	
	@SubscribeEvent
	public static void onBlockBroken(BlockEvent.BreakEvent event) {
		if (phase instanceof IListenForBlockEvents) {
			((IListenForBlockEvents) phase).onBlockBroken(event);
		}		
	}
	
	@SubscribeEvent
	public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
		if (phase instanceof IDrawBlockHighlights) {
			((IDrawBlockHighlights) phase).onBlockHighlight(event);
		}
	}
	
	public static void resetSchematic() {
		model = new Schematic();
	}
	
}
