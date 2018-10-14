package mineguard.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class NBTUtil
{
    public static final int UNDEFINED = -1;

    public static void writeNBT(String filename, NBTTagCompound compound) throws IOException
    {
        FileOutputStream fileOut = new FileOutputStream(filename);
        DataOutputStream dataoutputstream = new DataOutputStream(
                new BufferedOutputStream(new DeflaterOutputStream(fileOut)));
        CompressedStreamTools.write(compound, dataoutputstream);
        dataoutputstream.close();
    }

    public static NBTTagCompound readNBT(String filename) throws IOException
    {
        NBTTagCompound compound = null;
        FileInputStream fileIn = new FileInputStream(filename);
        DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new InflaterInputStream(fileIn)));
        compound = CompressedStreamTools.read(datainputstream);
        datainputstream.close();

        return compound;
    }
}
