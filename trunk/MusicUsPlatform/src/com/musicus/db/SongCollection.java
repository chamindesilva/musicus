package com.musicus.db;

import com.musicus.model.Song;

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
    private List<Song> songsList;


    @Override public FileSavable getInstance()
    {
        return new SongCollection();
    }

    @Override public boolean load( String[] dbValues )
    {
        boolean loadSuccess = false;
        if( dbValues.length == 3 )
        {
            name = dbValues[0];
            sequenced = Boolean.parseBoolean( dbValues[1] );
            enabled = Boolean.parseBoolean( dbValues[2] );
            List<Song> songsList = FileSavable.getFullSongsList();

            List<FileSavable> fileSavables = FileSavable.loadData( new SongCollectionEntry() );
            List<Song> filteredSongList = new ArrayList<Song>();
            for( FileSavable fileSavable : fileSavables )
            {
                SongCollectionEntry collectionEntry = (SongCollectionEntry) fileSavable;
                if( name.equals( collectionEntry.getSongCollectionName() ) )
                {
                    for( Song song : songsList )
                    {
                        if( collectionEntry.getSong().equals( song.getPath() ) )
                        {
                            filteredSongList.add( song );
                        }
                    }
                }
            }
            songsList = filteredSongList;
            loadSuccess = true;
        }
        else
        {
            loadSuccess = false;
        }

        return loadSuccess;
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

    public List<Song> getSongsList()
    {
        return songsList;
    }

    public void setSongsList( List<Song> songsList )
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
}

