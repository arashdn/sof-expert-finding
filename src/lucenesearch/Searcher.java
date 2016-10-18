package lucenesearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PointRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSelector;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.TermRangeQuery;
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

    public Searcher()
    {
        this.setPostIndexPath("./data/index");
    }

    public Searcher(String postIndexPath)
    {
        this.setPostIndexPath(postIndexPath);
    }
    
    
    public ArrayList<Post> search(boolean body , String bodyTerm ,
            boolean tag , String tags ,
            boolean date , long startDate , long endDate ,
            boolean searchId , int pid,
            boolean sortByScore,
            boolean serachParent , int parentId,
            int type)throws IOException, ParseException
    {
        String index = getPostIndexPath();
        String field = "Body";
        String queryString = "int string parse";

        int hitsPerPage = 100;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser(field, analyzer);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();


//        Query query1 = parser.parse(queryString);
//        long l1 = 1294512395110L;
//        long l2 = 1312632840280L;
//        Query query2 = LongPoint.newRangeQuery("CreationDate", l1 , l2);
//        Query query3 = IntPoint.newRangeQuery("ViewCount", 21888, 22000);
//        Query query4 = new QueryParser("Tags", analyzer).parse("java");
//        Query query5 = new QueryParser("Tags", analyzer).parse("xml");

        if(body)
        {
            booleanQuery.add(new QueryParser("Body", analyzer).parse(bodyTerm), BooleanClause.Occur.MUST);
        }
        if(tag)
        {
            String[] tgs = tags.split(" ");
            for (String tg : tgs)
            {
                booleanQuery.add(new QueryParser("Tags", analyzer).parse(tg), BooleanClause.Occur.MUST);
            }
        }
        if(date)
        {
            booleanQuery.add(LongPoint.newRangeQuery("CreationDate", startDate , endDate), BooleanClause.Occur.MUST);
        }
        if(type == 1 || type == 2)
        {
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", type), BooleanClause.Occur.MUST);
        }
        if(searchId)
        {
            booleanQuery.add(IntPoint.newExactQuery("Id", pid), BooleanClause.Occur.MUST);
        }
        if(serachParent)
        {
            booleanQuery.add(IntPoint.newExactQuery("ParentId", parentId), BooleanClause.Occur.MUST);
        }
        
        
        //System.out.println("Searching for: " + query.toString(field));
        ArrayList<Post> res = doSearch(searcher, booleanQuery.build(), hitsPerPage,sortByScore);
        reader.close();
        return res;
    }
    
    private ArrayList<Post> doSearch(IndexSearcher searcher, Query query, int hitsPerPage , boolean sortByScore) throws IOException
    {

        // Collect enough docs to show 5 pages
        TopDocs results;
        if(sortByScore)
        {
            results = searcher.search(query, 5 * hitsPerPage,new Sort(new SortedNumericSortField("SortScore", SortField.Type.INT,true)));
        }
        else
            results = searcher.search(query, 5 * hitsPerPage);
        
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);

        ArrayList<Post> res = new ArrayList<>();
        
        for (int i = start; i < end; i++)
        {
            Document doc = searcher.doc(hits[i].doc);
            Post p =new Post(doc);
            //System.out.println(p);
            res.add(p);
        }
        
        return res;
    }
    
    
    public ArrayList<Post> searchAnswer(boolean accepted , String questionTerm)throws IOException, ParseException
    {
        String index = getPostIndexPath();
        String field = "Body";

        int hitsPerPage = 35;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(new QueryParser("Body", analyzer).parse(questionTerm), BooleanClause.Occur.MUST);
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 1), BooleanClause.Occur.MUST);
        if(accepted)
            booleanQuery.add(IntPoint.newRangeQuery("AcceptedAnswerId",1,50000000), BooleanClause.Occur.MUST);
        //else
            //booleanQuery.add(IntPoint.newExactQuery("AcceptedAnswerId",0), BooleanClause.Occur.MUST);
        
        //System.out.println("Searching for: " + query.toString(field));
        ArrayList<Post> res = doSearch(searcher, booleanQuery.build(), hitsPerPage,false);
                
        for (Post p : res)
        {
            booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(IntPoint.newExactQuery("ParentId", p.getId()), BooleanClause.Occur.MUST);
            if(accepted)
                booleanQuery.add(IntPoint.newExactQuery("Id",p.getAcceptedAnswerId()), BooleanClause.Occur.MUST);
            else
                booleanQuery.add(IntPoint.newExactQuery("Id",p.getAcceptedAnswerId()), BooleanClause.Occur.MUST_NOT);
           ArrayList<Post> ans = doSearch(searcher, booleanQuery.build(), hitsPerPage , false);
            p.setAnswers(ans);
        }
        
        reader.close();
        return res;
    }
}
