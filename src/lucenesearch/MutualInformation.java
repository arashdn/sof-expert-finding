package lucenesearch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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


class ProbTranslate
{
    
    private String word;
    
    private double prob;

    public double getProb()
    {
        return prob;
    }

    public void setProb(double prob)
    {
        this.prob = prob;
    }


    public String getWord()
    {
        return word;
    }

    public void setWord(String word)
    {
        this.word = word;
    }

    public ProbTranslate(String word, double prob)
    {
        this.word = word;
        this.prob = prob;
    }
    
    

}

class Word implements Comparable<Word>
{
    //doc is question
    private String term;
    private Integer CXW1;//chand doc ke dar anha w rokh dade
    private Integer CXU1;//chand ta doc ba "tag" darim
    private Integer CXW_U_1;//chand sanad ke w dar anha rokh dade va "tag" has hastan
    private String tag;
    private int N;
    private boolean isCode;
    int AllhitsPerPage = 2000;

    public Word(String term,String tag , int N, boolean isCode)
    {
        setTerm(term,tag,N,isCode);
    }
    
    public Word(String term,String tag , int N)
    {
        setTerm(term,tag,N,false);
    }

    public Integer getCXW1() throws IOException, ParseException
    {
        if(CXW1 == null)
        {
            int hitsPerPage = this.AllhitsPerPage;

            String index = new Searcher().getPostIndexPath();
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        
                
            booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 1), BooleanClause.Occur.MUST);
            booleanQuery.add(new QueryParser(this.isCode?"Code":"Body", analyzer).parse(this.getTerm()), BooleanClause.Occur.MUST);

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
            int hitsPerPage = this.AllhitsPerPage;

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
            int hitsPerPage = this.AllhitsPerPage;

            String index = new Searcher().getPostIndexPath();
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        
                
            booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(IntPoint.newExactQuery("PostTypeId", 1), BooleanClause.Occur.MUST);
                booleanQuery.add(new QueryParser(this.isCode?"Code":"Body", analyzer).parse(this.term), BooleanClause.Occur.MUST);
            booleanQuery.add(new QueryParser("Tags", analyzer).parse(this.tag), BooleanClause.Occur.MUST);



            Query q = booleanQuery.build();

            TopDocs results;
            results = searcher.search(q , 5 * hitsPerPage);
            this.CXW_U_1 = results.totalHits;
            
            reader.close();
        }
        return CXW_U_1;
    }

    
    Double getMI() throws IOException, ParseException
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
            return "Word{" + 
                    "term=" + term + 
                    ", CXW1=" + getCXW1() + 
                    ", CXU1=" + getCXU1() + 
                    ", CXW_U_1=" + getCXW_U_1() + 
                    ", MI="+getMI() +
                    ", tag=" + tag + '}';
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

    public void setTerm(String term, String tag, int N, boolean isCode)
    {
        this.term = term;
        this.N = N;
        this.tag = tag;
        this.CXW1 = null;
        this.CXU1 = null;
        this.CXW_U_1 = null;
        this.isCode = isCode;
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
        return this.getTerms(tag, true);
    }
    public ArrayList<String> getTerms(String tag , boolean printDedug) throws IOException, ParseException
    {
        return this.getTerms(tag, printDedug,false);
    }
    public ArrayList<String> getTerms(String tag , boolean printDedug , boolean isCode) throws IOException, ParseException
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
        if(printDedug)
            System.out.println(numTotalHits + " Total document found.");
        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage); 
        
        HashMap<String,Long> res = new HashMap<>();
        
        for (int i = start; i < end; i++)
        {
            int docID = hits[i].doc;
            Document doc = searcher.doc(docID);
            ExtendedDocument ed = new ExtendedDocument(docID, reader);
            
            HashMap<String,Long> tmp = ed.getTermFrequency(isCode?"Code":"Body");
            
            Iterator it = tmp.entrySet().iterator();
            while (it.hasNext()) 
            {
                Map.Entry pair = (Map.Entry)it.next();
                String term = (String) pair.getKey();
                
                //this term causes lucene parser to crash!!!!!!
                if(term.equalsIgnoreCase("hh:mm:ss")||term.equalsIgnoreCase("jdbc:oracle:thin")||term.equalsIgnoreCase("hh:mm:ss.sss"))
                    continue;
                
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
        if(printDedug)
            System.out.println("Len: "+len);
        ArrayList<String> topWords = new ArrayList<>();
        Iterator it = sorted_map.entrySet().iterator();
        
        int i = 0;
        while (it.hasNext()) 
        {
            Map.Entry pair = (Map.Entry)it.next();
            topWords.add((String) pair.getKey());
            if(printDedug)
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
            if(printDedug)
                System.out.println((i++)+": "+w.toString());
        }
        
        Collections.sort(finalWords);
        
        ArrayList<String> resWords = new ArrayList<>();
        
        for (Word finalWord : finalWords)
        {
            resWords.add(finalWord.getTerm());
            if(printDedug)
                System.out.println(finalWord);
        }

        return resWords;
        
    }
    
    public ArrayList<ProbTranslate> getTermsAndProb(String tag) throws IOException, ParseException
    {
        return this.getTermsAndProb(tag, true);
    }
    
    public ArrayList<ProbTranslate> getTermsAndProb(String tag , boolean printDedug) throws IOException, ParseException
    {
        return this.getTermsAndProb(tag, printDedug, false);
    }
    
    public ArrayList<ProbTranslate> getTermsAndProb(String tag , boolean printDedug, boolean isCode) throws IOException, ParseException
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
        if(printDedug)
            System.out.println(numTotalHits + " Total document found.");
        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage); 
        
        HashMap<String,Long> res = new HashMap<>();
        
        for (int i = start; i < end; i++)
        {
            int docID = hits[i].doc;
            Document doc = searcher.doc(docID);
            ExtendedDocument ed = new ExtendedDocument(docID, reader);
            
            HashMap<String,Long> tmp = ed.getTermFrequency(isCode?"Code":"Body");
            
            Iterator it = tmp.entrySet().iterator();
            while (it.hasNext()) 
            {
                Map.Entry pair = (Map.Entry)it.next();
                String term = (String) pair.getKey();
                
                //this term causes lucene parser to crash!!!!!!
                if(term.equalsIgnoreCase("hh:mm:ss")||term.equalsIgnoreCase("jdbc:oracle:thin")||term.equalsIgnoreCase("hh:mm:ss.sss"))
                    continue;
                
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
        if(printDedug)
            System.out.println("Len: "+len);
        ArrayList<String> topWords = new ArrayList<>();
        Iterator it = sorted_map.entrySet().iterator();
        
        int i = 0;
        while (it.hasNext()) 
        {
            Map.Entry pair = (Map.Entry)it.next();
            topWords.add((String) pair.getKey());
            if(printDedug)
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
        
        double sumMI = 0;
        
        i = 0;
        ArrayList<Word> finalWords = new ArrayList<>();
        for (String topWord : topWords)
        {
            Word w = new Word(topWord,tag,numTotalHits);
            if(!w.getMI().isNaN())
            {
                finalWords.add(w);
                sumMI += w.getMI();
            }
            if(printDedug)
                System.out.println((i++)+": "+w.toString());
        }
        
        if(printDedug)
            System.out.println("Sum MI: "+sumMI);
        
        Collections.sort(finalWords);
        
        ArrayList<ProbTranslate> resWords = new ArrayList<>();
        
        for (Word finalWord : finalWords)
        {
            resWords.add(new ProbTranslate(finalWord.getTerm(), finalWord.getMI()*1.0/sumMI));
            if(printDedug)
                System.out.println(finalWord);
        }

        return resWords;
        
    }
    
    public void saveAllTransaltionsByTag(int topWordsCount) throws IOException, FileNotFoundException, ParseException
    {
        saveAllTransaltionsByTag(topWordsCount,false);
    }
    public void saveAllTransaltionsByTag(int topWordsCount , boolean isCode) throws FileNotFoundException, IOException, ParseException
    {
        ArrayList<String> tags = Utility.getTags();
        ArrayList<String> res = new ArrayList<>();
       
        
        PrintWriter out = new PrintWriter(isCode?"data/tag_mutuals_code.txt":"data/tag_mutuals.txt");
        int c = 0;
        for (String tag : tags)
        {
            //System.out.print((++c)+"-> "+tag+" => ");
            String s = tag+"~";
            res = getTerms(tag,false,isCode);
            int t = topWordsCount;
            if(res.size()<topWordsCount)
            {
                System.out.println("Small res "+res.size());
                t = res.size();
            }
            for (int i = 0; i < t; i++)
            {
                s += res.get(i)+ (i == topWordsCount - 1 ?"":",");
            }
            out.println(s);
            System.out.println(s);
        }
        out.close();
    }
    
    public void saveAllTransaltionsByTagAndProb(int topWordsCount) throws FileNotFoundException, IOException, ParseException
    {
        saveAllTransaltionsByTagAndProb(topWordsCount,false);
    }
    
    public void saveAllTransaltionsByTagAndProb(int topWordsCount, boolean isCode) throws FileNotFoundException, IOException, ParseException
    {
        ArrayList<String> tags = Utility.getTags();
        ArrayList<ProbTranslate> res = new ArrayList<>();
       
        
        PrintWriter out = new PrintWriter(isCode?"data/tag_mutuals_prob_code.txt":"data/tag_mutuals_prob.txt");
        int c = 0;
        for (String tag : tags)
        {
            //System.out.print((++c)+"-> "+tag+" => ");
            String s = tag+"~";
            res = getTermsAndProb(tag,false,isCode);
            int t = topWordsCount;
            if(res.size()<topWordsCount)
            {
                System.out.println("Small res "+res.size());
                t = res.size();
            }
            for (int i = 0; i < t; i++)
            {
                s += res.get(i).getWord() + ":" + res.get(i).getProb() + (i == topWordsCount - 1 ?"":",");
            }
            out.println(s);
            System.out.println(s);
        }
        out.close();
    }
    
    public void balogKarimZadeganProb(int countWords) throws IOException, ParseException
    {
        balogKarimZadeganProb(countWords,false);
    }
    public void balogKarimZadeganProb(int countWords, boolean isCode) throws IOException, ParseException
    {
        java.nio.file.Path filePath = new java.io.File(isCode?"data/tag_mutuals_prob_code.txt":"data/tag_mutuals_prob.txt").toPath();
        List<String> stringList = Files.readAllLines(filePath);
        
        HashMap<String,ArrayList<ProbTranslate>> tags = new HashMap<>();
        for (String s : stringList)
        {
            String[] tgs = s.split("~");
            ArrayList<ProbTranslate> e = new ArrayList<>();
            if(tgs.length > 1 && tgs[1] != null && tgs[1] != "")
            {
                String [] trs = tgs[1].split(",");
                
                for (int i = 0; i<countWords ; i++)
                {
                    String [] t = trs[i].split(":");
                    e.add(new ProbTranslate(t[0], 1));
//                    e.add(new ProbTranslate(t[0], Double.parseDouble(t[1])));
                }
            }
            else
            {
                e.add(new ProbTranslate(tgs[0], 1));
            }
            tags.put(tgs[0], e );
        }
        Balog balog = new Balog();
        Iterator it = tags.entrySet().iterator();
        HashMap<Integer, Double > userScores = null;
        HashMap<Integer, Double > totalUserScores = new HashMap<>();
        while (it.hasNext()) 
        {
            totalUserScores = new HashMap<>();
            Map.Entry pair = (Map.Entry)it.next();
            String tag = pair.getKey().toString();
            ArrayList<ProbTranslate> trans = (ArrayList<ProbTranslate>) pair.getValue();

            
            for (ProbTranslate tran : trans)
            {
                //System.out.println(tag+" -> "+tran.getWord()+": ");
                userScores = null;
                userScores = balog.calculateBalog2(10000, tran.getWord(), false, null, 0.5);
                
                Iterator it2 = userScores.entrySet().iterator();
                while (it2.hasNext())
                {
                    Map.Entry pair2 = (Map.Entry) it2.next();
                    if (totalUserScores.containsKey(pair2.getKey()))
                    {
                        double oldScore = userScores.get(pair2.getKey());
                        totalUserScores.replace(Integer.parseInt(pair2.getKey().toString()), tran.getProb()*Double.parseDouble(pair2.getValue().toString()) + oldScore);
                    }
                    else
                    {
                        totalUserScores.put(Integer.parseInt(pair2.getKey().toString()), tran.getProb()*Double.parseDouble(pair2.getValue().toString()));
                    }
                    it2.remove(); // avoids a ConcurrentModificationException
                }

            }
            ValueComparator3 bvc = new ValueComparator3(totalUserScores);
            TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(bvc);
            sorted_map.putAll(totalUserScores);
            ArrayList<Integer> lst = new ArrayList<>();

            for (Map.Entry<Integer, Double> entry : sorted_map.entrySet()) 
            {
                lst.add(entry.getKey());
            }
            Evaluator ev = new Evaluator();
            double map = ev.map(lst, balog.getGoldenList( Utility.getGoldenFileName(tag)));
            System.out.println(tag+","+map);
//        double p1 = ev.precisionAtK(lst, getGoldenList(goldenFile),1);
//        double p5 = ev.precisionAtK(lst, getGoldenList(goldenFile),5);
//        double p10 = ev.precisionAtK(lst, getGoldenList(goldenFile),10);
        
            
            it.remove(); // avoids a ConcurrentModificationException
        }
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

class ValueComparator3 implements Comparator<Integer> 
{
    Map<Integer, Double> base;

    public ValueComparator3(Map<Integer, Double> base) 
    {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(Integer a, Integer b) 
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
