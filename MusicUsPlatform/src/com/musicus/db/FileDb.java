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
        List<SongCollection> songCollections = new ArrayList<SongCollection>();
        List<FileSavable> fileSavables = FileSavable.loadData( new SongCollection() );
        for( FileSavable fileSavable : fileSavables )
        {
            SongCollection collection = (SongCollection) fileSavable;
            songCollections.add( collection );
        }

        return songCollections;
    }

    public static void saveSongCollection( List<SongCollection> collection )
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

        FileSavable.persistData( featureSet );
        FileSavable.persistData( songSet );
        FileSavable.persistData( collectionEntrySet );
        FileSavable.persistData( collection );
    }
}
