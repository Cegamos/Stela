package wtf.util.system;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EnemyManager {
    private static final Set<String> enemies = new HashSet<>();

    public static boolean addEnemy(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return enemies.add(name.toLowerCase().trim());
    }

    public static boolean removeEnemy(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return enemies.remove(name.toLowerCase().trim());
    }

    public static boolean isEnemy(String name) {
        if (name == null) return false;
        return enemies.contains(name.toLowerCase().trim());
    }

    public static Set<String> getEnemies() {
        return Collections.unmodifiableSet(enemies);
    }

    public static void clear() {
        enemies.clear();
    }
}
