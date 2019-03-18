package hu.benjaminteke.expensecalculator.app;

import hu.benjaminteke.expensecalculator.datamodel.DatabaseConnection;
import hu.benjaminteke.expensecalculator.gui.ExpenseCalculatorGui;

import javax.swing.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Benjamin on 12/6/2016.
 */
public class ExpenseCalculatorApp {
    private final static Logger LOGGER = Logger.getLogger(ExpenseCalculatorApp.class.getName());

    /**
     * Main method
     * @param args command line arguments
     * @throws IOException throws exception when an error occured in database creation or gui initialization
     */
    public static void main(String[] args) throws IOException {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            DatabaseConnection.initDatabase();
            javax.swing.SwingUtilities.invokeLater(() -> new ExpenseCalculatorGui().setVisible(true));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error occured: {0}.", e.toString());

        }


    }
}
