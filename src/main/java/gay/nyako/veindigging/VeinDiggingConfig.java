package gay.nyako.veindigging;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "veindigging")
public class VeinDiggingConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean sneakActivation = true;
    @ConfigEntry.Gui.Tooltip
    public boolean bindActivation = false;
    public boolean useGlobalWhitelist = false;
    public boolean useBindWhitelist = false;
    public boolean useShiftWhitelist = true;
    @ConfigEntry.Gui.Tooltip
    public int maxBlocks = 64;
    @ConfigEntry.Gui.Tooltip
    public int maxRange = 16;
}