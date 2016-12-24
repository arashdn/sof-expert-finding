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
import org.apache.lucene.index.Term;

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
