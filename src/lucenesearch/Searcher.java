package lucenesearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author arashdn
 */
public class Searcher
{
    
    private String postIndexPath;

    public String getPostIndexPath()
    {
        return postIndexPath;
    }

    public void setPostIndexPath(String postIndexPath)
    {
        this.postIndexPath = postIndexPath;
    }

    public Searcher(String postIndexPath)
    {
        this.setPostIndexPath(postIndexPath);
    }
    
    public Post search() throws IOException, ParseException
    {
        String index = getPostIndexPath();
        String field = "Body";
        String queryString = "int string parse";

        int hitsPerPage = 100;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser(field, analyzer);

        Query query = parser.parse(queryString);

        System.out.println("Searching for: " + query.toString(field));
        doSearch(searcher, query, hitsPerPage);
        reader.close();
        return null;
    }
    
    private void doSearch(IndexSearcher searcher, Query query, int hitsPerPage) throws IOException
    {

        // Collect enough docs to show 5 pages
        TopDocs results = searcher.search(query, 5 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);

        for (int i = start; i < end; i++)
        {
            Document doc = searcher.doc(hits[i].doc);
            Post p =new Post(doc);
            System.out.println(p);
        }
    }
}
