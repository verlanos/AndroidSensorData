package com.verlanos.sensors.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class SensorMapping {

    /**
     * An array of sample (dummy) items.
     */
    public static List<Mapping> ITEMS = new ArrayList<Mapping>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, Mapping> ITEM_MAP = new HashMap<String, Mapping>();

    static {
        // Add 3 sample items.
        addItem(new Mapping("1", "LIGHT"));
        addItem(new Mapping("2", "TEMPERATURE"));
        addItem(new Mapping("3", "PROXIMITY"));
        addItem(new Mapping("4", "PRESSURE"));
        addItem(new Mapping("5", "RELATIVE HUMIDITY"));
        addItem(new Mapping("6", "CELL"));
        addItem(new Mapping("7", "WIFI"));
    }

    private static void addItem(Mapping item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Mapping {
        public String id;
        public String content;

        public Mapping(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
