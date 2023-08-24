package hotelreviewsdata;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Class holding an inverted-index data structure to facilitate fast
 * querying of Reviews by word
 */
public class WordToReviewMap {
    protected static final Set<String> stopWords = Stream.of("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now")
            .collect(Collectors.toCollection(HashSet::new));
    protected final Map<String, Map<Integer, Set<Review>>> wtrMap;

    public WordToReviewMap() {
        wtrMap = new HashMap<>();
    }

    /**
     * Maps all words in every Review to Reviews that contain them
     * and respective word frequency within each review
     * @param mapIn Map data structure to pull objects from
     */
    public WordToReviewMap build(HotelReviewsMap mapIn) {
        for (Map.Entry<Hotel, Set<Review>> e : mapIn.hrMap.values()) {
            if (e == null) continue;

            Set<Review> s = e.getValue();
            if (s == null || s.size() == 0) continue;

            parseReviews(s);
        }

        return this;
    }

    /**
     * Function to parse reviews from an hrMap into the wtrMap
     * @param reviews Set of reviews pulled from an hrMap
     */
    private void parseReviews(Set<Review> reviews) {
        Map<String, Map<Review, Integer>> interMap = new HashMap<>();

            for (Review r : reviews) {
                String[] tokens = r.text().replaceAll("[^a-zA-Z' ]", " ").toLowerCase().split("\\s+");
                for (String t : tokens) {
                    if (t.isBlank() || t.length() < 3 || stopWords.contains(t)) continue;

                    interMap.computeIfAbsent(t, k -> new HashMap<>());
                    interMap.get(t).merge(r, 1, Integer::sum);
                }
            }

            for (Map.Entry<String, Map<Review, Integer>> word : interMap.entrySet()) {
                Map<Integer, Set<Review>> m = wtrMap.computeIfAbsent(word.getKey(), k ->
                        new TreeMap<>((a, b) -> b - a));

                for (Map.Entry<Review, Integer> e : word.getValue().entrySet()) {
                    m.computeIfAbsent(e.getValue(), k -> new TreeSet<>((r1, r2) ->
                            r2.datePosted().compareTo(r1.datePosted()))).add(e.getKey());
                }
            }
    }

    /**
     * Map function to check if a String is already a key
     * @param word String to check against the map
     * @return boolean if the key exists
     */
    public boolean containsKey(String word) {
        return wtrMap.containsKey(word);
    }

    /**
     * Access to wordToReviewMap Review values; first sorts by word frequency (descending), then date posted (descending)
     * @param word String key in wordToReviewMap
     * @return Stream of Reviews; how to use the data structure delegated to consumers
     */
    public Stream<Review> findWord(String word) {
        return wtrMap.get(word).entrySet().stream()
                .flatMap(e -> e.getValue().stream());
    }
}
