package com.musicus.model;

import com.musicus.db.FileSavable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 10/1/13
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class SongCollection extends FileSavable
{
    private String name;
    private boolean sequenced;  // true for playlists(have a sequencing of songs in playlist), false song directories
    private boolean enabled;
    private List<CollectionSong> songsList;

    // Temporary variables
    private CollectionSong collectionSelectedSong;


    //    @Override public FileSavable getInstance()
    //    {
    //        return new SongCollection();
    //    }


    public SongCollection()
    {
    }

    public SongCollection( String name, boolean sequenced, boolean enabled, List<CollectionSong> songsList )
    {
        this.name = name;
        this.sequenced = sequenced;
        this.enabled = enabled;
        this.songsList = songsList;
    }

    @Override public FileSavable load( String[] dbValues, String fileDirPath )
    {
        FileSavable loadedObj = null;
        if( dbValues.length == 3 )
        {
            String name = dbValues[0];
            boolean sequenced = Boolean.parseBoolean( dbValues[1] );
            boolean enabled = Boolean.parseBoolean( dbValues[2] );
            List<CollectionSong> filteredSongList = getfilteredSongs( name, fileDirPath );
            loadedObj = new SongCollection( name, sequenced, enabled, filteredSongList );
        }

        return loadedObj;
    }

    private List<CollectionSong> getfilteredSongs( String name, String fileDirPath )
    {
        List<CollectionSong> filteredSongList = new ArrayList<CollectionSong>();
        List<Song> fullSongsList = FileSavable.getFullSongsList( fileDirPath );
        List<FileSavable> fileSavables = FileSavable.loadData( new SongCollectionEntry( null, null ), fileDirPath );
        for( FileSavable fileSavable : fileSavables )
        {
            SongCollectionEntry collectionEntry = (SongCollectionEntry) fileSavable;
            if( name.equals( collectionEntry.getSongCollectionName() ) )
            {
                for( Song song : fullSongsList )
                {
                    if( collectionEntry.getSong().equals( song.getPath() ) )
                    {
                        filteredSongList.add( new CollectionSong( song ) );
                    }
                }
            }
        }

        return filteredSongList;
    }

    @Override public String getDbString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( name );
        sb.append( "," );
        sb.append( sequenced );
        sb.append( "," );
        sb.append( enabled );

        return sb.toString();
    }

    @Override public String getCompositePrimaryKey()
    {
        return name;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public boolean isSequenced()
    {
        return sequenced;
    }

    public void setSequenced( boolean sequenced )
    {
        this.sequenced = sequenced;
    }

    public List<CollectionSong> getSongsList()
    {
        if( songsList == null )
        {
            songsList = new ArrayList<CollectionSong>();
        }
        return songsList;
    }

    /**
     * Get not played and features extracted songs
     * @return
     */
    public List<CollectionSong> getNotPlayedSongsList()
    {
        // get not played songs
        List<CollectionSong> filteredNotPlayedSongList = new ArrayList<CollectionSong>();
        for( CollectionSong song : getSongsList() )
        {
            if( 0 == song.getPlayedNumber() && !song.getFeatures().isEmpty() )       // not played. if played no is positive.
            {
                filteredNotPlayedSongList.add( song );
            }
        }
        return filteredNotPlayedSongList;
    }

    public void setSongsList( List<CollectionSong> songsList )
    {
        this.songsList = songsList;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }

    public CollectionSong getCollectionSelectedSong()
    {
        return collectionSelectedSong;
    }

    public void setCollectionSelectedSong( CollectionSong collectionSelectedSong )
    {
        this.collectionSelectedSong = collectionSelectedSong;
    }
}

