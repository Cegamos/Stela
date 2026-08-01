package wtf.command.impl;

import wtf.command.Command;
import wtf.command.CommandManager;
import wtf.util.Utils;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "List all available commands", ".help", "h", "?");
    }

    @Override
    public void execute(String[] args) {
        Utils.Player.sendMessageToSelf("&b--- &lStela Commands &r&b---");
        for (Command cmd : CommandManager.getCommands()) {
            Utils.Player.sendMessageToSelf("&e." + cmd.getName() + " &7- " + cmd.getDescription() + " &8(" + cmd.getUsage() + ")");
        }
    }
}
