package wtf.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class Config
{
    public final File file;
    public final long creationDate;
    
    public Config(final File pathToFile) {
        this.file = pathToFile;
        long creationDate1 = 0L;
        Label_0066: {
            if (!this.file.exists()) {
                creationDate1 = System.currentTimeMillis();
                try {
                    this.file.createNewFile();
                    break Label_0066;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                creationDate1 = this.getData().get("creationTime").getAsLong();
            }
            catch (NullPointerException e2) {
                creationDate1 = 0L;
            }
        }
        this.creationDate = creationDate1;
    }
    
    public String getName() {
        return this.file.getName().replace(".bplus", "");
    }
    
    public JsonObject getData() {
        try (FileReader reader = new FileReader(this.file)) {
            JsonElement element = new JsonParser().parse(reader);

            if (element != null && element.isJsonObject()) {
                return element.getAsJsonObject();
            } else {
                System.err.println("The JSON is not an object.");
            }
        } catch (JsonSyntaxException | ClassCastException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public void save(final JsonObject data) {
        data.addProperty("creationTime", (Number)this.creationDate);
        try (final PrintWriter out = new PrintWriter(new FileWriter(this.file))) {
            out.write(data.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
