package hotelreviewsdata;

/**
 * Record to store hotel data
 * @param name String name of the Hotel
 * @param id long id, often up to 10 digits, radix 10
 * @param latitude double latitude
 * @param longitude double longitude
 * @param address Address containing street, city, state/province, country Strings
 */
public record Hotel(String name, long id, double latitude, double longitude, Address address) {

    @Override
    public synchronized String toString() {
        return String.format("%s: %d\n%s", name, id, address);
    }
}
