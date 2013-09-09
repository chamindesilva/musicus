package com.musicus.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <DESCRIPTION>
 *
 * @author: Chamin De Silva
 * @version: 1.0
 * Date: 09/09/13
 * Time: 17:58
 */
public class Listener
{
    private String name;
    private List<String> songDirs = new ArrayList<String>();
    private Map<String, List<Double>> songLibrary = new HashMap<String, List<Double>>();
    private Double[] songPreferenceFeatureModel;

    public Listener( String name )
    {
        this.name = name;
    }

    public List<String> getSongDirs()
    {
        return songDirs;
    }

    public void setSongDirs( List<String> songDirs )
    {
        if( this.songDirs == null )
        {
            this.songDirs = new ArrayList<String>();
        }
        this.songDirs = songDirs;
    }

    public Map<String, List<Double>> getSongLibrary()
    {
        return songLibrary;
    }

    public void setSongLibrary( Map<String, List<Double>> songLibrary )
    {
        this.songLibrary = songLibrary;
    }

    public Double[] getSongPreferenceFeatureModel()
    {
        return songPreferenceFeatureModel;
    }

    public void setSongPreferenceFeatureModel( Double[] songPreferenceFeatureModel )
    {
        this.songPreferenceFeatureModel = songPreferenceFeatureModel;
    }

    public void updateListenersLibrary( Map<String, List<Double>> musicLibrary )
    {

        Map<String, List<Double>> listenersSongLibrary = new HashMap<String, List<Double>>();
        for( String songDir : getSongDirs() )
        {
            for( Map.Entry<String, List<Double>> libraryEntry : musicLibrary.entrySet() )
            {
                if( libraryEntry.getKey().startsWith( songDir ) )
                {
                    listenersSongLibrary.put( libraryEntry.getKey(), libraryEntry.getValue() );
                }
            }
        }

        setSongLibrary( listenersSongLibrary );
    }

    public void updateSongPreference()
    {
        Double[] featureModel = new Double[100];    //**** No of features considered
        for( Map.Entry<String, List<Double>> libraryEntry : getSongLibrary().entrySet() )
        {
            List<Double> featureList = libraryEntry.getValue();
            for( int featureNo = 0; featureNo < featureList.size(); featureNo++)
            {
                featureModel[featureNo] += featureList.get( featureNo );
            }
        }

        for( int featureNo = 0; featureNo < featureModel.length; featureNo++)
        {
            featureModel[featureNo] /= songLibrary.size();
        }
        setSongPreferenceFeatureModel( featureModel );
    }
}
