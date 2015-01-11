package com.zwb.geekology.parser.discogs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.zwb.geekology.parser.impl.util.GkParserStringUtils;
import com.zwb.stringutil.ComparisonAlgorithm;
import com.zwb.stringutil.FilterArray;
import com.zwb.stringutil.ISatiniseFilter;
import com.zwb.stringutil.ISatiniseFilterArray;
import com.zwb.stringutil.RemovalFilter;
import com.zwb.stringutil.StringReformat;

public class StringUtilsDiscogs
{
    public static ComparisonAlgorithm COMPARISON_ALGORITHM = ComparisonAlgorithm.DICE_COEFFICIENT;
    
    public static double compareArtists(String name0, String name1)
    {
	String string0 = getAllArtistNameFilters().filter(name0, true);
	String string1 = getAllArtistNameFilters().filter(name1, true);
	return StringReformat.compare(string0, string1, COMPARISON_ALGORITHM);
    }
    
    public static double compareReleases(String artistName, String name0, String name1)
    {
	String string0 = getAllReleaseNameFilters(artistName).filter(name0, true);
	String string1 = getAllReleaseNameFilters(artistName).filter(name1, true);
	return StringReformat.compare(string0, string1, COMPARISON_ALGORITHM);
    }
    
    public static ISatiniseFilterArray getSpecificArtistNameFilters()
    {
	List<ISatiniseFilter> list = new ArrayList<ISatiniseFilter>();
	list.add(new RemovalFilter("\\([0-9]\\)", ISatiniseFilter.Location.ALL));
	list.add(new RemovalFilter("\\([0-9][0-9]\\)", ISatiniseFilter.Location.ALL));
	return new FilterArray(list);
    }
    
    public static ISatiniseFilterArray getSpecificReleaseNameFilters()
    {
	List<ISatiniseFilter> list = new ArrayList<ISatiniseFilter>();
	list.add(new RemovalFilter("\\([0-9]\\)", ISatiniseFilter.Location.ALL));
	list.add(new RemovalFilter("\\([0-9][0-9]\\)", ISatiniseFilter.Location.ALL));
	return new FilterArray(list);
    }

    public static ISatiniseFilterArray getSpecificTrackNameFilters()
    {
	return new FilterArray();
    }

    public static ISatiniseFilterArray getSpecificTagNameFilters()
    {
	return new FilterArray();
    }

    public static ISatiniseFilterArray getAllArtistNameFilters()
    {
	return getSpecificArtistNameFilters().add(GkParserStringUtils.getGeneralArtistNameFilters());
    }
    
    public static ISatiniseFilterArray getAllReleaseNameFilters(String artistName)
    {
	return getSpecificReleaseNameFilters().add(GkParserStringUtils.getGeneralReleaseNameFilters(artistName));
    }
    
    public static ISatiniseFilterArray getAllTrackNameFilters()
    {
	return getSpecificTrackNameFilters().add(GkParserStringUtils.getGeneralTrackNameFilters());
    }

    public static ISatiniseFilterArray getAllTagNameFilters()
    {
	return getSpecificTagNameFilters().add(GkParserStringUtils.getGeneralTagNameFilters());
    }

}
