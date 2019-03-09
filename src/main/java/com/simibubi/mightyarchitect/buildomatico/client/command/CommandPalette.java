package com.simibubi.mightyarchitect.buildomatico.client.command;

import com.simibubi.mightyarchitect.buildomatico.client.BuildingProcessClient;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;

public class CommandPalette extends CommandBase implements IClientCommand {

	@Override
	public String getName() {
		return "palette";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/palette create | save <name>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			if (args.length > 0) {
				switch (args[0].toLowerCase()) {
				case "create":
					BuildingProcessClient.createPalette(true);
					break;
				case "save":
					if (args.length > 1) {
						String name = "";
						for (int i = 1; i < args.length; i++) {
							name += args[i] + ((i == args.length - 1)? "" : " ");
						}
						BuildingProcessClient.finishPalette(name);
					} else {
						throw new CommandException("No palette name given: /palette save <name>");						
					}
				break;
				default:
					throw new CommandException("Subcommands for /palette: create, save");
				}
			} else {
				BuildingProcessClient.pickPalette();
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
