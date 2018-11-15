package mineguard.init;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class ModConfigServer extends ModConfig
{
    public static String SETTINGS_FILE = "settings.mg";
    public static int MAX_TROOP_SIZE = 64;
    public static float GUARD_ATTACK_DAMAGE = 1.0F;
    public static float GUARD_ATTACK_SPEED = 4.0F;
    public static float GUARD_KNOCKBACK_RESISTANCE = 0.0F;
    public static float GUARD_MAX_HEALTH = 20;
    public static float GUARD_MOVEMENT_SPEED = 0.3F;
    public static float GUARD_NAVIGATION_SPEED = 1.2F;
    public static float GUARD_FOLLOW_RANGE = 32;
    public static int GUARD_REGENERATION_TIME = 1200;
    public static float GUARD_DEFENSE_RATIO = 0.5F;

    public ModConfigServer(File configFile)
    {
        super(configFile);
    }

    @Override
    protected void load()
    {
        SETTINGS_FILE = config.getString("SettingsFile", Configuration.CATEGORY_GENERAL, SETTINGS_FILE, "");
        MAX_TROOP_SIZE = config.getInt("MaxTroopSize", Configuration.CATEGORY_GENERAL, MAX_TROOP_SIZE, 0,
                Integer.MAX_VALUE, "");
        GUARD_ATTACK_DAMAGE = config.getFloat("GuardAttackDamage", "guard", GUARD_ATTACK_DAMAGE, 0, Float.MAX_VALUE,
                "");
        GUARD_ATTACK_SPEED = config.getFloat("GuardAttackSpeed", "guard", GUARD_ATTACK_SPEED, 0, Float.MAX_VALUE, "");
        GUARD_KNOCKBACK_RESISTANCE = config.getFloat("GuardKnockBackResistance", "guard", GUARD_KNOCKBACK_RESISTANCE, 0,
                Float.MAX_VALUE, "");
        GUARD_MAX_HEALTH = config.getFloat("GuardMaxHealth", "guard", GUARD_MAX_HEALTH, 0, Float.MAX_VALUE, "");
        GUARD_MOVEMENT_SPEED = config.getFloat("GuardMovementSpeed", "guard", GUARD_MOVEMENT_SPEED, 0, Float.MAX_VALUE,
                "");
        GUARD_NAVIGATION_SPEED = config.getFloat("GuardSpeedTarget", "guard", GUARD_NAVIGATION_SPEED, 0,
                Float.MAX_VALUE, "");
        GUARD_FOLLOW_RANGE = config.getFloat("GuardFollowRange", "guard", GUARD_FOLLOW_RANGE, 0, Float.MAX_VALUE, "");
        GUARD_REGENERATION_TIME = config.getInt("GuardRegenerationTime", "guard", GUARD_REGENERATION_TIME, 0,
                Integer.MAX_VALUE, "");
        GUARD_DEFENSE_RATIO = config.getFloat("GuardDefenseRatio", "guard", GUARD_DEFENSE_RATIO, 0, Float.MAX_VALUE,
                "");
    }
}
