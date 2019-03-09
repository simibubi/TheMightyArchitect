package com.simibubi.mightyarchitect.buildomatico;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Design;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Design.Style;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Design.Type;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.Layer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DesignStorage {

	public static Map<Style, Map<Type, Set<Design>>> designMatrix;
	
	public static Design findRandomFor(Style weight, Type type, int height) {
		return findRandomFor(weight, type, 0, height);
	}
	
	public static Design findRandomFor(Style weight, Type type, int width, int height) {
		if (designMatrix == null)
			loadAllDesigns();
		
		List<Design> remainingDesigns = new ArrayList<>(designMatrix.get(weight).get(type));
		Random dice = new Random();
		while (!remainingDesigns.isEmpty()) {
			int index = dice.nextInt(remainingDesigns.size());
			Design chosen = remainingDesigns.get(index);
			if (height == 0 || chosen.fitsVertically(height)) {
				if (width == 0 || chosen.fitsHorizontally(width)) {
					return chosen;
				}
			}
			remainingDesigns.remove(index);
		}
		return null;
	}
	
	public static String exportDesign(World worldIn, EntityPlayer playerIn, BlockPos anchor, IBlockState blockState) {
		Design.Type type = Design.Type.values()[metaAt(worldIn, anchor)];
		BlockPos anchor2 = anchor;
		boolean found = false;
		for (int range = 1; range < 100 && !found; range++) {
			for (int i = 0; i < range; i++) {
				if (isWool(worldIn, anchor.add(range, 0, i))) {
					anchor2 = anchor.add(range, 0, i);
					found = true;
					break;
				} else if (isWool(worldIn, anchor.add(i, 0, range))) {
					anchor2 = anchor.add(i, 0, range);
					found = true;
					break;
				}
			}
		}
		if (found) {
			int height = 0;
			for (BlockPos pos = anchor2; isWool(worldIn, pos); pos = pos.up())
				height++;

			// Valid schematic
			BlockPos size = anchor2.west().subtract(anchor.east()).add(1, height, 1);
			int index = 0;
			String filename;
			String filepath;
			do {
				filename = "d" + index++ + ".design";
				filepath = "designs/" + filename;
			} while (Files.exists(Paths.get(filepath)));
			try {
				BufferedWriter writer = Files.newBufferedWriter(Paths.get(filepath), StandardOpenOption.CREATE);

				// DESIGN size and type
				writer.write(String.format("DESIGN %dx%dx%d %s", size.getX(), size.getY(), size.getZ(), type.name()));

				// WEIGHT
				if (isWool(worldIn, anchor.up()))
					writer.write(" " + Design.Style.values()[metaAt(worldIn, anchor.up())].name());
				else
					writer.write(" " + Design.Style.ANY.name());
				
				// ZSHIFT/RADIUS parameter
				if (isWool(worldIn, anchor.up(2)))
					writer.write(" " + metaAt(worldIn, anchor.up(2)));
				
				writer.newLine();
				writer.flush();

				// LAYERS
				Map<IBlockState, Palette> scanMap = new HashMap<>();
				for (Map.Entry<Palette, IBlockState> entry : PaletteDefinition.defaultPalette().getDefinition()
						.entrySet()) {
					scanMap.put(entry.getValue(), entry.getKey());
				}

				for (int y = 0; y < size.getY(); y++) {
					Layer.Type layertype = Layer.Type.values()[metaAt(worldIn, anchor2.up(y))];
					writer.write("LAYER " + layertype.name());
					writer.newLine();
					for (int z = 0; z < size.getZ(); z++) {
						for (int x = 0; x < size.getX(); x++) {
							Palette block = scanMap.get(worldIn.getBlockState(anchor.east().add(x, y, z)));
							if (block != null) {
								writer.write(block.asChar());
							} else {
								writer.write(' ');
							}
						}
						if (z < size.getZ() - 1) writer.write(",");
					}
					writer.newLine();
				}

				writer.write("THATS IT");
				writer.close();
				return filename;
			} catch (IOException e) {
				e.printStackTrace();
				
			}

			//
		}
		return "";
	}

	private static boolean isWool(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos).getBlock() == Blocks.WOOL;
	}

	private static int metaAt(World worldIn, BlockPos pos) {
		return Blocks.WOOL.getMetaFromState(worldIn.getBlockState(pos));
	}

	public static Design importDesign(String path) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(TheMightyArchitect.class.getClassLoader().getResourceAsStream(path)));
		List<String> def = new ArrayList<String>(reader.lines().collect(Collectors.toList()));
		return Design.fromDefinition(def);
	}
	
	private static void loadAllDesigns() {
		designMatrix = new HashMap<>();
		for (Style weight : Style.values())
			designMatrix.put(weight, new HashMap<>());
		
        int index = 0;
		while (index < 10000) {
			String path = "designs/d" + index + ".design";
			if (TheMightyArchitect.class.getClassLoader().getResource(path) == null)
				break;
			loadDesign(path);
			index++;
		}
	}
	
	private static void loadDesign(String path) {
		Design design = importDesign(path);
		Type type = design.getType();
		Map<Type, Set<Design>> map = designMatrix.get(design.style);
		if (!map.containsKey(type))
			map.put(type, new HashSet<>());
		map.get(type).add(design);
	}
	

}
