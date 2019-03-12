package com.simibubi.mightyarchitect.buildomatico.client;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.buildomatico.IPickDesigns;
import com.simibubi.mightyarchitect.buildomatico.PaletteDefinition;
import com.simibubi.mightyarchitect.buildomatico.PaletteStorage;
import com.simibubi.mightyarchitect.buildomatico.helpful.FilesHelper;
import com.simibubi.mightyarchitect.buildomatico.model.context.Context;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;
import com.simibubi.mightyarchitect.buildomatico.model.schematic.Schematic;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Sketch;
import com.simibubi.mightyarchitect.gui.GuiOpener;
import com.simibubi.mightyarchitect.networking.PacketInstantPrint;
import com.simibubi.mightyarchitect.networking.PacketSender;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;

public class BuildingProcessClient {

	public static void compose() {
		if (!GroundPlannerClient.isActive()) {
			chatMarkerTop();
			tellrawpacked("Choose the Theme for your Build:");
			for (DesignTheme theme : DesignTheme.values()) {
				tellrawpacked("[", clickable(theme.getDisplayName(), "/compose " + theme.name()), "]");
			}
			chatMarkerBottom();
		} else {
			status("Already composing, use /unload to reset progress.");
		}
	}
	
	public static void compose(DesignTheme theme) {
		if (!GroundPlannerClient.isActive()) {
			PaletteCreatorClient.reset();
			PalettePickerClient.reset();
			SchematicHologram.reset();

			if (GroundPlannerClient.isPresent()) {
				GroundPlannerClient.getInstance().setActive(true);
			} else {
				GroundPlannerClient.startComposing(theme);
				status("Compose your ground plan.");
				
				chatMarkerTop();
				tellrawpacked("The Mighty Architect v" + TheMightyArchitect.VERSION, " - Time for a new Build!");
				tellrawpacked("Once finished, type:        ", clickable("/design", "/design"), " to continue.");
				tellrawpacked("You can always exit with: ", clickable("/unload", "/unload"));
				chatMarkerBottom();
			}
		} else {
			status("Already composing, use /unload to reset progress.");
		}
	}

	public static void pauseCompose() {
		GroundPlannerClient.getInstance().setActive(false);
		status("Composer paused, use /compose to return.");
	}

	public static void unload() {
		GroundPlannerClient.reset();
		PalettePickerClient.reset();
		PaletteCreatorClient.reset();
		SchematicHologram.reset();
		status("Your progress has been reset.");
		
		chatMarkerTop();
		tellrawpacked("Progress has been reset.");
		tellrawpacked("Start a new Build: ", clickable("/compose", "/compose"));
		chatMarkerBottom();
	}
	
	public static void design() {
		if (GroundPlannerClient.isPresent()) {
			GroundPlannerClient.getInstance().setActive(false);
			
			GroundPlan groundPlan = GroundPlannerClient.getInstance().getGroundPlan();
			IPickDesigns designPicker = groundPlan.getTheme().getDesignPicker();
			Sketch sketch = designPicker.assembleSketch(groundPlan);
			BlockPos anchor = GroundPlannerClient.getInstance().getAnchor();
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			
			sketch.setContext(new Context(anchor, player));
			
			Schematic schematic;
			if (PalettePickerClient.isPresent()) {
				schematic = PalettePickerClient.getInstance().setSketch(sketch);
				status("Re-rolled designs.");
			} else {
				schematic = PalettePickerClient.providePalette(sketch);
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
			}
			SchematicHologram.display(schematic);
		} else {
			status("Use /compose to start your build.");
		}
	}

	public static void createPalette(boolean primary) {
		if (PalettePickerClient.isPresent()) {
			Vec3d pos = Minecraft.getMinecraft().player.getPositionVector();
			PaletteCreatorClient.startCreating(new BlockPos(pos),
					PalettePickerClient.getInstance().getSchematic(), primary);
			
			chatMarkerTop();
			tellrawpacked("Creating a new custom palette! Fill the marked positions with your block choices.");
			tellrawpacked("Once finished, click here or type:   ", clickable("/palette save", "/palette save"));
			tellrawpacked("Return to the palette picker with:   ", clickable("/palette", "/palette"), " (Palette will be discarded)");
			chatMarkerBottom();
		}
	}
	
	public static void finishPalette(String name) {
		if (PaletteCreatorClient.isPresent()) {
			if (name.isEmpty())
				name = "My Palette";
			
			boolean primary = PaletteCreatorClient.getInstance().isPrimary();
			PaletteDefinition palette = PaletteCreatorClient.finish();
			palette.setName(name);
			PaletteStorage.exportPalette(palette);
			PaletteStorage.loadAllPalettes();
			
			if (primary) {
				PalettePickerClient.getInstance().setPrimary(palette);
			} else {
				PalettePickerClient.getInstance().setSecondary(palette);
			}
			
			status("Your new palette has been saved.");
		}
	}
	
	public static void print() {
		if (PalettePickerClient.isPresent()) {
			if (!Minecraft.getMinecraft().isSingleplayer() || !Minecraft.getMinecraft().player.isCreative()) {
				status("Print is only available in creative singleplayer.");
				return;
			}
			Schematic schematic = PalettePickerClient.getInstance().getSchematic();
			for (PacketInstantPrint packet : PacketInstantPrint.splitSchematic(schematic)) {
				PacketSender.INSTANCE.sendToServer(packet);				
			}
			SchematicHologram.reset();
			status("Printed result. Use /unload to start over.");
		}
	}

	public static void writeToFile(String name) {
		if (PalettePickerClient.isPresent()) {
			if (name.isEmpty())
				name = "My Build";
			
			String folderPath = "schematics";
			
			FilesHelper.createFolderIfMissing(folderPath);
			String filename = FilesHelper.findFirstValidFilename(name, folderPath, "nbt");
			String filepath = folderPath + "/" + filename;
			
			OutputStream outputStream = null;
			try
            {
				outputStream = Files.newOutputStream(Paths.get(filepath), StandardOpenOption.CREATE);
                Schematic schematic = PalettePickerClient.getInstance().getSchematic();
				NBTTagCompound nbttagcompound = schematic.writeToTemplate().writeToNBT(new NBTTagCompound());
                CompressedStreamTools.writeCompressed(nbttagcompound, outputStream);
            } catch (IOException e) {
				e.printStackTrace();
			}
            finally
            {
            	if (outputStream != null)
            		IOUtils.closeQuietly(outputStream);
            }
			status("Saved as " + filepath);
		}
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
		return "{\"text\":\"" + message + "\",\"color\":\"green\",\"underline\":\"true\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + command + "\"}}";
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
		if (PaletteCreatorClient.isPresent()) {
			// reset palette creator progress
			PaletteCreatorClient.reset();
			PalettePickerClient picker = PalettePickerClient.getInstance();
			picker.reapplyCurrentPalettes();
			SchematicHologram.getInstance().schematicChanged();
		}
		if (PalettePickerClient.isPresent()) {
			GuiOpener.open(new GuiPalettePicker());			
		}		
	}

}
