package com.simibubi.mightyarchitect.control.design;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.simibubi.mightyarchitect.AllBlocks;
import com.simibubi.mightyarchitect.AllPackets;
import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.block.SliceMarkerBlock;
import com.simibubi.mightyarchitect.control.compose.Cuboid;
import com.simibubi.mightyarchitect.control.design.DesignSlice.DesignSliceTrait;
import com.simibubi.mightyarchitect.control.design.partials.Wall;
import com.simibubi.mightyarchitect.control.design.partials.Wall.ExpandBehaviour;
import com.simibubi.mightyarchitect.control.palette.BlockOrientation;
import com.simibubi.mightyarchitect.control.palette.Palette;
import com.simibubi.mightyarchitect.control.palette.PaletteDefinition;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.foundation.utility.FilesHelper;
import com.simibubi.mightyarchitect.networking.PlaceSignPacket;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class DesignExporter {

	public static PaletteDefinition scanningPalette = PaletteDefinition.defaultPalette();

	public static DesignTheme theme;
	public static DesignType type;
	public static DesignLayer layer;

	public static int designParameter;

	public static boolean changed = true;

	public static String exportDesign(World worldIn, BlockPos anchor) {
		BlockPos layerDefAnchor = anchor;
		boolean found = false;
		for (int range = 1; range < 100 && !found; range++) {
			for (int i = 0; i <= range; i++) {
				if (isMarker(worldIn, anchor.offset(range, 0, i))) {
					layerDefAnchor = anchor.offset(range, 0, i);
					found = true;
					break;
				} else if (isMarker(worldIn, anchor.offset(i, 0, range))) {
					layerDefAnchor = anchor.offset(i, 0, range);
					found = true;
					break;
				}
			}
		}

		if (!found) {
			return "";
		}

		// Collect information
		int height = 0;
		int effectiveHeight = 0;
		for (BlockPos pos = layerDefAnchor; isMarker(worldIn, pos); pos = pos.above()) {
			height++;
			if (DesignSliceTrait.values()[markerValueAt(worldIn, pos)] != DesignSliceTrait.MaskAbove)
				effectiveHeight++;
		}

		if (effectiveHeight != PhaseEditTheme.effectiveHeight) {
			PhaseEditTheme.effectiveHeight = effectiveHeight;
			changed = true;
		}

		BlockPos size = layerDefAnchor.west()
			.subtract(anchor.east())
			.offset(1, height, 1);

		boolean visualizing = PhaseEditTheme.isVisualizing();
		Cuboid bounds = new Cuboid(anchor.east(), size);
		boolean boundsChanged = visualizing && !PhaseEditTheme.selectedDesign.equals(bounds) || changed;

		changed = false;

		if (!visualizing || boundsChanged) {
			PhaseEditTheme.setVisualization(bounds);
			return "Design traits visualized, click again to confirm.";
		}

		PhaseEditTheme.resetVisualization();

		// Assemble nbt
		CompoundNBT compound = new CompoundNBT();
		compound.put("Size", NBTUtil.writeBlockPos(size));

		ListNBT layers = new ListNBT();

		for (int y = 0; y < size.getY(); y++) {
			CompoundNBT layerTag = new CompoundNBT();
			DesignSliceTrait trait = DesignSliceTrait.values()[markerValueAt(worldIn, layerDefAnchor.above(y))];
			layerTag.putString("Trait", trait.name());

			StringBuilder data = new StringBuilder();
			for (int z = 0; z < size.getZ(); z++) {
				for (int x = 0; x < size.getX(); x++) {
					BlockPos pos = anchor.east()
						.offset(x, y, z);
					BlockState blockState = worldIn.getBlockState(pos);
					Palette block = scanningPalette.scan(blockState);

					if (block == null && blockState.getBlock() != Blocks.AIR) {
						Minecraft.getInstance().player.displayClientMessage(
							new StringTextComponent(blockState.getBlock()
							.getDescriptionId() + " @" + pos.getX() + "," + pos.getY() + "," + pos.getZ()
							+ " does not belong to the Scanner Palette"), false);
						return "Export failed";
					}

					data.append(block != null ? block.asChar() : ' ');
				}
				if (z < size.getZ() - 1)
					data.append(",");
			}
			layerTag.putString("Blocks", data.toString());

			StringBuilder orientationStrip = new StringBuilder();
			for (int z = 0; z < size.getZ(); z++) {
				for (int x = 0; x < size.getX(); x++) {
					BlockOrientation orientation = BlockOrientation.byState(worldIn.getBlockState(anchor.east()
						.offset(x, y, z)));
					orientationStrip.append(orientation.asChar());
				}
				if (z < size.getZ() - 1)
					orientationStrip.append(",");
			}
			layerTag.putString("Facing", orientationStrip.toString());

			layers.add(layerTag);
		}

		compound.put("Layers", layers);

		// Additional data
		int data = designParameter;
		switch (type) {
		case ROOF:
			compound.putInt("Roofspan", data);
			break;
		case FLAT_ROOF:
			compound.putInt("Margin", data);
			break;
		case WALL:
			if (data == -1)
				return "Revisit the Design settings.";
			ExpandBehaviour expandBehaviour = Wall.ExpandBehaviour.values()[data];
			if (size.getX() == 1 && expandBehaviour == ExpandBehaviour.MergedRepeat)
				return "Can't merge Walls of length 1. Use 'Repeat' instead.";
			compound.putString("ExpandBehaviour", expandBehaviour.name());
			break;
		case TOWER_FLAT_ROOF:
		case TOWER_ROOF:
		case TOWER:
			compound.putInt("Radius", data);
			break;
		default:
			break;
		}

		// Write nbt to file

		String basePath = "themes";
		FilesHelper.createFolderIfMissing(basePath);
		String themePath = basePath + "/" + theme.getFilePath();
		FilesHelper.createFolderIfMissing(themePath);
		String layerPath = themePath + "/" + layer.getFilePath();
		FilesHelper.createFolderIfMissing(layerPath);
		String typePath = layerPath + "/" + type.getFilePath();
		FilesHelper.createFolderIfMissing(typePath);

		String filename = "";
		String designPath = "";

		BlockPos signPos = anchor.above();
		if (worldIn.getBlockState(signPos)
			.getBlock() == Blocks.SPRUCE_SIGN) {
			SignTileEntity sign = (SignTileEntity) worldIn.getBlockEntity(signPos);
			filename = sign.getMessage(1)
				.getString();
			designPath = typePath + "/" + filename;

		} else {
			int index = 0;
			while (index < 2048) {
				filename = "design" + ((index == 0) ? "" : "_" + index) + ".json";
				designPath = typePath + "/" + filename;
				if (TheMightyArchitect.class.getClassLoader()
					.getResource(designPath) == null && !Files.exists(Paths.get(designPath)))
					break;
				index++;
			}
		}

		AllPackets.channel.sendToServer(new PlaceSignPacket(layer.getDisplayName()
			.substring(0, 1) + ". " + type.getDisplayName(), filename, signPos));
		FilesHelper.saveTagCompoundAsJson(compound, designPath);
		return designPath;
		//

	}

	public static void setTheme(DesignTheme theme) {
		DesignExporter.theme = theme;
		scanningPalette = theme.getDefaultPalette();
		if (layer == null || !theme.getLayers()
			.contains(layer))
			layer = DesignLayer.Regular;
		if (type == null || !theme.getTypes()
			.contains(type))
			type = DesignType.WALL;
		changed = true;
	}

	public static DesignTheme getTheme() {
		return theme;
	}

	private static boolean isMarker(World worldIn, BlockPos pos) {
		return AllBlocks.SLICE_MARKER.typeOf(worldIn.getBlockState(pos));
	}

	private static int markerValueAt(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos)
			.getValue(SliceMarkerBlock.VARIANT)
			.ordinal();
	}

}
