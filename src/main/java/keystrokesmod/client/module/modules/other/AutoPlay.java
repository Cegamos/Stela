package keystrokesmod.client.module.modules.other;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.timing.Clock;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "AutoPlay", category = Category.Other)
public class AutoPlay extends Mod {
	
	private final DescriptionValue desc = new DescriptionValue("Only for Universocraft", this);
	private final NumberValue delay = new NumberValue("Delay", this, 0, 0, 600, 50);
	 private Clock clock = new Clock(0);
	
	private Map<String, String> uniCommands = new HashMap<>();
	
	@SubscribeEvent
	public void onInbound(ClientChatReceivedEvent event) {
	    if (!Utils.Player.isPlayerInGame()) return;
	    if (clock.finished((long) delay.getInput())) {
	        String receiveMessage = event.message.getUnformattedText();
	        String game = getDetectedGame(mc.theWorld.getScoreboard());
	
	        uniCommands.put("ArenaPvP", "/leave");
	        uniCommands.put("BedWars", "/bw random");
	        uniCommands.put("TNTTag", "/playagain");
	        uniCommands.put("SkyWars", "/sw random");
	        uniCommands.put("SkyWars Speed", "/sw random");
	
	        if (uniCommands.containsKey(game)) {
	            if (shouldSendCommand(game, receiveMessage)) {
	                String command = uniCommands.get(game);
	                if (mc.thePlayer != null) {
	                	mc.thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage(command));
	                }
	            }
	        }
	    }
	}

	private boolean shouldSendCommand(String game, String message) {
	    switch (game) {
	        case "ArenaPvP":
	            return message.contains("Información");

	        case "BedWars":
	        case "TNTTag":
	        case "SkyWars":
	        case "SkyWars Speed":
	            return message.contains("Jugar de nuevo")
	                || message.contains("ha ganado");

	        default:
	            return false;
	    }
	}

    private String getDetectedGame(Scoreboard scoreboard) {
        String[] games = {"BedWars", "SkyWars", "Skywars Speed", "TNTTag", "ArenaPvP"};
        return scoreboard.getScoreObjectives().stream().map(obj -> obj.getDisplayName().replaceAll("§[0-9A-FK-ORa-fk-or]", "")).filter(name -> containsAny(name, games)).findFirst().orElse("Unknown");
    }
    
    private boolean containsAny(String source, String... targets) {
	    return Arrays.stream(targets).anyMatch(source::contains);
	}
}
