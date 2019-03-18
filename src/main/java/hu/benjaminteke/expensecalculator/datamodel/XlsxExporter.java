package hu.benjaminteke.expensecalculator.datamodel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XlsxExporter {
    private final static Logger LOGGER = Logger.getLogger(XlsxExporter.class.getName());

    /**
     * Date time format to convert between string and timestamp.
     */
    private static SimpleDateFormat dtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Creates a new xlsx document and saves it to the folder of the jar file.
     *
     * @param name      name of the file to create.
     * @param data      data parameter.
     * @param startTime start time for filtering.
     * @param endTime   end time for filtering.
     * @throws IOException throws an error if the file creation has failed.
     */
    public static void exportToXlsx(String name, List<Object[]> data, String startTime, String endTime) throws IOException {
        int i = 0;
        Workbook wb = new XSSFWorkbook();

        Sheet expenseSheet = wb.createSheet("Expenses");

        LOGGER.log(Level.FINE, "Creating xlsx.");

        List<Object[]> filteredData = filterData(data, startTime, endTime);

        for (Object[] aData : filteredData) {
            Row row = expenseSheet.createRow(i++);
            row.createCell(0).setCellValue(aData[0].toString());
            row.createCell(1).setCellValue(Double.parseDouble(aData[1].toString()));
            row.createCell(2).setCellValue(Double.parseDouble(aData[2].toString()));
        }

        FileOutputStream fileOut = new FileOutputStream(name + ".xlsx");
        wb.write(fileOut);
        fileOut.close();
        LOGGER.log(Level.FINE, "Xlsx export done.");

    }

    /**
     * Filter the data for start and end time. The time parameters need to correspond to the format defined by the format string.
     *
     * @param data      input data
     * @param startTime start time parameter string.
     * @param endTime   end time parameter string.
     * @return filtered List of the data.
     */
    private static List<Object[]> filterData(List<Object[]> data, String startTime, String endTime) {
        Timestamp start = convertStringToTimestamp(startTime);
        Timestamp end = convertStringToTimestamp(endTime);
        Stream<Object[]> stream = data.stream();

        if (start != null) {
            stream = stream.filter(d -> convertStringToTimestamp(d[0].toString()).after(start));
        }

        if (end != null) {
            stream = stream.filter(d -> convertStringToTimestamp(d[0].toString()).before(end));
        }

        return stream.collect(Collectors.toList());
    }

    /**
     * Helper function to convert string dates to timestamps
     *
     * @param time a formatted string representing a date.
     * @return timestamp of the parameter date.
     */
    private static Timestamp convertStringToTimestamp(String time) {
        try {
            return new Timestamp(dtf.parse(time).getTime());
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Error converting timestamp.", e.toString());

            return null;
        }
    }
}
