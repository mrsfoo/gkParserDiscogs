package com.zwb.geekology.parser.discogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.discogs.exception.DiscogsException;
import org.discogs.model.Artist;
import org.discogs.model.ArtistRelease;
import org.discogs.model.DiscogsObjectRelease;
import org.discogs.model.Release;

import com.zwb.discogs.util.MyLogger;
import com.zwb.discogs.util.MyLogger.LogLevel;
import com.zwb.geekology.parser.abstr.db.AbstrGkParser;
import com.zwb.geekology.parser.api.db.IGkDbArtist;
import com.zwb.geekology.parser.api.db.IGkDbRelease;
import com.zwb.geekology.parser.api.exception.GkParserException;
import com.zwb.geekology.parser.api.parser.IGkParser;
import com.zwb.geekology.parser.api.parser.IGkParserQuery;
import com.zwb.geekology.parser.api.parser.IGkParsingResultArtist;
import com.zwb.geekology.parser.api.parser.IGkParsingResultSampler;
import com.zwb.geekology.parser.discogs.db.GkDbArtistDiscogs;
import com.zwb.geekology.parser.discogs.util.Config;
import com.zwb.geekology.parser.discogs.util.DiscogsHelper;
import com.zwb.geekology.parser.discogs.util.StringUtilsDiscogs;
import com.zwb.geekology.parser.enums.GkParsingEventType;
import com.zwb.geekology.parser.enums.GkParsingState;
import com.zwb.geekology.parser.impl.GkParsingResultArtist;
import com.zwb.geekology.parser.impl.GkParsingResultSampler;

public class GkParserDiscogs extends AbstrGkParser implements IGkParser
{
    
    private MyLogger log = new MyLogger(this.getClass());
    private DiscogsHelper discogs;
    
    public GkParserDiscogs()
    {
	super();
	log.debug("creating discogs parser");
	this.setSource(Config.getSourceString());
	try
	{
	    this.discogs = new DiscogsHelper();
	}
	catch (DiscogsException e)
	{
	    setConstructorEvent(GkParsingEventType.EXTERNAL_ERROR, "exception in last.fm framework; probably bad internet connection: " + e.getClass().getName() + " -- " + e.getMessage());
	}
    }
    
    @Override
    public IGkParsingResultArtist parseArtist(IGkParserQuery query) throws GkParserException
    {
	GkParsingResultArtist result = (GkParsingResultArtist) setResultStart(query, getSource());
	IGkDbArtist artist = null;
	try
	{
	    if (query.isSampler())
	    {
		// query for sampler
		log.debug("query for sampler " + query.getRelease());
		result.addEvent(GkParsingEventType.ERROR_ARGUMENT, "query for sampler with artist empty");
		result.setState(GkParsingState.ERROR);
		setResultErrorThrow(result, null);
	    }
	    if (query.hasRelease())
	    {
		log.debug("query for artist <" + query.getArtist() + "> with release <" + query.getRelease() + ">");
		artist = this.queryArtistViaRelease(query.getArtist(), query.getRelease(), result);
	    }
	    else
	    {
		log.debug("query for artist <" + query.getArtist() + ">");
		artist = this.queryArtist(query.getArtist(), result);
	    }
	    result.setArtist(artist);
	    return (IGkParsingResultArtist) setResultSuccess(result);
	}
	catch (DiscogsException e)
	{
	    result.addEvent(GkParsingEventType.EXTERNAL_ERROR, "exception in discogs framework: " + e.getClass().getName() + " -- " + e.getMessage());
	    this.setResultErrorThrow(result, e);
	}
	return null;
    }
    
    @Override
    public IGkParsingResultSampler parseSampler(IGkParserQuery query) throws GkParserException
    {
	GkParsingResultSampler result = (GkParsingResultSampler) setResultStart(query, getSource());
	
	if (query.isSampler())
	{
	    // query for sampler
	    // TODO
	    log.debug("query for sampler " + query.getRelease());
	    throw new RuntimeException("NOT IMPLEMENTED YET!");
	}
	else
	{
	    log.debug("query for artist <" + query.getArtist() + ">");
	    // TODO ERROR schmeissen!
	    throw new RuntimeException("NOT IMPLEMENTED YET!");
	}
    }
    
    private IGkDbArtist queryArtist(String artistName, GkParsingResultArtist result) throws GkParserException, DiscogsException
    {
	log.debug("QUERY: query artist <" + artistName + ">");
	Collection<Artist> artists = queryDiscogsArtists(artistName);
	if (!artists.isEmpty())
	{
	    result.addEvent(GkParsingEventType.ENTRY_FOUND, "query for artist <" + artistName + "> returned <" + artists.size() + "> matches");
	    Artist chosen = findBestMatchingArtist(artistName, artists);
	    log.debug("QUERY: queried artist <" + artistName + ">: " + chosen);
	    return new GkDbArtistDiscogs(chosen);
	}
	log.debug("QUERY: no result for artist query <" + artistName + ">");
	result.addEvent(GkParsingEventType.NO_ENTRY_FOUND, "query for artist <" + artistName + "> returned <" + artists.size() + "> matches");
	setResultErrorThrow(result, null);
	/** won't be reached */
	return null;
    }
    
    private IGkDbArtist queryArtistViaRelease(String artistName, String releaseName, GkParsingResultArtist result) throws GkParserException, DiscogsException
    {
	log.debug("QUERY: query artist <" + artistName + "> via release <" + releaseName + ">");
	Artist artist = this.queryDiscogsArtistWithReleases(artistName, releaseName);
	if (artist == null)
	{
	    log.debug("QUERY: no result for artist query <" + artistName + "> with release <" + releaseName + ">, trying query with via release another way");
	    artist = queryDiscogsArtistViaReleases(artistName, releaseName);
	}
	if (artist == null)
	{
	    result.addEvent(GkParsingEventType.NO_ENTRY_FOUND, "release <" + releaseName + "> is NOT available for artist <" + artistName + ">");
	    log.debug("QUERY: no result for artist query <" + artistName + "> via release <" + releaseName + ">, trying query without release");
	    return this.queryArtist(artistName, result);
	}
	log.debug("QUERY: queried artist <" + artistName + "> via release <" + releaseName + ">: " + artist);
	result.addEvent(GkParsingEventType.ENTRY_FOUND, "release <" + releaseName + "> is available for artist <" + artistName + ">");
	return new GkDbArtistDiscogs(artist);
    }
    
    private IGkDbRelease querySampler(String samplerName)
    {
	// TODO
	throw new RuntimeException("NOT IMPLEMENTED YET!");
    }
    
    private Collection<Artist> queryDiscogsArtists(String artistName) throws DiscogsException
    {
	log.debug("QUERY-DISCOGS: query discogs artist <" + artistName + ">");
	Collection<Artist> artists = this.discogs.searchArtist(artistName, false);
	logArtistList(LogLevel.DEBUG, artists, "QUERY-DISCOGS: query for artist <" + artistName + "> returned:", "QUERY-DISCOGS: ");
	return artists;
    }
    
    private Artist queryDiscogsArtistViaReleases(String artistName, String releaseName) throws DiscogsException
    {
	log.debug("QUERY-DISCOGS: query discogs artist <" + artistName + "> via release <" + releaseName + ">");
	Collection<Release> albums = null;
	try
	{
	    albums = this.discogs.searchRelease(releaseName, false);
	}
	catch (DiscogsException e)
	{
	    log.error("QUERY-DISCOGS: caught discogs exception <" + e.getClass().getName() + "> searching for artist <" + artistName + "> via release <" + releaseName + ">");
	}
	if ((albums == null) || (albums.size() == 0))
	{
	    log.debug("QUERY-DISCOGS: query for discogs artist <" + artistName + "> via release <" + releaseName + "> returned no result");
	    return null;
	}
	logReleaseList(LogLevel.DEBUG, albums, "QUERY-DISCOGS: query for artist <" + artistName + "> via release <" + releaseName + "> returned:", "QUERY-DISCOGS: ");
	Artist a = findBestMatchingAlbumArtist(artistName, releaseName, albums);
	log.debug("QUERY-DISCOGS: found best matching artist for query for artist <" + artistName + "> via release <" + releaseName + ">: " + a);
	return a;
    }
    
    private Artist queryDiscogsArtistWithReleases(String artistName, String releaseName) throws DiscogsException
    {
	log.debug("QUERY-DISCOGS: query discogs artist <" + artistName + "> with release <" + releaseName + ">");
	Collection<Artist> artists = this.discogs.searchArtist(artistName, false);
	logArtistList(LogLevel.DEBUG, artists, "QUERY-DISCOGS: query for artist <" + artistName + "> with release <" + releaseName + "> returned:", "QUERY-DISCOGS: ");
	Artist a = findBestMatchingArtistsWithRelease(artistName, releaseName, artists);
	log.debug("QUERY-DISCOGS: found best matching artist for query for artist <" + artistName + "> with release <" + releaseName + ">: " + a);
	return a;
    }
    
    private Artist findBestMatchingArtist(String artistName, Collection<Artist> artists)
    {
	Iterator<Artist> it = artists.iterator();
	int i = 0;
	double thresh = Config.getSearchTreshold();
	log.trace("MATCH: find best matching artist; name=<" + artistName + ">, thresh=<" + thresh + ">, from: <" + artists + ">");
	while (it.hasNext())
	{
	    if (i >= Config.getSearchDepth())
	    {
		log.warn("MATCH: search depth [" + i + "/" + artists.size() + "] reached, break up!");
		break;
	    }
	    Artist me = it.next();
	    String meName = me.getName();
	    double val = StringUtilsDiscogs.compareArtists(meName, artistName);
	    log.trace("MATCH: comparing: <" + artistName + "> with <" + meName + "> --> [" + val + ">=" + thresh + "]?");
	    if (val >= thresh)
	    {
		log.info("MATCH: match <" + artistName + "> ~~ <" + meName + ">!");
		return me;
	    }
	}
	return artists.iterator().next();
    }
    
    private Artist findBestMatchingAlbumArtist(String artistName, String releaseName, Collection<Release> albums) throws DiscogsException
    {
	List<String> matches = new ArrayList<>();
	Artist ret = null;
	Iterator<Release> it = albums.iterator();
	LogLevel level = LogLevel.DEBUG;
	double thresh = Config.getSearchTresholdViaAlbum();
	log.trace("MATCH: find best matching album artist; artist name=<" + artistName + ">, release name=<" + releaseName + ">, thresh=<" + thresh + ">, from: <" + albums + ">");
	int i = 0;
	while (it.hasNext())
	{
	    Release albumLocal = it.next();
	    String artistNameLocal = albumLocal.getArtists().get(0).getName();
	    matches.add(artistNameLocal + "/" + albumLocal.getTitle());
	    if (i >= Config.getSearchDepth())
	    {
		log.warn("MATCH: search depth [" + i + "/" + albums.size() + "] reached, break up!");
		break;
	    }
	    
	    double val = StringUtilsDiscogs.compareArtists(artistNameLocal, artistName);
	    log.trace("MATCH: comparing: <" + artistName + "> with <" + artistNameLocal + "> --> [" + val + ">=" + thresh + "]?");
	    if (val >= thresh)
	    {
		log.info("MATCH: match <" + artistName + "> ~~ <" + artistNameLocal + ">!");
		ret = this.discogs.searchArtist(artistNameLocal, false).iterator().next();
		if (log.isLogLevelEnabled(level))
		{
		    break;
		}
	    }
	}
	log.log(level, "MATCH: query for release <" + releaseName + "> returned <" + albums.size() + "> matches: " + matches);
	return ret;
    }
    
    private Artist findBestMatchingArtistsWithRelease(String artistName, String releaseName, Collection<Artist> artists) throws DiscogsException
    {
	Iterator<Artist> it = artists.iterator();
	double threshRelease = Config.getSearchTresholdViaAlbum();
	double threshArtist = Config.getSearchTreshold();
	int i = 0;
	log.trace("MATCH: find best matching release from artists; artist name=<" + artistName + ">, release name=<" + releaseName + ">, thresh=<" + threshRelease + ">, from: <" + artists + ">");
	while (it.hasNext())
	{
	    if (i >= Config.getSearchDepth())
	    {
		log.warn("MATCH: search depth [" + i + "/" + artists.size() + "] reached, break up!");
		break;
	    }
	    Artist a = it.next();
	    String aName = a.getName();
	    double val = StringUtilsDiscogs.compareArtists(aName, artistName);
	    log.trace("MATCH: comparing: <" + artistName + "> with <" + aName + "> --> [" + val + ">=" + threshArtist + "]?");
	    if (val < threshArtist)
	    {
		log.trace("MATCH: artist name similarity too small; continue!");
		continue;
	    }
	    log.trace("MATCH: getting releases of artist <" + a.getName() + ">");
	    List<ArtistRelease> ars = a.getReleases();
	    logReleaseList(LogLevel.DEBUG, ars, "MATCH: query for artist releases of <" + artistName + "> returned:", "MATCH: ");
	    for (ArtistRelease ar : ars)
	    {
		String title = ar.getTitle();
		val = StringUtilsDiscogs.compareReleases(artistName, title, releaseName);
		log.trace("MATCH: comparing for artist <" + artistName + ">: release name <" + releaseName + "> with <" + title + "> --> [" + val + ">=" + threshRelease + "]?");
		if (val >= threshRelease)
		{
		    log.info("MATCH: match for <" + artistName + ">: <" + releaseName + "> ~~ <" + title + ">!");
		    return a;
		}
	    }
	    i++;
	}
	return null;
    }
    
    private void logArtistList(LogLevel level, Collection<Artist> list, String headline, String prefix)
    {
	if (log.isLogLevelEnabled(level))
	{
	    
	    log.log(level, headline);
	    log.log(level, "(size is [" + list.size() + "])");
	    Iterator<Artist> it = list.iterator();
	    while (it.hasNext())
	    {
		log.log(level, "  " + prefix + it.next().getName());
	    }
	}
    }
    
    private void logReleaseList(LogLevel level, Collection<? extends DiscogsObjectRelease> list, String headline, String prefix)
    {
	if (log.isLogLevelEnabled(level))
	{
	    
	    log.log(level, headline);
	    log.log(level, "(size is [" + list.size() + "])");
	    Iterator<? extends DiscogsObjectRelease> it = list.iterator();
	    while (it.hasNext())
	    {
		log.log(level, "  " + prefix + it.next().getTitle());
	    }
	}
    }
}
