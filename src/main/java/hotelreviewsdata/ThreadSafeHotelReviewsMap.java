package hotelreviewsdata;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;


/**
 * Wrapper class to use multi-threading with HotelReviewsMap
 */
public class ThreadSafeHotelReviewsMap extends HotelReviewsMap {
    private final ReentrantReadWriteLock lock;
    private final ExecutorService threadPool;
    private final Phaser phaser;

    public ThreadSafeHotelReviewsMap(int numThreads) {
        super();
        lock = new ReentrantReadWriteLock();
        threadPool = Executors.newFixedThreadPool(numThreads);
        phaser = new Phaser(1);
    }

    /**
     * Opens file and directory readers and delegates creation of the map
     * to various helper functions
     *
     * @param hotelsFileString String of hotels json file location
     * @param reviewsDirString String of directory holding reviews json files
     */
    public ThreadSafeHotelReviewsMap init(String hotelsFileString, String reviewsDirString) {
        if (reviewsDirString != null) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(reviewsDirString))) {
                this.threadSafeReviewDirSift(dirStream);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("\nCould not open reviews directory " + reviewsDirString);
                System.out.println("Now exiting");
                System.exit(0);
            }
        }

        if (hotelsFileString != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(hotelsFileString))) {
                try {
                    lock.writeLock().lock();
                    this.parseHotelsFile(br);
                } finally {
                    lock.writeLock().unlock();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("\nCould not open hotels file " + hotelsFileString);
                System.out.println("Now exiting");
                System.exit(0);
            }
        }

        phaser.arriveAndDeregister();
        int phase = phaser.getPhase();
        phaser.awaitAdvance(phase);
        threadPool.shutdownNow();

        return this;
    }

    /**
     * Helper function used in constructor to find and open reviews json files
     * @param dirStream DirectoryStream opened on the directory containing reviews json files
     */
    protected void threadSafeReviewDirSift(DirectoryStream<Path> dirStream) {
        for (Path path : dirStream) {
            if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> recDirStream = Files.newDirectoryStream(path)) {
                    threadSafeReviewDirSift(recDirStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (path.toString().endsWith(".json")) {
                threadPool.submit(new ReviewFileParser(path));
            }
        }
    }

    /**
     * Function to add reviews from a ReviewFileParser thread into the hrMap
     *
     * @param reviews Set of reviews from a ReviewFileParser
     */
    private void addParsedReviews(Set<Review> reviews) {
        try {
            lock.writeLock().lock();
            reviews.forEach(r -> {
                long hotelId = r.hotelId();
                hrMap.computeIfAbsent(hotelId, k ->
                        new AbstractMap.SimpleEntry<>(null, new TreeSet<>()));
                hrMap.get(hotelId).getValue().add(r);
            });
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Prints all Hotels and Reviews to a file
     * @param outFilename String designation of file to print to
     */
    public void printToFile(String outFilename) {
        try (FileWriter writer = new FileWriter(outFilename)) {
            try {
                lock.readLock().lock();
                for (Map.Entry<Long, Map.Entry<Hotel, Set<Review>>> e : hrMap.entrySet()) {
                    if (e == null) continue;

                    if (e.getValue().getKey() != null) {
                        writer.write("\n********************\n");
                        writer.write(e.getValue().getKey().toString());
                        writer.write("\n");
                    }

                    if (e.getValue().getValue() != null && e.getValue().getValue().size() > 0) {
                        for (Review r : e.getValue().getValue()) {
                            if (r == null) continue;

                            writer.write("--------------------\n");
                            writer.write(r.toString());
                            writer.write("\n");
                        }
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
        } catch (IOException e) {
            System.out.println("Failed to open output file");
            e.printStackTrace();
        }
    }

    /**
     * Map function that checks if a key exists in map
     *
     * @param hotelId long map key
     * @return boolean true if key exists, false otherwise
     */
    @Override
    public boolean containsKey(long hotelId) {
        boolean r;
        try {
            lock.readLock().lock();
            r = super.containsKey(hotelId);
        } finally {
            lock.readLock().unlock();
        }
        return r;
    }

    /**
     * Access to map Hotel values
     *
     * @param hotelId long map key
     * @return Hotel value associated with provided key
     */
    @Override
    public Hotel findHotel(long hotelId) {
        Hotel r;
        try {
            lock.readLock().lock();
            r =  super.findHotel(hotelId);
        } finally {
            lock.readLock().unlock();
        }
        return r;
    }

    /**
     * Access to map Review values; first sorts by date posted (descending), then review id (ascending)
     *
     * @param hotelId long map key
     * @return Review values associated with provided key; how to use the data structure delegated to consumers
     */
    @Override
    public Stream<Review> findReviews(long hotelId) {
        Stream<Review> r;
        try {
            lock.readLock().lock();
            r =  super.findReviews(hotelId);
        } finally {
            lock.readLock().unlock();
        }
        return r;
    }

    /**
     * Inner class defining the work to submit to a Thread
     */
    private class ReviewFileParser implements Runnable {
        private final Path p;

        public ReviewFileParser(Path p) {
            phaser.register();
            this.p = p;
        }

        @Override
        public void run() {
            try {
                String s = Files.readString(p);

                Set<Review> reviews = new HashSet<>();
                Gson gson = new GsonBuilder().registerTypeAdapter(Review.class, new ReviewDeserializer()).create();

                JsonArray jArr = JsonParser.parseString(s).getAsJsonObject()
                        .get("reviewDetails").getAsJsonObject()
                        .get("reviewCollection").getAsJsonObject()
                        .getAsJsonArray("review");
                for (JsonElement e : jArr) {
                    JsonObject o = e.getAsJsonObject();
                    reviews.add(gson.fromJson(o, Review.class));
                }

                addParsedReviews(reviews);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                phaser.arriveAndDeregister();
            }
        }
    }
}
