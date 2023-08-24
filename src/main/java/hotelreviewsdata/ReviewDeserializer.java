package hotelreviewsdata;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * Class to facilitate turning json data into Review objects
 */
public class ReviewDeserializer implements JsonDeserializer<Review> {

    @Override
    public Review deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jObject = json.getAsJsonObject();

        LocalDate datePosted = null;
        if (jObject.has("reviewSubmissionTime")) {
            datePosted = LocalDate.parse(jObject.get("reviewSubmissionTime").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
        }

        return new Review(
                (jObject.has("hotelId")) ? jObject.get("hotelId").getAsLong() : 0,
                (jObject.has("reviewId")) ? jObject.get("reviewId").getAsString() : "",
                (jObject.has("ratingOverall")) ? jObject.get("ratingOverall").getAsInt() : 0,
                (jObject.has("title")) ? jObject.get("title").getAsString() : "", // .replaceAll("[\r\n\t]", " ") : "",
                (jObject.has("reviewText")) ? jObject.get("reviewText").getAsString() : "", // .replaceAll("[\r\n\t]", " ") : "",
                (jObject.has("userNickname") && !jObject.get("userNickname").getAsString().isBlank()) ? jObject.get("userNickname").getAsString() : "Anonymous",
                datePosted
        );
    }
}
