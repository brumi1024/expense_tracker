package hu.benjaminteke.expensecalculator.datamodel;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private final static Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private final static String DATABASE_URL = "jdbc:sqlite:expense.db";

    private static Dao<Expense, Integer> expenseDao;

    private static ConnectionSource connectionSource;

    /**
     * Initializes the database.
     */
    public static void initDatabase() {
        try {
            connectionSource = null;
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            setupDatabase(connectionSource);

            LOGGER.log(Level.FINE, "Database connection established");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not create database connection. {0}", e.toString());
        }
    }

    /**
     * Setup the database and DAOs, create the table if it does not exists.
     *
     * @param connectionSource ConnectionSource param
     * @throws Exception throws an error if the table creation failed
     */
    private static void setupDatabase(ConnectionSource connectionSource) throws Exception {

        expenseDao = DaoManager.createDao(connectionSource, Expense.class);
        TableUtils.createTableIfNotExists(connectionSource, Expense.class);
    }

    /**
     * Write out the data to the database.
     *
     * @param timeOfExpense time of the expense
     * @param expense actual expense value
     * @throws SQLException throws an exception if an SQL error occured
     */
    static void writeOne(String timeOfExpense, Double expense) throws SQLException {
        Double balance = DatabaseConnection.readLastValue().getBalance() + expense;
        expenseDao.create(new Expense(timeOfExpense, expense, balance));

        LOGGER.log(Level.FINE, "Write data to database successful.");
    }

    /**
     * Read all the data from the database.
     *
     * @return an Object[][] that contains the rows as arrays.
     * @throws SQLException throws an exception if an SQL error occured
     */
    static Object[][] readAll() throws SQLException {
        List<Expense> expenses = expenseDao.queryForAll();

        LOGGER.log(Level.FINE, "Reading the values from the database.");

        Object[][] tempList = new Object[expenses.size()][];
        for (int i = 0, expensesSize = expenses.size(); i < expensesSize; i++) {
            tempList[i] = expenses.get(i).getObject();
        }

        return tempList;
    }

    /**
     * Read the last recorded expense.
     *
     * @return last (and latest) row of the database
     * @throws SQLException throws an exception if an SQL error occured
     */
    static Expense readLastValue() throws SQLException {
        String tableName = DatabaseTableConfig.extractTableName(Expense.class);
        String selectQuery = "SELECT MAX(id) FROM " + tableName + ";";

        Expense lastExpense = readOneById((int) expenseDao.queryRawValue(selectQuery));

        LOGGER.log(Level.FINE, "Reading last value from the database.");

        return lastExpense != null ? lastExpense : new Expense("", 0.0, 0.0);
    }

    /**
     * Helper function for finding the latest value. Gets the row with the specified id
     *
     * @param id id to search for in the table
     * @return row with the specified id
     * @throws SQLException throws an exception if an SQL error occured
     */
    private static Expense readOneById(int id) throws SQLException {
        return expenseDao.queryForId(id);
    }


    /**
     * Checks if the connection is open to the database.
     *
     * @return true if it is open, false if not.
     */
    static boolean isConnected() {
        LOGGER.log(Level.FINE, "Connection to the database: {0}.", connectionSource.isOpen(
                DatabaseTableConfig.extractTableName(Expense.class)));

        return connectionSource.isOpen(DatabaseTableConfig.extractTableName(Expense.class));
    }
}
