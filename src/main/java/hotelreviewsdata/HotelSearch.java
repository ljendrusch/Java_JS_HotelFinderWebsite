package hotelreviewsdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** Main class. Parse .json files containing hotel and review data,
 * then query that data through the command line.
 *
 * Usage:
 * ./HotelSearch -hotels [hotels.json] -reviews [review_directory]
 *
 * Command-line queries:
 *   find [hotel_id]          - find information on the given hotel
 *   findReviews [hotel_id]   - find reviews on the given hotel
 *   findWord [word]          - find reviews that contain the given word
 *   q to quit
 *   h for help
 */
public class HotelSearch {
    private ThreadSafeHotelReviewsMap hrMap;
    private ThreadSafeWordToReviewMap wtrMap;
    private Map<String, String> argsMap;

    public HotelSearch() {
        parseArgs(new String[]{});
    }
    public HotelSearch(String[] args) {
        parseArgs(args);
    }

    /**
     * Takes command line input and parses it into a map
     * @param args command line input
     */
    public void parseArgs(String[] args) {
        argsMap = new HashMap<>();
        argsMap.put("-hotels", "input/hotels/hotels.json");
        argsMap.put("-reviews", "input/reviews");
        argsMap.put("-threads", "4");
        argsMap.put("-output", "output/out.txt");

        for (int i = 0; i < args.length; i+=2) {
            String arg = args[i].toLowerCase().trim();

            if (!argsMap.containsKey(arg)) {
                UserIO.printUsage();
                System.exit(0);
            }

            argsMap.put(arg, args[i+1].trim());
        }
    }

    /**
     * Factory function that facilitates building the major data structures
     */
    public HotelSearch init() {
        String hotelsFileString = argsMap.get("-hotels");
        String reviewsDirString = argsMap.get("-reviews");
        int numThreads = Integer.parseInt(argsMap.get("-threads"));

        hrMap = new ThreadSafeHotelReviewsMap(numThreads).init(hotelsFileString, reviewsDirString);
        wtrMap = new ThreadSafeWordToReviewMap(numThreads).init(hrMap);

        return this;
    }

    /**
     * Prints all Hotels and Reviews to a file
     */
    public void printToFile() {
        String outFilename = argsMap.get("-output");
        hrMap.printToFile(outFilename);
    }

    /**
     * Main. Parses command-line args, delegates file opening and object construction from json files,
     * and manages terminal i/o
     * @param args -hotels [hotels.json] -reviews [review_directory]
     */
    public static void main(String[] args) {
        if (!(args.length == 0 || args.length == 2 || args.length == 4 || args.length == 6 || args.length == 8)) {
            UserIO.printUsage();
            System.exit(0);
        }

        HotelSearch hotelSearch = new HotelSearch(args).init();
        hotelSearch.printToFile();

        UserIO cli = new UserIO(hotelSearch);
        cli.run();
    }

    /**
     * Function to facilitate the find [hotel_id] query
     * @param id long hotel id
     */
    public void printHotel(long id) {
        System.out.println("Finding hotel " + id);
        if (hrMap.containsKey(id)) {
            Hotel h = hrMap.findHotel(id);
            if (h == null) {
                System.out.println("No information on that hotel");
            }
            System.out.println("\n********************");
            System.out.println(h);
        } else {
            System.out.println("No hotel matches that id");
        }
    }

    /**
     * Function to facilitate iterating through hotels and reviews
     * @return unmodifiable List of Longs containing all hotel ids
     */
    public List<Long> returnAllHotelIds() {
        return hrMap.hrMap.keySet().stream().toList();
    }

    /**
     * Function to facilitate the find [hotel_id] server query
     * @param id long hotel id
     */
    public Hotel returnHotel(long id) {
        if (!hrMap.containsKey(id)) {
            return null;
        }

        return hrMap.findHotel(id);
    }

    /**
     * Function to facilitate website hotel search
     * @param id long hotel id
     */
    public Double getHotelRating(long id) {
        List<Review> reviews = returnReviews(id);
        if (reviews == null || reviews.isEmpty()) {
            return null;
        }

        int n = reviews.size();
        int sum = reviews.stream().map(Review::ratingOverall).reduce(0, Integer::sum);

        return (double)sum / (double)n;
    }

    /**
     * Function to facilitate website hotel search
     * @param id long hotel id
     */
    public String getHotelLink(long id) {
        Hotel h = returnHotel(id);
        if (h == null) return null;

        // if it has a " - " remove everything past it, remove all non-alphanum chars, trim, replace spaces with "-"
        String hotelName = h.name();
        int idx;
        if ((idx = hotelName.indexOf(" - ")) >= 0)
            hotelName = hotelName.substring(0, idx).trim();
        hotelName = hotelName.replaceAll("[^\\w\\s]", "").replace(" ", "-");
        return "expedia.com/" + h.address().city().replace(" ", "-") + "-Hotels-" + hotelName + ".h" + h.id() +  ".Hotel-Information";
    }

    /**
     * Function to facilitate the findReviews [hotel_id] query
     * @param id long hotel id
     */
    public void printReviews(long id) {
        System.out.println("Finding reviews for hotel " + id);
        if (hrMap.containsKey(id)) {
            List<Review> reviews = hrMap.findReviews(id).toList();
            if (reviews.size() == 0) {
                System.out.println("No reviews exist for that hotel");
            } else {
                reviews.forEach((r) -> {
                    System.out.println("--------------------");
                    System.out.println(r);
                });
            }
        } else {
            System.out.println("No hotel matches that id");
        }
    }

    /**
     * Function to facilitate the findReviews [hotel_id] server query
     * @param id long hotel id
     */
    public List<Review> returnReviews(long id) {
        if (!hrMap.containsKey(id)) return null;

        List<Review> reviews = hrMap.findReviews(id).toList();
        if (reviews.isEmpty()) return null;

        return reviews;
    }

    /**
     * Function to facilitate the findReviews [hotel_id] server query
     * @param id long hotel id
     */
    public List<Review> returnReviews(long id, int num) {
        if (!hrMap.containsKey(id)) return null;

        List<Review> reviews = hrMap.findReviews(id).toList();
        if (reviews.isEmpty()) return null;

        return reviews.subList(0, num);
    }

    /**
     * Function to facilitate the findWord [word] query
     * @param word word
     */
    public void printWord(String word) {
        System.out.println("Finding reviews with the word \"" + word + "\"");
        if (wtrMap.containsKey(word)) {
            List<Review> reviews = wtrMap.findWord(word).toList();
            if (reviews.size() == 0) {
                System.out.println("No reviews exist for that hotel");
            } else {
                reviews.forEach(r -> {
                    System.out.println("--------------------");
                    System.out.println(r);
                });
            }
        } else {
            System.out.println("No reviews use that word");
        }
    }

    /**
     * Function to facilitate the findWord [word] server query
     * @param word word
     */
    public List<Review> returnWord(String word, int num) {
        if (!wtrMap.containsKey(word)) return null;

        List<Review> reviews = wtrMap.findWord(word).toList();
        if (reviews.isEmpty()) return null;

        return reviews.subList(0, num);
    }
}
