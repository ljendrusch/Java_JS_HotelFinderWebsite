package hotelreviewsdata;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;


/**
 * Wrapper class to use multi-threading with WordToReviewMap
 */
public class ThreadSafeWordToReviewMap extends WordToReviewMap
{
    private final ReentrantReadWriteLock lock;
    private final ExecutorService threadPool;
    private final Phaser phaser;

    public ThreadSafeWordToReviewMap(int numThreads) {
        super();
        lock = new ReentrantReadWriteLock();
        threadPool = Executors.newFixedThreadPool(numThreads);
        phaser = new Phaser(1);
    }

    /**
     * Maps all words in every Review to Reviews that contain them
     * and respective word frequency within each review
     *
     * @param mapIn Map data structure to pull objects from
     */
    public ThreadSafeWordToReviewMap init(HotelReviewsMap mapIn) {
        List<Set<Review>> reviewsList = new ArrayList<>(8);
        for (Map.Entry<Hotel, Set<Review>> e : mapIn.hrMap.values()) {
            if (e == null) continue;

            Set<Review> s = e.getValue();
            if (s == null || s.size() == 0) continue;

            reviewsList.add(s);
            if (reviewsList.size() == 8) {
                threadPool.submit(new ReviewsParser(reviewsList));
                reviewsList = new ArrayList<>(8);
            }
        }
        if (reviewsList.size() > 0) {
            threadPool.submit(new ReviewsParser(reviewsList));
        }

        phaser.arriveAndDeregister();
        int phase = phaser.getPhase();
        phaser.awaitAdvance(phase);
        threadPool.shutdownNow();

        return this;
    }

    /**
     * Function to add reviews from a ReviewsParser thread into the wtrMap
     *
     * @param interMap intermediate map from a ReviewsParser
     */
    private void addParsedReviews(Map<String, Map<Review, Integer>> interMap) {
        try {
            lock.writeLock().lock();
            for (Map.Entry<String, Map<Review, Integer>> word : interMap.entrySet()) {
                Map<Integer, Set<Review>> m = wtrMap.computeIfAbsent(word.getKey(), k ->
                        new TreeMap<>((a, b) -> b - a));

                for (Map.Entry<Review, Integer> e : word.getValue().entrySet()) {
                    m.computeIfAbsent(e.getValue(), k -> new TreeSet<>((r1, r2) ->
                            r2.datePosted().compareTo(r1.datePosted()))).add(e.getKey());
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Map function to check if a String is already a key
     *
     * @param word String to check against the map
     * @return boolean if the key exists
     */
    @Override
    public boolean containsKey(String word) {
        boolean r;
        try {
            lock.readLock().lock();
            r = super.containsKey(word);
        } finally {
            lock.readLock().unlock();
        }
        return r;
    }

    /**
     * Access to wordToReviewMap Review values; first sorts by word frequency (descending), then date posted (descending)
     *
     * @param word String key in wordToReviewMap
     * @return Stream of Reviews; how to use the data structure delegated to consumers
     */
    @Override
    public Stream<Review> findWord(String word) {
        Stream<Review> r;
        try {
            lock.readLock().lock();
            r = super.findWord(word);
        } finally {
            lock.readLock().unlock();
        }
        return r;
    }

    /**
     * Inner class defining the work to submit to a Thread
     */
    private class ReviewsParser implements Runnable {
        private final List<Set<Review>> reviews;

        public ReviewsParser(List<Set<Review>> s) {
            phaser.register();
            reviews = s;
        }

        @Override
        public void run() {
            try {
                Map<String, Map<Review, Integer>> interMap = new HashMap<>();

                for (Set<Review> s : reviews) {
                    for (Review r : s) {
                        String[] tokens = r.text().replaceAll("[^a-zA-Z' ]", " ").toLowerCase().split("\\s+");
                        for (String t : tokens) {
                            if (t.isBlank() || t.length() < 3 || stopWords.contains(t)) continue;

                            interMap.computeIfAbsent(t, k -> new HashMap<>());
                            interMap.get(t).merge(r, 1, Integer::sum);
                        }
                    }
                }

                addParsedReviews(interMap);
            } finally {
                phaser.arriveAndDeregister();
            }
        }
    }
}
