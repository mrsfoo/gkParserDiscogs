package com.zwb.geekology.parser.discogs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.discogs.exception.FetchException;
import org.discogs.model.Artist;
import org.discogs.model.Release;
import org.discogs.ws.Discogs;
import org.discogs.ws.search.ArtistSearchResult;
import org.discogs.ws.search.ReleaseSearchResult;
import org.discogs.ws.search.Search;

public class DiscogsHelper
{
    private Discogs discogs;
    
    public DiscogsHelper()
    {
	this.discogs = new Discogs(Config.getApiKey());
    }
    
    public Collection<Artist> searchArtist(String artistName, boolean catchExceptions) throws FetchException
    {
	try
	{
	    List<ArtistSearchResult> searchResults = this.discogs.search(artistName).getArtistSearchResults();
	    List<Artist> result = new ArrayList<>();
	    for(ArtistSearchResult a: searchResults)
	    {
		result.add(a.getArtist());
	    }
	    return result;
	}
	catch (FetchException e)
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
    
    public Collection<Release> searchRelease(String releaseName, boolean catchExceptions) throws FetchException
    {
	try
	{
	    List<ReleaseSearchResult> searchResults = this.discogs.search(releaseName).getReleaseSearchResult();
	    List<Release> result = new ArrayList<>();
	    for(ReleaseSearchResult a: searchResults)
	    {
		result.add(a.getRelease());
	    }
	    return result;
	}
	catch (FetchException e)
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
    
    //    public Collection<Release> searchAlbumsForArtist(String artistName, boolean catchExceptions)
//    {
////	try
////	{
////	    List<ArtistSearchResult> searchResults = this.discogs.search(artistName).getArtistSearchResults();
////	    List<Artist> result = new ArrayList<>();
////	    for(ArtistSearchResult a: searchResults)
////	    {
////		result.add(a.getArtist());
////	    }
////	    return result;
////	}
////	catch (FetchException e)
////	{
////	    if (catchExceptions)
////	    {
////		return Collections.emptyList();
////	    }
////	    else
////	    {
////		throw e;
////	    }
////	}
//    }
}
