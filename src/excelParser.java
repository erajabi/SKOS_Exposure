import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Enayat
 * Date: 5/8/13
 * Time: 7:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class excelParser   {
        static String sql=null;
        static Statement stmt=null;
        static int baseSchema=-1;
        // ********************  READ EXCEL FILE *******************************
        // This function insert record to TERMINOLOGY table
        public static int insertRecordToTermForExcelFile( Connection conn,String tableName, String term, String URL, int schemaID ){
            int termID=0;
            try{
                stmt = conn.createStatement();
                term=term.replace("'", "\\'");
                URL=URL.replace("'", "\\'");
                sql = "INSERT INTO "+tableName+" (terminology,URL,schemaID)" +
                        "VALUES ('"+ term+"','"+ URL+"','"+ schemaID+"');";
                System.out.print(sql);
                stmt.executeUpdate(sql);
                sql = "SELECT * from "+tableName+";";
                ResultSet rs=stmt.executeQuery(sql);
                rs.last();
                termID=rs.getInt("terminologyID");
                rs.close();
                stmt.close();
            }catch(SQLException se){
                se.printStackTrace();
            }catch(Exception e){
                //Handle errors for Class.forName
                e.printStackTrace();
            }finally{
            }//end try
            return termID;
        }

        // This function insert relationship between two terminologies (Broader and Narrower)
        public static void insertBroaderForExcelFile(Connection conn,int termID1, int termID2, String relation){
            try{
                stmt = conn.createStatement();
                sql = "INSERT INTO terminology_relationship (terminologyID ,relatedtermID,relation)" +
                        "VALUES ('"+ termID1+"','"+ termID2+"','"+ relation+"');";
                System.out.println("SQL="+sql);
                stmt.executeUpdate(sql);
                stmt.close();
            }catch(SQLException se){
                se.printStackTrace();
            }catch(Exception e){
                //Handle errors for Class.forName
                e.printStackTrace();
            }finally{
            }
        }
        public static int insertSchemaintoTable(Connection conn, String root){
             int schemaID=-1;
            try{
            stmt = conn.createStatement();
            sql = "INSERT INTO baseschema (name,alias,nameSpace)" +
                    "VALUES ('"+ root +"',null, null);";
            System.out.println( "SQL_Insert_baseSchema="+sql);
            stmt.executeUpdate(sql);
            sql = "select schemaID from baseschema;";
            System.out.println( "SQL_Select_baseSchema="+sql);
            stmt.execute(sql);
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) schemaID=rs.getInt("schemaID");
            rs.close();
            stmt.close();
        }catch(SQLException s){
            System.out.println("Error in inserting base schema!");
            s.printStackTrace();
        }
            return schemaID;
        }

        // ******************* Process Excel file ****************************
        public static int processExcelFile(Connection conn,String inputExcelFilePath,int defaultSheet, String OWLPrefix){

            int counter=0, termID=0, termRelatedID=0, parentID=0;
            String subTerm=null;
            String subCat=null;
            String WorkSheetName=null;
            String root=null;
            int rootID=-1;


            try {

                FileInputStream file = new FileInputStream(new File(inputExcelFilePath));
                //Get the workbook instance for XLS file
                HSSFWorkbook workbook = new HSSFWorkbook(file);
                //Get first sheet from the workbook
                HSSFSheet sheet = workbook.getSheetAt(defaultSheet);
                //Iterate through each rows from first sheet
                Iterator<Row> rowIterator = sheet.iterator();

                // Worksheet name will be the root of the tree
                System.out.println("Worksheet="+workbook.getSheetName(defaultSheet));
                WorkSheetName=workbook.getSheetName(defaultSheet);
                root=WorkSheetName;

                baseSchema=insertSchemaintoTable(conn,root);

                // Insert worksheet name into the table as a terminology
                rootID=insertRecordToTermForExcelFile(conn,"terminology", WorkSheetName, (OWLPrefix + WorkSheetName), baseSchema);
                while(rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    // Pass the first row which is the heading (Category, sub_category,...)
                    // Don't read first row of the worksheet, it is column
                    if(counter==0){ counter++; continue;}
                    // counting worksheet row
                    counter++;
                    //For each row, iterate through each columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    int c=0,level=0;

                    while(cellIterator.hasNext()) {

                        Cell cell = cellIterator.next();
                        // indentify the column to find out the level of the terminology
                        if(cell.getCellType()==3) level++;
                        //System.out.println("Number="+c +" Cell Type="+cell.getCellType()+ " Level="+r +" Row="+row.getRowNum()+" term="+subTerm+" cell="+cell.getStringCellValue());

                        // relation between the first column and second column (also worksheet name and first column)
                        if(c==1 && cell.getCellType() ==1 && level==0) {
                            subCat = cell.getStringCellValue();
                            // Insert relationship between worksheetname and first column
                            termID=insertRecordToTermForExcelFile(conn,"terminology", subTerm, (OWLPrefix + subTerm), baseSchema);
                            insertBroaderForExcelFile(conn,rootID, termID, "narrower");
                            insertBroaderForExcelFile(conn,termID, rootID, "broader");
                            System.out.println("WorkSheet="+WorkSheetName+" AND "+subTerm);
                            System.out.println("First Level="+subTerm+" AND "+cell.getStringCellValue());
                            System.out.println("Insert="+subTerm);
                            System.out.println("Insert="+subCat);

                            // Insert relationship between first column and second column
                            parentID=termID;
                            termRelatedID=insertRecordToTermForExcelFile(conn,"terminology", subCat, (OWLPrefix + subCat), baseSchema);
                            insertBroaderForExcelFile(conn,termID, termRelatedID, "narrower");
                            insertBroaderForExcelFile(conn,termRelatedID, termID, "broader");
                            termID = termRelatedID;
                        }

                        // relation between the second column and third column
                        if(cell.getCellType() ==1 && level==2) {
                            System.out.println("Second Level0="+subCat+" AND "+cell.getStringCellValue());
                            System.out.println("Insert="+cell.getStringCellValue());
                            termRelatedID=insertRecordToTermForExcelFile(conn,"terminology", cell.getStringCellValue(), (OWLPrefix + cell.getStringCellValue()), baseSchema);
                            insertBroaderForExcelFile(conn,termID, termRelatedID, "narrower");
                            insertBroaderForExcelFile(conn,termRelatedID, termID, "broader");
                        }
                        // relation between the second column and third column
                        if(c==2 && cell.getCellType() ==1 && level==1)  {
                            System.out.println("Second Level1="+subTerm+" AND "+cell.getStringCellValue());
                            System.out.println("Insert="+subTerm +" cat="+parentID);
                            termRelatedID=insertRecordToTermForExcelFile(conn,"terminology", subTerm, (OWLPrefix + subTerm), baseSchema);
                            insertBroaderForExcelFile(conn,parentID, termRelatedID, "narrower");
                            insertBroaderForExcelFile(conn,termRelatedID, parentID, "broader");
                            subCat = subTerm;
                            System.out.println("Insert="+cell.getStringCellValue());
                            termID = termRelatedID;
                            termRelatedID=insertRecordToTermForExcelFile(conn,"terminology", cell.getStringCellValue(), (OWLPrefix + cell.getStringCellValue()), baseSchema);
                            insertBroaderForExcelFile(conn,termID, termRelatedID, "narrower");
                            insertBroaderForExcelFile(conn,termRelatedID, termID, "broader");
                        }
                        // relation between the second column and third column
                        if(c==2 && cell.getCellType() ==1 && level==0)  {
                            System.out.println("Second Level2="+subTerm+" AND "+cell.getStringCellValue());
                            subCat = subTerm;
                            System.out.println("Insert="+cell.getStringCellValue());
                            termRelatedID=insertRecordToTermForExcelFile(conn,"terminology", cell.getStringCellValue(), (OWLPrefix + cell.getStringCellValue()), baseSchema);
                            insertBroaderForExcelFile(conn,termID, termRelatedID, "narrower");
                            insertBroaderForExcelFile(conn,termRelatedID, termID, "broader");
                        }
                        c++;
                        switch(cell.getCellType()) {
                            case Cell.CELL_TYPE_BOOLEAN:
                                // System.out.print(cell.getBooleanCellValue() + "\t\t");
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                //System.out.print(cell.getNumericCellValue() + "\t\t");
                                break;
                            case Cell.CELL_TYPE_STRING:
                                subTerm=cell.getStringCellValue();
                                break;
                        }
                    }
                }
                file.close();

                //System.out.println("Root="+root);
                //System.out.println("Root ID="+rootID);

                /* FileOutputStream out =
                        new FileOutputStream(new File("C:\\Enayat\\test.xls"));
                workbook.write(out);
                out.close();*/

            } catch (FileNotFoundException e) {
                System.out.println("File path="+inputExcelFilePath+" does not exist!");
                System.exit(1);
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Unknown Error in parsing Excel file!");
                System.exit(1);
            }
            rootID=terminology.findRoot(conn,root,baseSchema);
            return rootID;

        }

}
