package com.musicus.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping between song collection and song
 * User: Chamin
 * Date: 10/1/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SongCollectionEntry extends FileSavable
{
    private String songCollectionName;
    private String song;
    private String compositeKey;

//    @Override public FileSavable getInstance()
//    {
//        return new SongCollectionEntry();
//    }


    public SongCollectionEntry()
    {
    }

    public SongCollectionEntry( String songCollectionName, String song )
    {
        this.songCollectionName = songCollectionName;
        this.song = song;
    }

    @Override public FileSavable load( String[] dbValues, String fileDirPath )
    {
        FileSavable loadedObj = null;
        if( dbValues.length == 2 )
        {
            String songCollectionName = dbValues[0];
            String song = dbValues[1];
            loadedObj =  new SongCollectionEntry(songCollectionName, song);
        }

        return loadedObj;
    }

    @Override public String getDbString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( songCollectionName );
        sb.append( "," );
        sb.append( song );

        return sb.toString();
    }

    public String getCompositePrimaryKey()
    {
        if( compositeKey == null )
        {
            compositeKey = songCollectionName + "##" + song;
        }
        return compositeKey;
    }

    public String getSongCollectionName()
    {
        return songCollectionName;
    }

    public void setSongCollectionName( String songCollectionName )
    {
        this.songCollectionName = songCollectionName;
    }

    public String getSong()
    {
        return song;
    }

    public void setSong( String song )
    {
        this.song = song;
    }
}
