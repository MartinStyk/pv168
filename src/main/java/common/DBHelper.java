/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
/**
 *
 * @author Martin Styk
 */
public class DBHelper {

    private static final Logger logger = Logger.getLogger(
            DBHelper.class.getName());

    public static DataSource getDataSource()  {
   
             BasicDataSource ds = new BasicDataSource();

             ds.setUrl("jdbc:derby://localhost:1527/pv168");
            ds.setUsername("davidmato");
            ds.setPassword("matodavid");
            
            return ds;
    }
    
    /**
     * Executes SQL script.
     * 
     * @param ds datasource
     * @param scriptUrl url of sql script to be executed
     * @throws SQLException when operation fails
     */
   
    /**
     * Executes SQL script.
     *
     * @param ds datasource
     * @param is sql script to be executed
     * @throws SQLException when operation fails
     */
    public static void executeSqlScript(DataSource ds, InputStream is) throws SQLException {
        try (Connection conn = ds.getConnection()) {
            for (String sqlStatement : readSqlStatements(is)) {
                if (!sqlStatement.trim().isEmpty()) {
                    try (PreparedStatement preparedStatement = conn.prepareStatement(sqlStatement)) {
                        preparedStatement.executeUpdate();
                    }
                }
            }
        }
    }
        public static void executeSqlScript(DataSource ds, URL scriptUrl) throws SQLException {
      
        try (Connection conn = ds.getConnection()){
           
            for (String sqlStatement : readSqlStatements(scriptUrl)) {
                if (!sqlStatement.trim().isEmpty()) {
                    conn.prepareStatement(sqlStatement).executeUpdate();
                }
            }
        } 
    }
        
    private static String[] readSqlStatements(URL url) {
        try {
            char buffer[] = new char[256];
            StringBuilder result = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(url.openStream(), "UTF-8");
            while (true) {
                int count = reader.read(buffer);
                if (count < 0) {
                    break;
                }
                result.append(buffer, 0, count);
            }
            return result.toString().split(";");
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read " + url, ex);
        }
    }




    /**
     * Extract key from given ResultSet.
     *
     * @param key resultSet with key
     * @return key from given result set
     * @throws SQLException when operation fails
     */
    public static Long getId(ResultSet key) throws SQLException {
        if (key.getMetaData().getColumnCount() != 1) {
            throw new IllegalArgumentException("Given ResultSet contains more columns");
        }
        if (key.next()) {
            Long result = key.getLong(1);
            if (key.next()) {
                throw new IllegalArgumentException("Given ResultSet contains more rows");
            }
            return result;
        } else {
            throw new IllegalArgumentException("Given ResultSet contain no rows");
        }
    }

        /**
     * Reads SQL statements from file. SQL commands in file must be separated by
     * a semicolon.
     *
     * @param is input stream of the file
     * @return array of command  strings
     */
    private static String[] readSqlStatements(InputStream is) {
        try {
            char buffer[] = new char[256];
            StringBuilder result = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            while (true) {
                int count = reader.read(buffer);
                if (count < 0) {
                    break;
                }
                result.append(buffer, 0, count);
            }
            return result.toString().split(";");
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read ", ex);
        }
    }


    /* * Check if updates count is exactly one. Otherwise appropriate exception is thrown.
     * 
     * @param count updates count.
     * @param entity updated entity (for includig to error message)
     * @param insert flag if insert operation was performed. 
     * @throws IllegalEntityException when update/delete operation was performed 
     * and updates count is zero, so updated entity does not exist
     * @throws ServiceFailureException when updates count is unexpected number
     */
    public static void checkUpdatesCount(int count, Object entity,
            boolean insert) throws ServiceFailureException {

        if (!insert && count == 0) {
            throw new ServiceFailureException("Entity " + entity + " does not exist in the db");
        }
        if (count != 1) {
            throw new ServiceFailureException("Internal integrity error: Unexpected rows count in database affected: " + count);
        }
    }

}
