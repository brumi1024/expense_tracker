/**
 * Created by Benjamin on 12/6/2016.
 */
package hu.benjaminteke.expensecalculator.datamodel;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class SheetManager {
    private final static Logger LOGGER = Logger.getLogger(SheetManager.class.getName());

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Expense Calculator";

    /**
     * Directory to store user credentials in.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/sheets.googleapis.com-expense-calculator");
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /**
     * Global instance of the scopes required by this application.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    /**
     * Id of the used spreadsheet.
     */
    private static final String spreadsheetId = "1W55QAkr94ATvXEohgabHl_Dc6UZ6IZanVAJi379Of6Q";
    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Error creating GoogleNetHTTPTransport {0} ", t.toString());
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    private static Credential authorize() throws IOException {
        InputStream in = SheetManager.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        LOGGER.log(Level.FINE, "Credentials saved to {0}", DATA_STORE_DIR.getAbsolutePath());

        return credential;
    }

    /**
     * Build and return with an authorized Sheets API client service.
     *
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    private static Sheets getSheetsService() throws IOException {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Get values from spreadsheet between the columns of A and C.
     *
     * @return a ValueRange Object containing the values.
     * @throws IOException
     */
    private static ValueRange getResponse() throws IOException {
        Sheets service = getSheetsService();

        String range = "A2:C";

        LOGGER.log(Level.FINE, "Loading values from spreadsheet");

        return service.spreadsheets().values()
                .get(spreadsheetId, range).execute();
    }

    /**
     * Append a new expense to the end of the spreadsheet.
     *
     * @param ParamData a Collections.List that contains Lists with the values.
     * @throws IOException
     */
    private static void appendValue(List<List<Object>> ParamData) throws IOException {
        Sheets service = getSheetsService();

        String range = "A2:C2";

        ValueRange oRange = new ValueRange();
        oRange.setRange(range);
        oRange.setValues(ParamData);

        LOGGER.log(Level.FINE, "Appending value to spreadsheet");

        service.spreadsheets().values().append(spreadsheetId, range, oRange).setValueInputOption("USER_ENTERED").execute();

    }

    /**
     * Helper function that creates the data to append.
     *
     * @param time  time of the expense
     * @param value value of the expense
     * @throws IOException
     */
    static void writeOne(String time, Double value) throws IOException {
        ValueRange range = getResponse();
        List<List<Object>> values = range.getValues();

        List<Object> data1 = new ArrayList<>();
        data1.add(time);
        data1.add(value);

        if (values == null) {
            data1.add(value);
        } else {
            data1.add(Double.parseDouble(getLastValue()[2].toString()) + value);
        }

        List<List<Object>> data = new ArrayList<>();
        data.add(data1);

        appendValue(data);
    }

    /**
     * Get the last recorded expense.
     *
     * @return last row of the spreadsheet
     * @throws IOException
     */
    static Object[] getLastValue() throws IOException {
        ValueRange range = getResponse();
        List<List<Object>> values = range.getValues();

        if (values != null) {
            List<Object> lastElement = Iterables.getLast(values, null);
            return lastElement != null ? lastElement.toArray() : new Object[]{"", "", ""};
        } else {
            return new Object[]{"", "", ""};
        }

    }

    /**
     * Get all the rows.
     *
     * @return an Object[][] that contains the rows as arrays.
     * @throws IOException
     */
    static Object[][] getAll() throws IOException {
        ValueRange range = getResponse();
        List<List<Object>> values = range.getValues();

        if (values != null) {
            Object[][] tempList = new Object[values.size()][];
            for (int i = 0, valuesSize = values.size(); i < valuesSize; i++) {
                tempList[i] = values.get(i).toArray();
            }
            return tempList;
        } else {
            return new Object[0][];
        }
    }
}