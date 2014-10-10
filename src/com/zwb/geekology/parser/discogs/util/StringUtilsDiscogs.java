package com.zwb.geekology.parser.discogs.util;

import com.zwb.stringutil.StringReformat;

public class StringUtilsDiscogs
{
    public static double compare(String string0, String string1)
    {
	String s0 = reformat(string0);
	String s1 = reformat(string1);
	return StringReformat.compareDice(s0, s1, true);
    }
    
    public static String reformat(String s)
    {
	s = s.replaceAll("([0-9])", "");
	s = s.replaceAll("([0-9][0-9])", "");
	s = s.trim();
	return s;
    }
    
}
