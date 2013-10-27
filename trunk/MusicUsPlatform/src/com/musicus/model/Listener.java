package com.musicus.model;

import com.musicus.agent.Constants;
import com.musicus.agent.MusicUsAgent;
import jade.core.AID;

import java.util.ArrayList;
import java.util.Arrays;
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
    private AID library;
    private String libraryName;
    private int receivedFiles;
    //    private List<String> songDirs = new ArrayList<String>();
    //    private Map<String, List<Double>> songLibrary = new HashMap<String, List<Double>>();
    private List<SongCollection> musicLibraryCollection;
    private double[] songPreferenceFeatureModel;
    private double MSL = 0.5D;              // Music Satisfactory Level ( range : 0 - 1 )

    public Listener( String libraryName )
    {
        this.libraryName = libraryName;
    }

    public AID getLibrary()
    {
        return library;
    }

    public void setLibrary( AID library )
    {
        this.library = library;
    }

    public String getLibraryName()
    {
        return libraryName;
    }

    //    public List<String> getSongDirs()
    //    {
    //        return songDirs;
    //    }
    //
    //    public void setSongDirs( List<String> songDirs )
    //    {
    //        if( this.songDirs == null )
    //        {
    //            this.songDirs = new ArrayList<String>();
    //        }
    //        this.songDirs = songDirs;
    //    }

    //    public Map<String, List<Double>> getSongLibrary()
    //    {
    //        return songLibrary;
    //    }
    //
    //    public void setSongLibrary( Map<String, List<Double>> songLibrary )
    //    {
    //        this.songLibrary = songLibrary;
    //    }

    public int incrementReceivedFiles()
    {
        return ++receivedFiles;
    }

    public int getReceivedFiles()
    {
        return receivedFiles;
    }

    public void setReceivedFiles( int receivedFiles )
    {
        this.receivedFiles = receivedFiles;
    }

    /**
     * Get enabled library collections(Playlists)
     *
     * @return
     */
    public List<SongCollection> getMusicLibraryCollection()
    {
        if( null == musicLibraryCollection )
        {
            musicLibraryCollection = new ArrayList<SongCollection>();
        }

        return musicLibraryCollection;
    }

    /**
     * Get enabled library collections(Playlists)
     *
     * @return
     */
    public List<SongCollection> getEnabledMusicLibraryCollection()
    {
        List<SongCollection> enabledMusicLibraryCollection = new ArrayList<SongCollection>();
        for( SongCollection collection : getMusicLibraryCollection() )
        {
            if( collection.isEnabled() )
            {
                enabledMusicLibraryCollection.add( collection );
            }
        }

        return enabledMusicLibraryCollection;
    }

    public List<SongCollection> getFullMusicLibraryCollection()
    {
        return musicLibraryCollection;
    }

    public void setMusicLibraryCollection( List<SongCollection> musicLibraryCollection )
    {
        this.musicLibraryCollection = musicLibraryCollection;
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

    /**
     * Update music lib list in the dirs of the listner's dirs list
     */
    //    public void updateListenersLibrary( Map<String, List<Double>> musicLibrary )
    //    {
    //
    //        Map<String, List<Double>> listenersSongLibrary = new HashMap<String, List<Double>>();
    //        for( String songDir : getSongDirs() )
    //        {
    //            for( Map.Entry<String, List<Double>> libraryEntry : musicLibrary.entrySet() )
    //            {
    //                if( libraryEntry.getValue() != null && libraryEntry.getKey().startsWith( songDir ) )
    //                {
    //                    listenersSongLibrary.put( libraryEntry.getKey(), libraryEntry.getValue() );
    //                }
    //            }
    //        }
    //
    //        setSongLibrary( listenersSongLibrary );
    //    }
    public void updateSongPreference()
    {
        double[] featureModel = new double[Constants.CALCULATION_USED_FEATURES.length];     // No of features considered
        int calculatedSongCount = 0;

        for( SongCollection collection : getEnabledMusicLibraryCollection() )
        {
            for( CollectionSong collectionSong : collection.getNotPlayedSongsList() )       // Get not played and features extracted songs
            {
                System.out.println( getLibraryName() + " : Going through features of : " + collectionSong.getPath() );
                Map<String, Feature> featuresMap = collectionSong.getFeatures();

                if( !featuresMap.isEmpty() )
                {
                    // Check whether all the feature values are available
                    String missingFeature = getNotAvailableFeatures( featuresMap );
                    if( null == missingFeature )        // All features are available
                    {
                        calculatedSongCount++;

                        for( int featureNo = 0; featureNo < Constants.CALCULATION_USED_FEATURES.length; featureNo++ )
                        //                for( String calculationUsedFeature : Constants.CALCULATION_USED_FEATURES )
                        {
                            String calculationUsedFeatureName = Constants.CALCULATION_USED_FEATURES[featureNo];

                            Feature feature = null;
                            if( ( feature = featuresMap.get( calculationUsedFeatureName ) ) != null )
                            {
                                System.out.print( calculationUsedFeatureName + ":::" + featureModel[featureNo] + " += " + feature.getVal() );
                                featureModel[featureNo] += feature.getVal();
                                System.out.println( " = " + featureModel[featureNo] );
                            }
                            else
                            {
                                System.out.println( ">>>>> FEATURE " + calculationUsedFeatureName + "NOT FOUND FOR SONG " + collectionSong.getPath() );
                            }
                        }

                    }
                    else
                    {
                        System.out.println( ">>>>> FEATURE " + missingFeature + "NOT FOUND FOR SONG " + collectionSong.getPath() );
                    }
                }
            }

            for( int featureNo = 0; featureNo < featureModel.length; featureNo++ )
            {
                System.out.print( ">>" + featureModel[featureNo] + " /= " + calculatedSongCount );
                featureModel[featureNo] /= calculatedSongCount;
                System.out.println( " = " + featureModel[featureNo] );
            }
            MusicUsAgent.log( getLibraryName(), "Feature values ", Arrays.toString( featureModel ) );
            setSongPreferenceFeatureModel( featureModel );
        }
    }

    /**
     * Returns null if all features used by DJ for calculations are available
     *
     * @param featuresMap
     * @return
     */
    private String getNotAvailableFeatures( Map<String, Feature> featuresMap )
    {
        for( String calculationUsedFeatureName : Constants.CALCULATION_USED_FEATURES )
        {
            if( !featuresMap.containsKey( calculationUsedFeatureName ) )
            {
                return calculationUsedFeatureName;
            }
        }

        return null;
    }
}
