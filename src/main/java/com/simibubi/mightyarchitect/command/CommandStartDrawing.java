package com.simibubi.mightyarchitect.command;

import com.simibubi.mightyarchitect.buildomatico.client.ArchitectController;
import com.simibubi.mightyarchitect.buildomatico.model.sketch.DesignTheme;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;

public class CommandStartDrawing extends CommandBase implements IClientCommand {

	@Override
	public String getName() {
		return "compose";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/compose";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			if (args.length == 0) {
				ArchitectController.compose();
			} else {
				for (DesignTheme theme : DesignTheme.values()) {
					if (args[0].equals(theme.name())) {
						ArchitectController.compose(theme);
						return;
					}
				}
				throw new CommandException("There is no theme named: " + args[0]);
			}
		}
		
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
		return false;
	}

}
