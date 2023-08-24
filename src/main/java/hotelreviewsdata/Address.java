package hotelreviewsdata;

/**
 * Record for an address
 * @param street String for street name
 * @param city String for city name
 * @param state String for state or province name
 * @param country String for country name
 */
public record Address(String street, String city, String state, String country) {

    @Override
    public String toString() {
        return String.format("%s\n%s, %s", street, city, state);
    }
}
