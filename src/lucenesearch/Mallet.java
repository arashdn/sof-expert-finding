/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucenesearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import lucenesearch.LuceneTools.ExtendedDocument;
import lucenesearch.LuceneTools.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Arashdn
 */
public class Mallet
{
    public void getMalletOutput() throws IOException
    {
        int hitsPerPage = 10000000;
        
        String index = new Searcher().getPostIndexPath();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        
        //booleanQuery.add(new QueryParser("Body", analyzer).parse(""), BooleanClause.Occur.MUST);
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 2), BooleanClause.Occur.MUST);
        
        TopDocs results;

        results = searcher.search(booleanQuery.build(), hitsPerPage);
        
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);
        
        PrintWriter pw = new PrintWriter("./data/mallet.txt");

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
        {
            System.out.println("Doc "+i);
            Document doc = searcher.doc(hits[i].doc);
            ArrayList<String> res = LuceneUtils.getAnalyzedRemoveHtml(doc.get("Body"));
        

            int id=Integer.parseInt(doc.get("SId"));
            sb = new StringBuilder();
            sb.append(id);
            sb.append("\t");
            for (String re : res)
            {
                re = re.replaceAll("\r\n", " ").replaceAll("\n"," ").replaceAll("<.+?>", "").replaceAll(" +"," ").replaceAll("[^\\x00-\\x7F]", " ").trim();
                sb.append(re).append(" ");
            }
            sb.append("\n");
            pw.print(sb.toString());
            
            
        }
        pw.close();
        
    }
    
    public void getMalletAllOutput() throws IOException
    {
        
        String index = new Searcher().getPostIndexPath();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        
        PrintWriter pw = new PrintWriter("./data/mallet_all.txt");

        StringBuilder sb = new StringBuilder();
        
        
        for (int i = 0; i < reader.maxDoc(); i++)
        {
            Document doc = reader.document(i);
            System.out.println("Doc "+i);
            
            ArrayList<String> res = LuceneUtils.getAnalyzedRemoveHtml(doc.get("Body"));
        

            int id=Integer.parseInt(doc.get("SId"));
            sb = new StringBuilder();
            sb.append(id);
            sb.append("\t");
            for (String re : res)
            {
                re = re.replaceAll("\r\n", " ").replaceAll("\n"," ").replaceAll("<.+?>", "").replaceAll(" +"," ").replaceAll("[^\\x00-\\x7F]", " ").trim();
                sb.append(re).append(" ");
            }
            sb.append("\n");
            pw.print(sb.toString());
            
            
        }
        pw.close();
        
    }
}
