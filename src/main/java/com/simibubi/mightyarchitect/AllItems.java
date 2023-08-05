package com.simibubi.mightyarchitect;

import java.util.Locale;
import java.util.function.Supplier;

import com.simibubi.mightyarchitect.item.ArchitectWandItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public enum AllItems {

	ARCHITECT_WAND(() -> new ArchitectWandItem(standardProperties()));

	private Supplier<Item> factory;
	private Item item;

	public ResourceLocation id;

	private AllItems(Supplier<Item> factory) {
		this.factory = factory;
		this.id = TheMightyArchitect.asResource(name().toLowerCase(Locale.ROOT));
	}

	public static Properties standardProperties() {
		return new Properties();
	}

	public static void registerItems(RegisterEvent event) {
		event.register(ForgeRegistries.ITEMS.getRegistryKey(), helper -> {
			for (AllItems item : values())
				helper.register(item.id, item.get());
		});
	}

	public Item get() {
		if (item == null)
			item = factory.get();
		return item;
	}

	public boolean typeOf(ItemStack stack) {
		return stack.getItem() == item;
	}

	public static void initColorHandlers() {}

}
