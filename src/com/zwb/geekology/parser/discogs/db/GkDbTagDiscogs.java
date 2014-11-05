package com.zwb.geekology.parser.discogs.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.zwb.geekology.parser.api.db.IGkDbItem;
import com.zwb.geekology.parser.api.db.IGkDbTag;
import com.zwb.geekology.parser.api.parser.GkParserObjectFactory;
import com.zwb.geekology.parser.discogs.util.Config;
import com.zwb.geekology.parser.discogs.util.StringUtilsDiscogs;
import com.zwb.geekology.parser.impl.util.GkParserStringUtils;
import com.zwb.stringutil.ComparisonAlgorithm;
import com.zwb.stringutil.ISatiniseFilterArray;

public class GkDbTagDiscogs extends AbstrGkDbItemDiscogs implements IGkDbTag
{
    private final double HIGH_WEIGHT = IGkDbTag.defaultWeight*1.25;
    private final double LOW_WEIGHT = IGkDbTag.defaultWeight*.75;
    
    private double weight = LOW_WEIGHT;
    
    public GkDbTagDiscogs(String tag, boolean isHighWeight)
    {
	super(tag, GkParserObjectFactory.createSource(Config.getSourceString()));
	if(isHighWeight)
	{
	    this.weight = HIGH_WEIGHT;
	}
    }

    @Override
    public String getDescriptionSummary()
    {
	return "";
    }

    @Override
    public String getDescription()
    {
	return "";
    }

    @Override
    public Double getWeight()
    {
	return this.weight;
    }

    @Override
    public List<IGkDbTag> getSimilar()
    {
	return new ArrayList<IGkDbTag>();
    }

    @Override
    public List<String> getSimilarsNames()
    {
	return new ArrayList<String>();
    }

    @Override
    public boolean hasDescriptionSummary()
    {
	return false;
    }

    @Override
    public boolean hasDescription()
    {
	return false;
    }

    @Override
    public boolean hasSimilar()
    {
	return false;
    }

    @Override
    public int compareTo(IGkDbTag o)
    {
	if(this.getWeight()>o.getWeight())
	{
	    return -1;
	}
	else if(this.getWeight()<o.getWeight())
	{
	    return 1;
	}
	else
	{
	    return this.getName().compareTo(o.getName());
	}
    }

    @Override
    public boolean equals(Object o)
    {
	if(o==null)
	{
	    return false;
	}
	if(!this.getClass().equals(o.getClass()))
	{
	    return false;
	}
	GkDbTagDiscogs cmpTag = (GkDbTagDiscogs)o;
	return this.getName().equals(cmpTag.getName());
    }
    
    @Override
    public int hashCode()
    {
	return this.getName().hashCode();
    }

    @Override
    public ISatiniseFilterArray getFilters()
    {
	return StringUtilsDiscogs.getAllTagNameFilters();
    }

}
