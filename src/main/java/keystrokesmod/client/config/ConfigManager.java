package keystrokesmod.client.config;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.value.Value;
import keystrokesmod.client.module.value.impl.*;

import java.io.*;
import java.util.List;

public class ConfigManager {
    private static final byte[] MAGIC = new byte[]{0x53, 0x54, 0x45, 0x4C, 0x41}; // "STELA"
    private static final byte VERSION = 0x01;
    public static final File PROFILES_DIR = new File("profiles");
    private static String currentProfileName = "default";

    public static String getCurrentProfileName() {
        return currentProfileName;
    }

    public static void saveConfigByName(String name) {
        currentProfileName = name;
        saveConfig(new File(PROFILES_DIR, name + ".stela"), Raven.moduleManager.getModules());
    }

    public static void loadConfigByName(String name) {
        currentProfileName = name;
        loadConfig(new File(PROFILES_DIR, name + ".stela"), Raven.moduleManager.getModules());
    }

    public static void saveConfig(File destination, List<Mod> modules) {
        try {
            if (destination.getParentFile() != null && !destination.getParentFile().exists()) {
                destination.getParentFile().mkdirs();
            }

            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destination)))) {
                dos.write(MAGIC);
                dos.writeByte(VERSION);

                dos.writeShort(modules.size());

                for (Mod module : modules) {
                    dos.writeUTF(module.getName());
                    dos.writeBoolean(module.isEnabled());
                    dos.writeInt(module.getKeycode());

                    List<Value> settings = module.getSettings();
                    dos.writeShort(settings != null ? settings.size() : 0);

                    if (settings != null) {
                        for (Value setting : settings) {
                            dos.writeUTF(setting.getName());

                            if (setting instanceof BooleanValue) {
                                dos.writeByte(1);
                                dos.writeBoolean(((BooleanValue) setting).isToggled());
                            } else if (setting instanceof NumberValue) {
                                dos.writeByte(2);
                                dos.writeDouble(((NumberValue) setting).getInput());
                            } else if (setting instanceof RangeValue) {
                                dos.writeByte(3);
                                dos.writeDouble(((RangeValue) setting).getInputMin());
                                dos.writeDouble(((RangeValue) setting).getInputMax());
                            } else if (setting instanceof ModeValue) {
                                dos.writeByte(4);
                                ModeValue cs = (ModeValue) setting;
                                dos.writeUTF(cs.getMode() != null ? cs.getMode() : "");
                            } else {
                                dos.writeByte(0);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Config] Failed to save binary config: " + destination.getName());
            e.printStackTrace();
        }
    }

    public static void loadConfig(File source, List<Mod> modules) {
        if (!source.exists()) return;

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(source)))) {
            byte[] magic = new byte[5];
            dis.readFully(magic);
            for (int i = 0; i < 5; i++) {
                if (magic[i] != MAGIC[i]) {
                    System.err.println("[Config] Invalid magic bytes in " + source.getName());
                    return;
                }
            }

            byte version = dis.readByte();
            if (version != VERSION) {
                System.err.println("[Config] Unsupported config version: " + version);
                return;
            }

            short moduleCount = dis.readShort();

            for (int i = 0; i < moduleCount; i++) {
                String moduleName = dis.readUTF();
                boolean enabled = dis.readBoolean();
                int keycode = dis.readInt();

                Mod module = findModule(modules, moduleName);
                if (module != null) {
                    try {
                        if (enabled != module.isEnabled()) {
                            module.toggle();
                        }
                    } catch (Exception toggleError) {
                        System.err.println("[Config] Failed to toggle module '" + moduleName + "': " + toggleError);
                        toggleError.printStackTrace();
                    }
                    module.setKeycode(keycode);
                }

                short settingCount = dis.readShort();
                for (int j = 0; j < settingCount; j++) {
                    String settingName = dis.readUTF();
                    byte type = dis.readByte();

                    Value setting = module != null ? findSetting(module, settingName) : null;

                    try {
                        switch (type) {
                            case 1: // TickSetting
                                boolean bVal = dis.readBoolean();
                                if (setting instanceof BooleanValue) {
                                    ((BooleanValue) setting).setEnabled(bVal);
                                }
                                break;
                            case 2: // SliderSetting
                                double sVal = dis.readDouble();
                                if (setting instanceof NumberValue) {
                                    ((NumberValue) setting).setValue(sVal);
                                }
                                break;
                            case 3: // DoubleSliderSetting
                                double minVal = dis.readDouble();
                                double maxVal = dis.readDouble();
                                if (setting instanceof RangeValue) {
                                    ((RangeValue) setting).setValueMin(minVal);
                                    ((RangeValue) setting).setValueMax(maxVal);
                                }
                                break;
                            case 4: // ComboSetting
                                String mVal = dis.readUTF();
                                if (setting instanceof ModeValue) {
                                    ((ModeValue) setting).setMode(mVal);
                                }
                                break;
                            case 0:
                                break;
                        }
                    } catch (Exception settingError) {
                        System.err.println("[Config] Failed to apply setting '" + settingName + "' on '" + moduleName + "': " + settingError);
                        settingError.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Config] Failed to load binary config: " + source.getName());
            e.printStackTrace();
        }
    }

    private static Mod findModule(List<Mod> modules, String name) {
        for (Mod m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    private static Value findSetting(Mod module, String name) {
        if (module == null || module.getSettings() == null) return null;
        for (Value s : module.getSettings()) {
            if (s.getName().equalsIgnoreCase(name)) return s;
        }
        return null;
    }
}
