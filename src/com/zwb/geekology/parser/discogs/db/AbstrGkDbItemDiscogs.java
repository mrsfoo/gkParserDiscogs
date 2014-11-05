package com.zwb.geekology.parser.discogs.db;

import java.util.List;

import com.zwb.geekology.parser.abstr.db.AbstrGkDbItem;
import com.zwb.geekology.parser.api.db.IGkDbItem;
import com.zwb.geekology.parser.api.parser.IGkParsingSource;
import com.zwb.stringutil.ComparisonAlgorithm;
import com.zwb.stringutil.ISatiniseFilter;

public abstract class AbstrGkDbItemDiscogs extends AbstrGkDbItem
{
    public AbstrGkDbItemDiscogs(String name, IGkParsingSource source)
    {
	super(name, source);
    }
}
