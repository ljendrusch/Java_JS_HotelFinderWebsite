package hotelreviewsdata;

import com.google.gson.*;

import java.lang.reflect.Type;


/**
 * Class to facilitate turning json data into Review objects
 */
public class HotelDeserializer implements JsonDeserializer<Hotel> {

    @Override
    public Hotel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jObject = json.getAsJsonObject();

        Address address = new Address(
            (jObject.has("ad")) ? jObject.get("ad").getAsString() : "",
            (jObject.has("ci")) ? jObject.get("ci").getAsString() : "",
            (jObject.has("pr")) ? jObject.get("pr").getAsString() : "",
            (jObject.has("c")) ? jObject.get("c").getAsString() : ""
            );

        return new Hotel(
            (jObject.has("f")) ? jObject.get("f").getAsString() : "",
            (jObject.has("id")) ? jObject.get("id").getAsLong() : 0,
            (jObject.has("ll") && jObject.get("ll").getAsJsonObject().has("lat")) ? jObject.get("ll").getAsJsonObject().get("lat").getAsDouble() : 0,
            (jObject.has("ll") && jObject.get("ll").getAsJsonObject().has("lng")) ? jObject.get("ll").getAsJsonObject().get("lng").getAsDouble(): 0,
            address
            );
    }
}
