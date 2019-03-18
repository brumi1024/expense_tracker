package hu.benjaminteke.expensecalculator.datamodel;

import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager {
    private final static Logger LOGGER = Logger.getLogger(DataManager.class.getName());

    /**
     * Date time format to convert between string and timestamp.
     */
    private static SimpleDateFormat dtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Write data to the database and to the spreadsheet (if any of them is available).
     *
     * @param expense expense to write
     */
    public static void writeData(Double expense) {
        Date now = new Date();
        String timeString = dtf.format(now);

        try {
            if (DatabaseConnection.isConnected()) {
                DatabaseConnection.writeOne(timeString, expense);
            }

            SheetManager.writeOne(timeString, expense);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not write to spreadsheet. {0}", e.toString());
        } catch (SQLException s) {
            LOGGER.log(Level.SEVERE, "Could not write to DB. {0}", s.toString());
        }
    }

    /**
     * Read all data from the database or the spreadsheet.
     *
     * @return Object[][] with the data.
     */
    public static Object[][] readAllData() {
        try {
            if (DatabaseConnection.isConnected()) {
                return DatabaseConnection.readAll();
            } else {
                return SheetManager.getAll();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not read to spreadsheet. {0}", e.toString());
        } catch (SQLException s) {
            LOGGER.log(Level.SEVERE, "Could not read to DB. {0}", s.toString());
        }
        return new Object[0][];
    }

    /**
     * Get the last row from the available data source.
     *
     * @return An Object[] with the last row.
     */
    public static Object[] readLastData() {
        try {
            if (DatabaseConnection.isConnected()) {
                return DatabaseConnection.readLastValue().getObject();
            } else {
                return SheetManager.getLastValue();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not read to spreadsheet. {0}", e.toString());
        } catch (SQLException s) {
            LOGGER.log(Level.SEVERE, "Could not read to DB. {0}", s.toString());
        }
        return new Object[0][];
    }

    /**
     * Sync the data using Guava's MapDifference between the two data sources.
     *
     * @return false if failed, true if successful.
     */
    public static boolean sync() {
        Map<Timestamp, Object[]> db;
        Map<Timestamp, Object[]> docs;
        Map<Timestamp, Object[]> differing;

        try {
            LOGGER.log(Level.FINE, "Syncing the data.");

            if (DatabaseConnection.isConnected()) {
                List<Object[]> dbElements = Lists.newArrayList(DatabaseConnection.readAll());

                List<Object[]> docsElements = Lists.newArrayList(SheetManager.getAll());

                db = Maps.uniqueIndex(dbElements, DataManager::convertObjectStringToTimestamp);

                docs = Maps.uniqueIndex(docsElements, DataManager::convertObjectStringToTimestamp);

                MapDifference<Timestamp, Object[]> mapDifference = Maps.difference(db, docs);

                differing = mapDifference.entriesOnlyOnRight();


                for (Timestamp key : differing.keySet()) {
                    Object[] value = differing.get(key);
                    DatabaseConnection.writeOne(value[0].toString(),
                            Double.parseDouble(value[1].toString()));
                }

                differing = mapDifference.entriesOnlyOnLeft();

                for (Timestamp key : differing.keySet()) {
                    Object[] value = differing.get(key);
                    SheetManager.writeOne(value[0].toString(),
                            Double.parseDouble(value[1].toString()));
                }
            } else {
                return false;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not sync the spreadsheet to the database. IOError {0}", e.toString());
        } catch (SQLException s) {
            LOGGER.log(Level.SEVERE, "Could not sync the spreadsheet to the database. SQLError {0}", s.toString());
        }
        return false;
    }

    /**
     * Helper function to convert date strings to timestamps.
     *
     * @param obj object which contains the data under inspection.
     * @return timestamp
     */
    private static Timestamp convertObjectStringToTimestamp(Object[] obj) {
        try {
            return new Timestamp(dtf.parse(obj[0].toString()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper function to append a value to an Object array.
     *
     * @param array array to append to.
     * @param value Object[] which is appended to the array
     * @return new array with the appended value
     */
    public static Object[][] appendToArray(Object[][] array, Object[] value) {
        List<Object[]> temp = new ArrayList<>(Arrays.asList(array));
        temp.add(value);

        Object[][] retArray = new Object[temp.size()][];
        retArray = temp.toArray(retArray);

        return retArray;
    }
}
