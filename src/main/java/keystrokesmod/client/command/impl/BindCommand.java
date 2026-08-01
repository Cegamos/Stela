package keystrokesmod.client.command.impl;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.command.Command;
import keystrokesmod.client.command.CommandInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.util.Utils;

import org.lwjgl.input.Keyboard;

@CommandInfo(
    name = "bind",
    description = "Bind a module to a key",
    usage = ".bind <module> <key>",
    aliases = {"b"}
)
public class BindCommand extends Command {

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            Utils.Player.sendMessageToSelf("&cUsage: " + getUsage());
            return;
        }

        String moduleName = args[0];
        String keyName = args[1].toUpperCase();

        Mod module = Kevin.moduleManager.getModuleByName(moduleName);
        if (module == null) {
            Utils.Player.sendMessageToSelf("&cModule &e" + moduleName + " &cnot found!");
            return;
        }

        int keycode = Keyboard.getKeyIndex(keyName);
        if (keyName.equals("NONE") || keyName.equals("0")) {
            keycode = Keyboard.KEY_NONE;
        }

        module.setKeycode(keycode);
        Utils.Player.sendMessageToSelf("&7Module &e" + module.getName() + " &7bound to &a" + Keyboard.getKeyName(keycode));
    }
}
