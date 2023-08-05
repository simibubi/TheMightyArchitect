package com.simibubi.mightyarchitect.control.phase;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.TemplateBlockAccess;
import com.simibubi.mightyarchitect.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
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

		minecraft.player.commandSigned("setblock checking permission for 'The Mighty Architect'.", null);
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
				BlockState state = getModel().getMaterializedSketch()
					.getBlockState(pos);

				if (minecraft.level.getBlockState(pos) == state)
					continue;
				if (!minecraft.level.isUnobstructed(state, pos, CollisionContext.of(minecraft.player)))
					continue;

				String blockstring = state.toString()
					.replaceFirst("Block\\{", "")
					.replaceFirst("\\}", "");

				Minecraft.getInstance().player.commandSigned(
					"setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + blockstring, null);
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
				if (iTextComponent instanceof MutableComponent mc
					&& mc.getContents() instanceof TranslatableContents tc) {
					String test = tc.getKey();
					TheMightyArchitect.logger.info(test);

					if (test.equals("command.unknown.command")) {
						cooldown = 0;
						event.setMessage(Component
							.literal(ChatFormatting.RED + "You do not have permission to print on this server."));
						return;
					}
					if (test.equals("parsing.int.expected")) {
						approved = true;
						LocalPlayer player = Minecraft.getInstance().player;
						player.commandSigned("gamerule sendCommandFeedback false", null);
						player.commandSigned("gamerule logAdminCommands false", null);
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
	public void whenExited() {
		if (approved) {
			LocalPlayer player = Minecraft.getInstance().player;
			Lang.text(ChatFormatting.GREEN + "Finished Printing, enjoy!")
				.sendChat(player);
			player.commandSigned("gamerule logAdminCommands true", null);
			player.commandSigned("gamerule sendCommandFeedback true", null);
		}
		cooldown = 0;
	}

	@Override
	public List<String> getToolTip() {
		return ImmutableList.of("Please be patient while your building is being transferred.");
	}

}
