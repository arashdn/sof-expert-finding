package lucenesearch;

import java.util.ArrayList;

/**
 *
 * @author arashdn
 */
public class Evaluator
{
    
    
    protected boolean expertExist(ArrayList<Integer> set, Integer e) 
    {
        for (int golden : set) {
            if(golden == e)
                return true;
        }
        return false;
    }
    
    public double map(ArrayList<Integer> a , ArrayList<Integer> golden)
    {
        int countRel = 0;
        double sum = 0;
        for (int i = 0; i < a.size(); i++) 
        {
            if (expertExist(golden, a.get(i))) 
            {
                countRel++;
                sum += ( (double) countRel/ (i+1)  );//i should be from 1
            }

        }
        return sum / golden.size();
    }
    
    public double precisionAtK(ArrayList<Integer> a , ArrayList<Integer> golden , int k)
    {
        int countRel = 0;

        for (int i = 0; i < k; i++) 
        {
            if (expertExist(golden, a.get(i))) 
            {
                countRel++;
            }

        }
        return (double)countRel / k;
    }
}
