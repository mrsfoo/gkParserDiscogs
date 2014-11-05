package com.zwb.geekology.parser.discogs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.discogs.exception.DiscogsException;
import org.discogs.exception.DiscogsFetchException;
import org.discogs.exception.DiscogsAuthException;
import org.discogs.model.Artist;
import org.discogs.model.Release;
import org.discogs.ws.Discogs;
import org.discogs.ws.search.ArtistSearchResult;
import org.discogs.ws.search.ReleaseSearchResult;
import org.discogs.ws.search.Search;

import com.zwb.discogs.util.MyLogger;

public class DiscogsHelper
{
    private Discogs discogs;
    private MyLogger log = new MyLogger(this.getClass());
    
    public DiscogsHelper() throws DiscogsAuthException
    {
	if (Config.getUseOAuth())
	{
	    log.info("creating discogs parser with oAuth authentication");
	    this.discogs = new Discogs(Config.getApiKey(), Config.getUserAgent(), Config.getConsumerKey(), Config.getConsumerSecret(), Config.getVerificationCode());
	}
	else
	{
	    log.info("creating discogs parser without using oAuth authentication");
	    this.discogs = new Discogs(Config.getApiKey());
	}
    }
    
    public Collection<Artist> searchArtist(String artistName, boolean catchExceptions) throws DiscogsException
    {
	try
	{
	    List<ArtistSearchResult> searchResults = this.discogs.search(artistName).getArtistSearchResults();
	    List<Artist> result = new ArrayList<>();
	    for (ArtistSearchResult a : searchResults)
	    {
		result.add(a.getArtist());
	    }
	    return result;
	}
	catch (DiscogsException e)
	{
	    if (catchExceptions)
	    {
		return Collections.emptyList();
	    }
	    else
	    {
		throw e;
	    }
	}
    }
    
    public Collection<Release> searchRelease(String releaseName, boolean catchExceptions) throws DiscogsException
    {
	try
	{
	    List<ReleaseSearchResult> searchResults = this.discogs.search(releaseName).getReleaseSearchResult();
	    List<Release> result = new ArrayList<>();
	    for (ReleaseSearchResult a : searchResults)
	    {
		result.add(a.getRelease());
	    }
	    return result;
	}
	catch (DiscogsException e)
	{
	    if (catchExceptions)
	    {
		return Collections.emptyList();
	    }
	    else
	    {
		throw e;
	    }
	}
    }
    
    // public Collection<Release> searchAlbumsForArtist(String artistName,
    // boolean catchExceptions)
    // {
    // // try
    // // {
    // // List<ArtistSearchResult> searchResults =
    // this.discogs.search(artistName).getArtistSearchResults();
    // // List<Artist> result = new ArrayList<>();
    // // for(ArtistSearchResult a: searchResults)
    // // {
    // // result.add(a.getArtist());
    // // }
    // // return result;
    // // }
    // // catch (FetchException e)
    // // {
    // // if (catchExceptions)
    // // {
    // // return Collections.emptyList();
    // // }
    // // else
    // // {
    // // throw e;
    // // }
    // // }
    // }
}
