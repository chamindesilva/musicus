package com.musicus.model;

import com.musicus.agent.Constants;
import com.musicus.db.FileSavable;
import com.musicus.db.Savable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/30/13
 * Time: 11:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class Song extends FileSavable
{
    private String path;        // Primary key
    private String name;
    private Map<String, Feature> features;
    private int status;
    private int playedNumber;       // if the song is played, no of the order it played is stored

    // hold the list of all song objs. So before creating a new song obj, check whether a song obj is already created
    private static Map<String, Song> createdSongs = new HashMap<String, Song>();

    public Song( String path, String name )
    {
        this.path = path;
        this.name = name;
    }

    public static FileSavable getInstance( String path, String name, String fileDirPath )
    {
        if( !createdSongs.containsKey( path ) )
        {
            Song song = new Song( path, name );
            song.setFeatures( getLoadingFeatures( path, fileDirPath ) );
            createdSongs.put( song.getPath(), song );
        }
        return createdSongs.get( path );
    }

    public static Song getSongInstance( String path )
    {
        return createdSongs.get( path );
    }

    @Override public FileSavable  load( String[] dbValues, String fileDirPath )
    {
        FileSavable loadedObj = null;
        if( dbValues.length == 2 )
        {
            path = dbValues[0];
            name = dbValues[1];
            loadedObj = getInstance( path, name, fileDirPath );
        }

        return loadedObj;
    }

    private static Map<String, Feature> getLoadingFeatures( String path, String fileDirPath )
    {
        Map<String, Feature> featureSet = new HashMap<String, Feature>();
        List<FileSavable> fileSavables = FileSavable.loadData( new Feature(), fileDirPath );
        for( FileSavable fileSavable : fileSavables )
        {
            Feature feature = (Feature) fileSavable;
            if( path.equals( feature.getOwner() ) )
            {
                featureSet.put( feature.getName(), feature );
            }
        }

        return featureSet;
    }

    @Override public String getDbString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( path );
        sb.append( "," );
        sb.append( name );

        return sb.toString();
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public Map<String, Feature> getFeatures()
    {
        if( features == null )
        {
            features = new HashMap<String, Feature>();
        }
        return features;
    }

    public void setFeatures( Map<String, Feature> features )
    {
        this.features = features;
    }

    public int getPlayedNumber()
    {
        return playedNumber;
    }

    public void setPlayedNumber( int playedNumber )
    {
        this.playedNumber = playedNumber;
    }

    public String getCompositePrimaryKey()
    {
        return path;
    }

    public double[] getCalculationUsedFeatureValArr()
    {
        double[] calculationUsedFeatureValArr = new double[Constants.CALCULATION_USED_FEATURES.length];

        for( int featureNo = 0; featureNo < Constants.CALCULATION_USED_FEATURES.length; featureNo++ )
        {
            String calculationUsedFeatureName = Constants.CALCULATION_USED_FEATURES[featureNo];

            Feature feature = null;
            if( ( feature = getFeatures().get( calculationUsedFeatureName ) ) != null )
            {
                calculationUsedFeatureValArr[featureNo] = feature.getVal();
            }
//            else
//            {
//                System.out.println( ">>>>> FEATURE " + calculationUsedFeatureName + "NOT FOUND FOR SONG " + song.getPath() );
//            }
        }

        return calculationUsedFeatureValArr;
    }
}
