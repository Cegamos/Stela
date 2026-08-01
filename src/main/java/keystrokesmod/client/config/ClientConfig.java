package keystrokesmod.client.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.clickgui.component.CategoryPanel;
import keystrokesmod.client.module.modules.client.HUD;
import keystrokesmod.client.module.modules.client.Terminal;
import keystrokesmod.client.util.IMinecraft;
import keystrokesmod.client.util.Utils;

public class ClientConfig implements IMinecraft {
    private final File configFile;
    private final File configDir;
    
    public ClientConfig() {
        this.configDir = new File(mc.mcDataDir, "keystrokes");
        if (!this.configDir.exists()) {
            this.configDir.mkdir();
        }
        this.configFile = new File(this.configDir, "config");
        if (!this.configFile.exists()) {
            try {
                this.configFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveConfig() {
        final List<String> config = new ArrayList<String>();
        config.add("clickgui-pos~ " + this.getClickGuiPos());
        config.add("loaded-cfg~ " + ConfigManager.getCurrentProfileName());
        config.add("HUDX~ " + HUD.hudX);
        config.add("HUDY~ " + HUD.hudY);
        config.add("terminal-pos~ " + Kevin.clickGui.terminal.getX() + "," + Kevin.clickGui.terminal.getY());
        config.add("terminal-size~ " + Kevin.clickGui.terminal.getWidth() + "," + Kevin.clickGui.terminal.height());
        config.add("terminal-opened~ " + Kevin.clickGui.terminal.opened);
        config.add("terminal-hidden~ " + Kevin.clickGui.terminal.hidden);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(this.configFile);
            for (final String line : config) {
                writer.println(line);
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void applyConfig() {
        final List<String> config = this.parseConfigFile();
        for (final String line : config) {
             if (line.startsWith("clickgui-pos~ ")) {
                this.loadClickGuiCoords(line.replace("clickgui-pos~ ", ""));
            }
            else if (line.startsWith("loaded-cfg~ ")) {
                ConfigManager.loadConfigByName(line.replace("loaded-cfg~ ", ""));
            }
            else if (line.startsWith("HUDX~ ")) {
                try {
                    HUD.hudX = Integer.parseInt(line.replace("HUDX~ ", ""));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (line.startsWith("HUDY~ ")) {
                try {
                    HUD.hudY = Integer.parseInt(line.replace("HUDY~ ", ""));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (line.startsWith("terminal-pos~ ")) {
                try {
                    final String[] split_up = line.replace("terminal-pos~ ", "").split(",");
                    final int i1 = Integer.parseInt(split_up[0]);
                    final int i2 = Integer.parseInt(split_up[1]);
                    Kevin.clickGui.terminal.setLocation(i1, i2);
                }
                catch (Exception ex) {}
            }
            else if (line.startsWith("terminal-size~ ")) {
                try {
                    final String[] split_up = line.replace("terminal-size~ ", "").split(",");
                    final int i1 = Integer.parseInt(split_up[0]);
                    final int i2 = Integer.parseInt(split_up[1]);
                    Kevin.clickGui.terminal.setSize(i1, i2);
                }
                catch (Exception ex2) {}
            }
            else if (line.startsWith("terminal-opened~ ")) {
                try {
                    Kevin.clickGui.terminal.opened = Boolean.parseBoolean(line.replace("terminal-opened~ ", ""));
                }
                catch (Exception ex3) {}
            }
            else {
                if (!line.startsWith("terminal-hidden~ ")) {
                    continue;
                }
                try {
                    final Terminal terminalModule = (Terminal)Kevin.moduleManager.getModuleByClazz(Terminal.class);
                    terminalModule.setToggled(!Boolean.parseBoolean(line.replace("terminal-hidden~ ", "")));
                }
                catch (Exception ex4) {}
            }
        }
    }
    
    private List<String> parseConfigFile() {
        final List<String> configFileContents = new ArrayList<String>();
        Scanner reader = null;
        try {
            reader = new Scanner(this.configFile);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (reader.hasNextLine()) {
            configFileContents.add(reader.nextLine());
        }
        return configFileContents;
    }
    
    private void loadClickGuiCoords(final String decryptedString) {
        for (final String what : decryptedString.split("/")) {
            for (final CategoryPanel cat : Kevin.clickGui.getCategoryList()) {
                if (what.startsWith(cat.categoryName.name())) {
                    final List<String> cfg = Utils.Java.StringListToList(what.split("~"));
                    cat.setX(Integer.parseInt(cfg.get(1)));
                    cat.setY(Integer.parseInt(cfg.get(2)));
                    cat.setOpened(Boolean.parseBoolean(cfg.get(3)));
                }
            }
        }
    }
    
    public String getClickGuiPos() {
        final StringBuilder posConfig = new StringBuilder();
        for (final CategoryPanel cat : Kevin.clickGui.getCategoryList()) {
            posConfig.append(cat.categoryName.name());
            posConfig.append("~");
            posConfig.append(cat.getX());
            posConfig.append("~");
            posConfig.append(cat.getY());
            posConfig.append("~");
            posConfig.append(cat.isOpened());
            posConfig.append("/");
        }
        return posConfig.substring(0, posConfig.toString().length() - 2);
    }
}