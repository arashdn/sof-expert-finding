/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucenesearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import lucenesearch.LuceneTools.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.IBSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author arashdn
 */
public class RelevantPostFinder 
{
    public void saveRelevantPost() throws SQLException, IOException, ParseException
    {
        String url = "jdbc:mysql://localhost:3306/sof17";
        String username = "root";
        String password = "root";
        String folderPath = "./data/rel_posts/";
        String dupNotFound = "./data/dup_not_exist.txt";
        int hitsPerPage = 10000;

        System.out.println("Connecting database...");

        Connection conn = DriverManager.getConnection(url, username, password);
        System.out.println("Database connected!");
        Statement stmt = conn.createStatement();
        String query = "select PostId,PostBody,OriginalPostId from java_test_data";
        ResultSet rs = stmt.executeQuery(query);
        
        
        
        String index = new Searcher().getPostIndexPath();

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
//        searcher.setSimilarity(new BM25Similarity(0.05f, 0.03f)); //!!!!!!!!!!!!!!
        searcher.setSimilarity(new BM25Similarity()); //!!!!!!!!!!!!!!
        
        Analyzer analyzer = new StandardAnalyzer();
        
        int cnt = 0;
        
        while (rs.next())
        {
            System.out.println("Processing post "+(++cnt));
            
            int postid = rs.getInt("PostId");
            int dupId = rs.getInt("OriginalPostId");
            ArrayList<String> bd = LuceneUtils.getAnalyzedRemoveHtml(rs.getString("PostBody").replace(':', ' '));       
            
            StringBuilder sb = new StringBuilder();
            int j = 0;
            for (String b : bd) 
            {
                if (++j > 600)
                    break;
                sb.append(b);
                sb.append(" ");
            }
            String body = sb.toString();
            
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 1), BooleanClause.Occur.MUST);
            booleanQuery.add(new QueryParser("Tags", analyzer).parse("java"), BooleanClause.Occur.MUST);
            booleanQuery.add(new QueryParser("Body", analyzer).parse(body), BooleanClause.Occur.MUST);
            
            TopDocs results;
            results = searcher.search(booleanQuery.build(), hitsPerPage);
        
            ScoreDoc[] hits = results.scoreDocs;

            int numTotalHits = results.totalHits;
            System.out.println(numTotalHits + " total matching documents");

            int start = 0;
            int end = Math.min(numTotalHits, hitsPerPage);
            
            PrintWriter out = new PrintWriter (folderPath+postid+".txt");
            
            boolean isFound = false;

            for (int i = start; i < end; i++)
            {
                Document doc = searcher.doc(hits[i].doc);
                int id = Integer.parseInt(doc.get("SId"));
                String s = doc.get("Body");
                if (id == dupId)
                    isFound = true;
                out.println(id);
            }
            out.close();
           
            if (!isFound)
            {
                System.out.println("Duplicate not found");
                PrintWriter out2 = new PrintWriter (new FileOutputStream(new File(dupNotFound), true /* append = true */));
                out2.println(postid);
                out2.close();
            }
            
        }
        rs.close();
        stmt.close();
        conn.close();
    }
}
