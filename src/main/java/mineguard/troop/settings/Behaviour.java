package mineguard.troop.settings;

import java.util.ArrayList;
import java.util.List;

public enum Behaviour
{
    AGGRESSIVE(0, "aggressive"),
    BERSERKER(1, "berserker"),
    DEFENSIVE(2, "defensive"),
    STILL(3, "still");

    private int id;
    private String text;

    Behaviour(int id, String text)
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

    public static Behaviour get(int id)
    {
        for (Behaviour behaviour : values()) {
            if (behaviour.id == id)
                return behaviour;
        }
        return null;
    }

    public static Behaviour get(String text)
    {
        for (Behaviour behaviour : values()) {
            if (behaviour.text.equals(text))
                return behaviour;
        }
        return null;
    }

    public static List<String> getEnumList()
    {
        List<String> list = new ArrayList<String>();
        for (Behaviour behaviour : values())
            list.add(behaviour.text);
        return list;
    }
}
