package keystrokesmod.client.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import keystrokesmod.client.Raven;
import keystrokesmod.client.clickgui.raven.components.CategoryComponent;
import keystrokesmod.client.module.modules.client.HUD;
import keystrokesmod.client.module.modules.client.Terminal;
import keystrokesmod.client.util.IMinecraft;
import keystrokesmod.client.util.Utils;
import keystrokesmod.keystroke.KeyStroke;

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
    
    public static void saveKeyStrokeSettingsToConfigFile() {
        try {
            final File file = new File(ClientConfig.mc.mcDataDir + File.separator + "keystrokesmod", "config");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            final FileWriter writer = new FileWriter(file, false);
            writer.write(KeyStroke.x + "\n" + KeyStroke.y + "\n" + KeyStroke.enabled + "\n" + KeyStroke.showMouseButtons + "\n" + KeyStroke.currentColorNumber + "\n" + KeyStroke.outline);
            writer.close();
        }
        catch (Throwable var2) {
            var2.printStackTrace();
        }
    }
    
    public static void applyKeyStrokeSettingsFromConfigFile() {
        try {
            final File file = new File(ClientConfig.mc.mcDataDir + File.separator + "keystrokesmod", "config");
            if (!file.exists()) {
                return;
            }
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            int i = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                switch (i) {
                    case 0: {
                        KeyStroke.x = Integer.parseInt(line);
                        break;
                    }
                    case 1: {
                        KeyStroke.y = Integer.parseInt(line);
                        break;
                    }
                    case 2: {
                        KeyStroke.enabled = Boolean.parseBoolean(line);
                        break;
                    }
                    case 3: {
                        KeyStroke.showMouseButtons = Boolean.parseBoolean(line);
                        break;
                    }
                    case 4: {
                        KeyStroke.currentColorNumber = Integer.parseInt(line);
                        break;
                    }
                    case 5: {
                        KeyStroke.outline = Boolean.parseBoolean(line);
                        break;
                    }
                }
                ++i;
            }
            reader.close();
        }
        catch (Throwable var4) {
            var4.printStackTrace();
        }
    }
    
    public void saveConfig() {
        final List<String> config = new ArrayList<String>();
        config.add("clickgui-pos~ " + this.getClickGuiPos());
        config.add("loaded-cfg~ " + ConfigManager.getCurrentProfileName());
        config.add("HUDX~ " + HUD.hudX);
        config.add("HUDY~ " + HUD.hudY);
        config.add("terminal-pos~ " + Raven.clickGui.terminal.getX() + "," + Raven.clickGui.terminal.getY());
        config.add("terminal-size~ " + Raven.clickGui.terminal.getWidth() + "," + Raven.clickGui.terminal.height());
        config.add("terminal-opened~ " + Raven.clickGui.terminal.opened);
        config.add("terminal-hidden~ " + Raven.clickGui.terminal.hidden);
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
                    Raven.clickGui.terminal.setLocation(i1, i2);
                }
                catch (Exception ex) {}
            }
            else if (line.startsWith("terminal-size~ ")) {
                try {
                    final String[] split_up = line.replace("terminal-size~ ", "").split(",");
                    final int i1 = Integer.parseInt(split_up[0]);
                    final int i2 = Integer.parseInt(split_up[1]);
                    Raven.clickGui.terminal.setSize(i1, i2);
                }
                catch (Exception ex2) {}
            }
            else if (line.startsWith("terminal-opened~ ")) {
                try {
                    Raven.clickGui.terminal.opened = Boolean.parseBoolean(line.replace("terminal-opened~ ", ""));
                }
                catch (Exception ex3) {}
            }
            else {
                if (!line.startsWith("terminal-hidden~ ")) {
                    continue;
                }
                try {
                    final Terminal terminalModule = (Terminal)Raven.moduleManager.getModuleByClazz(Terminal.class);
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
            for (final CategoryComponent cat : Raven.clickGui.getCategoryList()) {
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
        for (final CategoryComponent cat : Raven.clickGui.getCategoryList()) {
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