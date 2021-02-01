package com.ms3.CodingChallenge;

import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import javax.xml.crypto.Data;

public class DataProcessor {
    private static final String SQL_DROP = "DROP TABLE IF EXISTS user";
    private static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS user"
            + "("
            + " A VARCHAR(20),"
            + " B varchar(20) ,"
            + " C VARCHAR(320),"
            + " D VARCHAR(6) CHECK(D IS NULL OR D = 'Male' OR D = 'Female'),"
            + " E TEXT,"
            + " F VARCHAR(40),"
            + " G DECIMAL(15,2),"
            + " H BOOLEAN,"
            + " I BOOLEAN,"
            + " J VARCHAR(320)"
            + ")";

    private static final String SQL_INSERT_PREPARED = "INSERT INTO user (A, B, C, D, E, F, G, H, I, J) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String CONFIG_PROPS_PATH="./config.properties";
    public static final String RECORD_COUNT_MSG="Received: ";
    public static final String VALID_RECORD_COUNT_MSG ="Successful: ";
    public static final String INVALID_RECORD_COUNT_MSG="Failed: ";


    private String DBUrl;
    private String inputCSVPath;
    private String logPath;
    private String badColCSVPath;

    private int recordCount;
    private int validRecordCount;
    private int invalidRecordCount;


    private final static Logger logger =
            Logger.getLogger(DataProcessor.class.getName());
    private final static boolean logAppendMode = true;
    private FileHandler logFh;

    public DataProcessor() {
        initializeProperties();
        initializeLogger();

    }

    private void initializeProperties() {
        Properties prop = new Properties();

        try (InputStream input = DataProcessor.class.getClassLoader().getResourceAsStream(DataProcessor.CONFIG_PROPS_PATH)) {
            if(input == null) {
                throw new FileNotFoundException("Unable to find config.properties. Exiting now.");
            }

            // load a properties file
            prop.load(input);

            DBUrl = prop.getProperty("DB.Url");
            inputCSVPath = prop.getProperty("InputCSVPath");
            badColCSVPath=prop.getProperty("BadColCSVPath");
            logPath = prop.getProperty("LogPath");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void initializeLogger() {
        logger.setLevel(Level.INFO);
        try {
            logFh = new FileHandler(logPath, logAppendMode);
            logger.addHandler(logFh);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logStatistics() {
        logger.log(Level.INFO, String.format("%s%d", DataProcessor.RECORD_COUNT_MSG, recordCount));
        logger.log(Level.INFO, String.format("%s%d", DataProcessor.VALID_RECORD_COUNT_MSG, validRecordCount));
        logger.log(Level.INFO, String.format("%s%d", DataProcessor.INVALID_RECORD_COUNT_MSG, invalidRecordCount));
        logFh.close();
    }

    private String[] processRecord(String[] header, String[] record, PreparedStatement statement, CSVWriter csvWriter) throws SQLException {
        // ignore repeated header row
        boolean headerRowRepeated = Arrays.equals(header, record);
        if(headerRowRepeated) return record;

        recordCount++;

        String[] cleanRecord = Arrays.copyOfRange(record, 0, 10);
        boolean nullColExists = Arrays.stream(cleanRecord).anyMatch(String::isEmpty);
        if(nullColExists) {
            csvWriter.writeNext(record);
            invalidRecordCount++;
            return record;
        }

        validRecordCount++;

        PreparedStatement preparedStmt = prepareStatement(cleanRecord, statement);

        preparedStmt.addBatch();
        return cleanRecord;
    }

    private PreparedStatement prepareStatement(String[] cleanRecord, PreparedStatement statement) throws SQLException {
        String A = cleanRecord[0];
        String B = cleanRecord[1];
        String C = cleanRecord[2];
        String D = cleanRecord[3];
        String E = cleanRecord[4];
        String F = cleanRecord[5];
        Double G = !cleanRecord[6].isEmpty() ? Double.parseDouble(cleanRecord[6].substring(1)) : null; // removes dollar sign
        Boolean H = !cleanRecord[7].isEmpty() ? Boolean.parseBoolean(cleanRecord[7]) : null;
        Boolean I = !cleanRecord[8].isEmpty() ? Boolean.parseBoolean(cleanRecord[8]) : null;
        String J = cleanRecord[9];

        if(!A.isEmpty()) statement.setString(1, A);
        if(!B.isEmpty()) statement.setString(2, B);
        if(!C.isEmpty()) statement.setString(3, C);
        if(!D.isEmpty()) statement.setString(4, D);
        if(!E.isEmpty()) statement.setString(5, E);
        if(!F.isEmpty()) statement.setString(6, F);
        if(G != null) statement.setDouble(7, G);
        if(H != null) statement.setBoolean(8, H);
        if(I != null) statement.setBoolean(9, I);
        if(!J.isEmpty()) statement.setString(10, J);
        return statement;
    }

    public void processRecords() {
        String jdbcURL = String.format("jdbc:sqlite:%s", DBUrl);

        int batchSize = 20;
        Connection connection = null;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection(jdbcURL);
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();
            stmt.execute(SQL_DROP);
            stmt.execute(SQL_CREATE);

            PreparedStatement statement = connection.prepareStatement(DataProcessor.SQL_INSERT_PREPARED);


            CSVWriter csvWriter = new CSVWriter(new FileWriter(badColCSVPath));

            InputStream input = DataProcessor.class.getClassLoader().getResourceAsStream(inputCSVPath);
            if(input == null) throw new FileNotFoundException(String.format("Could not find input CSV at: %s", inputCSVPath));
            CSVReader csvReader = new CSVReader(new InputStreamReader(input));
            String[] nextRecord;

            String[] header = csvReader.readNext();

            // \\p{C} used to remove invisible Unicode characters
            header[0] = header[0].replaceAll("\\p{C}", "");

            csvWriter.writeNext(header);

            while((nextRecord = csvReader.readNext()) != null) {
               processRecord(header, nextRecord, statement, csvWriter);

                if(validRecordCount % batchSize == 0) {
                    statement.executeBatch();
                }
            }

            csvReader.close();
            csvWriter.close();

            logStatistics();


            // execute remaining queries
            statement.executeBatch();

            connection.commit();
            connection.close();

        } catch(IOException e) {
            System.err.println(e);
        }
        catch(SQLException e) {
            e.printStackTrace();

            try {
                connection.rollback();
            } catch(SQLException ex) {
                ex.printStackTrace();
            }
        }
        finally {
            try {
                if(connection != null)
                    connection.close();
            } catch(SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }


    public static void main(String[] args) {
        DataProcessor dp = new DataProcessor();
        dp.processRecords();
    }

}