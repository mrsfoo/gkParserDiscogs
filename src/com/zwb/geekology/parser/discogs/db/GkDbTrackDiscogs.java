package com.zwb.geekology.parser.discogs.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.discogs.model.Track;

import com.zwb.geekology.parser.api.db.IGkDbArtist;
import com.zwb.geekology.parser.api.db.IGkDbRelease;
import com.zwb.geekology.parser.api.db.IGkDbTag;
import com.zwb.geekology.parser.api.db.IGkDbTrack;
import com.zwb.geekology.parser.api.parser.GkParserObjectFactory;
import com.zwb.geekology.parser.discogs.util.Config;
import com.zwb.geekology.parser.discogs.util.StringUtilsDiscogs;
import com.zwb.lazyload.ILoader;
import com.zwb.lazyload.LazyLoader;
import com.zwb.lazyload.Ptr;
import com.zwb.stringutil.ISatiniseFilterArray;

public class GkDbTrackDiscogs extends AbstrGkDbItemDiscogs implements IGkDbTrack
{
    private Track track;
    private Integer trackNo;
    private GkDbArtistDiscogs artist;
    private GkDbReleaseDiscogs release;
    private Ptr<Integer> absPos = new Ptr<Integer>();
    
    public GkDbTrackDiscogs(Track track, GkDbArtistDiscogs artist, GkDbReleaseDiscogs release)
    {
	super(track.getTitle(), GkParserObjectFactory.createSource(Config.getSourceString()));
	this.track = track;
	this.artist = artist;
	this.release = release;
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
	return new ArrayList<IGkDbTag>();
    }
    
    @Override
    public List<String> getStyleTagNames()
    {
	return new ArrayList<String>();
    }
    
    @Override
    public Integer getTrackNo()
    {
	if (this.trackNo == null)
	{
	    this.trackNo = this.track.getNumber();
	}
	return this.trackNo;
    }
    
    @Override
    public Integer getDiscNo()
    {
	return this.track.getDisc();
    }
    
    public void setTrackNo(int trackNo)
    {
	this.trackNo = trackNo;
    }
    
    @Override
    public IGkDbRelease getRelease()
    {
	return this.release;
    }
    
    @Override
    public IGkDbArtist getArtist()
    {
	return this.artist;
    }
    
    @Override
    public Long getDuration()
    {
	return this.track.getDurationInMillis();
    }
    
    @Override
    public boolean hasDuration()
    {
	return this.getDuration() > 0;
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
    public int compareTo(IGkDbTrack o)
    {
	if (this.getAbsolutePosition() > o.getAbsolutePosition())
	{
	    return 1;
	}
	else if (this.getAbsolutePosition() < o.getAbsolutePosition())
	{
	    return -1;
	}
	else
	{
	    return this.getName().compareTo(o.getName());
	}
    }
    
    @Override
    public Integer getAbsolutePosition()
    {
	return LazyLoader.loadLazy(this.absPos, new AbsolutePositionLoader());
    }
    
    class AbsolutePositionLoader implements ILoader<Integer>
    {
	@Override
	public Integer load()
	{
	    Map<Integer, Integer> trackNos = new HashMap<Integer, Integer>();
	    IGkDbRelease release = GkDbTrackDiscogs.this.getRelease();
	    for (IGkDbTrack t : release.getTracks())
	    {
		Integer discNo = t.getDiscNo();
		if (!trackNos.containsKey(discNo))
		{
		    trackNos.put(discNo, 0);
		}
		trackNos.put(discNo, trackNos.get(discNo) + 1);
	    }
	    
	    int abs = 0;
	    for (int i = 1; i < GkDbTrackDiscogs.this.getDiscNo(); i++)
	    {
		abs += trackNos.get(i);
	    }
	    abs += GkDbTrackDiscogs.this.getTrackNo();
	    return abs;
	}
    }

    @Override
    public ISatiniseFilterArray getFilters()
    {
	return StringUtilsDiscogs.getAllTrackNameFilters();
    }

}
