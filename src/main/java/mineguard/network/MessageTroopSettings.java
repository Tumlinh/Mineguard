package mineguard.network;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import mineguard.settings.Settings;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

// TODO: Mask to select relevant settings
public class MessageTroopSettings implements IMessage
{
    public enum Type
    {
        REQ(0),
        DATA(1);

        public int id;

        Type(int id)
        {
            this.id = id;
        }

        public static Type getType(int id)
        {
            for (Type type : Type.values()) {
                if (type.id == id)
                    return type;
            }
            return null;
        }
    }

    private Type type;
    private Settings settings;

    public MessageTroopSettings(Type type, @Nullable Settings settings)
    {
        this.type = type;
        this.settings = settings;
    }

    public MessageTroopSettings()
    {
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        type = Type.getType(buf.readByte());
        if (type == Type.DATA) {
            settings = new Settings();
            settings.fromBytes(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(type.id);
        if (settings != null)
            settings.toBytes(buf);
    }

    public Type getType()
    {
        return type;
    }

    public Settings getSettings()
    {
        return settings;
    }
}
