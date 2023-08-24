package hotelapp;

import hotelreviewsdata.Hotel;
import hotelreviewsdata.HotelSearch;
import hotelreviewsdata.Review;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;


/**
 * Singleton class that handles all transactions with the SQL database tables
 */
public class DBTablesHandler {
    private static final DBTablesHandler instance = new DBTablesHandler();
    private final Properties config;
    private final String uri;

    private DBTablesHandler() {
        Properties p = new Properties();
        try (FileReader fr = new FileReader("database.properties")) {
            p.load(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.config = p;
        this.uri = "jdbc:mysql://"+ config.getProperty("hostname") + "/" + config.getProperty("username") + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    }
    public static DBTablesHandler get() { return instance; }

    private boolean checkUsersTable() {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            Statement statement = dbConnection.createStatement();
            statement.executeQuery(PreparedStatements.CHECK_USERS_TABLE);

            if (statement.getResultSet().next())
                flag = true;

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    private void createUsersTable() {
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(PreparedStatements.CREATE_USERS_TABLE);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkHotelsTable() {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            Statement statement = dbConnection.createStatement();
            statement.executeQuery(PreparedStatements.CHECK_HOTELS_TABLE);

            if (statement.getResultSet().next())
                flag = true;

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    private void createHotelsTable() {
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(PreparedStatements.CREATE_HOTELS_TABLE);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateHotelsTable(HotelSearch hs) {
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            dbConnection.setAutoCommit(true);
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.INSERT_HOTEL);

            Hotel h;
            for (Long l : hs.returnAllHotelIds()) {
                if ((h = hs.returnHotel(l)) != null) {
                    ps.setLong(1, h.id()); // hotelid MEDIUMINT UNSIGNED
                    ps.setString(2, h.name()); // hotelname VARCHAR(64)
                    ps.setString(3, h.address().toString()); // address VARCHAR(128)
                    ps.setFloat(4, (float) h.latitude()); // lat DECIMAL(4,2)
                    ps.setFloat(5, (float) h.longitude()); // lng DECIMAL(4,2)
                    ps.setObject(6, hs.getHotelRating(l), JDBCType.DECIMAL); // rating DECIMAL(2,1)
                    ps.setString(7, hs.getHotelLink(l)); // link VARCHAR(255)
                    ps.addBatch();
                }
            }

            ps.executeBatch();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkReviewsTable() {
        boolean flag = false;
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            Statement statement = dbConnection.createStatement();
            statement.executeQuery(PreparedStatements.CHECK_REVIEWS_TABLE);
            if (statement.getResultSet().next())
                flag = true;
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    private void createReviewsTable() {
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(PreparedStatements.CREATE_REVIEWS_TABLE);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateReviewsTable(HotelSearch hs) {
        try (Connection dbConnection = DriverManager.getConnection(uri, config.getProperty("username"), config.getProperty("password"))) {
            dbConnection.setAutoCommit(true);
            PreparedStatement ps = dbConnection.prepareStatement(PreparedStatements.INSERT_REVIEW);

            List<Review> rs;
            LocalDate ld;
            for (Long l : hs.returnAllHotelIds()) {
                if ((rs = hs.returnReviews(l)) != null) {
                    for (Review r : rs) {
                        ps.setString(1, r.reviewId()); // reviewid VARCHAR(24)
                        ps.setLong(2, r.hotelId()); // hotelid MEDIUMINT UNSIGNED
                        ps.setString(3, r.username()); // username VARCHAR(32)
                        ps.setObject(4, r.title(), JDBCType.LONGVARCHAR); // title TEXT
                        ps.setObject(5, r.text(), JDBCType.LONGVARCHAR); // text TEXT
                        if ((ld = r.datePosted()) != null)
                            ps.setDate(6, Date.valueOf(ld)); // datetimeposted DATETIME
                        else
                            ps.setDate(6, Date.valueOf(LocalDate.MIN)); // datetimeposted DATETIME
                        ps.addBatch();
                    }
                }
            }

            ps.executeBatch();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if necessary tables exist, and if not,
     * create and populate them
     */
    public void checkTables() {
        // to clear tables, do 'truncate table [table_name];' in mysql
        if (!checkUsersTable())
            createUsersTable();

        boolean checkHotelsTable = checkHotelsTable();
        boolean checkReviewsTable = checkReviewsTable();
        if (!checkHotelsTable || !checkReviewsTable) {
            HotelSearch hs = new HotelSearch().init();

            if (!checkHotelsTable) {
                createHotelsTable();
                populateHotelsTable(hs);
            }

            if (!checkReviewsTable) {
                createReviewsTable();
                populateReviewsTable(hs);
            }
        }
    }
}
