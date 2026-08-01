package wtf.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wtf.command.impl.*;
import wtf.util.Utils;

public class CommandManager {
    private static final List<Command> commands = new ArrayList<>();
    private static String prefix = ".";

    public static void init() {
        commands.clear();
        register(new ConfigCommand());
        register(new ToggleCommand());
        register(new BindCommand());
        register(new HelpCommand());
    }

    public static void register(Command command) {
        commands.add(command);
    }

    public static boolean execute(String rawInput) {
        if (rawInput == null || !rawInput.startsWith(prefix)) {
            return false;
        }

        String withoutPrefix = rawInput.substring(prefix.length()).trim();
        if (withoutPrefix.isEmpty()) {
            return true;
        }

        String[] parts = withoutPrefix.split(" ");
        String commandName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        Command command = getCommandByNameOrAlias(commandName);
        if (command != null) {
            try {
                command.execute(args);
            } catch (Exception e) {
                Utils.Player.sendMessageToSelf("&cError executing command: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Utils.Player.sendMessageToSelf("&cUnknown command &e" + commandName + "&c. Type &e" + prefix + "help &cfor commands.");
        }

        return true;
    }

    public static Command getCommandByNameOrAlias(String name) {
        for (Command cmd : commands) {
            if (cmd.getName().equalsIgnoreCase(name)) {
                return cmd;
            }
            for (String alias : cmd.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return cmd;
                }
            }
        }
        return null;
    }

    public static List<Command> getCommands() {
        return commands;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static void setPrefix(String newPrefix) {
        prefix = newPrefix;
    }
}
