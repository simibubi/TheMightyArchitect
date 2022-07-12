package com.simibubi.mightyarchitect.control.phase;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.TemplateBlockAccess;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
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
		remaining.sort((o1, o2) -> Integer.compare(o1.getY(), o2.getY()));
		Minecraft.getInstance().player.chat("/setblock checking permission for 'The Mighty Architect'.");
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
				pos = pos.offset(getModel().getAnchor());
				BlockState state = getModel().getMaterializedSketch().getBlockState(pos);

				if (minecraft.level.getBlockState(pos) == state)
					continue;
				if (!minecraft.level.isUnobstructed(state, pos, CollisionContext.of(minecraft.player)))
					continue;

				String blockstring = state.toString().replaceFirst("Block\\{", "").replaceFirst("\\}", "");
				
				Minecraft.getInstance().player.chat("/setblock " + pos.getX() + " " + pos.getY() + " "
						+ pos.getZ() + " " + blockstring);
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
			List<Component> checking = new LinkedList<>();
			checking.add(event.getMessage());

			while (!checking.isEmpty()) {
				Component iTextComponent = checking.get(0);
				if (iTextComponent instanceof TranslatableComponent) {
					String test = ((TranslatableComponent) iTextComponent).getKey();
					
				TheMightyArchitect.logger.info(test);
					
					if (test.equals("command.unknown.command")) {
						cooldown = 0;
						event.setMessage(new TextComponent(
								ChatFormatting.RED + "You do not have permission to print on this server."));
						return;
					}
					if (test.equals("parsing.int.expected")) {
						approved = true;
						Minecraft.getInstance().player
								.chat("/me is printing a structure created by the Mighty Architect.");
						Minecraft.getInstance().player.chat("/gamerule sendCommandFeedback false");
						Minecraft.getInstance().player.chat("/gamerule logAdminCommands false");
						event.setCanceled(true);
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
	public void render(PoseStack ms, MultiBufferSource buffer) {
	}

	@Override
	public void whenExited() {
		if (approved) {
			Minecraft.getInstance().player.displayClientMessage(new TextComponent(ChatFormatting.GREEN + "Finished Printing, enjoy!"),
					false);
			Minecraft.getInstance().player.chat("/gamerule logAdminCommands true");
			Minecraft.getInstance().player.chat("/gamerule sendCommandFeedback true");
		}
		cooldown = 0;
	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("Please be patient while your building is being transferred.");
	}

}
