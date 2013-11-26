package com.verlanos.sensors.datastructure;

/**
 * Created by Sefverl on 11/11/13.
 */
public class Item {

    private Object value;
    private Object label;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Object getLabel()
    {
        return this.label;
    }

    public void setLabel(Object label)
    {
        this.label = label;
    }


    private int key;
    private static int key_to_allocate = 0;

    public Item(Object label,Object value)
    {
        this.label = label;
        this.value = value;
        this.key = generateKey();
    }

    public String toString()
    {
        if (value != null && label != null)
        {
            return label+" : "+value;
        }
        else if(label != null)
        {
            return label+" : N/A";
        }
        else
        {
            return "null";
        }
    }

    public static int generateKey()
    {
        return key_to_allocate++;
    }

    public static void resetKey()
    {
        key_to_allocate = 0;
    }
}
