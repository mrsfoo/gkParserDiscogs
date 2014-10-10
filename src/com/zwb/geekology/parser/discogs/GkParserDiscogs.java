package com.zwb.geekology.parser.discogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.discogs.exception.FetchException;
import org.discogs.model.Artist;
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
	this.discogs = new DiscogsHelper();
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
	catch (FetchException e)
	{
	    result.addEvent(GkParsingEventType.EXTERNAL_ERROR, "exception in last.fm framework; probably bad internet connection: " + e.getClass().getName() + " -- " + e.getMessage());
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
    
    private IGkDbArtist queryArtist(String artistName, GkParsingResultArtist result) throws GkParserException, FetchException
    {
	Collection<Artist> artists = queryDiscogsArtists(artistName);
	if (!artists.isEmpty())
	{
	    result.addEvent(GkParsingEventType.ENTRY_FOUND, "query for artist <" + artistName + "> returned <" + artists.size() + "> matches");
	    Artist chosen = findBestMatchingArtist(artistName, artists);
	    return new GkDbArtistDiscogs(chosen);
	}
	result.addEvent(GkParsingEventType.NO_ENTRY_FOUND, "query for artist <" + artistName + "> returned <" + artists.size() + "> matches");
	setResultErrorThrow(result, null);
	/** won't be reached */
	return null;
    }
    
    private IGkDbArtist queryArtistViaRelease(String artistName, String releaseName, GkParsingResultArtist result) throws GkParserException, FetchException
    {
	Artist artist = queryDiscogsArtistViaReleases(artistName, releaseName);
	if (artist == null)
	{
	    result.addEvent(GkParsingEventType.NO_ENTRY_FOUND, "release <" + releaseName + "> is NOT available for artist <" + artistName + ">");
	    return this.queryArtist(artistName, result);
	}
	result.addEvent(GkParsingEventType.ENTRY_FOUND, "release <" + releaseName + "> is available for artist <" + artistName + ">");
	return new GkDbArtistDiscogs(artist);
    }
    
    private IGkDbRelease querySampler(String samplerName)
    {
	// TODO
	throw new RuntimeException("NOT IMPLEMENTED YET!");
    }
    
    private Collection<Artist> queryDiscogsArtists(String artistName) throws FetchException
    {
	Collection<Artist> artists = this.discogs.searchArtist(artistName, false);
	LogLevel level = LogLevel.DEBUG;
	if (log.isLogLevelEnabled(level))
	{
	    List<String> matches = new ArrayList<>();
	    Iterator<Artist> it = artists.iterator();
	    while (it.hasNext())
	    {
		matches.add(it.next().getName());
	    }
	    log.log(level, "query for artist <" + artistName + "> returned <" + artists.size() + "> matches: " + matches);
	}
	return artists;
    }
    
    private Artist queryDiscogsArtistViaReleases(String artistName, String releaseName) throws FetchException
    {
	Collection<Release> albums = this.discogs.searchRelease(releaseName, false);
	if ((albums == null) || (albums.size() == 0))
	{
	    return null;
	}
	return findBestMatchingAlbumArtist(artistName, releaseName, albums);
    }
    
    private Artist findBestMatchingArtist(String artistName, Collection<Artist> artists)
    {
	Iterator<Artist> it = artists.iterator();
	int i = 0;
	while (it.hasNext())
	{
	    if (i >= Config.getSearchDepth())
	    {
		break;
	    }
	    Artist me = it.next();
	    String meName = me.getName();
	    double thresh = Config.getSearchTreshold();
	    if (StringUtilsDiscogs.compare(meName, artistName) >= thresh)
	    {
		return me;
	    }
	}
	return artists.iterator().next();
    }
    
    private Artist findBestMatchingAlbumArtist(String artistName, String releaseName, Collection<Release> albums) throws FetchException
    {
	List<String> matches = new ArrayList<>();
	Artist ret = null;
	Iterator<Release> it = albums.iterator();
	LogLevel level = LogLevel.DEBUG;
	int i = 0;
	while (it.hasNext())
	{
	    Release albumLocal = it.next();
	    String artistNameLocal = albumLocal.getArtists().get(0).getName();
	    matches.add(artistNameLocal + "/" + albumLocal.getTitle());
	    if (i >= Config.getSearchDepth())
	    {
		break;
	    }
	    
	    double thresh = Config.getSearchTresholdViaAlbum();
	    if (StringUtilsDiscogs.compare(artistNameLocal, artistName) >= thresh)
	    {
		ret = this.discogs.searchArtist(artistNameLocal, false).iterator().next();
		if (log.isLogLevelEnabled(level))
		{
		    break;
		}
	    }
	}
	log.log(level, "query for release <" + releaseName + "> returned <" + albums.size() + "> matches: " + matches);
	return ret;
    }
}
