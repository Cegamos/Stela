package keystrokesmod.client.command.impl;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.command.Command;
import keystrokesmod.client.command.CommandInfo;
import keystrokesmod.client.command.SubCommand;
import keystrokesmod.client.config.ConfigManager;
import keystrokesmod.client.util.Utils;

import java.io.File;

@CommandInfo(
    name = "config",
    description = "Manage binary profiles (.stela)",
    usage = ".config <load/save> <name>",
    aliases = {"cfg", "profile", "p"}
)
public class ConfigCommand extends Command {

    public ConfigCommand() {
        addSubCommand(new SubCommand("save", ".config save <name>", "Saves a profile") {
            @Override
            public void execute(String[] args) {
                if (args.length < 1) {
                    Utils.Player.sendMessageToSelf("&cUsage: " + getUsage());
                    return;
                }
                File dest = new File(ConfigManager.PROFILES_DIR, args[0] + ".stela");
                ConfigManager.saveConfig(dest, Kevin.moduleManager.getModules());
                Utils.Player.sendMessageToSelf("&aSaved profile &e" + args[0] + ".stela");
            }
        });

        addSubCommand(new SubCommand("load", ".config load <name>", "Loads a profile") {
            @Override
            public void execute(String[] args) {
                if (args.length < 1) {
                    Utils.Player.sendMessageToSelf("&cUsage: " + getUsage());
                    return;
                }
                File source = new File(ConfigManager.PROFILES_DIR, args[0] + ".stela");
                if (!source.exists()) {
                    Utils.Player.sendMessageToSelf("&cProfile &e" + args[0] + ".stela &cnot found!");
                    return;
                }
                ConfigManager.loadConfig(source, Kevin.moduleManager.getModules());
                Utils.Player.sendMessageToSelf("&aLoaded profile &e" + args[0] + ".stela");
            }
        });
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            Utils.Player.sendMessageToSelf("&cUsage: " + getUsage());
            return;
        }

        SubCommand sub = getSubCommand(args[0]);
        if (sub != null) {
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, subArgs.length);
            sub.execute(subArgs);
        } else {
            Utils.Player.sendMessageToSelf("&cUnknown subcommand &e" + args[0] + "&c. Usage: " + getUsage());
        }
    }
}
