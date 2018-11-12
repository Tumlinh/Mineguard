package mineguard.command;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mineguard.Troop;
import mineguard.Troop.BodyguardOverflowException;
import mineguard.Troop.TroopInOtherDimensionException;
import mineguard.entity.EntityBodyguard;
import mineguard.entity.ai.EntityAIReform;
import mineguard.settings.Behaviour;
import mineguard.settings.Formation;
import mineguard.util.EntityUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
        return "\n/bg mk <number>\n" + "/bg set <behaviour|follow|formation|size> [value]\n" + "/bg <info|rm|rs>";
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
                try {
                    troop.summonBodyguards(world, player.getPosition(), bgCount);
                } catch (TroopInOtherDimensionException e) {
                    sendMessage(sender, e.getMessage(), TextFormatting.RED);
                } catch (BodyguardOverflowException e) {
                    sendMessage(sender, e.getMessage(), TextFormatting.RED);
                }
            }

            // Remove bodyguards
            else if (args.length == 1 && args[0].equals("rm")) {
                troop.removeBodyguards();
            }

            // Give bodyguards
            else if (args.length == 2 && args[0].equals("give")) {
                try {
                    troop.give(args[1]);
                } catch (BodyguardOverflowException e) {
                    sendMessage(sender, e.getMessage(), TextFormatting.RED);
                }
            }

            // Display or change settings
            else if (args.length > 1 && args[0].equals("set")) {
                String setting = args[1];

                if (args.length == 2) {
                    String msg = "";
                    switch (setting) {
                    case "behaviour":
                        msg = troop.getSettings().getBehaviour().getText();
                        break;
                    case "color":
                        String hexColor = Integer.toHexString(troop.getSettings().getColor() & 0xFFFFFF);
                        // Pad with zeros
                        int len = hexColor.length();
                        for (int i = 0; i < 6 - len; i++)
                            hexColor = "0" + hexColor;
                        msg = "#" + hexColor;
                        break;
                    case "displayName":
                        msg = Boolean.toString(troop.getSettings().isDisplayName());
                        break;
                    case "follow":
                        msg = Boolean.toString(troop.getSettings().isFollowing());
                        break;
                    case "formation":
                        msg = troop.getSettings().getFormation().getText();
                        break;
                    case "nameFormat":
                        msg = troop.getSettings().getNameFormat().replace("%", "%%");
                        break;
                    case "size":
                        msg = Double.toString(troop.getSettings().getSize());
                        break;
                    default:
                        return;
                    }
                    msg = setting + " = " + msg;
                    sendMessage(sender, msg, TextFormatting.WHITE);
                } else if (args.length == 3) {
                    String value = args[2];
                    // TODO: better way to handle string (multiple quoted words) and booleans
                    switch (setting) {
                    case "behaviour":
                        troop.getSettings().setBehaviour(Behaviour.get(value));
                        break;
                    case "color":
                        troop.getSettings().setColor(Color.decode(value).getRGB());
                        break;
                    case "displayName":
                        troop.getSettings().setDisplayName(!value.equals("0"));
                        break;
                    case "follow":
                        troop.getSettings().setFollow(!value.equals("0"));
                        break;
                    case "formation":
                        troop.getSettings().setFormation(Formation.get(value));
                        break;
                    case "nameFormat":
                        troop.getSettings().setNameFormat(value);
                        break;
                    case "size":
                        troop.getSettings().setSize(Double.parseDouble(value));
                        break;
                    default:
                        break;
                    }
                }
            }

            // Reset bg's behaviour, whitelist, blacklist, formation, etc.
            // TODO: implement or remove it
            else if (args.length == 1 && args[0].equals("rs")) {

            }

            // Get debug information
            else if (args.length == 1 && args[0].equals("info")) {
                System.out.println(troop);
            }

            // Make bodyguard attack a player (for debugging purpose)
            else if (args.length == 2 && args[0].equals("attack")) {
                EntityBodyguard bg = troop.getFirstBodyguard();
                if (bg != null) {
                    // Disable relevant AI tasks
                    for (EntityAITaskEntry task : bg.targetTasks.taskEntries)
                        bg.targetTasks.removeTask(task.action);
                    for (EntityAITaskEntry task : bg.tasks.taskEntries)
                        if (task.action instanceof EntityAIReform)
                            bg.tasks.removeTask(task.action);

                    EntityPlayer target = EntityUtil.getPlayerFromName(args[1]);
                    if (target != null)
                        bg.setAttackTarget(target);
                }
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
                return getListOfStringsMatchingLastWord(args, Arrays.asList("behaviour", "color", "displayName",
                        "follow", "formation", "nameFormat", "size"));
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
        } else if (args[0].equals("attack") || args[0].equals("give")) {
            if (args.length == 2)
                return getListOfStringsMatchingLastWord(args,
                        Arrays.asList(FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames()));
        }
        return new ArrayList<String>();
    }

    private static void sendMessage(ICommandSender sender, String message, TextFormatting textFormatting)
    {
        ITextComponent msg = new TextComponentTranslation(message, new Object[0]);
        msg.getStyle().setColor(textFormatting);
        sender.sendMessage(msg);
    }
}
