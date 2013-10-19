import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: Enayat
 * Date: 5/8/13
 * Time: 7:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class interfaceMaker {
    public static final int MaxNumber =1000000;

    // ********************* Write into XML ******************************
    public static void xmlWriter(String interfacePath, int rootID, int termID[], String termValue[], int size, int sourceID[], int targetID[], String relationValue[], int indexRelation){
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("RelationViewerData");
            doc.appendChild(rootElement);

            // staff elements
            Element Settings = doc.createElement("Settings");
            rootElement.appendChild(Settings);

            // set attribute to staff element
            Attr appTitle = doc.createAttribute("appTitle");
            appTitle.setValue("SKOS Demonstration");
            Settings.setAttributeNode(appTitle);
            Attr startID = doc.createAttribute("startID");
            startID.setValue(Integer.toString(rootID));
            Settings.setAttributeNode(startID);
            Attr defaultRadius = doc.createAttribute("defaultRadius");
            defaultRadius.setValue("150");
            Settings.setAttributeNode(defaultRadius);
            Attr maxRadius = doc.createAttribute("maxRadius");
            maxRadius.setValue("180");
            Settings.setAttributeNode(maxRadius);

            Element RelationTypes = doc.createElement("RelationTypes");
            Settings.appendChild(RelationTypes);

            Element UndirectedRelation = doc.createElement("UndirectedRelation");
            RelationTypes.appendChild(UndirectedRelation);
            Attr color = doc.createAttribute("color");
            color.setValue("0x85CDE4");
            UndirectedRelation.setAttributeNode(color);
            Attr lineSize = doc.createAttribute("lineSize");
            lineSize.setValue("4");
            UndirectedRelation.setAttributeNode(lineSize);
            Attr labelText = doc.createAttribute("labelText");
            labelText.setValue("association");
            UndirectedRelation.setAttributeNode(labelText);
            Attr letterSymbol = doc.createAttribute("letterSymbol");
            letterSymbol.setValue("B");
            UndirectedRelation.setAttributeNode(letterSymbol);


            Element DirectedRelation = doc.createElement("DirectedRelation");
            RelationTypes.appendChild(DirectedRelation);
            color = doc.createAttribute("color");
            color.setValue("0xAAAAAA");
            DirectedRelation.setAttributeNode(color);
            lineSize = doc.createAttribute("lineSize");
            lineSize.setValue("4");
            DirectedRelation.setAttributeNode(lineSize);

            Element Narrower = doc.createElement("MyCustomRelation");
            RelationTypes.appendChild(Narrower);
            color = doc.createAttribute("color");
            color.setValue("0xB7C631");
            Narrower.setAttributeNode(color);
            lineSize = doc.createAttribute("lineSize");
            lineSize.setValue("4");
            Narrower.setAttributeNode(lineSize);
            labelText = doc.createAttribute("labelText");
            labelText.setValue("Narrower");
            Narrower.setAttributeNode(labelText);
            letterSymbol = doc.createAttribute("letterSymbol");
            letterSymbol.setValue("N");
            Narrower.setAttributeNode(letterSymbol);

            Element NodeTypes = doc.createElement("NodeTypes");
            Settings.appendChild(NodeTypes);

            Element Node = doc.createElement("Node");
            NodeTypes.appendChild(Node);

            Element Comment = doc.createElement("Comment");
            NodeTypes.appendChild(Comment);

            Element Person = doc.createElement("Person");
            NodeTypes.appendChild(Person);

            Element Document = doc.createElement("Document");
            NodeTypes.appendChild(Document);


            Element Nodes = doc.createElement("Nodes");
            rootElement.appendChild(Nodes);

            for(int i=0;i<size;i++){
                Node = doc.createElement("Node");
                Nodes.appendChild(Node);
                Attr id = doc.createAttribute("id");
                id.setValue(Integer.toString(termID[i]));
                Node.setAttributeNode(id);
                Attr name = doc.createAttribute("name");
                name.setValue(termValue[i]);
                Node.setAttributeNode(name);
                Attr dataURL = doc.createAttribute("dataURL");
                dataURL.setValue("");
                Node.setAttributeNode(dataURL);
                Attr URL = doc.createAttribute("URL");
                URL.setValue("http://creativecommons.org/licenses/by-nc-sa/2.5/");
                Node.setAttributeNode(URL);
            }
            Document = doc.createElement("Document");
            Nodes.appendChild(Document);
            Attr id = doc.createAttribute("Document");
            id.setValue("der-mo.net");
            Nodes.setAttributeNode(id);
            Attr name = doc.createAttribute("name");
            name.setValue("http://der-mo.net");
            Nodes.setAttributeNode(name);
            Attr dataURL = doc.createAttribute("dataURL");
            dataURL.setValue("moreNodes.xml");
            Nodes.setAttributeNode(dataURL);
            Attr URL = doc.createAttribute("URL");
            URL.setValue("http://der-mo.net");
            Nodes.setAttributeNode(URL);


            // 0000000000000 Relation 000000000000000000000000000

            for(int i=0;i<indexRelation;i++){
                if(relationValue[i].equals("narrower")) {
                    Element Relations = doc.createElement("Relations");
                    rootElement.appendChild(Relations);
                    Narrower = doc.createElement("MyCustomRelation");
                    Relations.appendChild(Narrower);
                    Attr fromID = doc.createAttribute("fromID");
                    fromID.setValue(Integer.toString(sourceID[i]));
                    Narrower.setAttributeNode(fromID);
                    Attr toID = doc.createAttribute("toID");
                    toID.setValue(Integer.toString(targetID[i]));
                    Narrower.setAttributeNode(toID);
                }
            }

            // shorten way
            // staff.setAttribute("id", "1");



            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(interfacePath));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    // ********************** SKOS GUI ********************************
    public static void SKOS_gui(String interfacePath, int rootID,Connection conn){

        int termID[]=new int[MaxNumber] ;
        String termValue[]=new String[MaxNumber] ;

        int sourceID[]=new int[MaxNumber] ;
        int targetID[]=new int[MaxNumber] ;
        String relationValue[]=new String[MaxNumber] ;

        try{
            Statement stmt = conn.createStatement();
            String sql = "SELECT terminology.terminologyID, terminology.terminology\n" +
                    "FROM terminology ";
            //System.out.println("SQL="+sql);
            ResultSet rs = stmt.executeQuery(sql);
            int indexTerm=0;
            while (rs.next()){
                termID[indexTerm]=rs.getInt("terminology.terminologyID");
                termValue[indexTerm]=rs.getString("terminology.terminology");
                indexTerm++;
            }
            sql = "SELECT terminology1.terminologyID, terminology2.terminologyID, terminology_relationship.relation\n" +
                    "FROM terminology terminology1, terminology terminology2 , terminology_relationship\n" +
                    "WHERE terminology1.terminologyID = terminology_relationship.terminologyID AND terminology2.terminologyID = terminology_relationship.relatedtermID";
            //System.out.println("SQL="+sql);
            rs = stmt.executeQuery(sql);
            int indexRelation=0;
            while (rs.next()){
                sourceID[indexRelation]=rs.getInt("terminology1.terminologyID");
                targetID[indexRelation]=rs.getInt("terminology2.terminologyID");
                relationValue[indexRelation]=rs.getString("terminology_relationship.relation");
                indexRelation++;
            }
            /*for(int i=0;i<indexTerm;i++){
                System.out.println(sourceID[i]+" --- "+targetID[i]+" --- "+relationValue[i]);
            }       */
            rs.close();
            stmt.close();
            xmlWriter(interfacePath,rootID,termID, termValue, indexTerm,sourceID,targetID,relationValue,indexRelation);


        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
        }
    }


}
