package com.musicus.model;

import com.musicus.agent.Constants;

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
    private double[] songPreferenceFeatureModel;
    private double MSL = 0.5D;              // Music Satisfactory Level ( range : 0 - 1 )

    public Listener( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
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

    public double[] getSongPreferenceFeatureModel()
    {
        return songPreferenceFeatureModel;
    }

    public void setSongPreferenceFeatureModel( double[] songPreferenceFeatureModel )
    {
        this.songPreferenceFeatureModel = songPreferenceFeatureModel;
    }

    public double getMSL()
    {
        return MSL;
    }

    public void setMSL( double MSL )
    {
        this.MSL = MSL;
    }

    public void updateListenersLibrary( Map<String, List<Double>> musicLibrary )
    {

        Map<String, List<Double>> listenersSongLibrary = new HashMap<String, List<Double>>();
        for( String songDir : getSongDirs() )
        {
            for( Map.Entry<String, List<Double>> libraryEntry : musicLibrary.entrySet() )
            {
                if( libraryEntry.getValue() != null && libraryEntry.getKey().startsWith( songDir ) )
                {
                    listenersSongLibrary.put( libraryEntry.getKey(), libraryEntry.getValue() );
                }
            }
        }

        setSongLibrary( listenersSongLibrary );
    }

    public void updateSongPreference()
    {
        double[] featureModel = new double[Constants.ANALYSED_FEATURES_COUNT];    //**** No of features considered
        for( Map.Entry<String, List<Double>> libraryEntry : getSongLibrary().entrySet() )
        {
//            System.out.println("Going through features of : " + libraryEntry.getKey());
            List<Double> featureList = libraryEntry.getValue();
            for( int featureNo = 0; featureNo < featureList.size(); featureNo++ )
            {
//                System.out.print( featureNo + ":::" + featureModel[featureNo] + " += " + featureList.get( featureNo ) );
                featureModel[featureNo] += featureList.get( featureNo );
//                System.out.println( " = " + featureModel[featureNo]);
            }
        }

        for( int featureNo = 0; featureNo < featureModel.length; featureNo++ )
        {
//            System.out.print( ">>"+featureModel[featureNo] + " /= " + songLibrary.size() );
            featureModel[featureNo] /= songLibrary.size();
//            System.out.println( " = " + featureModel[featureNo]);
        }
        setSongPreferenceFeatureModel( featureModel );
    }
}
