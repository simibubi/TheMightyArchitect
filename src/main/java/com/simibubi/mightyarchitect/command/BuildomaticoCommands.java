package com.simibubi.mightyarchitect.command;

import net.minecraft.command.CommandBase;
import net.minecraftforge.client.ClientCommandHandler;

public class BuildomaticoCommands {

	public static void init() {
		CommandBase[] commands = new CommandBase[] {
				new CommandValidate()
		};
		
		for (CommandBase commandBase : commands) {
			ClientCommandHandler.instance.registerCommand(commandBase);
		}
	}

}
