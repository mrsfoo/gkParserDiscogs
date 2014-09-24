package com.zwb.geekology.parser.discogs.junit;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.discogs.exception.FetchException;
import org.discogs.model.Artist;
import org.discogs.model.ArtistRelease;
import org.discogs.model.Format;
import org.discogs.model.Label;
import org.discogs.model.LabelRelease;
import org.discogs.model.Release;
import org.discogs.model.ReleaseArtist;
import org.discogs.model.Track;
import org.discogs.ws.Discogs;
import org.discogs.ws.search.ArtistSearchResult;
import org.discogs.ws.search.LabelSearchResult;
import org.discogs.ws.search.ReleaseSearchResult;
import org.discogs.ws.search.Search;
import org.discogs.ws.search.SearchResult;

import com.zwb.tab.Tab;

public class ParserAPITest extends TestCase
{
    public void testParserAPI()
    {
	// create with your API key or start VM with -DapiKey=[apikey]
	Discogs discogs = new Discogs("NyBvidRBkyHQMArVTOuL");
	try
	{
	    Search s = discogs.search("Richard H. Kirk");
	    List<SearchResult> all = s.getSearchResults();
	    List<Artist> artists = new ArrayList<Artist>();
	    List<Label> labels = new ArrayList<Label>();
	    List<Release> releases = new ArrayList<Release>();
	    List<String> error = new ArrayList<String>();
	    for (SearchResult r : all)
	    {
		System.out.println("+ <" + r.getType() + ">--<" + r.getTitle() + ">");
		if (r.getType().equals("artist"))
		{
		    try
		    {
			Artist a = ((ArtistSearchResult) r).getArtist();
			artists.add(a);
		    }
		    catch (FetchException e)
		    {
			error.add(e.getClass().getName() + " while trying to get artist: type=<" + r.getType() + "> title=<" + r.getTitle() + "> id=<" + r.getId() + ">");
			continue;
		    }
		}
		else if (r.getType().equals("label"))
		{
		    try
		    {
			Label l = ((LabelSearchResult) r).getLabel();
			labels.add(l);
		    }
		    catch (FetchException e)
		    {
			error.add(e.getClass().getName() + " while trying to get label: type=<" + r.getType() + "> title=<" + r.getTitle() + "> id=<" + r.getId() + ">");
			continue;
		    }
		}
		else if (r.getType().equals("release"))
		{
		    try
		    {
			Release a = ((ReleaseSearchResult) r).getRelease();
			releases.add(a);
		    }
		    catch (FetchException e)
		    {
			error.add(e.getClass().getName() + " while trying to get release: type=<" + r.getType() + "> title=<" + r.getTitle() + "> id=<" + r.getId() + ">");
			continue;
		    }
		}
		else
		{
		    error.add("unknown element: type=<" + r.getType() + "> title=<" + r.getTitle() + "> id=<" + r.getId() + ">");
		}
	    }
	    
	    System.out.println("SEARCH RESULTS:");
	    for (SearchResult r : all)
	    {
		System.out.println("+ <" + r.getType() + ">--<" + r.getTitle() + ">");
	    }
	    
	    System.out.println("ERRORS:");
	    for (String r : error)
	    {
		System.out.println("+ <" + r + ">");
	    }
	    
	    Tab artistTab = new Tab("ARTISTS", "", "");
	    addArtists(artistTab, 0, artists);
	    
	    Tab releaseTab = new Tab("RELEASE", "", "");
	    addReleases(releaseTab, 0, releases);
	    
	    Tab labelTab = new Tab("LABEL", "", "");
	    addLabels(labelTab, 0, labels);

	    System.out.println(artistTab.printFormatted());
	    System.out.println(releaseTab.printFormatted());
	    System.out.println(labelTab.printFormatted());
	}
	catch (Exception e)
	{
	    Throwable ex = new Throwable(e);
	    while (ex != null)
	    {
		System.err.println(ex.getClass() + ": " + ex.getMessage());
		ex = ex.getCause();
	    }
	    e.printStackTrace();
	}
    }
    
    private static final String INDENT = "  ";
    private static final String BULLET = "+ ";
    
    private Tab addRow(Tab tab, int dep, String... row)
    {
	List<String> roww = new ArrayList<>();
	String first = new String(row[0]);
	first = BULLET + first;
	for (int i = 0; i < dep; i++)
	{
	    first = INDENT + first;
	}
	roww.add(first);
	for (int i = 1; i < row.length; i++)
	{
	    roww.add(row[i]);
	}
	tab.addRow(roww);
	return tab;
    }
    
    private Tab addArtist(Tab tab, int dep, Artist a) throws FetchException
    {
	tab = addRow(tab, dep, "name", a.getName());
	tab = addRow(tab, dep + 1, "name variations", o2str(a.getNameVariations()));
	tab = addRow(tab, dep + 1, "aliases", o2str(a.getAliases()));
	tab = addRow(tab, dep + 1, "real name", a.getRealName());
	tab = addRow(tab, dep + 1, "groups", o2str(a.getGroups()));
	tab = addRow(tab, dep + 1, "releases", " ");
	tab = addArtistReleases(tab, dep + 2, a.getReleases());
	a.getProfile();
	// tab = addRow(tab, dep + 1, "profile", a.getProfile());
	tab.addSeparator();
	return tab;
    }
    
    private Tab addRelease(Tab tab, int dep, Release r) throws FetchException
    {
	tab = addRow(tab, dep, "title", r.getTitle());
	tab = addRow(tab, dep + 1, "id", r.getId());
	tab = addRow(tab, dep + 1, "status", r.getStatus());
	tab = addRow(tab, dep + 1, "styles", o2str(r.getStyles()));
	tab = addRow(tab, dep + 1, "genres", o2str(r.getGenres()));
	r.getNotes();
//	tab = addRow(tab, dep + 1, "notes", r.getNotes());
	tab = addRow(tab, dep + 1, "country", r.getCountry());
	tab = addRow(tab, dep + 1, "release date", o2str(r.getReleaseDate()));
	tab = addRow(tab, dep + 1, "artists", " ");
	tab = addReleaseArtists(tab, dep + 2, r.getArtists());
	tab = addRow(tab, dep + 1, "extra artists", " ");
	tab = addReleaseArtists(tab, dep + 2, r.getExtraArtists());
	tab = addRow(tab, dep + 1, "tracks", " ");
	tab = addTracks(tab, dep + 2, r.getTracks());
	tab = addRow(tab, dep + 1, "formats", " ");
	tab = addFormats(tab, dep + 2, r.getFormats());
	tab.addSeparator();
	return tab;
    }
    
    private Tab addLabel(Tab tab, int dep, Label l) throws FetchException
    {
	tab = addRow(tab, dep, "name", l.getName());
	tab = addRow(tab, dep + 1, "contact info", l.getContactInfo());
	tab = addRow(tab, dep + 1, "releases", " ");
	tab = addLabelReleases(tab, dep + 2, l.getReleases());
	l.getProfile();
	// tab = addRow(tab, dep + 1, "profile", l.getProfile());
	tab.addSeparator();
	return tab;
    }
    
    private Tab addArtistRelease(Tab tab, int dep, ArtistRelease arl) throws FetchException
    {
	tab = addRow(tab, dep, "title", arl.getTitle());
	tab = addRow(tab, dep + 1, "id", arl.getId());
	tab = addRow(tab, dep + 1, "type", arl.getType());
	tab = addRow(tab, dep + 1, "status", arl.getStatus());
	tab = addRow(tab, dep + 1, "year", Integer.toString(arl.getYear()));
	tab = addRow(tab, dep + 1, "formats", o2str(arl.getFormats()));
	tab = addRow(tab, dep + 1, "format string", arl.getFormatString());
	tab = addRow(tab, dep + 1, "label", o2str(arl.getLabel()));
	tab = addRow(tab, dep + 1, "label name", arl.getLabelName());
	tab = addRow(tab, dep + 1, "labels", o2str(arl.getLabels()));
	tab = addRow(tab, dep + 1, "label names", o2str(arl.getLabelNames()));
	return tab;
    }
    
    private Tab addReleaseArtist(Tab tab, int dep, ReleaseArtist rla) throws FetchException
    {
	tab = addRow(tab, dep, "name", rla.getName());
	tab = addRow(tab, dep +1 , "nested artist", rla.getArtist().getName());
	tab = addRow(tab, dep + 1, "ANV", rla.getANV());
	tab = addRow(tab, dep + 1, "roles", o2str(rla.getRoles()));
	return tab;
    }
    
    private Tab addFormat(Tab tab, int dep, Format f) throws FetchException
    {
	tab = addRow(tab, dep, "name", f.getName());
	tab = addRow(tab, dep + 1, "quantitiy", Integer.toString(f.getQuantity()));
	tab = addRow(tab, dep + 1, "descriptions", o2str(f.getDescriptions()));
	return tab;
    }
    
    private Tab addTrack(Tab tab, int dep, Track t) throws FetchException
    {
	tab = addRow(tab, dep, "title", t.getTitle());
	tab = addRow(tab, dep + 1, "position", t.getPosition());
	tab = addRow(tab, dep + 1, "disc", Integer.toString(t.getDisc()));
	tab = addRow(tab, dep + 1, "no", Integer.toString(t.getNumber()));
	tab = addRow(tab, dep + 1, "duration", t.getDuration());
	tab = addRow(tab, dep + 1, "duration [ms]", Long.toString(t.getDurationInMillis()));
	return tab;
    }
    
    private Tab addLabelRelease(Tab tab, int dep, LabelRelease r) throws FetchException
    {
	tab = addRow(tab, dep, "title", r.getTitle());
	tab = addRow(tab, dep + 1, "nested release", r.getRelease().getTitle());
	tab = addRow(tab, dep + 1, "id", r.getId());
	tab = addRow(tab, dep + 1, "artist", r.getArtist());
	tab = addRow(tab, dep + 1, "status", r.getStatus());
	tab = addRow(tab, dep + 1, "catalog number", r.getCatalogNumber());
	tab = addRow(tab, dep + 1, "label name", r.getLabelName());
	tab = addRow(tab, dep + 1, "format", r.getFormat());
	tab = addRow(tab, dep + 1, "formats", r.getFormats().toString());
	return tab;
    }
    
    private Tab addArtists(Tab tab, int dep, List<Artist> a) throws FetchException
    {
	for (Artist i : a)
	{
	    tab = addArtist(tab, dep, i);
	}
	return tab;
    }
    
    private Tab addReleases(Tab tab, int dep, List<Release> r) throws FetchException
    {
	for (Release i : r)
	{
	    tab = addRelease(tab, dep, i);
	}
	return tab;
    }
    
    private Tab addArtistReleases(Tab tab, int dep, List<ArtistRelease> arl) throws FetchException
    {
	for (ArtistRelease i : arl)
	{
	    tab = addArtistRelease(tab, dep, i);
	}
	return tab;
    }
    
    private Tab addLabelReleases(Tab tab, int dep, List<LabelRelease> arl) throws FetchException
    {
	for (LabelRelease i : arl)
	{
	    tab = addLabelRelease(tab, dep, i);
	}
	return tab;
    }
    
    private Tab addReleaseArtists(Tab tab, int dep, List<ReleaseArtist> rla) throws FetchException
    {
	for (ReleaseArtist i : rla)
	{
	    tab = addReleaseArtist(tab, dep, i);
	}
	return tab;
    }
    
    private Tab addLabels(Tab tab, int dep, List<Label> l) throws FetchException
    {
	for (Label i : l)
	{
	    tab = addLabel(tab, dep, i);
	}
	return tab;
    }
    
    private Tab addFormats(Tab tab, int dep, List<Format> f) throws FetchException
    {
	for (Format i : f)
	{
	    tab = addFormat(tab, dep, i);
	}
	return tab;
    }
    
    private Tab addTracks(Tab tab, int dep, List<Track> t) throws FetchException
    {
	for (Track i : t)
	{
	    tab = addTrack(tab, dep, i);
	}
	return tab;
    }
    
    private String o2str(Object o)
    {
	if (o == null)
	{
	    return "NULL";
	}
	return o.toString();
    }
    
}
