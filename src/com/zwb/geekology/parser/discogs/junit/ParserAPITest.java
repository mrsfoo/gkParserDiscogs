package com.zwb.geekology.parser.discogs.junit;

import junit.framework.TestCase;

import org.discogs.model.Artist;
import org.discogs.ws.Discogs;

public class ParserAPITest extends TestCase
{
	public void testParserAPI()
	{
		// create with your API key or start VM with -DapiKey=[apikey]
		Discogs discogs=new Discogs("NyBvidRBkyHQMArVTOuL");
//		discogs.setUserAgent("discogs-java/0.01 +http://benow.ca/projects/discogs-java +APIKey=NyBvidRBkyHQMArVTOuL");
		discogs.setUserAgent("geekOlogy/0.01");
		try 
		{
			Artist artist;
			artist = discogs.getArtist("Richard H. Kirk");
			System.out.println(artist.getProfile());
			System.out.println(artist);
		} 
		catch (Exception e) 
		{
			Throwable ex = new Throwable(e);
			while(ex!=null)
			{
				System.err.println(ex.getClass()+": "+ex.getMessage());				
				ex = ex.getCause();
			}
			e.printStackTrace();
		}
	}

}
