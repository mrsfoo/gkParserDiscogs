package com.zwb.geekology.parser.discogs.junit;

import junit.framework.TestCase;

public class RegexTest extends TestCase
{
    public void testRegEx()
    {
	String s = "	<script>\r\n"
		+ "		window.__start = new Date().getTime();\r\n"
		+ "	</script>\r\n";
	
	s = s.replaceAll("(?s)<script.*?</script>", "<!-- removed script block -->");
//	s = s.replaceAll("<script.*?>", "<!-- removed script line -->");
	
	System.out.println(s);
    }
}
