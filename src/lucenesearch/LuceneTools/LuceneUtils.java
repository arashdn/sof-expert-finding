package lucenesearch.LuceneTools;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author arashdn
 */
public class LuceneUtils
{
    
    public static FieldType getVectorField()
    {
        FieldType myFieldType = new FieldType(TextField.TYPE_STORED);
        myFieldType.setStoreTermVectors(true);
        return myFieldType;
    }
    
    private IndexReader reader;
       
    public IndexReader getReader()
    {
        return reader;
    }

    public void setReader(IndexReader reader)
    {
        this.reader = reader;
    }

    
    
    
    
    public LuceneUtils(IndexReader reader)
    {
        setReader(reader);
    }
    
    public long getCountOfAllTerms(String field) throws IOException
    {
        return reader.getSumTotalTermFreq(field);
    }
    
    public long getNumberOfAllDocs(String field) throws IOException
    {
        return reader.getDocCount(field);
    }
    
    public long getTermFrequencyInCollection(String field, String term) throws IOException
    {
        return reader.totalTermFreq(new Term(field, term));

    }
    
    public double getTFIDFScoreInCollection(String FIELD, String word) throws IOException
    {
        
        IndexSearcher searcher = new IndexSearcher(reader);
        ClassicSimilarity similarity = new ClassicSimilarity();
        IndexReaderContext context = searcher.getTopReaderContext();
        CollectionStatistics collectionStats = searcher.collectionStatistics(FIELD);
        
        long totalDocCount = collectionStats.docCount();
        //Terms termVector = reader.getTermVector(docId, FIELD);
        //TermsEnum iterator = termVector.iterator(); 
        
   
        BytesRef ref = new BytesRef(word);

        long termFreq = this.getTermFrequencyInCollection(FIELD,word);
        float tf = similarity.tf(termFreq);

        Term term = new Term(FIELD, ref);
        TermContext termContext = TermContext.build(context, term);

        TermStatistics termStats = searcher.termStatistics(term, termContext);
        long docFreq = termStats.docFreq();
        float idf = similarity.idf(docFreq, totalDocCount);

        return tf*idf;
        
    }
    
    public static ArrayList<String> getAnalayzed(String token) throws IOException
    {
        ArrayList<String> res = new ArrayList<>();
        String text = token;
        Analyzer analyzer = new StandardAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(text));
//        TermAttribute term = tokenStream.addAttribute(TermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken())
        {
            res.add(tokenStream.addAttribute(TermToBytesRefAttribute.class).toString());
        }
        tokenStream.close();
        return res;
    }
    
    public static ArrayList<String> getAnalyzedRemoveHtml(String token) throws IOException
    {
        token = token.replaceAll("\\<.*?>"," ");
        return getAnalayzed(token);
    }
    
}
