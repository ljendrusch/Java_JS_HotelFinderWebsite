package hotelapp;

import com.google.gson.JsonArray;
import java.util.*;


/**
 * (De)Serialization of fav_hotels TEXT field in SQL users table
 */
public class FavHotels {
    private String username;
    private final Set<String> set = new HashSet<>();

    private FavHotels(){}
    public static FavHotels of(String username) {
        FavHotels fh = new FavHotels();
        fh.username = username;

        String favHotels = DBQueriesHandler.get().getFavHotels(username);
        if (favHotels == null)
            return fh;

        Collections.addAll(fh.set, favHotels.split(","));
        return fh;
    }

    /**
     * Check if user has any favorited hotels
     * @return boolean no favorited hotels
     */
    public boolean isEmpty() {
        return set.isEmpty();
    }

    /**
     * Check if hotel with hotelId is favorited by user
     * @param hotelId String id of hotel to check
     * @return boolean hotel is favorited by user
     */
    public boolean contains(String hotelId) {
        return set.contains(hotelId);
    }

    /**
     * Turns on or off the favorited status of hotel with hotelId
     * @param hotelId String id of hotel to toggle
     * @return boolean new status of hotel with hotelId is favorited / not favorited
     */
    public boolean toggleFavStatus(String hotelId) {
        boolean flag;
        if (set.contains(hotelId)) {
            set.remove(hotelId);
            flag = false;
        } else {
            set.add(hotelId);
            flag = true;
        }

        DBQueriesHandler.get().setFavHotels(username, this.toString());
        return flag;
    }

    /**
     * Clears all favorited hotels for user with username
     * @param username String username of user to clear fav hotels
     * @return boolean success of DB clear query
     */
    public static boolean clear(String username) {
        return DBQueriesHandler.get().clearFavHotels(username);
    }

    /**
     * Clears all favorited hotels for user corresponding to this FavHotels object
     * @return boolean success of DB clear query
     */
    public boolean clear() {
        return DBQueriesHandler.get().clearFavHotels(username);
    }

    /**
     * Makes and returns a String representation of this FavHotels object
     * @return String representation
     */
    @Override
    public String toString() {
        if (set.isEmpty()) return "";

        Iterator<String> iterator = set.iterator();
        String s1 = iterator.next();

        StringBuilder stb = new StringBuilder();
        stb.append(s1);
        while (iterator.hasNext()) {
            String s = iterator.next();
            stb.append(",").append(s);
        }
        return stb.toString();
    }

    /**
     * Makes and returns a JsonArray representation of this FavHotels object
     * @return JsonArray representation
     */
    public JsonArray toJsonArray() {
        JsonArray ja = new JsonArray();
        for (String s : set)
            ja.add(s);
        return ja;
    }
}
