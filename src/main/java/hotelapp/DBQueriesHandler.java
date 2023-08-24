package hotelapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.security.MessageDigest;
import java.util.Random;


/**
 * Singleton class that handles all transactions with SQL database data
 */
public class DBQueriesHandler {
    private static final DBQueriesHandler instance = new DBQueriesHandler();
    private final Properties config;
    private final String uri;
    private final Random rand = new Random();

    private DBQueriesHandler() {
        Properties p = new Properties();
        try (FileReader fr = new FileReader("database.properties")) {
            p.load(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.config = p;
        this.uri = "jdbc:mysql://"+ config.getProperty("hostname") + "/" + config.getProperty("username") + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    }
    public static DBQueriesHandler get() { return instance; }

    private List<String> reviewString(ResultSet rs, boolean reviewLink) {
        List<String> review = new ArrayList<>();
        try {
            if (!reviewLink) {
                String username = rs.getString(1);
                if (username == null || username.isBlank()) username = "Anonymous";
                review.add("Review by " + username);
            }
            review.add("Date posted: " + rs.getDate(4));
            if (reviewLink) review.add("<br>");

            String title = rs.getString(2);
            if (title == null || title.isBlank()) title = "[title blank]";
            review.add("Title:");
            if (reviewLink) review.add("<br>");
            review.add(title);
            if (reviewLink) review.add("<br>");

            String text = rs.getString(3);
            if (text == null || text.isBlank()) text = "[review text blank]";
            review.add("Text:");
            if (reviewLink) review.add("<br>");
            review.add(text);
            if (reviewLink) review.add("<br>");

            if (reviewLink) {
                review.add("<a href=\"/myReviews?id=" + rs.getString(5) + "\"><button class=\"btn btn-default\">Edit</button></a>");
                review.add("<a href=\"/myReviews?id=" + rs.getString(5) + "&type=delete\"><button class=\"btn btn-default\">Delete</button></a>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (review.size() == 0) ? null : review;
    }

    /**
     * Find all hotels in the database that have hotelNameFragment in their hotelname
     * @param hotelNameFragment String hotel name fragment to search on
     * @return JsonArray of JsonObjects representing hotels
     */
    public JsonArray searchHotels(String hotelNameFragment, String username) {
        if (hotelNameFragment.isBlank()) return searchAllHotels(username);

        JsonArray ja = new JsonArray();
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.SEARCH_HOTEL_BY_STRING);
            String frag = "%" + hotelNameFragment + "%";
            ps.setString(1, frag);

            ResultSet rs = ps.executeQuery();
            FavHotels fh = FavHotels.of(username);
            while (rs.next()) {
                JsonObject hotel = new JsonObject();
                hotel.addProperty("hotelid", rs.getInt(1));
                hotel.addProperty("hotelname", rs.getString(2));
                hotel.addProperty("address", rs.getString(3));
                hotel.addProperty("lat", rs.getFloat(4));
                hotel.addProperty("lng", rs.getFloat(5));
                hotel.addProperty("rating", rs.getFloat(6));
                hotel.addProperty("link", rs.getString(7));
                hotel.addProperty("fav", fh.contains(rs.getString(1)));
                ja.add(hotel);
            }

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ja;
    }

    /**
     * Find all hotels in the database
     * @return JsonArray of JsonObjects representing hotels
     */
    private JsonArray searchAllHotels(String username) {
        JsonArray ja = new JsonArray();
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.SEARCH_ALL_HOTELS);

            ResultSet rs = ps.executeQuery();
            FavHotels fh = FavHotels.of(username);
            while (rs.next()) {
                JsonObject hotel = new JsonObject();
                hotel.addProperty("hotelid", rs.getInt(1));
                hotel.addProperty("hotelname", rs.getString(2));
                hotel.addProperty("address", rs.getString(3));
                hotel.addProperty("lat", rs.getFloat(4));
                hotel.addProperty("lng", rs.getFloat(5));
                hotel.addProperty("rating", rs.getFloat(6));
                hotel.addProperty("link", rs.getString(7));
                hotel.addProperty("fav", fh.contains(rs.getString(1)));
                ja.add(hotel);
            }

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ja;
    }

    /**
     * Find a hotel in the database with id hotelId
     * @param hotelId long hotel id to select on
     * @return JsonObject containing a hotel
     */
    public JsonObject searchHotel(long hotelId, String username) {
        JsonObject jo = new JsonObject();
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.SEARCH_HOTEL_BY_ID);
            ps.setLong(1, hotelId);

            ResultSet rs = ps.executeQuery();
            FavHotels fh = FavHotels.of(username);
            if (rs.next()) {
                jo.addProperty("hotelid", rs.getInt(1));
                jo.addProperty("hotelname", rs.getString(2));
                jo.addProperty("address", rs.getString(3));
                jo.addProperty("lat", rs.getFloat(4));
                jo.addProperty("lng", rs.getFloat(5));
                jo.addProperty("rating", rs.getFloat(6));
                jo.addProperty("link", rs.getString(7));
                jo.addProperty("fav", fh.contains(rs.getString(1)));
            }

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jo;
    }

    /**
     * Gets a slice of the reviews for hotel with hotelId
     * @param hotelId long hotelId of hotel
     * @param limit int number of reviews to return
     * @param offset int index of first review to return
     * @return JsonObject containing a JsonArray of review JsonObjects and
     * property "len" with the size of the full review set for respective hotel
     */
    public JsonObject getReviewsSlice(long hotelId, int limit, int offset) {
        // username, title, body, dateposted
        JsonObject jo = new JsonObject();
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            JsonArray ja = new JsonArray();

            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.GET_REVIEWS_SLICE);
            ps.setLong(1, hotelId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JsonObject review = new JsonObject();
                review.addProperty("username", rs.getString(1));
                review.addProperty("title", rs.getString(2));
                review.addProperty("text", rs.getString(3));
                review.addProperty("dateposted", rs.getString(4));
                ja.add(review);
            }
            jo.add("reviews", ja);

            ps = dbConnection.prepareStatement(PreparedStatements.NUM_REVIEWS_FOR_HOTEL);
            ps.setLong(1, hotelId);

            rs = ps.executeQuery();
            int len = 0;
            if (rs.next())
                len = rs.getInt(1);
            jo.addProperty("len", len);

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jo;
    }

    /**
     * Checks if a hotel with id from idString exists in the database
     * @param idString String to turn into a long and select on
     * @return true if idString corresponds to a hotel id
     */
    public boolean checkHotelId(String idString) {
        if (!idString.matches("^\\d+$")) return false;

        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.SEARCH_HOTEL_BY_ID);
            ps.setLong(1, Long.parseLong(idString));

            if (ps.executeQuery().next())
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Checks if a hotel with id from idString exists in the database
     * @param hotelName String to select on
     * @return true if hotelName corresponds to a hotel name
     */
    public boolean checkHotelName(String hotelName) {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.SEARCH_HOTEL_BY_STRING);
            ps.setString(1, "%" + hotelName + "%");

            if (ps.executeQuery().next())
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Find a review based on username and review id
     * @param username String username
     * @param reviewId String review id
     * @return List of Strings of a review broken up into lines,
     * or an empty list if no review found
     */
    public List<String> findMyReview(String username, String reviewId) {
        List<String> review = new ArrayList<>();
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.SEARCH_REVIEW_BY_ID);
            ps.setString(1, username);
            ps.setString(2, reviewId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                List<String> r = reviewString(rs, false);
                if (r != null)
                    review = r;
            }

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return review;
    }

    /**
     * Find all of a user's reviews
     * @param username String username
     * @return List of Lists of Strings of reviews broken up into lines,
     * or an empty list if no reviews found
     */
    public List<List<String>> getMyReviews(String username) {
        List<List<String>> myReviews = new ArrayList<>();
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.GET_MY_REVIEWS);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                List<String> review = reviewString(rs, true);
                if (review != null)
                    myReviews.add(review);
            }

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return myReviews;
    }

    /**
     * Delete a review by username and review id
     * @param username String username
     * @param reviewId String review id
     * @return true if review found and deleted
     */
    public boolean deleteReview(String username, String reviewId) {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.DELETE_REVIEW);
            ps.setString(1, username);
            ps.setString(2, reviewId);

            if (ps.executeUpdate() > 0)
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Add a review
     * @param reviewId String review id
     * @param hotelId long hotel id
     * @param username String username
     * @param title String review title
     * @param text String review body
     * @return true if review successfully added
     */
    public boolean insertReview(String reviewId, long hotelId, String username, String title, String text) {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.INSERT_REVIEW_NO_DATE);
            ps.setString(1, reviewId); // reviewid VARCHAR(24)
            ps.setLong(2, hotelId); // hotelid MEDIUMINT UNSIGNED
            ps.setString(3, username); // username VARCHAR(32)
            ps.setObject(4, title, JDBCType.LONGVARCHAR); // title TEXT
            ps.setObject(5, text, JDBCType.LONGVARCHAR); // text TEXT

            if (ps.executeUpdate() > 0)
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Edit a review
     * @param title String review title
     * @param text String review body
     * @param username String username
     * @param reviewId String review id
     * @return true if review successfully updated
     */
    public boolean updateReview(String title, String text, String username, String reviewId) {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.UPDATE_REVIEW);
            ps.setObject(1, title, JDBCType.LONGVARCHAR);
            ps.setObject(2, text, JDBCType.LONGVARCHAR);
            ps.setString(3, username);
            ps.setString(4, reviewId);

            if (ps.executeUpdate() > 0)
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Returns the hex encoding of a byte array.
     *
     * @param bytes - byte array to encode
     * @param length - desired length of encoding
     * @return hex encoded byte array
     */
    public static String encodeHex(byte[] bytes, int length) {
        BigInteger bigint = new BigInteger(1, bytes);
        String hex = String.format("%0" + length + "X", bigint);

        assert hex.length() == length;
        return hex;
    }

    /**
     * Calculates the hash of a password and salt using SHA-256.
     *
     * @param password - password to hash
     * @param salt - salt associated with user
     * @return hashed password
     */
    public static String getHash(String password, String salt) {
        String salted = salt + password;
        String hashed = salted;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salted.getBytes());
            hashed = encodeHex(md.digest(), 64);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hashed;
    }

    /**
     * Registers a new user, placing the username, password hash, and
     * salt into the database.
     *
     * @param username - username of new user
     * @param password - password of new user
     */
    public boolean registerUser(String username, String password) {
        boolean flag = false;

        byte[] saltBytes = new byte[16];
        rand.nextBytes(saltBytes);

        String salt = encodeHex(saltBytes, 32); // salt
        String hashedPw = getHash(password, salt); // hashed password

        PreparedStatement ps;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            ps = dbConnection.prepareStatement(PreparedStatements.REGISTER_USER);
            ps.setString(1, username);
            ps.setString(2, hashedPw);
            ps.setString(3, salt);
            if (ps.executeUpdate() > 0)
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return flag;
    }

    /**
     * Registers a new user, placing the username, password hash, and
     * salt into the database.
     *
     * @param username - username of new user
     * @param password - password of new user
     */
    public boolean loginUser(String username, String password) {
        boolean flag = false;

        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement saltStatement = dbConnection.prepareStatement(PreparedStatements.GET_SALT);
            saltStatement.setString(1, username);

            ResultSet saltRs = saltStatement.executeQuery(); // salt
            if (!saltRs.next()) {
                saltStatement.close();
                return false;
            }

            String salt = saltRs.getString(1);
            String hashedPw = getHash(password, salt); // hashed password
            saltStatement.close();

            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.LOGIN_USER);
            statement.setString(1, username);
            statement.setString(2, hashedPw);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                flag = true;

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return flag;
    }

    /**
     * Returns last login time and updates the DB with current login time
     * @param username String username to cycle last login
     * @return String representation of last login time
     */
    public String cycleLastLogin(String username) {
        String lastLogin = null;

        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.GET_LAST_LOGIN);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next())
                lastLogin = rs.getString(1);

            ps = dbConnection.prepareStatement(PreparedStatements.SET_LAST_LOGIN);
            ps.setString(1, username);
            ps.executeUpdate();

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (lastLogin == null) ? "First time user!" : "Last Login: " + lastLogin;
    }

    /**
     * Gets link history of user from DB
     * @param username String name of user to check link history on
     * @return String representation of full link history
     */
    public String getLinkHistory(String username) {
        String linkHistory = null;

        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.GET_LINK_HISTORY);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next())
                linkHistory = rs.getString(1);

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return linkHistory;
    }

    /**
     * Sets link history of user to DB
     * @param username String name of user to set link history on
     * @return boolean success or failure of SQL update query
     */
    public boolean setLinkHistory(String username, String linkHistory) {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.SET_LINK_HISTORY);
            ps.setObject(1, linkHistory, JDBCType.LONGVARCHAR);
            ps.setString(2, username);

            if (ps.execute())
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Clears link history of user in DB
     * @param username String name of user to clear link history on
     * @return boolean success or failure of SQL delete query
     */
    public boolean clearLinkHistory(String username) {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.CLEAR_LINK_HISTORY);
            ps.setString(1, username);

            if (ps.execute())
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Checks DB for proper link query
     * @param link String link to check
     * @return boolean link is in DB or not
     */
    public boolean checkLink(String link) {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.CHECK_LINK);
            ps.setString(1, link);

            if (ps.executeQuery().next())
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Gets favorited hotels of user from DB
     * @param username String name of user to fav hotels on
     * @return String representation of all fav hotels
     */
    public String getFavHotels(String username) {
        String favHotels = null;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.GET_FAV_HOTELS);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next())
                favHotels = rs.getString(1);

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favHotels;
    }

    /**
     * Sets favorited hotels of user to DB
     * @param username String name of user to set fav hotels on
     * @return boolean success or failure of SQL update query
     */
    public boolean setFavHotels(String username, String favHotels) {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.SET_FAV_HOTELS);
            ps.setObject(1, favHotels, JDBCType.LONGVARCHAR);
            ps.setString(2, username);

            if (ps.executeUpdate() > 0)
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Clears favorited hotels of user in DB
     * @param username String name of user to clear fav hotels on
     * @return boolean success or failure of SQL delete query
     */
    public boolean clearFavHotels(String username) {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.CLEAR_FAV_HOTELS);
            ps.setString(1, username);

            if (ps.execute())
                flag = true;

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
