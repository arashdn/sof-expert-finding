package lucenesearch;

/**
 *
 * @author arashdn
 */
class EvalResult implements Comparable<EvalResult>
{
    private String tag;
    private double map;
    private double p1;
    private double p5;
    private double p10;

    public EvalResult()
    {
    }
    
    

    public EvalResult(String tag, double map, double p1, double p5, double p10)
    {
        this.setTag(tag);
        this.setMap(map);
        this.setP1(p1);
        this.setP5(p5);
        this.setP10(p10);
    }
    
    
    

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }
    
    public double getMap()
    {
        return map;
    }

    public void setMap(double map)
    {
        this.map = map;
    }

    public double getP1()
    {
        return p1;
    }

    public void setP1(double p1)
    {
        this.p1 = p1;
    }

    public double getP5()
    {
        return p5;
    }

    public void setP5(double p5)
    {
        this.p5 = p5;
    }

    public double getP10()
    {
        return p10;
    }

    public void setP10(double p10)
    {
        this.p10 = p10;
    }

    @Override
    public String toString()
    {
        return "EvalResult{" + "tag=" + tag + ", map=" + map + ", p1=" + p1 + ", p10=" + p10 + '}';
    }

    @Override
    public int compareTo(EvalResult o)
    {
        return Double.compare(o.getMap(), getMap());
    }
    
    
}
