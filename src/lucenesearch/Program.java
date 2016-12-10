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
    

    public static void main2(String[] args) throws ParserConfigurationException, SAXException, ParseException, org.apache.lucene.queryparser.classic.ParseException
    {
        String postIndexPath = "./data/Posts.xml";
        try
        {
            if(false)
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

    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        if(args == null || args.length == 0)
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    new MainForm().setVisible(true);
                }
            });
        }
        else
        {
            if(args[0].equalsIgnoreCase("ngram"))
            {
                try 
                {
                    int n = Integer.parseInt(args[1]);
                    int hit = Integer.parseInt(args[2]);
                    new NGram().getNGram(n,hit);
                }
                catch (IOException ex) {
                    Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (org.apache.lucene.queryparser.classic.ParseException ex)
                {
                    Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else
            {
                System.out.println("Invalid Arg");
            }
        }
    }
    
}
