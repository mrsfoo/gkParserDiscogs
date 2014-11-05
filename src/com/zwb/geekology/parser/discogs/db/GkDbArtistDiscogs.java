package com.zwb.geekology.parser.discogs.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.discogs.exception.DiscogsAuthException;
import org.discogs.exception.DiscogsException;
import org.discogs.exception.DiscogsFetchException;
import org.discogs.model.Artist;
import org.discogs.model.ArtistRelease;
import org.discogs.model.Release;

import com.zwb.discogs.util.MyLogger;
import com.zwb.geekology.parser.api.db.IGkDbArtist;
import com.zwb.geekology.parser.api.db.IGkDbRelease;
import com.zwb.geekology.parser.api.db.IGkDbTag;
import com.zwb.geekology.parser.api.parser.GkParserObjectFactory;
import com.zwb.geekology.parser.discogs.util.Config;
import com.zwb.geekology.parser.discogs.util.StringUtilsDiscogs;
import com.zwb.geekology.parser.enums.GkParsingEventType;
import com.zwb.geekology.parser.impl.util.GkParserStringUtils;
import com.zwb.geekology.parser.impl.util.NameLoader;
import com.zwb.lazyload.ILoader;
import com.zwb.lazyload.LazyLoader;
import com.zwb.lazyload.Ptr;
import com.zwb.stringutil.ISatiniseFilter;
import com.zwb.stringutil.ISatiniseFilterArray;

import exception.internal.AuthRuntimeException;
import exception.internal.FetchRuntimeException;

//TODO
public class GkDbArtistDiscogs extends AbstrGkDbItemDiscogs implements IGkDbArtist
{
    private MyLogger log = new MyLogger(this.getClass());
    
    private Artist artist;
    private Ptr<List<IGkDbRelease>> releases = new Ptr<>();
    private Ptr<List<String>> releaseNames = new Ptr<>();
    private Ptr<List<IGkDbTag>> tags = new Ptr<>();
    private Ptr<List<String>> tagNames = new Ptr<List<String>>();
    
    public GkDbArtistDiscogs(Artist artist)
    {
	super(artist.getName(), GkParserObjectFactory.createSource(Config.getSourceString()));
	this.artist = artist;
    }
    
    @Override
    public String getDescriptionSummary()
    {
	return "";
    }
    
    @Override
    public String getDescription()
    {
	return this.artist.getProfile();
    }
    
    @Override
    public List<IGkDbTag> getStyleTags()
    {
	return LazyLoader.loadLazy(this.tags, new TagLoader());
    }
    
    @Override
    public List<String> getStyleTagNames()
    {
	return LazyLoader.loadLazy(this.tagNames, new NameLoader(this.getStyleTags()));
    }
    
    @Override
    public List<IGkDbRelease> getReleases()
    {
	try
	{
	    return LazyLoader.loadLazy(this.releases, new ReleaseLoader());
	}
	catch (FetchRuntimeException e)
	{
	    this.addEvent(GkParserObjectFactory.createParsingEvent(GkParsingEventType.EXTERNAL_ERROR, "exception in dicogs framework while loading releases of artist <" + this.getName() + ">; probably bad internet connection: " + e.getCause().getClass().getName() + " -- " + e.getCause().getMessage(), this.getSource()));
	    return null;
	}
    }
    
    @Override
    public List<String> getReleaseNames()
    {
	return LazyLoader.loadLazy(this.releaseNames, new NameLoader(this.getReleases()));
    }
    
    @Override
    public List<IGkDbArtist> getSimilar()
    {
	return new ArrayList<IGkDbArtist>();
    }
    
    @Override
    public List<String> getSimilarsNames()
    {
	return new ArrayList<String>();
    }
    
    @Override
    public boolean hasDescriptionSummary()
    {
	return false;
    }
    
    @Override
    public boolean hasDescription()
    {
	String desc = this.getDescription();
	return desc != null && !desc.isEmpty();
    }
    
    @Override
    public boolean hasSimilars()
    {
	return false;
    }
    
    class ReleaseLoader implements ILoader
    {
	@Override
	public List<IGkDbRelease> load()
	{
	    List<ArtistRelease> artistReleases;
	    try
	    {
		artistReleases = GkDbArtistDiscogs.this.artist.getReleases();
		log.info("for artist <" + GkDbArtistDiscogs.this.getName() + "> loaded the following releases: " + artistReleases);
	    }
	    catch (DiscogsFetchException e)
	    {
		throw new FetchRuntimeException(e);
	    }
	    catch (DiscogsAuthException e)
	    {
		throw new AuthRuntimeException(e);
	    }
	    List<IGkDbRelease> releases = new ArrayList<>();
	    for (ArtistRelease r : artistReleases)
	    {
		try
		{
		    // TODO Hier werden nur die Main Releases gelesen und Samplerbeiträge ignoriert; sobald wir Sampler implementiert haben -> aktualisieren!
		    if (r.getRole().equals("Main"))
		    {
			Release rr = r.getRelease();
			log.info("for artist release <" + r.getTitle() + "> loaded release <" + rr.getTitle() + ">");
			log.info("JSON:\n" + r.getString());
			GkDbReleaseDiscogs rel = new GkDbReleaseDiscogs(rr, GkDbArtistDiscogs.this);
			releases.add(rel);
		    }
		    else
		    {
			log.info("for artist release <" + r.getTitle() + "> artist <" + GkDbArtistDiscogs.this.getName() + "> has role <" + r.getRole() + ">!=Main -> sampler result, dismissing!");
		    }
		}
		catch (DiscogsException e)
		{
		    GkDbArtistDiscogs.this.addEvent(GkParserObjectFactory.createParsingEvent(GkParsingEventType.EXTERNAL_ERROR, "exception in dicogs framework while loading release info of release <" + r.getTitle() + "> of artist <" + GkDbArtistDiscogs.this.getName() + ">; probably bad internet connection: " + e.getCause().getClass().getName() + " -- " + e.getCause().getMessage(), GkDbArtistDiscogs.this.getSource()));
		}
	    }
	    return releases;
	}
    }
    
    class TagLoader implements ILoader
    {
	@Override
	public List<IGkDbTag> load()
	{
	    List<IGkDbRelease> releases = GkDbArtistDiscogs.this.getReleases();
	    if (releases == null)
	    {
		return new ArrayList<IGkDbTag>();
	    }
	    Map<GkDbTagDiscogs, Double> tagMap = new HashMap<GkDbTagDiscogs, Double>();
	    log.trace("processing style tags for artist <" + GkDbArtistDiscogs.this.getName() + ">");
	    for (IGkDbRelease r : releases)
	    {
		List<IGkDbTag> releaseTags = r.getStyleTags();
		log.trace("*** artist <" + GkDbArtistDiscogs.this.getName() + "> / release <" + r.getName() + "> / formats <" + r.getFormats() + "> / styles: " + r.getStyleTagNames());
		for (IGkDbTag t : releaseTags)
		{
		    if (tagMap.containsKey(t))
		    {
			tagMap.put((GkDbTagDiscogs) t, tagMap.get(t) + t.getWeight());
		    }
		    else
		    {
			tagMap.put((GkDbTagDiscogs) t, t.getWeight());
		    }
		}
	    }
	    List<IGkDbTag> result = new ArrayList<IGkDbTag>(tagMap.keySet());
	    Collections.sort(result);
	    log.trace("resulting tag map with weights:");
	    for (Entry<GkDbTagDiscogs, Double> e : tagMap.entrySet())
	    {
		log.trace("*** " + e.getKey().getName() + " = " + e.getValue());
	    }
	    log.trace("resulting tag list (sorted!):");
	    for (IGkDbTag e : result)
	    {
		log.trace("*** " + e.getName() + " = " + e.getWeight());
	    }
	    
	    return result;
	}
    }
    
    @Override
    public ISatiniseFilterArray getFilters()
    {
	return StringUtilsDiscogs.getAllArtistNameFilters();
    }
}
