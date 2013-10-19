/**
 * Created with IntelliJ IDEA.
 * User: Enayat Rajabi
 * Date: 04/28/13
 * Time: 9:04 PM
 * To change this template use File | Settings | File Templates.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

    import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

    public class ontologyParser {
        static String Prefix="http://www.inter2geo.eu/2008/ontology/GeoSkills#";
        public static int baseSchema=5;
        static String owlFilepath=null;//"src\\OE.owl";
        static String interfacePath=null;//"src\\Interface\\relationBrowser.xml";

        public static int rootID=0;
        public static String root=null;


        //*************************** Database configuration ***************************************************
        static String databaseName=null;//"sampleskos";
        static String userName=null;//e="root";
        static String passWord=null;//"123";
        static int index=0;
        static String connectionString=null;//"jdbc:mysql://localhost:3306/";

        //*************************** Excel configuration ***************************************************
        public static String inputExcelFilePath=null;//"src\\ODS_Taxonomy.xls";
        public static int firstSheet=-1;//8;
        public static int lastSheet=-1;//8;

        public static String OWLPrefix=null;//"http://www.semanticweb.org/ontologies/2013/3/Ontology1366095914701.owl#";
        //****************************************************************************************************
        public static void  readConfigFile(){
            try {
                File fXmlFile = new File("src\\config.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                doc.getDocumentElement().normalize();
                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                NodeList nList = doc.getElementsByTagName("database");
                System.out.println("----------------------------");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    System.out.println("\nCurrent Element :" + nNode.getNodeName());
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        connectionString=eElement.getElementsByTagName("connectionString").item(0).getTextContent().trim();
                        databaseName=eElement.getElementsByTagName("databaseName").item(0).getTextContent().trim();
                        userName= eElement.getElementsByTagName("username").item(0).getTextContent().trim();
                        passWord=eElement.getElementsByTagName("password").item(0).getTextContent().trim();
                    }
                }
                nList = doc.getElementsByTagName("excel");
                System.out.println("----------------------------");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    System.out.println("\nCurrent Element :" + nNode.getNodeName());
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        inputExcelFilePath=eElement.getElementsByTagName("inputExcelFilePath").item(0).getTextContent().trim();
                        firstSheet=Integer.parseInt(eElement.getElementsByTagName("firstSheet").item(0).getTextContent().trim());
                        lastSheet=Integer.parseInt(eElement.getElementsByTagName("lastSheet").item(0).getTextContent().trim());
                        OWLPrefix= eElement.getElementsByTagName("OWLPrefix").item(0).getTextContent().trim();
                        interfacePath=eElement.getElementsByTagName("interfacePath").item(0).getTextContent().trim();
                    }
                }
                nList = doc.getElementsByTagName("owl");
                System.out.println("----------------------------");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    System.out.println("\nCurrent Element :" + nNode.getNodeName());
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        interfacePath= eElement.getElementsByTagName("interfacePath").item(0).getTextContent().trim();
                        owlFilepath=eElement.getElementsByTagName("owlFilepath").item(0).getTextContent().trim();
                    }
                }
            } catch (FileNotFoundException NF) {
                System.out.println("config.xml not found ");
                System.exit(1);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void main( String[] args) {
            InputStream testFileIn = null;
            boolean flag=true;  // flag is true when program reads EXCEL file
            String sql=null;
            Statement statement=null;
            interfaceMaker interfaceMaker=new interfaceMaker();

            //*************************** Creating database connection *************************************************
            Connection conn=null;

            //*************************** Reading configuration from XML file (config.XML)******************************
            readConfigFile();
            //System.exit(1);

            dbManager DBM=new dbManager();
            try{
                 conn= DBM.Get_Connection(databaseName, connectionString, userName,passWord);
                //*************************** Delete all records ***************************************************
               // DBM.DeleteAllrecords(conn);
                //System.exit(1);
            }
            catch (SQLException e)
            {
                System.out.println("SQL problem...");
            }
            catch (Exception e)
            {
                System.out.println("Connection problem...");
            }
            // if flag is true, reads from EXCEL, else from OWL file
            if(flag) {
                excelParser excelParser=new excelParser();
                for(int i=firstSheet;i<=lastSheet;i++) {
                    //   OWLPrefix = ""
                    rootID=excelParser.processExcelFile(conn,inputExcelFilePath,i,OWLPrefix);
                }
            }
            else{
                // what is the root element?
                root="Product";
                try {
                    testFileIn = FileManager.get().open(owlFilepath);
                } catch(IllegalArgumentException ex) {
                    System.out.println( "The OWL file not found");
                    System.exit(1);
                }
                // Support OWL Full and no reasoning added
                OntModel ontModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM);
                System.out.println( "ontModel=" + ontModel);

                ontModel.read( testFileIn,"RDF/OWL");
                // Get the base model
                Model baseModel = ontModel.getBaseModel();
                System.out.println( "baseModel=" + baseModel);
                //*************************** Insert term into the terminology tables***************************************************
                terminology terminology=new terminology();
                terminology.InsertTerminologyIntoTable(ontModel, baseSchema, conn);
                terminology.InsertRelationship(ontModel, conn, baseSchema, root);

                //*************************** Insert property into the property tables**************************************************
                //property property=new property();
                //property.InsertPropertyIntoTable(ontModel,baseSchema,conn);

                //*************************** Insert property relationship into the property tables*************************************
                //property.InsertPropertyRelationship(ontModel,conn);

                //*************************** Insert terminology relationship into the relationship tables******************************
                rootID=terminology.findRoot(conn,root,baseSchema);
            }


            interfaceMaker.SKOS_gui(interfacePath, rootID, conn);

            System.out.println("rootID="+rootID+"  root="+root);
            try{
                conn.close();
            }catch (Exception e){
                System.out.println("Error in closing connection!");
                e.printStackTrace();
            }

        }
    }




