package hotelreviewsdata;

import java.time.LocalDate;


/**
 * Record for a Review
 * @param hotelId long hotelId of the associated Hotel
 * @param reviewId review id; String representation of a 12-byte hexadecimal value
 * @param ratingOverall double user's rating of the hotel
 * @param title String title of the Review; often empty or very long, becoming a first chapter to Review.text
 * @param text String content of the Review
 * @param username String nickname of the user associated with the Review
 * @param datePosted LocalDate date the Review was posted
 */
public record Review(long hotelId, String reviewId, int ratingOverall, String title, String text, String username,
                     LocalDate datePosted) implements Comparable<Review> {

    /**
     * Computes the content of the Review, including title and text if either exists
     * @return String concatenation of title and text
     */
    public synchronized String fullText() {
        if ((title == null || title.isBlank()) && (text == null || text.isBlank())) {
            return "";
        }
        if (title == null || title.isBlank()) {
            return text;
        }
        if (text == null || text.isBlank()) {
            return title;
        }

        return title + " " + text;
    }

    /**
     * Override function for Comparable inheritance; orders Reviews
     * by date posted, descending, then review id, ascending
     * @param o the other Review to be compared
     * @return int to denote order of the two Reviews
     */
    @Override
    public synchronized int compareTo(Review o) {
        if (o == null) return 1;

        int moreRecent = o.datePosted.compareTo(this.datePosted);
        if (moreRecent != 0) return moreRecent;

        return this.reviewId.compareTo(o.reviewId);
    }

    /**
     * Checks if two Reviews are equivalent; used in Sets and HashMap keys
     * @param obj the reference object with which to compare
     * @return boolean true if equivalent, false otherwise
     */
    @Override
    public synchronized boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Review) obj;
        return this.reviewId.compareTo(that.reviewId) == 0;
    }

    /**
     * Returns a code to identify a Review object
     * @return int hash of this Review's reviewId String
     */
    @Override
    public synchronized int hashCode() {
        return reviewId.hashCode();
    }

    @Override
    public synchronized String toString() {
        return String.format("Review by %s on %s\nRating: %d\nReviewId: %s\n%s\n%s",
                username, datePosted, ratingOverall, reviewId, title, text);
    }
}
