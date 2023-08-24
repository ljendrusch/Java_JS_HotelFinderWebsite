package hotelreviewsdata;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


/**
 * Class that handles the main data structures of this project
 * map is a HashMap that organizes Hotels and Reviews by hotel id
 * wordToReviewMap is a wrapper on a HashMap that organizes Reviews by the words in their text
 */
public class HotelReviewsMap {
    protected final Map<Long, Map.Entry<Hotel, Set<Review>>> hrMap;

    public HotelReviewsMap() {
        hrMap = new TreeMap<>(Comparator.comparing(a -> Long.toString(a)));
    }

    /**
     * Opens file and directory readers and delegates initialization of the map and
     * wordToReviewMap data structures to helper functions
     * @param hotelsFileString String of hotels json file location
     * @param reviewsDirString String of directory holding reviews json files
     */
    public HotelReviewsMap build(String hotelsFileString, String reviewsDirString) {
        if (reviewsDirString != null) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(reviewsDirString))) {
                this.reviewDirSift(dirStream);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("\nCould not open reviews directory " + reviewsDirString);
                System.out.println("Now exiting");
                System.exit(0);
            }
        }

        if (hotelsFileString != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(hotelsFileString))) {
                this.parseHotelsFile(br);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("\nCould not open hotels file " + hotelsFileString);
                System.out.println("Now exiting");
                System.exit(0);
            }
        }

        return this;
    }

    /**
     * Helper function used in constructor to populate Hotel objects into map
     * @param br BufferedReader opened on the hotels json file
     */
    protected void parseHotelsFile(BufferedReader br) {
        JsonArray jArr = JsonParser.parseReader(br).getAsJsonObject().getAsJsonArray("sr");
        Gson gson = new GsonBuilder().registerTypeAdapter(Hotel.class, new HotelDeserializer()).create();
        for (JsonElement e : jArr) {
            JsonObject o = e.getAsJsonObject();
            long hotelId = o.get("id").getAsInt();
            Hotel hotel = gson.fromJson(o, Hotel.class);
            hrMap.compute(hotelId, (k, v) ->
                (v != null && v.getValue() != null) ?
                    new AbstractMap.SimpleEntry<>(hotel, v.getValue()) :
                    new AbstractMap.SimpleEntry<>(hotel, new TreeSet<>()));
        }
    }

    /**
     * Helper function used in constructor to find and open reviews json files
     * @param dirStream DirectoryStream opened on the directory containing reviews json files
     */
    protected void reviewDirSift(DirectoryStream<Path> dirStream) {
        for (Path path : dirStream) {
            if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> recDirStream = Files.newDirectoryStream(path)) {
                    reviewDirSift(recDirStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (path.toString().endsWith(".json")) {
                addReviews(path);
            }
        }
    }

    /**
     * Helper function used in reviewDirSift to populate Review objects into map
     * @param path Path to a reviews json file
     */
    protected void addReviews(Path path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path.toString()))) {
            JsonArray jArr = JsonParser.parseReader(br).getAsJsonObject().get("reviewDetails").getAsJsonObject().get("reviewCollection").getAsJsonObject().getAsJsonArray("review");
            Gson gson = new GsonBuilder().registerTypeAdapter(Review.class, new ReviewDeserializer()).create();
            for (JsonElement e : jArr) {
                JsonObject o = e.getAsJsonObject();
                long hotelId = o.get("hotelId").getAsLong();
                Review review = gson.fromJson(o, Review.class);

                hrMap.computeIfAbsent(hotelId, k ->
                        new AbstractMap.SimpleEntry<>(null, new TreeSet<>()));
                hrMap.get(hotelId).getValue().add(review);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Map function that checks if a key exists in map
     * @param hotelId long map key
     * @return boolean true if key exists, false otherwise
     */
    public boolean containsKey(long hotelId) {
        return hrMap.containsKey(hotelId);
    }

    /**
     * Access to map Hotel values
     * @param hotelId long map key
     * @return Hotel value associated with provided key
     */
    public Hotel findHotel(long hotelId) {
        return hrMap.get(hotelId).getKey();
    }

    /**
     * Access to map Review values; first sorts by date posted (descending), then review id (ascending)
     * @param hotelId long map key
     * @return Review values associated with provided key; how to use the data structure delegated to consumers
     */
    public Stream<Review> findReviews(long hotelId) {
        return hrMap.get(hotelId).getValue().stream();
    }
}
