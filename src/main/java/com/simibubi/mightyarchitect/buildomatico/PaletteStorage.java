package com.simibubi.mightyarchitect.buildomatico;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

public class PaletteStorage {

	private static Map<String, PaletteDefinition> palettes;
	private static Map<String, PaletteDefinition> resourcePalettes;

	public static PaletteDefinition getRandomPalette() {
		if (palettes == null)
			loadAllPalettes();
		return null;
	}

	public static PaletteDefinition getPalette(String name) {
		if (palettes == null)
			loadAllPalettes();
		if (palettes.containsKey(name))
			return palettes.get(name);
		else
			return resourcePalettes.get(name);
	}

	public static List<String> getPaletteNames() {
		if (palettes == null)
			loadAllPalettes();
		return new ArrayList<>(palettes.keySet());
	}

	public static List<String> getResourcePaletteNames() {
		if (resourcePalettes == null)
			loadAllPalettes();
		return new ArrayList<>(resourcePalettes.keySet());
	}

	public static void exportPalette(PaletteDefinition palette) {
		String folderPath = "palettes";
		
		FilesHelper.createFolderIfMissing(folderPath);
		String filename = FilesHelper.findFirstValidFilename(palette.getName(), folderPath, "json");
		String filepath = folderPath + "/" + filename;
		
		try {
			JsonWriter writer = new JsonWriter(Files.newBufferedWriter(Paths.get(filepath), StandardOpenOption.CREATE));
			writer.setIndent("  ");
			String string = palette.writeToNBT(new NBTTagCompound()).toString();
			Streams.write(new JsonParser().parse(string), writer);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static PaletteDefinition importPalette(Path path) {
		try {
			JsonReader reader = new JsonReader(Files.newBufferedReader(path));
			reader.setLenient(true);
			JsonElement element = Streams.parse(reader);
			return PaletteDefinition.fromNBT(JsonToNBT.getTagFromJson(element.toString()));
		} catch (IOException | NBTException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PaletteDefinition fromResources(String file) {
		try {
			JsonReader reader = new JsonReader(new BufferedReader(
					new InputStreamReader(TheMightyArchitect.class.getClassLoader().getResourceAsStream(file))));
			reader.setLenient(true);
			JsonElement element = Streams.parse(reader);
			return PaletteDefinition.fromNBT(JsonToNBT.getTagFromJson(element.toString()));
		} catch (NBTException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void loadAllPalettes() {
		palettes = new HashMap<>();
		resourcePalettes = new HashMap<>();
		loadResourcePalettes();
		try {
			Files.list(Paths.get("palettes/")).forEach(path -> loadPalette(path));
		} catch (NoSuchFileException e) {
			// no palettes created yet
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadPalette(Path path) {
		PaletteDefinition palette = importPalette(path);
		palettes.put(palette.getName(), palette);
	}

	public static void loadResourcePalettes() {
		int index = 0;
		while (index < 10000) {
			String path = "palettes/p" + index + ".json";
			if (TheMightyArchitect.class.getClassLoader().getResource(path) == null)
				break;
			PaletteDefinition paletteDefinition = fromResources(path);
			resourcePalettes.put(paletteDefinition.getName(), paletteDefinition);			
			index++;
		}
	}

}
