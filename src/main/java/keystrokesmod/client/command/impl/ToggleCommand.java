package keystrokesmod.client.command.impl;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.command.Command;
import keystrokesmod.client.command.CommandInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.util.Utils;

@CommandInfo(
    name = "toggle",
    description = "Toggle a module on or off",
    usage = ".toggle <module>",
    aliases = {"t"}
)
public class ToggleCommand extends Command {

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            Utils.Player.sendMessageToSelf("&cUsage: " + getUsage());
            return;
        }

        String moduleName = args[0];
        Mod module = Kevin.moduleManager.getModuleByName(moduleName);

        if (module == null) {
            Utils.Player.sendMessageToSelf("&cModule &e" + moduleName + " &cnot found!");
            return;
        }

        module.toggle();
        Utils.Player.sendMessageToSelf("&7Module &e" + module.getName() + " &7is now " + (module.isEnabled() ? "&aENABLED" : "&cDISABLED"));
    }
}
