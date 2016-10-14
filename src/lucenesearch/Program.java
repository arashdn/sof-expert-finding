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
    

    public static void main(String[] args) throws ParserConfigurationException, SAXException, ParseException, org.apache.lucene.queryparser.classic.ParseException
    {
        String postIndexPath = "./data/Posts.xml";
        try
        {
            if(true)
            {
                Indexer i = new Indexer();
                i.indexPosts(postIndexPath);
            }
//            Searcher s = new Searcher("./data/index");
//            s.search();
            
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
