package wtf.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Command {
    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases;
    private final List<SubCommand> subCommands = new ArrayList<>();

    public Command() {
        CommandInfo info = getClass().getAnnotation(CommandInfo.class);
        if (info != null) {
            this.name = info.name();
            this.description = info.description();
            this.usage = info.usage();
            this.aliases = new ArrayList<>(Arrays.asList(info.aliases()));
        } else {
            this.name = getClass().getSimpleName().toLowerCase();
            this.description = "";
            this.usage = "." + this.name;
            this.aliases = new ArrayList<>();
        }
    }

    public Command(String name, String description, String usage, String... aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = new ArrayList<>(Arrays.asList(aliases));
    }

    public abstract void execute(String[] args);

    public void addSubCommand(SubCommand subCommand) {
        this.subCommands.add(subCommand);
    }

    public List<SubCommand> getSubCommands() {
        return subCommands;
    }

    public SubCommand getSubCommand(String name) {
        for (SubCommand sub : subCommands) {
            if (sub.getName().equalsIgnoreCase(name)) {
                return sub;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
