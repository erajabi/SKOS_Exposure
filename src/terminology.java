import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Enayat
 * Date: 5/8/13
 * Time: 7:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class terminology {
    // **********************************  TERMINOLOGY ******************************************************
    // ------------     Save terminologies in arrays       --------------------------
    public static final int MaxNumber =1000000;
    public static final int MaxLanguages =40;


    static String []classNames=new String[MaxNumber];
    static String []classNamespaces=new String[MaxNumber];
    static String []classURLs=new String[MaxNumber];
    static String [][]classLabels=new String[MaxNumber][MaxLanguages];
    static String [][]classLanguages=new String[MaxNumber][MaxLanguages];
    static String []language={"en","de","fr","es","nl"};



    public static void InsertTerminologyIntoTable(OntModel ontModel ,int baseSchema, Connection conn) {
        OntClass ontClass;
        int counter = 0;
        int index=0;
        String temp=null;
        Iterator<OntClass> itrClass = ontModel.listClasses();

        // ==========  Read OWL file to read the class name, labels, URLs, ...
        while ( itrClass.hasNext()) {
            ontClass = itrClass.next();
            counter++;
            System.out.println( "------- NO." + counter + " -------");
            if ( ontClass.isRestriction() == false) {
                System.out.println( "NameSpace=" + ontClass.getNameSpace());
                temp=ontClass.getLocalName();
                if(temp!=null)
                    classNames[index]=temp;
                temp=ontClass.getNameSpace();
                if(temp!=null)
                    classNamespaces[index]=temp;
                temp=ontClass.getURI();
                if(temp!=null)
                    classURLs[index]=temp;
                int tempIndex=0;
                if(ontClass.getLabel(language[0])!=null) {
                    classLabels[index][tempIndex]=ontClass.getLabel(language[0]);
                    classLanguages[index][tempIndex]=language[0];
                    tempIndex++;
                }
                if(ontClass.getLabel(language[1])!=null) {
                    classLabels[index][tempIndex]=ontClass.getLabel(language[1]);
                    classLanguages[index][tempIndex]=language[1];
                    tempIndex++;
                }
                if(ontClass.getLabel(language[2])!=null) {
                    classLabels[index][tempIndex]=ontClass.getLabel(language[2]);
                    classLanguages[index][tempIndex]=language[2];
                    tempIndex++;
                }
                if(ontClass.getLabel(language[3])!=null) {
                    classLabels[index][tempIndex]=ontClass.getLabel(language[3]);
                    classLanguages[index][tempIndex]=language[3];
                    tempIndex++;
                }
                if(ontClass.getLabel(language[4])!=null) {
                    classLabels[index][tempIndex]=ontClass.getLabel(language[4]);
                    classLanguages[index][tempIndex]=language[4];
                    tempIndex++;
                }
                //System.out.println( "LocalName=" + ontClass.getLocalName());
                //System.out.println( "URI=" + ontClass.getURI());
                index++;
            }
            else {
                System.out.println( "Restrictions:");
                Restriction restriction = ontClass.asRestriction();
                //displayAllInfoForRestriction( restriction);
            }
            System.out.println( "---------------------");
        }
        System.out.println( "=======  " + counter + " classes read =======");
        System.out.println( "======= Insert terms into the table =======");
        for(int i=0;i<index;i++){
            if(classNames[i]!=null)
                insertRecordToTerm(conn,"terminology",classNames[i], classURLs[i],baseSchema,classLabels[i],classLanguages[i]);
        }
        System.out.println( "============= Insert done! ===================");

    }

    // ------------     Insert terminology into TERMINOLOGY table     --------------------------
    public static void insertRecordToTerm(Connection conn, String tableName, String term,String URL, int schemaID, String labels[],String classLang[]){
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql = "INSERT INTO "+tableName+" (terminology,URL,schemaID)" +
                    "VALUES ('"+ term+"','"+ URL+"','"+ schemaID+"');";
            System.out.print(sql);
            stmt.executeUpdate(sql);
            sql = "select * from "+tableName+";";
            ResultSet rs = stmt.executeQuery(sql);
            rs.last();
            int termID=rs.getInt("terminologyID");
            System.out.println( "======= term! ======="+termID);

            for(int i=0;i<labels.length;i++)  {
                if(labels[i]==null) continue;
                labels[i]=labels[i].replace("'", "\\'");
                sql = "INSERT INTO preflabel (prefLabel,language,terminologyID)" +
                        "VALUES ('"+ labels[i]+"','"+ classLang[i]+"','"+ termID+"');";
                System.out.println( "Label="+labels[i]+" Lang="+classLang[i]);

                System.out.print(sql);
                stmt.executeUpdate(sql);
            }
            rs.close();
            stmt.close();
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
        }//end try
    }

    // ------------     finding relationships between terminologies     --------------------------
    public static void InsertRelationship(OntModel ontModel, Connection conn, int baseSchema, String root ) {
        OntClass ontClass;
        OntClass ontSuperClass;
        OntClass ontSubClass;

        int counter = 0;
        Iterator<OntClass> itrClass = ontModel.listClasses();
        while ( itrClass.hasNext()) {
            ontClass = itrClass.next();
            counter++;
            System.out.println( "------- NO." + counter + " -------");
            if ( ontClass.isRestriction() == false) {
                boolean hasSuperClasses = ontClass.hasSuperClass();
                System.out.println( "HasSuperClass=" + hasSuperClasses);
                if ( hasSuperClasses == true) {
                    System.out.println( "-Super Classes begin:");
                    // Display the basic info for all the super classes
                    try{
                        Iterator<OntClass> itrSuperClass = ontClass.listSuperClasses();
                        System.out.println( "Error:");
                        while ( itrSuperClass.hasNext()) {
                            ontSuperClass = itrSuperClass.next();
                            insertBroader(conn, ontClass.getURI().toString(),ontSuperClass.getURI().toString(),"broader",baseSchema);
                        }
                    }catch(Exception ec){
                        System.out.println("Error in super class reading:"+ec.getMessage());
                    }
                    System.out.println( "-Super Classes end");
                }
                boolean hasSubClasses = ontClass.hasSubClass();
                System.out.println( "HasSubClass=" + hasSubClasses);
                if ( hasSubClasses == true) {
                    System.out.println( "-Sub Classes begin:");
                    // Display the basic info for all the sub classes
                    try{
                        Iterator<OntClass> itrSubClass = ontClass.listSubClasses();
                        while ( itrSubClass.hasNext()) {
                            ontSubClass = itrSubClass.next();
                            insertBroader(conn,ontClass.getURI().toString(),ontSubClass.getURI().toString(),"narrower",baseSchema);
                        }
                        System.out.println( "-Sub Classes end");
                    }catch(Exception ec){
                        System.out.println("Error in super class reading:"+ec.getMessage());
                    }
                }
            }
            else {
                System.out.println( "Restrictions:");
                Restriction restriction = ontClass.asRestriction();
                //displayAllInfoForRestriction( restriction);
            }
            System.out.println( "---------------------");
        }
        System.out.println( "======= " + counter + " =======");
    }

    // ------------     Insert terminology relationship (SUPER CLASS AND SUB CLASS) into Terminology_relationship table     --------------------------
    public static void insertBroader(Connection conn,String term1, String term2, String relation, int baseSchema){
        Statement stmt = null;
        int termID1=-1, termID2=-1;
        try{
            stmt = conn.createStatement();
            String sql = "SELECT * from terminology where URL like '"+term1+"' and schemaID="+baseSchema;
            System.out.println("SQL="+sql);
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next())
                termID1=rs.getInt("terminologyID");

            sql = "SELECT * from terminology where URL like '"+term2+"' and schemaID="+baseSchema;
            rs = stmt.executeQuery(sql);
            if(rs.next())
                termID2=rs.getInt("terminologyID");
            if(termID1!=-1 && termID2!=-1){
                sql = "INSERT INTO terminology_relationship (terminologyID ,relatedtermID,relation)" +
                        "VALUES ('"+ termID1+"','"+ termID2+"','"+ relation+"');";
                System.out.println("SQL="+sql);
                stmt.executeUpdate(sql);
            }
            rs.close();
            stmt.close();
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            System.out.println("Error!");
        }
    }
    // ------------     Insert terminology relationship (SUPER CLASS AND SUB CLASS) into Terminology_relationship table     --------------------------
    public static int findRoot(Connection conn,String root, int baseSchema){
        Statement stmt = null;
        int rootID=-1;
        try{
            stmt = conn.createStatement();
            String sql = "SELECT * from terminology where terminology like '"+root+"' and schemaID="+baseSchema;
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next())
                rootID=rs.getInt("terminologyID");
            rs.close();
            stmt.close();
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
        }
    return rootID;
    }

}
