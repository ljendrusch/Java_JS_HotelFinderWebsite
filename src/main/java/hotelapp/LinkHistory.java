package hotelapp;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * (De)Serialization of link_history TEXT field in SQL users table
 */
public class LinkHistory {
    private String username;
    private final Map<String, Integer> map = new HashMap<>();

    private LinkHistory(){}
    public static LinkHistory of(String username) {
        LinkHistory lh = new LinkHistory();
        lh.username = username;
        String fullHistory = DBQueriesHandler.get().getLinkHistory(username);
        if (fullHistory == null)
            return lh;

        String[] tokens = fullHistory.split(",");
        for (String t : tokens) {
            int splitIdx = t.indexOf(":");
            lh.map.put(t.substring(0, splitIdx), Integer.parseInt(t.substring(splitIdx+1)));
        }
        return lh;
    }

    /**
     * Check if user has any link history
     * @return boolean no link history
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Increments link click history of link
     * @param link String link clicked
     * @return boolean success of DB update query
     */
    public boolean addClick(String link) {
        map.compute(link, (k, v) -> (v == null) ? 1 : v+1);
        return DBQueriesHandler.get().setLinkHistory(username, this.toString());
    }

    /**
     * Clears all link history for user with username
     * @param username String username of user to clear link history
     * @return boolean success of DB clear query
     */
    public static boolean clear(String username) {
        return DBQueriesHandler.get().clearLinkHistory(username);
    }

    /**
     * Clears all link history for user corresponding to this LinkHistory object
     * @return boolean success of DB clear query
     */
    public boolean clear() {
        return DBQueriesHandler.get().clearLinkHistory(username);
    }

    /**
     * Makes and returns a String representation of this LinkHistory object
     * @return String representation
     */
    @Override
    public String toString() {
        if (map.isEmpty()) return "";

        Iterator<Map.Entry<String, Integer>> entrySet = map.entrySet().iterator();
        Map.Entry<String, Integer> e1 = entrySet.next();

        StringBuilder stb = new StringBuilder();
        stb.append(e1.getKey()).append(":").append(e1.getValue());
        while (entrySet.hasNext()) {
            Map.Entry<String, Integer> e = entrySet.next();
            stb.append(",").append(e.getKey()).append(":").append(e.getValue());
        }
        return stb.toString();
    }

    /**
     * Makes and returns a JsonObject representation of this LinkHistory object
     * @return JsonObject representation
     */
    public JsonObject toJsonObject() {
        JsonObject jo = new JsonObject();
        for (Map.Entry<String, Integer> e : map.entrySet())
            jo.addProperty(e.getKey(), e.getValue());
        return jo;
    }
}
