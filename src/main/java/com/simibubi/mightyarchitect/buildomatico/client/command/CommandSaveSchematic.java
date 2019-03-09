package com.simibubi.mightyarchitect.buildomatico.client.command;

import com.simibubi.mightyarchitect.buildomatico.client.BuildingProcessClient;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;

public class CommandSaveSchematic extends CommandBase implements IClientCommand {

	@Override
	public String getName() {
		return "saveSchematic";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/saveSchematic <name>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			if (args.length > 0) {
				String name = "";
				for (int i = 0; i < args.length; i++) {
					name += args[i] + ((i == args.length - 1)? "" : " ");
				}
				BuildingProcessClient.writeToFile(name);
			} else {
				throw new CommandException("Please specify a name: " + getUsage(sender));
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
