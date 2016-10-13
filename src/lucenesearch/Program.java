package lucenesearch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
/**
 *
 * @author arashdn
 */
public class Program
{
    

    public static void main(String[] args) throws ParserConfigurationException, SAXException, ParseException
    {
//        // Open the file
//        FileInputStream fstream;
//        try
//        {
//            fstream = new FileInputStream("./data/Posts.xml");
//            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
//
//            String strLine;
//            
//            
//            //skip first two lines which contains xml definations
//            br.readLine();
//            br.readLine();
//            
//            String s = br.readLine();
//            InputSource is = new InputSource(new StringReader(s));
//            DOMParser dp = new DOMParser();
//            dp.parse(is);
//            Document doc = dp.getDocument();
//            NodeList nl = doc.getElementsByTagName("row");
//            Node n = nl.item(0);
//            NamedNodeMap nnm = n.getAttributes();
//            String Id = nnm.getNamedItem("CreationDate").getFirstChild().getTextContent();
//            String d = Id.replaceAll("T", " ");
//            final String NEW_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
//            Date parsedDate = dateFormat.parse(d);
//            System.out.println(parsedDate.getTime());
//
//            //Read File Line By Line
////            while ((strLine = br.readLine()) != null)
////            {
////                // Print the content on the console
////                System.out.println(strLine);
////            }
//
//            //Close the input stream
//            br.close();
//        }
        try
        {
            Indexer i = new Indexer();
            i.indexPosts("./data/Posts.xml");
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
