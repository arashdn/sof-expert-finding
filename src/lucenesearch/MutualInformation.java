package lucenesearch;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lucenesearch.LuceneTools.ExtendedDocument;
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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author arashdn
 */


class Word implements Comparable<Word>
{
    //doc is question
    private String term;
    private Integer CXW1;//chand doc ke dar anha w rokh dade
    private Integer CXU1;//chand ta doc ba "tag" darim
    private Integer CXW_U_1;//chand sanad ke w dar anha rokh dade va "tag" has hastan
    private String tag;
    private int N;

    public Word(String term,String tag , int N)
    {
        setTerm(term,tag,N);
    }
    

    public Integer getCXW1() throws IOException, ParseException
    {
        if(CXW1 == null)
        {
            int hitsPerPage = 1000;

            String index = new Searcher().getPostIndexPath();
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        
                
            booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 1), BooleanClause.Occur.MUST);
            booleanQuery.add(new QueryParser("Body", analyzer).parse(this.term), BooleanClause.Occur.MUST);



            Query q = booleanQuery.build();

            TopDocs results;
            results = searcher.search(q , 5 * hitsPerPage);
            this.CXW1 = results.totalHits;
            
            reader.close();
        }
        return CXW1;
    }

    public Integer getCXU1() throws IOException, ParseException
    {
        if(CXU1 == null)
        {
            int hitsPerPage = 1000;

            String index = new Searcher().getPostIndexPath();
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        
                
            booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 1), BooleanClause.Occur.MUST);
            booleanQuery.add(new QueryParser("Tags", analyzer).parse(this.tag), BooleanClause.Occur.MUST);



            Query q = booleanQuery.build();

            TopDocs results;
            results = searcher.search(q , 5 * hitsPerPage);
            this.CXU1 = results.totalHits;
            
            reader.close();
        }
        return CXU1;
    }

    public Integer getCXW_U_1() throws IOException, ParseException
    {
        if(CXW_U_1 == null)
        {
            int hitsPerPage = 1000;

            String index = new Searcher().getPostIndexPath();
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        
                
            booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 1), BooleanClause.Occur.MUST);
                booleanQuery.add(new QueryParser("Body", analyzer).parse(this.term), BooleanClause.Occur.MUST);
            booleanQuery.add(new QueryParser("Tags", analyzer).parse(this.tag), BooleanClause.Occur.MUST);



            Query q = booleanQuery.build();

            TopDocs results;
            results = searcher.search(q , 5 * hitsPerPage);
            this.CXW_U_1 = results.totalHits;
            
            reader.close();
        }
        return CXW_U_1;
    }

    
    double getMI() throws IOException, ParseException
    {
        double res = 0;
        
        double pw1 = (double)this.getCXW1()/N;
        double pw0 = 1-pw1;
        double pu1 = (double)this.getCXU1()/N;
        double pu0 = 1-pu1;
        
        double pw1u1 = (double)this.getCXW_U_1()/N;
        double pw1u0 = ((double)(this.getCXW1()-this.getCXW_U_1()))/(N);
        double pw0u1 = ((double)(this.getCXU1()-this.getCXW_U_1()))/(N);
        double pw0u0 = 1-(pw1u1+pw1u0+pw0u1);
        
        res += pw1u1*Math.log((pw1u1)/(pw1*pu1));
        res += pw0u0*Math.log((pw0u0)/(pw0*pu0));
        res += pw0u1*Math.log((pw0u1)/(pw0*pu1));
        res += pw1u0*Math.log((pw1u0)/(pw1*pu0));
        
        return res;
    }
    
    @Override
    public String toString()
    {
        try
        {
            return "Word{" + "term=" + term + ", CXW1=" + getCXW1() + ", CXU1=" + getCXU1() + ", CXW_U_1=" + getCXW_U_1() + ", tag=" + tag + '}';
        }
        catch (IOException ex)
        {
            Logger.getLogger(Word.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ParseException ex)
        {
            Logger.getLogger(Word.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    
    
    

    public String getTerm()
    {
        return term;
    }

    public void setTerm(String term, String tag, int N)
    {
        this.term = term;
        this.N = N;
        this.tag = tag;
        this.CXW1 = null;
        this.CXU1 = null;
        this.CXW_U_1 = null;
    }

    @Override
    public int compareTo(Word o)
    {
        try
        {
            return Double.compare(o.getMI(),this.getMI());
        }
        catch (IOException ex)
        {
            Logger.getLogger(Word.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ParseException ex)
        {
            Logger.getLogger(Word.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    
    
}

public class MutualInformation
{
    public ArrayList<String> getTerms(String tag) throws IOException, ParseException
    {
        int hitsPerPage = 100000;

        String index = new Searcher().getPostIndexPath();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        
                
        booleanQuery = new BooleanQuery.Builder();
        //booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 1), BooleanClause.Occur.MUST);
        booleanQuery.add(new QueryParser("Tags", analyzer).parse(tag), BooleanClause.Occur.MUST);
        
        
        
        Query q = booleanQuery.build();
                
        TopDocs results;
        results = searcher.search(q , 2 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " Total document found.");
        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage); 
        
        HashMap<String,Long> res = new HashMap<>();
        
        for (int i = start; i < end; i++)
        {
            int docID = hits[i].doc;
            Document doc = searcher.doc(docID);
            ExtendedDocument ed = new ExtendedDocument(docID, reader);
            
            HashMap<String,Long> tmp = ed.getTermFrequency("Body");
            
            Iterator it = tmp.entrySet().iterator();
            while (it.hasNext()) 
            {
                Map.Entry pair = (Map.Entry)it.next();
                String term = (String) pair.getKey();
                long value = (long)pair.getValue();
                if(res.containsKey(term))
                {
                    Long cur = res.get(term);
                    res.put( term , cur+value);
                }
                else
                {
                    res.put(term, value );
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
        
        
        ValueComparator2 bvc = new ValueComparator2(res);
        TreeMap<String, Long> sorted_map = new TreeMap<String, Long>(bvc);
        sorted_map.putAll(res);
        
        
        int len = Math.min(sorted_map.size(), 300);
        System.out.println("Len: "+len);
        ArrayList<String> topWords = new ArrayList<>();
        Iterator it = sorted_map.entrySet().iterator();
        
        int i = 0;
        while (it.hasNext()) 
        {
            Map.Entry pair = (Map.Entry)it.next();
            topWords.add((String) pair.getKey());
            System.out.println(pair.getKey() + " => " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
            if(++i>=len)
                break;
            
        }
        
        
        booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 1), BooleanClause.Occur.MUST);        
        q = booleanQuery.build();
                
        results = searcher.search(q , 2 * hitsPerPage);
        numTotalHits = results.totalHits;
        
        i = 0;
        ArrayList<Word> finalWords = new ArrayList<>();
        for (String topWord : topWords)
        {
            Word w = new Word(topWord,tag,numTotalHits);
            finalWords.add(w);
            System.out.println((i++)+": "+w.toString());
        }
//        for (String topWord : topWords)
//        {
//            finalWords.add(new Word(topWord,tag,numTotalHits));
//            System.out.println((i++)+" word");
//        }
        
        Collections.sort(finalWords);
        
        ArrayList<String> resWords = new ArrayList<>();
        
        for (Word finalWord : finalWords)
        {
            resWords.add(finalWord.getTerm());
            System.out.println(finalWord);
        }

        return resWords;
        
    }
}


class ValueComparator2 implements Comparator<String> 
{
    Map<String, Long> base;

    public ValueComparator2(Map<String, Long> base) 
    {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) 
    {
        if (base.get(a) >= base.get(b)) 
        {
            return -1;
        } 
        else 
        {
            return 1;
        } // returning 0 would merge keys
    }
}
