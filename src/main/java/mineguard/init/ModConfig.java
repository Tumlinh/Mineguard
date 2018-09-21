package mineguard.init;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class ModConfig
{
    private Configuration config;

    public static String SETTINGS_FILE = "settings.mg";
    public static int MAX_BODYGUARDS = 64;
    public static float BODYGUARD_ATTACK_DAMAGE = 20;
    public static float BODYGUARD_ATTACK_SPEED = 2.0F;
    public static float BODYGUARD_KNOCKBACK_RESISTANCE = 5;
    public static float BODYGUARD_MAX_HEALTH = 100;
    public static float BODYGUARD_MOVEMENT_SPEED = 0.3F;
    public static float BODYGUARD_SPEED_TARGET = 1.4F;
    public static float BODYGUARD_FOLLOW_RANGE = 32;

    public ModConfig(File configFile)
    {
        this.config = new Configuration(configFile);
        this.config.load();
        this.load();
        this.config.save();
    }

    private void load()
    {
        SETTINGS_FILE = config.getString("SettingsFile", "", SETTINGS_FILE, "");
        MAX_BODYGUARDS = config.getInt("maxBodyguards", "", MAX_BODYGUARDS, 0, Integer.MAX_VALUE, "");
        BODYGUARD_ATTACK_DAMAGE = config.getFloat("BodyguardAttackDamage", "", BODYGUARD_ATTACK_DAMAGE, 0,
                Float.MAX_VALUE, "");
        BODYGUARD_ATTACK_SPEED = config.getFloat("BodyguardAttackSpeed", "", BODYGUARD_ATTACK_SPEED, 0, Float.MAX_VALUE,
                "");
        BODYGUARD_KNOCKBACK_RESISTANCE = config.getFloat("BodyguardKnockBackResistance", "",
                BODYGUARD_KNOCKBACK_RESISTANCE, 0, Float.MAX_VALUE, "");
        BODYGUARD_MAX_HEALTH = config.getFloat("BodyguardMaxHealth", "", BODYGUARD_MAX_HEALTH, 0, Float.MAX_VALUE, "");
        BODYGUARD_MOVEMENT_SPEED = config.getFloat("BodyguardMovementSpeed", "", BODYGUARD_MOVEMENT_SPEED, 0,
                Float.MAX_VALUE, "");
        BODYGUARD_SPEED_TARGET = config.getFloat("BodyguardSpeedTarget", "", BODYGUARD_SPEED_TARGET, 0, Float.MAX_VALUE,
                "");
        BODYGUARD_FOLLOW_RANGE = config.getFloat("BodyguardFollowRange", "", BODYGUARD_FOLLOW_RANGE, 0, Float.MAX_VALUE,
                "");
    }
}
