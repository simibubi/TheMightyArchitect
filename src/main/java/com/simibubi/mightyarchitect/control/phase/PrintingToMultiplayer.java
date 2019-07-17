package com.simibubi.mightyarchitect.control.phase;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.TemplateBlockAccess;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class PrintingToMultiplayer extends PhaseBase {

	static List<BlockPos> remaining;
	static int cooldown;
	static boolean approved;

	@Override
	public void whenEntered() {
		remaining = new LinkedList<>(((TemplateBlockAccess) getModel().getMaterializedSketch()).getAllPositions());
		Minecraft.getInstance().player.sendChatMessage("/setblock checking permission for 'The Mighty Architect'.");
		cooldown = 500;
		approved = false;
	}

	@Override
	public void update() {
		if (cooldown > 0 && !approved) {
			cooldown--;
			return;
		}
		if (cooldown == 0) {
			ArchitectManager.enterPhase(ArchitectPhases.Previewing);
			return;
		}
		
		for (int i = 0; i < 10; i++) {
			if (!remaining.isEmpty()) {
				BlockPos pos = remaining.get(0);
				remaining.remove(0);
				pos = pos.add(getModel().getAnchor());
				BlockState state = getModel().getMaterializedSketch().getBlockState(pos);
				
				if (!minecraft.world.func_217350_a(state, pos, ISelectionContext.forEntity(minecraft.player)))
					continue;
				
				Minecraft.getInstance().player.sendChatMessage("/setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
				+ " " + state.getBlock().getRegistryName());
			} else {
				ArchitectManager.unload();
				break;
			}			
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public static void onCommandFeedback(ClientChatReceivedEvent event) {
		if (event.getMessage() == null)
			return;

		if (cooldown > 0) {
			List<ITextComponent> checking = new LinkedList<>();
			checking.add(event.getMessage());

			while (!checking.isEmpty()) {
				ITextComponent iTextComponent = checking.get(0);
				if (iTextComponent instanceof TranslationTextComponent) {
					String test = ((TranslationTextComponent) iTextComponent).getKey();
					if (test.equals("commands.generic.permission")) {
						cooldown = 0;
						return;
					}
					if (test.equals("commands.generic.num.invalid")) {
						approved = true;
						Minecraft.getInstance().player
								.sendChatMessage("/me is printing a structure created by the Mighty Architect.");
						Minecraft.getInstance().player.sendChatMessage("/gamerule sendCommandFeedback false");
						Minecraft.getInstance().player.sendChatMessage("/gamerule logAdminCommands false");
						return;
					}
				} else {
					checking.addAll(iTextComponent.getSiblings());
				}
				checking.remove(iTextComponent);
			}
		}
	}

	@Override
	public void render() {
	}

	@Override
	public void whenExited() {
		if (approved) {
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent("Finished Printing, enjoy!"),
					false);
			Minecraft.getInstance().player.sendChatMessage("/gamerule logAdminCommands true");
			Minecraft.getInstance().player.sendChatMessage("/gamerule sendCommandFeedback true");
		}
		cooldown = 0;
	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("Please be patient while your building is being transferred.");
	}

}
