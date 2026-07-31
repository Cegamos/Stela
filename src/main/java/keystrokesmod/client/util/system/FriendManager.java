package keystrokesmod.client.util.system;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FriendManager {
    private static final Set<String> friends = new HashSet<>();

    public static boolean addFriend(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return friends.add(name.toLowerCase().trim());
    }

    public static boolean removeFriend(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return friends.remove(name.toLowerCase().trim());
    }

    public static boolean isFriend(String name) {
        if (name == null) return false;
        return friends.contains(name.toLowerCase().trim());
    }

    public static Set<String> getFriends() {
        return Collections.unmodifiableSet(friends);
    }

    public static void clear() {
        friends.clear();
    }
}
