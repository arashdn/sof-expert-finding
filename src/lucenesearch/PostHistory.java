/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucenesearch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author arashdn
 */
public class PostHistory
{
    private int id;
    private int PostHistoryTypeId; 
    private int PostId;
    private int UserId;
    private Date creationDate;
    private String Text;
    
    // <editor-fold desc="Setters and getters" defaultstate="collapsed">

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getPostHistoryTypeId()
    {
        return PostHistoryTypeId;
    }

    public void setPostHistoryTypeId(int PostHistoryTypeId)
    {
        this.PostHistoryTypeId = PostHistoryTypeId;
    }

    public int getPostId()
    {
        return PostId;
    }

    public void setPostId(int PostId)
    {
        this.PostId = PostId;
    }

    public int getUserId()
    {
        return UserId;
    }

    public void setUserId(int UserId)
    {
        this.UserId = UserId;
    }

    public String getText()
    {
        return Text;
    }

    public void setText(String Text)
    {
        this.Text = Text;
    }
    

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date CreationDate)
    {
        this.creationDate = CreationDate;
    }

    public void setCreationDate(String CreationDate) throws ParseException
    {
        String d = CreationDate.replaceAll("T", " ");
        final String NEW_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        this.setCreationDate(dateFormat.parse(d));
    }
    
// </editor-fold>

    public PostHistory(NamedNodeMap nnm) throws ParseException
    {
        if (nnm.getNamedItem("Id") != null)
        {
            this.setId(Integer.parseInt(nnm.getNamedItem("Id").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("PostHistoryTypeId") != null)
        {
            this.setPostHistoryTypeId(Integer.parseInt(nnm.getNamedItem("PostHistoryTypeId").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("PostId") != null)
        {
            this.setPostId(Integer.parseInt(nnm.getNamedItem("PostId").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("UserId") != null)
        {
            this.setUserId(Integer.parseInt(nnm.getNamedItem("UserId").getFirstChild().getTextContent()));
        }
        if (nnm.getNamedItem("CreationDate") != null)
        {
            this.setCreationDate(nnm.getNamedItem("CreationDate").getFirstChild().getTextContent());
        }
        if (nnm.getNamedItem("Text") != null)
        {
            this.setText(nnm.getNamedItem("Text").getFirstChild().getTextContent());
        }
        
    }
    
    public PostHistory(org.apache.lucene.document.Document doc) 
    {
        if(doc.get("SId") != null)
            this.setId(Integer.parseInt(doc.get("SId")));
        
        if (doc.get("SPostHistoryTypeId") != null)
        {
            this.setPostHistoryTypeId(Integer.parseInt(doc.get("SPostHistoryTypeId")));
        }
        if (doc.get("SPostId") != null)
        {
            this.setPostId(Integer.parseInt(doc.get("SPostId")));
        }
        if (doc.get("SUserId") != null)
        {
            this.setUserId(Integer.parseInt(doc.get("SUserId")));
        }
        if (doc.get("SCreationDate") != null)
        {
            this.setCreationDate(new Date(Long.parseLong(doc.get("SCreationDate"))));
        }
        
        if (doc.get("Text") != null)
        {
            this.setText(doc.get("Text"));
        }
        

    }

    public PostHistory()
    {
        
    }

    @Override
    public String toString()
    {
        return "PostHistory{" + "id=" + id + 
                ", PostHistoryTypeId=" + PostHistoryTypeId + 
                ", PostId=" + PostId + 
                ", UserId=" + UserId + 
                ", creationDate=" + creationDate + 
                ", Text=" + Text + '}';
    }
    
    
}
