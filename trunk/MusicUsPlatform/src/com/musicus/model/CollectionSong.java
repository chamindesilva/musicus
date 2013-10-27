package com.musicus.model;

import com.musicus.agent.Constants;
import com.musicus.db.FileSavable;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for songs to store collection level info
 * User: Chamin
 * Date: 10/26/13
 * Time: 8:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollectionSong extends Song
{
    Song delegatedSong;
    private Map<String, Feature> collectionAdjustedFeatures;


    public CollectionSong( String path, String name )
    {
        super( path, name );
    }

    public CollectionSong( Song delegatedSong )
    {
        super( delegatedSong.getPath(), delegatedSong.getName() );
        this.delegatedSong = delegatedSong;
        //Duplicate features from delegated song to this wrapper layer
        for( Map.Entry<String, Feature> featureEntry : this.delegatedSong.getFeatures().entrySet() )
        {
            Map<String, Feature> collectionAdjustedFeatures = getFeatures();
            Feature originalFeature = featureEntry.getValue();
            collectionAdjustedFeatures.put( featureEntry.getKey(), new Feature( originalFeature.getOwner(), originalFeature.getName(), originalFeature.getVal() ) );
        }
    }

    public Song getDelegatedSong()
    {
        return delegatedSong;
    }

    public void setDelegatedSong( Song delegatedSong )
    {
        this.delegatedSong = delegatedSong;
    }

    public static FileSavable getInstance( String path, String name, String fileDirPath )
    {
        CollectionSong collectionSong = new CollectionSong( path, name );
        collectionSong.setDelegatedSong( (Song) Song.getInstance( path, name, fileDirPath ) );
        return collectionSong;
    }

    public static Song getSongInstance( String path )
    {
        return Song.getSongInstance( path );
    }

    @Override public FileSavable load( String[] dbValues, String fileDirPath )
    {
        return delegatedSong.load( dbValues, fileDirPath );
    }

    @Override public String getDbString()
    {
        return delegatedSong.getDbString();
    }

    @Override public String getName()
    {
        return delegatedSong.getName();
    }

    @Override public void setName( String name )
    {
        delegatedSong.setName( name );
    }

    @Override public String getPath()
    {
        return delegatedSong.getPath();
    }

    @Override public void setPath( String path )
    {
        delegatedSong.setPath( path );
    }

    @Override public Map<String, Feature> getFeatures()
    {
        if( collectionAdjustedFeatures == null )
        {
            collectionAdjustedFeatures = new HashMap<String, Feature>();
        }
        return collectionAdjustedFeatures;
    }

    @Override public void setFeatures( Map<String, Feature> features )
    {
        collectionAdjustedFeatures = features;
    }

    @Override public int getPlayedNumber()
    {
        return delegatedSong.getPlayedNumber();
    }

    @Override public void setPlayedNumber( int playedNumber )
    {
        delegatedSong.setPlayedNumber( playedNumber );
    }

    @Override public String getCompositePrimaryKey()
    {
        return delegatedSong.getCompositePrimaryKey();
    }

    @Override public double[] getCalculationUsedFeatureValArr()
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
