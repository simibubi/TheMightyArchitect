package com.simibubi.mightyarchitect.control.helpful;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.simibubi.mightyarchitect.TheMightyArchitect;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

public class FilesHelper {

	public static void createFolderIfMissing(String name) {
		if (!Files.isDirectory(Paths.get(name))) {
			try {
				Files.createDirectory(Paths.get(name));
			} catch (IOException e) {
				TheMightyArchitect.logger.warn("Could not create Folder: " + name);
			}
		}
	}

	public static String findFirstValidFilename(String name, String folderPath, String extension) {
		int index = 0;
		String filename;
		String filepath;
		do {
			filename = name.toLowerCase().replace(' ', '_') + ((index == 0) ? "" : "_" + index) + "." + extension;
			index++;
			filepath = folderPath + "/" + filename;
		} while (Files.exists(Paths.get(filepath)));
		return filename;
	}

	public static boolean saveTagCompoundAsJson(NBTTagCompound compound, String path) {
		try {
			JsonWriter writer = new JsonWriter(Files.newBufferedWriter(Paths.get(path), StandardOpenOption.CREATE));
			writer.setIndent("  ");
			Streams.write(new JsonParser().parse(compound.toString()), writer);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static NBTTagCompound loadJsonResourceAsNBT(String filepath) {
		try {
			JsonReader reader = new JsonReader(new BufferedReader(
					new InputStreamReader(TheMightyArchitect.class.getClassLoader().getResourceAsStream(filepath))));
			reader.setLenient(true);
			JsonElement element = Streams.parse(reader);
			return JsonToNBT.getTagFromJson(element.toString());
		} catch (NBTException e) {
			e.printStackTrace();
		}
		return null;
	}

}
