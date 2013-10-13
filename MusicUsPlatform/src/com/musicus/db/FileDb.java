package com.musicus.db;

import com.musicus.model.Feature;
import com.musicus.model.Song;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 10/1/13
 * Time: 10:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileDb
{

    public static List<SongCollection> getSongCollections()
    {
        return getSongCollections( null );
    }

    public static List<SongCollection> getSongCollections( String dir )
    {
        List<SongCollection> songCollections = new ArrayList<SongCollection>();
        List<FileSavable> fileSavables = FileSavable.loadData( new SongCollection(), dir );
        for( FileSavable fileSavable : fileSavables )
        {
            SongCollection collection = (SongCollection) fileSavable;
            songCollections.add( collection );
        }

        return songCollections;
    }

    public static String[] saveSongCollection( List<SongCollection> collection )
    {
        return saveSongCollection( collection, null );
    }

    // INCREASE THIS IF LIBRARY IS ADDED WITH MORE LEVELS(MORE FILES TO SAVE)
    public static final int TOTAL_FILE_COUNT_FOR_DB = 4;

    public static String[] saveSongCollection( List<SongCollection> collection, String dir )
    {
        Collection<SongCollectionEntry> collectionEntrySet = new ArrayList<SongCollectionEntry>();      // Order matters
        Collection<Song> songSet = new HashSet<Song>();
        Collection<Feature> featureSet = new HashSet<Feature>();
        for( SongCollection songCollection : collection )
        {
            List<Song> songsList = songCollection.getSongsList();
            if( songsList != null && !songsList.isEmpty() )
            {
                for( Song song : songsList )
                {
                    SongCollectionEntry entry = new SongCollectionEntry();
                    entry.setSongCollectionName( songCollection.getName() );
                    entry.setSong( song.getPath() );
                    collectionEntrySet.add( entry );

                    songSet.add( song );

                    Set<Feature> features = song.getFeatures();
                    if( features != null )
                    {
                        featureSet.addAll( features );
                    }
                }
            }
        }

        String featureFileName         = FileSavable.persistData( featureSet, dir );
        String songFileName            = FileSavable.persistData( songSet, dir );
        String collectionEntryFileName = FileSavable.persistData( collectionEntrySet, dir );
        String collectionFileName      = FileSavable.persistData( collection, dir );

        return new String[]{featureFileName, songFileName, collectionEntryFileName, collectionFileName};
    }
}
