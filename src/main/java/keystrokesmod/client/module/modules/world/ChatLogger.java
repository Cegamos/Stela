package keystrokesmod.client.module.modules.world;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "ChatLogger", category = Category.World)
public class ChatLogger extends Mod {
	private final File dir;
	private File chatLog;
	public String fileName;
	public String extension;

	public ChatLogger() {
		this.extension = "txt";
		this.dir = new File(mc.mcDataDir, "keystrokes" + File.separator + "logs");
		if (!this.dir.exists()) {
			this.dir.mkdir();
		}
	}

	@Override
	public void onEnable() {
		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH.mm.ss");
		final LocalDateTime now = LocalDateTime.now();
		this.fileName = dtf.format(now) + "." + this.extension;
		this.chatLog = new File(this.dir, this.fileName);
		if (!this.chatLog.exists()) {
			try {
				this.chatLog.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.onEnable();
	}

	@SubscribeEvent
	public void onMessageRecieved(final ClientChatReceivedEvent c) {
		try (final FileWriter fw = new FileWriter(this.chatLog.getPath(), true);
				final BufferedWriter bw = new BufferedWriter(fw);
				final PrintWriter out = new PrintWriter(bw)) {
			out.println(c.message.getUnformattedText());
		} catch (IOException ex) {
		}
	}
}
