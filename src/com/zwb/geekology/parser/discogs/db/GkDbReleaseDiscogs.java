package com.zwb.geekology.parser.discogs.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.discogs.model.Format;
import org.discogs.model.Release;
import org.discogs.model.Track;

import com.zwb.discogs.util.MyLogger;
import com.zwb.geekology.parser.api.db.IGkDbArtist;
import com.zwb.geekology.parser.api.db.IGkDbRelease;
import com.zwb.geekology.parser.api.db.IGkDbTag;
import com.zwb.geekology.parser.api.db.IGkDbTrack;
import com.zwb.geekology.parser.api.parser.GkParserObjectFactory;
import com.zwb.geekology.parser.discogs.util.Config;
import com.zwb.geekology.parser.impl.NameLoader;
import com.zwb.lazyload.ILoader;
import com.zwb.lazyload.LazyLoader;
import com.zwb.lazyload.Ptr;

public class GkDbReleaseDiscogs extends AbstrGkDbItemDiscogs implements IGkDbRelease
{
    private com.zwb.discogs.util.MyLogger log = new MyLogger(this.getClass());

    private Release release;
    private GkDbArtistDiscogs artist;
    private Ptr<List<IGkDbTag>> tags = new Ptr<>();
    private Ptr<List<String>> tagNames = new Ptr<List<String>>();
    private Ptr<List<IGkDbTrack>> tracks = new Ptr<>();
    private Ptr<List<String>> trackNames = new Ptr<>();
    private Ptr<List<String>> formats = new Ptr<List<String>>();
    private Ptr<List<String>> labels = new Ptr<List<String>>();
    
    public GkDbReleaseDiscogs(Release release, GkDbArtistDiscogs artist)
    {
	super(release.getTitle(), GkParserObjectFactory.createSource(Config.getSourceString()));
	this.release = release;
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
	return "";
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
    public boolean isSampler()
    {
	// TODO
	throw new RuntimeException("NOT IMPLEMENTED YET!");
    }
    
    @Override
    public IGkDbArtist getArtist()
    {
	return this.artist;
    }
    
    @Override
    public List<IGkDbTrack> getTracks()
    {
	return LazyLoader.loadLazy(this.tracks, new TrackLoader());
    }
    
    @Override
    public List<String> getTrackNames()
    {
	return LazyLoader.loadLazy(this.trackNames, new NameLoader(this.getTracks()));
    }
    
    @Override
    public Date getReleaseDate()
    {
	return this.release.getReleaseDate();
    }
    
    @Override
    public boolean hasReleaseDate()
    {
	return (this.getReleaseDate() != null);
    }
    
    @Override
    public boolean hasDescriptionSummary()
    {
	return false;
    }
    
    @Override
    public boolean hasDescription()
    {
	return false;
    }
    
    @Override
    public Integer getTrackCount()
    {
	return this.getTracks().size();
    }
    
    @Override
    public Integer getDiscCount()
    {
	List<IGkDbTrack> tracks = this.getTracks();
	int discs = 0;
	for (IGkDbTrack t : tracks)
	{
	    discs = Math.max(discs, t.getDiscNo());
	}
	return discs;
    }
    
    @Override
    public List<String> getFormats()
    {
	return LazyLoader.loadLazy(this.formats, new FormatLoader());
    }

    @Override
    public boolean hasFormats()
    {
	return this.getFormats()!=null && !this.getFormats().isEmpty();
    }

    @Override
    public List<String> getLabels()
    {
	return LazyLoader.loadLazy(this.labels, new LabelLoader());
    }

    @Override
    public boolean hasLabels()
    {
	return this.getLabels()!=null && !this.getLabels().isEmpty();
    }

    class TagLoader implements ILoader
    {
	public List<IGkDbTag> load()
	{
	    List<String> genres = release.getGenres();
	    List<String> styles = release.getStyles();
	    List<IGkDbTag> tags = new ArrayList<IGkDbTag>();
	    for (String s : genres)
	    {
		tags.add(new GkDbTagDiscogs(s, true));
	    }
	    for (String s : styles)
	    {
		tags.add(new GkDbTagDiscogs(s, false));
	    }
	    return tags;
	}
    }
    
    class TrackLoader implements ILoader
    {
	public List<IGkDbTrack> load()
	{
	    List<Track> discogsTracks = GkDbReleaseDiscogs.this.release.getTracks();
	    List<IGkDbTrack> tracks = new ArrayList<IGkDbTrack>();
	    boolean manualTrackNo = false;
	    for (Track t : discogsTracks)
	    {
		if (t.getNumber() == -1)
		{
		    log.debug("no dicogs track number info for release <"+GkDbReleaseDiscogs.this.getName()+">, track <"+t.getTitle()+"> -> track no format <"+t.getPosition()+">; switching to sequence based numbering for the whole release");
		    manualTrackNo = true;
		}
		break;
	    }
	    int i = 1;
	    for (Track t : discogsTracks)
	    {
		if(t.getTrackType().equals("heading"))
		{
		    continue;
		}
		GkDbTrackDiscogs track = new GkDbTrackDiscogs(t, GkDbReleaseDiscogs.this.artist, GkDbReleaseDiscogs.this);
		if (manualTrackNo)
		{
		    track.setTrackNo(i);
		}
		tracks.add(track);
		i++;
	    }
	    return tracks;
	}
    }
    
    class FormatLoader implements ILoader
    {
	public List<String> load()
	{
	    List<Format> discogsFormats = GkDbReleaseDiscogs.this.release.getFormats();
	    List<String> formats = new ArrayList<String>();
	    for(Format f: discogsFormats)
	    {
		formats.add(f.getName()+"/"+f.getDescriptions());
	    }
	    return formats;
	}
    }

    class LabelLoader implements ILoader
    {
	public List<String> load()
	{
	    return GkDbReleaseDiscogs.this.release.getLabelNames();
	}
    }
}
