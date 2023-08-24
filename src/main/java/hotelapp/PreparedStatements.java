package hotelapp;

/**
 * Utility class containing all SQL queries used by the DBHandlers
 */
public class PreparedStatements {
    /** SQL Check users table */
    public static final String CHECK_USERS_TABLE =
            "SHOW TABLES LIKE 'users';";

    /** SQL Create users table */
    public static final String CREATE_USERS_TABLE =
            "CREATE TABLE users (" +
                    "userid INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(32) NOT NULL UNIQUE, " +
                    "password CHAR(64) NOT NULL, " +
                    "salt CHAR(32) NOT NULL, " +
                    "last_login DATETIME, " +
                    "link_history TEXT, " +
                    "fav_hotels TEXT);";

    /** SQL Get last_login by username */
    public static final String GET_LAST_LOGIN =
            "SELECT last_login FROM users WHERE username = ?;";

    /** SQL Set last_login by username */
    public static final String SET_LAST_LOGIN =
            "UPDATE users SET last_login = NOW() WHERE username = ?;";

    /** SQL Get link_history by username */
    public static final String GET_LINK_HISTORY =
            "SELECT link_history FROM users WHERE username = ?;";

    /** SQL Set link_history by username */
    public static final String SET_LINK_HISTORY =
            "UPDATE users SET link_history = ? WHERE username = ?;";

    /** SQL Clear link_history by username */
    public static final String CLEAR_LINK_HISTORY =
            "UPDATE users SET link_history = NULL WHERE username = ?;";

    /** SQL Check if link in DB */
    public static final String CHECK_LINK =
            "SELECT hotelid FROM hotels WHERE link = ?;";

    /** SQL Get fav_hotels by username */
    public static final String GET_FAV_HOTELS =
            "SELECT fav_hotels FROM users WHERE username = ?;";

    /** SQL Set fav_hotels by username */
    public static final String SET_FAV_HOTELS =
            "UPDATE users SET fav_hotels = ? WHERE username = ?;";

    /** SQL Clear fav_hotels by username */
    public static final String CLEAR_FAV_HOTELS =
            "UPDATE users SET fav_hotels = NULL WHERE username = ?;";

    /** SQL Check hotels table exists */
    public static final String CHECK_HOTELS_TABLE =
            "SHOW TABLES LIKE 'hotels';";

    /** SQL Create hotels table */
    public static final String CREATE_HOTELS_TABLE =
            "CREATE TABLE hotels (" +
                    "hotelid MEDIUMINT UNSIGNED NOT NULL UNIQUE PRIMARY KEY, " +
                    "hotelname VARCHAR(64) NOT NULL, " +
                    "address VARCHAR(128), " +
                    "lat FLOAT, " +
                    "lng FLOAT, " +
                    "rating DECIMAL(2,1), " +
                    "link VARCHAR(255));";

    /** SQL Insert a hotel into hotels table */
    public static final String INSERT_HOTEL =
            "INSERT INTO hotels (hotelid, hotelname, address, lat, lng, rating, link) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);";

    /** SQL Search hotels by part of hotelname */
    public static final String SEARCH_HOTEL_BY_STRING =
            "SELECT * FROM hotels WHERE hotelname LIKE ?;";

    /** SQL Search all hotels */
    public static final String SEARCH_ALL_HOTELS =
            "SELECT * FROM hotels;";

    /** SQL Search hotel by id */
    public static final String SEARCH_HOTEL_BY_ID =
            "SELECT * FROM hotels WHERE hotelid = ?;";

    /** SQL Search slice of reviews by hotelid */
    public static final String GET_REVIEWS_SLICE =
            "SELECT username, title, body, dateposted FROM reviews WHERE hotelid = ? LIMIT ? OFFSET ?;";

    /** SQL Get number of reviews for a hotel */
    public static final String NUM_REVIEWS_FOR_HOTEL =
            "SELECT count(*) AS num_reviews FROM reviews WHERE hotelid = ?;";

    /** SQL Search review by id */
    public static final String SEARCH_REVIEW_BY_ID =
            "SELECT username, title, body, dateposted FROM reviews WHERE username = ? AND reviewid = ?;";

    /** SQL Search reviews by username */
    public static final String GET_MY_REVIEWS =
            "SELECT username, title, body, dateposted, reviewid FROM reviews WHERE username = ?;";

    /** SQL Delete review by username and reviewid */
    public static final String DELETE_REVIEW =
            "DELETE FROM reviews WHERE username = ? AND reviewid = ?;";

    /** SQL Check reviews table exists */
    public static final String CHECK_REVIEWS_TABLE =
            "SHOW TABLES LIKE 'reviews';";

    /** SQL Create reviews table */
    public static final String CREATE_REVIEWS_TABLE =
            "CREATE TABLE reviews (" +
                    "reviewid VARCHAR(24) NOT NULL PRIMARY KEY, " +
                    "hotelid MEDIUMINT UNSIGNED NOT NULL, " +
                    "username VARCHAR(32), " +
                    "title TEXT, " +
                    "body TEXT, " +
                    "dateposted DATE default NOW());";

    /** SQL Insert a review into reviews table */
    public static final String INSERT_REVIEW =
            "INSERT INTO reviews (reviewid, hotelid, username, title, body, dateposted) " +
                    "VALUES (?, ?, ?, ?, ?, ?);";

    /** SQL Insert a review into reviews table (DB will auto-populate dateposted) */
    public static final String INSERT_REVIEW_NO_DATE =
            "INSERT INTO reviews (reviewid, hotelid, username, title, body) " +
                    "VALUES (?, ?, ?, ?, ?);";

    /** SQL Modify a review's title and body in reviews table */
    public static final String UPDATE_REVIEW =
            "UPDATE reviews SET title = ?, body = ? " +
                    "WHERE username = ? AND reviewid = ?;";

    /** SQL Insert a new user into users */
    public static final String REGISTER_USER =
            "INSERT INTO users (username, password, salt) " +
                    "VALUES (?, ?, ?);";

    /** SQL Check if login credentials are correct in users */
    public static final String LOGIN_USER =
            "SELECT username FROM users " +
                    "WHERE username = ? AND password = ?";

    /** SQL Get password salt string from users by username */
    public static final String GET_SALT =
            "SELECT salt FROM users WHERE username = ?";
}
