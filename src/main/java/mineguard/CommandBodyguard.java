package mineguard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mineguard.settings.Behaviour;
import mineguard.settings.Formation;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommandBodyguard extends CommandBase
{
    @Override
    public String getName()
    {
        return "bg";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/bg <info|mk|rm|rs|set>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 0) {
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            Troop troop = Troop.getTroop(player.getName());

            // Add bodyguard(s)
            if (args.length == 2 && args[0].equals("mk")) {
                int bgCount = Integer.parseInt(args[1]);
                World world = player.world;
                troop.summonBodyguards(world, player.getPosition(), bgCount);
                troop.reform();
            }

            // Remove bodyguards
            else if (args.length == 1 && args[0].equals("rm")) {
                troop.removeBodyguards();
            }

            // Change settings
            else if (args.length > 2 && args[0].equals("set")) {
                String setting = args[1], value = args[2];
                // TODO: try to read value as integer, then fall back to string
                switch (setting) {
                case "behaviour":
                    troop.getSettings().setBehaviour(Behaviour.get(value));
                    break;
                case "follow":
                    troop.getSettings().setFollow(!value.equals("0"));
                    break;
                case "formation":
                    troop.getSettings().setFormation(Formation.get(value));
                    break;
                case "size":
                    troop.getSettings().setSize(Double.parseDouble(value));
                    break;
                default:
                    break;
                }
            }

            // TODO: Reset bg's behaviour, whitelist, blacklist, formation, etc.
            else if (args.length == 1 && args[0].equals("rs")) {

            }

            // Get debug information
            else if (args.length == 1 && args[0].equals("info")) {
                System.out.println(troop);
            }
        }

        else {
            throw new WrongUsageException(getUsage(null), new Object[0]);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args[0].equals("set")) {
            if (args.length == 2) {
                return getListOfStringsMatchingLastWord(args,
                        Arrays.asList("master", "follow", "formation", "size", "behaviour"));
            } else if (args.length == 3) {
                List<String> possibilities = new ArrayList<String>();
                switch (args[1]) {
                case "master":
                    possibilities = Arrays
                            .asList(FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames());
                    break;
                case "behaviour":
                    possibilities = Behaviour.getEnumList();
                    break;
                case "formation":
                    possibilities = Formation.getEnumList();
                    break;
                default:
                    break;
                }
                return getListOfStringsMatchingLastWord(args, possibilities);
            }
        }
        return new ArrayList<String>();
    }

    public static String[] getStringFromEnum(Class<? extends Enum<?>> e)
    {
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }
}
