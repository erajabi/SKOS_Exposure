import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Enayat
 * Date: 5/8/13
 * Time: 7:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class property {

    // **********************************  PROPERTY ******************************************************
    public static final int MaxNumber =5000;
    static String []propertyNames=new String[MaxNumber];
    static String []propertyNamespaces=new String[MaxNumber];
    static String []propertyURLs=new String[MaxNumber];
    static int propertyIndex=0;
    // ------------     Insert property into PROPERTY table     --------------------------
    public static void insertRecordToProperty( Connection conn,String term,String URL, int schemaID, String classURL){
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql=null;
            int temp=0;
            if(classURL!=null){
                // What is terminology domain of this property
                sql = "SELECT * from terminology where URL like '"+classURL+" and schemaID='"+schemaID;
                System.out.print(sql);
                ResultSet rs = stmt.executeQuery(sql);
                if(rs.next())
                    temp=rs.getInt("terminologyID");
                sql = "INSERT INTO property (property,URL,schemaID, terminologyID)" +
                        "VALUES ('"+ term+"','"+ URL+"','"+ schemaID+"','"+temp+"');";
                System.out.print("insertRecordToProperty="+sql);

            } else
                // When there is no terminology domain for this property
                sql = "INSERT INTO property (property,URL,schemaID)" +
                        "VALUES ('"+ term+"','"+ URL+"','"+ schemaID+"');";
            System.out.print("insertRecordToProperty="+sql);
            stmt.executeUpdate(sql);
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
        }//end try
    }
    public static void InsertPropertyIntoTable( OntModel ontModel, int baseSchema, Connection conn) {
        OntProperty ontProperty;
        int counter = 0;
        OntResource ontResourceTmp;
        String temp=null;
        Iterator<OntProperty> itrProperty = ontModel.listAllOntProperties();
        while ( itrProperty.hasNext()) {
            ontProperty = itrProperty.next();
            counter++;
            System.out.println( "------- NO." + counter + " -------");
            System.out.println( "NameSpace=" + ontProperty.getNameSpace());
            temp=ontProperty.getLocalName();
            if(temp!=null)
                propertyNames[propertyIndex]=temp;
            temp=ontProperty.getNameSpace();
            if(temp!=null)
                propertyNamespaces[propertyIndex]=temp;
            temp=ontProperty.getURI();
            if(temp!=null)
                propertyURLs[propertyIndex]=temp;
            System.out.println( "---------------------");

            Iterator<? extends OntResource> itrDomain = ontProperty.listDomain();
            boolean hasDomain = itrDomain.hasNext();
            System.out.println( "HasDomain=" + hasDomain);
            if ( hasDomain == true) {
                System.out.println( "Domains begin:");
                while ( itrDomain.hasNext()) {
                    ontResourceTmp = itrDomain.next();
                    System.out.println("URL="+ontResourceTmp.getURI());
                    insertRecordToProperty(conn, propertyNames[propertyIndex], propertyURLs[propertyIndex],baseSchema,ontResourceTmp.getURI());
                    //displayBasicInfoForResource( ontResourceTmp);
                }
                System.out.println( "Domains end");
            } else{
                insertRecordToProperty(conn,propertyNames[propertyIndex], propertyURLs[propertyIndex],baseSchema,null);
            }
            // Check if has inverseOf properties
            propertyIndex++;
        }
    }

    // ------------     Insert relationship between terminology and property in relatedterminolgy_to_property table     --------------------------
    public static void insertDeclaredTerminology (String term, String property, Connection conn){
        Statement stmt = null;
        int ID1=-1, ID2=-1;
        try{
            stmt = conn.createStatement();
            String sql = "SELECT * from terminology where URL like '"+term+"'";
            System.out.println("SQL="+sql);
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next())
                ID1=rs.getInt("terminologyID");

            sql = "SELECT * from property where URL like '"+property+"'";
            rs = stmt.executeQuery(sql);
            if(rs.next())
                ID2=rs.getInt("propertyID");

            if(ID1!=-1 && ID2!=-1){
                sql = "INSERT INTO relatedterminolgy_to_property (terminologyID ,propertyID)" +
                        "VALUES ('"+ ID1+"','"+ ID2+"');";
                System.out.println("SQL="+sql);
                stmt.executeUpdate(sql);
            }
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
        }
    }

    // ------------     Insert relationship between properties in propertyRelation table     --------------------------
    public static void insertRecordToPropertyRelation(String term1, String term2, String relation, Connection conn){
        Statement stmt = null;
        int termID1=-1, termID2=-1;
        try{
            stmt = conn.createStatement();
            String sql = "SELECT * from property where URL like '"+term1+"'";
            System.out.println("SQL="+sql);
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next())
                termID1=rs.getInt("propertyID");

            sql = "SELECT * from property where URL like '"+term2+"'";
            rs = stmt.executeQuery(sql);
            if(rs.next())
                termID2=rs.getInt("propertyID");

            if(termID1!=-1 && termID2!=-1){

                sql = "INSERT INTO propertyRelation (propertyID ,relatedPropertyID,relation)" +
                        "VALUES ('"+ termID1+"','"+ termID2+"','"+ relation+"');";
                System.out.println("SQL="+sql);
                stmt.executeUpdate(sql);
            }
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
        }
    }

    // Finding properties relationships (Property to property and property to terminology)
    public static void InsertPropertyRelationship(OntModel ontModel, Connection conn){
        OntProperty ontProperty;
        OntClass ontClassTmp;
        int counter = 0;
        String temp=null;
        Iterator<OntProperty> itrProperty = ontModel.listAllOntProperties();
        while ( itrProperty.hasNext()) {
            ontProperty = itrProperty.next();
            counter++;
            Iterator<? extends OntProperty> itrInverseOfProp = ontProperty.listInverseOf();
            OntProperty ontPropTmp;
            boolean hasInverseOfProperty = itrInverseOfProp.hasNext();
            System.out.println( "HasInverseOfProperty=" + hasInverseOfProperty);
            if ( hasInverseOfProperty == true) {
                System.out.println( "-InverseOf Properties beign:");
                while ( itrInverseOfProp.hasNext()) {
                    ontPropTmp = itrInverseOfProp.next();
                    insertRecordToPropertyRelation(ontProperty.getURI(), ontPropTmp.getURI(),"Inverse", conn);
                }
                System.out.println( "-InverseOf Properties end");
            }
            Iterator<? extends OntClass> itrDeclaringClass = ontProperty.listDeclaringClasses();
            boolean hasDeclaringClass = itrDeclaringClass.hasNext();
            System.out.println( "HasDeclaringClass=" + hasDeclaringClass);
            if ( hasDeclaringClass == true) {
                System.out.println( "-Declaring Classes begin:");
                while ( itrDeclaringClass.hasNext()) {
                    ontClassTmp = itrDeclaringClass.next();
                    if(ontClassTmp.getURI()!=null)
                        insertDeclaredTerminology(ontClassTmp.getURI(),ontProperty.getURI(),conn);
                }
                System.out.println( "-Declaring Classes end");
            }
        }
    }

}
