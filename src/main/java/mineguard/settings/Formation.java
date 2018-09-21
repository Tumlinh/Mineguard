package mineguard.settings;

import java.util.ArrayList;
import java.util.List;

public enum Formation
{
    CIRCLE(0, "circle"),
    SQUARE(1, "square");

    private int id;
    private String text;

    Formation(int id, String text)
    {
        this.id = id;
        this.text = text;
    }

    public int getId()
    {
        return id;
    }

    public String getText()
    {
        return text;
    }

    public static Formation get(int id)
    {
        for (Formation formation : values()) {
            if (formation.id == id)
                return formation;
        }
        return null;
    }

    public static Formation get(String text)
    {
        for (Formation formation : values()) {
            if (formation.text.equals(text))
                return formation;
        }
        return null;
    }

    public static List<String> getEnumList()
    {
        List<String> list = new ArrayList<String>();
        for (Formation formation : values())
            list.add(formation.text);
        return list;
    }
}
