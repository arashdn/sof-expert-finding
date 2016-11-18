package lucenesearch.LuceneTools;

import java.io.IOException;
import java.util.HashMap;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author arashdn
 */
public class ExtendedDocument
{

    public ExtendedDocument(int docId, IndexReader reader)
    {
        setReader(reader);
        this.setDoc(docId);
    }
    
    
    
//    private Document doc;
    private int docId;
    private Terms termVector;
    private HashMap<String,Long> termsFrequency;
    private IndexReader reader;
    private int termsCount;

    
    public int getTermsCount(String field) throws IOException
    {
        calculateTermFrequency(field);
        return termsCount;
    }
  
    public void setReader(IndexReader reader)
    {
        this.reader = reader;
    }

    public Terms getTermVector(String field) throws IOException
    {
        if (termVector == null)
        {
            termVector = reader.getTermVector(getDocId(), field);
        }
        return termVector;
    }
    
    
    protected void calculateTermFrequency(String field) throws IOException
    {
        if(termsFrequency == null)
        {
            termsCount = 0;
            
            termsFrequency = new HashMap<>();
            TermsEnum itr = getTermVector(field).iterator();
            BytesRef term;
            while ((term = itr.next()) != null)
            {
                String termText = term.utf8ToString();
                long tf = itr.totalTermFreq();
                termsFrequency.put(termText, tf);
                termsCount += itr.totalTermFreq();
            }
        }
    }
    
    public HashMap<String,Long> getTermFrequency(String field) throws IOException
    {
        calculateTermFrequency(field);//only works if TF is null
        return termsFrequency;
    }

    public int getDocId()
    {
        return docId;
    }


    public void setDoc(int docId)
    {
        this.docId = docId;
        termVector = null;
        termsFrequency = null;
        termsCount = -1;
    }

}
