package lucenesearch.LuceneTools;

import java.io.IOException;
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
    
}
