package hotelreviewsdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Class to handle input/output over console
 */
public class UserIO {
    HotelSearch hotelSearch;

    public UserIO(HotelSearch h) {
        hotelSearch = h;
    }

    public void run() {
        System.out.println();
        printQueries();
        System.out.println();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String line = br.readLine();
                if (line.isBlank()) continue;
                line = line.trim().toLowerCase();

                if (line.compareTo("q") == 0) break;
                if (line.compareTo("h") == 0) { printQueries(); continue; }

                String[] tokens = line.split("\\s+");

                if (tokens.length != 2) { printInvalidQuery(line); continue; }

                if (tokens[0].compareTo("find") == 0) {
                    int id;
                    try {
                        id = Integer.parseInt(tokens[1]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        printInvalidQuery(line);
                        continue;
                    }

                    hotelSearch.printHotel(id);
                }

                if (tokens[0].compareTo("findreviews") == 0) {
                    int id;
                    try {
                        id = Integer.parseInt(tokens[1]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        printInvalidQuery(line);
                        continue;
                    }

                    hotelSearch.printReviews(id);
                }

                if (tokens[0].compareTo("findword") == 0) {
                    hotelSearch.printWord(tokens[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error opening standard in");
            System.out.println("Now exiting");
            System.exit(0);
        }
    }

    /**
     * Terminal output explaining program usage
     */
    static void printUsage() {
        System.out.print("""
                    Hotel Search
                --------------------
                Find information on select hotels
                and browse their reviews
                
                    Usage
                -------------
                ./HotelSearch -hotels [hotels.json] -reviews [review_directory] -threads [num_threads] -output [output_file.txt]
                """);
    }

    /**
     * Terminal output explaining queries
     */
    static void printQueries() {
        System.out.print("""
                    Valid Commands
                --------------------
                find [hotel_id]
                findReviews [hotel_id]
                findWord [word]
                q to quit
                """);
    }

    /**
     * Terminal output on invalid query
     * @param query String invalid user input
     */
    static void printInvalidQuery(String query) {
        System.out.println("Unable to parse the query");
        System.out.println("  \"" + query + "\"");
        System.out.println("Enter \"h\" for help");
    }
}
