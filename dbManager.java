import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: Enayat
 * Date: 5/8/13
 * Time: 7:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class dbManager {

    // ------------     Get Connection to MySQL       --------------------------
    public static Connection Get_Connection(String databaseName,String connectionString, String userName, String passWord) throws Exception
    {
        try
        {
            System.out.println("Connecting to a selected database...");
            //  Name of database
            String connectionURL = connectionString+databaseName;
            Connection connection = null;
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            //  Connection username and password
            connection = DriverManager.getConnection(connectionURL, userName, passWord);
            System.out.println("Connected database successfully...");

            return connection;
        }
        catch (SQLException e)
        {
            System.out.println("SQL problem...");
            throw e;
        }
        catch (Exception e)
        {
            System.out.println("Connection problem...");
            throw e;
        }
    }
    // ****************************** DELETE ALL RECORDS ******************************
    public static void DeleteAllrecords(Connection conn){

        try{
            Statement stmt = conn.createStatement();
            String sql = "delete from terminology;";
            System.out.println("SQL="+sql);
            stmt.executeUpdate(sql);
            sql = "delete from altlabel;";
            System.out.println("SQL="+sql);
            stmt.executeUpdate(sql);
            sql = "delete from preflabel;";
            System.out.println("SQL="+sql);
            stmt.executeUpdate(sql);
            sql = "delete from property;";
            System.out.println("SQL="+sql);
            stmt.executeUpdate(sql);
            sql = "delete from propertyrelation;";
            System.out.println("SQL="+sql);
            stmt.executeUpdate(sql);
            sql = "delete from relatedterminolgy_to_property;";
            System.out.println("SQL="+sql);
            stmt.executeUpdate(sql);
            sql = "delete from terminology_relationship;";
            System.out.println("SQL="+sql);
            stmt.executeUpdate(sql);
            sql = "delete from baseschema;";
            System.out.println("SQL="+sql);
            stmt.executeUpdate(sql);
            stmt.close();
            System.out.println("All the tables were deleted!");

        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
        }
    }
    //*********************************************************************************

}
