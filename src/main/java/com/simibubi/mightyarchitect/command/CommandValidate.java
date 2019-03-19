package com.simibubi.mightyarchitect.command;

import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.ThemeValidator;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;

public class CommandValidate extends CommandBase implements IClientCommand {

	@Override
	public String getName() {
		return "validate";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/validate <theme>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			if (args.length == 0)
				throw new CommandException("Please specify a theme.");
			
			DesignTheme theme = DesignTheme.valueOf(args[0]);
			ThemeValidator.check(theme);
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
