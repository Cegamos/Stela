package keystrokesmod.client.mixin;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("StelaLoader")
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({
    "keystrokesmod.client.stela.",
    "keystrokesmod.client.mixin.",
    "org.objectweb.asm."
})
public class MixinLoader implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "keystrokesmod.client.stela.StelaTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { 
        // No operation needed
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}