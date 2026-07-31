package keystrokesmod.client.util.system;

import keystrokesmod.client.util.IMinecraft;
import keystrokesmod.client.util.Utils;

public class DimensionUtil implements IMinecraft {
    public static boolean isPlayerInNether() {
        return Utils.Player.isPlayerInGame() && mc.thePlayer.dimension == DIMENSIONS.NETHER.getDimensionID();
    }
    
    public static boolean isPlayerInEnd() {
        return Utils.Player.isPlayerInGame() && mc.thePlayer.dimension == DIMENSIONS.END.getDimensionID();
    }
    
    public static boolean isPlayerInOverworld() {
        return Utils.Player.isPlayerInGame() && mc.thePlayer.dimension == DIMENSIONS.OVERWORLD.getDimensionID();
    }
    
    public enum DIMENSIONS {
        NETHER(-1), 
        OVERWORLD(0), 
        END(1);
        
        private final int dimensionID;
        
        DIMENSIONS(final int n) {
            this.dimensionID = n;
        }
        
        public int getDimensionID() {
            return this.dimensionID;
        }
    }
}
