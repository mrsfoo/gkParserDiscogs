package com.zwb.geekology.parser.discogs.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.discogs.exception.FetchException;
import org.discogs.model.Artist;

import junit.framework.TestCase;

import com.zwb.geekology.parser.api.db.IGkDbArtist;
import com.zwb.geekology.parser.api.db.PrintDetailLevel;
import com.zwb.geekology.parser.api.exception.GkParserException;
import com.zwb.geekology.parser.api.exception.GkParserExceptionExternalError;
import com.zwb.geekology.parser.api.exception.GkParserExceptionIllegalArgument;
import com.zwb.geekology.parser.api.exception.GkParserExceptionNoResultFound;
import com.zwb.geekology.parser.api.parser.GkParserObjectFactory;
import com.zwb.geekology.parser.api.parser.IGkParsingResult;
import com.zwb.geekology.parser.api.parser.IGkParsingResultArtist;
import com.zwb.geekology.parser.discogs.GkParserDiscogs;
import com.zwb.geekology.parser.discogs.util.Config;
import com.zwb.geekology.parser.discogs.util.DiscogsHelper;
import com.zwb.geekology.parser.impl.DbItemFileWriter;
import com.zwb.stringutil.StringReformat;
import com.zwb.tab.Tab;

public class TestSandboxDiscogs extends TestCase
{
    public void testQuery() throws GkParserException
    {
	GkParserDiscogs parser = new GkParserDiscogs();
	
	Map<String, String> input = new HashMap<>();
	input.put("motorhead", "nr n,k43t wfne");
	input.put("scott walker", "scott 2");
	input.put("walker brothers", "images");
	input.put("trümmer", "schutt und asche");
	input.put("julgast", "feel like dreaming ep");
	input.put("ja, panik", "the angst and the money");
	input.put("marr435ko fürstenberg", "gesamtlaufzeit");
	input.put("biodub", "goldkaefer");
	input.put("noetics", "rotterdub ep");
	input.put("nr n,k 3455 43t wfne", "4545 45rtg tdgs 43tgr");
	input.put("", "scott 2");
	input.put("metallica", "");
	input.put("4545 45 435 tg tdgs 43tgr", "scott 2");
	
	Map<String, IGkParsingResultArtist> resultsArtistQuery = new HashMap<>();
	Map<String, IGkParsingResultArtist> resultsReleaseQuery = new HashMap<>();
	List<IGkParsingResult> resultsListArtistQuery = new ArrayList<>();
	List<IGkParsingResult> resultsListReleaseQuery = new ArrayList<>();
	for (Entry<String, String> e : input.entrySet())
	{
	    System.out.println("parsing for --> " + e.getKey());
	    IGkParsingResultArtist result;
	    try
	    {
		result = parser.parseArtist(GkParserObjectFactory.createQueryForArtist(e.getKey()));
	    }
	    catch (GkParserExceptionNoResultFound ex)
	    {
		result = (IGkParsingResultArtist) ex.getResult();
	    }
	    catch (GkParserExceptionIllegalArgument ex)
	    {
		result = (IGkParsingResultArtist) ex.getResult();
	    }
	    resultsArtistQuery.put(e.getKey(), result);
	    resultsListArtistQuery.add(result);
	}
	for (Entry<String, String> e : input.entrySet())
	{
	    System.out.println("parsing for --> " + e.getKey() + "/" + e.getValue());
	    IGkParsingResultArtist result;
	    try
	    {
		result = parser.parseArtist(GkParserObjectFactory.createQueryForArtist(e.getKey(), e.getValue()));
	    }
	    catch (GkParserExceptionNoResultFound ex)
	    {
		result = (IGkParsingResultArtist) ex.getResult();
	    }
	    catch (GkParserExceptionExternalError | GkParserExceptionIllegalArgument ex)
	    {
		result = (IGkParsingResultArtist) ex.getResult();
	    }
	    resultsReleaseQuery.put(e.getKey(), result);
	    resultsListReleaseQuery.add(result);
	    if (result != null && result.getArtist() != null) result.getArtist().getStyleTags();
	}
	
	Tab tab = new Tab("result table for [" + input.size() + "] queries", "#", "artist query string", "release query string", "state query solo", "state query with release", "queried artist solo", "queried artist with release", "event list query solo", "event list query with release");
	int i = 0;
	String protocolsA = "\n\nEVENT PROTOCOLS:\n";
	String protocolsB = "\n\nEVENT PROTOCOLS:\n";
	String details = "\n\nDETAILS:";
	for (Entry<String, String> e : input.entrySet())
	{
	    IGkParsingResultArtist resA = resultsArtistQuery.get(e.getKey());
	    IGkParsingResultArtist resB = resultsReleaseQuery.get(e.getKey());
	    String artistNameA = "NULL";
	    String artistNameB = "NULL";
	    if (resA.getArtist() != null)
	    {
		artistNameA = resA.getArtist().getName();
	    }
	    if (resB.getArtist() != null)
	    {
		artistNameB = resB.getArtist().getName();
	    }
	    tab.addRow(Integer.toString(i), e.getKey(), input.get(e.getKey()), resA.getState().toString(), resB.getState().toString(), artistNameA, artistNameB, resA.getEventList(), resB.getEventList());
	    protocolsA += resA.getEventProtocol() + "\n";
	    protocolsB += resB.getEventProtocol() + "\n";
	    
	    IGkDbArtist a = resA.getArtist();
	    if (a != null)
	    {
		details += "-----------------------------------------------\n";
		details += "artist          : " + a.getName() + "\n";
		details += "summary         : " + a.getDescriptionSummary() + "\n";
		details += "description     : " + a.getDescription() + "\n";
		details += "releases        : " + a.getReleaseNames() + "\n";
		details += "similar artists : " + a.getSimilarsNames() + "\n";
		details += "style tags      : " + a.getStyleTagNames() + "\n";
	    }
	    i++;
	}
	details += "-----------------------------------------------\n";
	System.out.println(protocolsA);
	System.out.println(protocolsB);
	
	System.out.println(details);
	
	System.out.println(tab.printFormatted());
	
	DbItemFileWriter.writeResultsToFile("TestResults/resultsArtistQuery.txt", resultsListArtistQuery, PrintDetailLevel.LOW, false);
	DbItemFileWriter.writeResultsToFile("TestResults/resultsReleaseQuery.txt", resultsListReleaseQuery, PrintDetailLevel.LOW, false);
	
    }
    
//    public void testComparisons() throws FetchException
//    {
//	List<String> input = Arrays.asList("scott walker", "walker brothers", "trümmer");
//	int dep = 8;
//	
//	Map<String, List<String>> map = new HashMap<>();
//	for (String s : input)
//	{
//	    map.put(s, new ArrayList<String>());
//	    Collection<Artist> c = new DiscogsHelper().searchArtist(s, false);
//	    Iterator<Artist> it = c.iterator();
//	    for (int i = 0; i < Math.min(dep, c.size()); i++)
//	    {
//		map.get(s).add(it.next().getName());
//	    }
//	}
//	
//	for (Entry<String, List<String>> e : map.entrySet())
//	{
//	    Tab tab = new Tab("ComparisonResults for <" + e.getKey() + ">", "Orig SResult", "Compare", "equals", "equals reformat", "equals reformat/remove", "Dice reformat", "Dice reformat/remove", "Levenshtein reformat", "Levenshtein reformat/remove");
//	    for (String s : e.getValue())
//	    {
//		String string = s;
//		String string0 = string.replaceAll("\\([0-9]\\)", "");
//		string0 = string0.replaceAll("\\([0-9][0-9]\\)", "");
//		String string1 = e.getKey();
//		
//		String equals = Boolean.toString(string0.equals(string1));
//		String equalsReformat = Boolean.toString(StringReformat.equals(string0, string1, false));
//		String equalsReformatRemove = Boolean.toString(StringReformat.equals(string0, string1, true));
//		String diceReformat = Double.toString(StringReformat.compareDice(string0, string1, false));
//		String diceReformatRemove = Double.toString(StringReformat.compareDice(string0, string1, true));
//		String levenshteinReformat = Double.toString(StringReformat.compareLevenshtein(string0, string1, false));
//		String levenshteinReformatRemove = Double.toString(StringReformat.compareLevenshtein(string0, string1, true));
//		tab.addRow(string, string0, equals, equalsReformat, equalsReformatRemove, diceReformat, diceReformatRemove, levenshteinReformat, levenshteinReformatRemove);
//	    }
//	    System.out.println("\n\n" + tab.printFormatted());
//	}
//	/**
//	 * Resultat: am besten Dice reformat/remove mit Schwelle 0.8-0.81
//	 */
//    }
//    
}
