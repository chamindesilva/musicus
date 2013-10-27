package com.musicus.agent;

import com.musicus.Utils.Calculations;
import com.musicus.db.FileDb;
import com.musicus.model.CollectionSong;
import com.musicus.model.SongCollection;
import com.musicus.model.Feature;
import com.musicus.model.Listener;
import com.musicus.model.Song;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 10/13/13
 * Time: 7:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class DjAgent extends MusicUsAgent
{
    private Map<String, Listener> connectedListeners = new HashMap<String, Listener>();

    private double[] featureMaxValues = new double[Constants.CALCULATION_USED_FEATURES.length];      // used to normalize data
    private double[] featureMinValues = new double[Constants.CALCULATION_USED_FEATURES.length];      // used to normalize data

    private int playingSongsNo;

    @Override protected String getAgentType()
    {
        return getAgentTypeCode();
    }

    public static String getAgentTypeCode()
    {
        return Constants.DJ;
    }

    @Override protected void addBehaviours()
    {
        // receive data files froom connected platforms and update libraries
        addBehaviour( new CyclicBehaviour()
        {
            @Override public void action()
            {
                MessageTemplate updatedLibFileInfo = MessageTemplate.MatchPerformative( ACLMessage.INFORM );
                ACLMessage msg = myAgent.receive( updatedLibFileInfo );
                if( msg != null )
                {
                    AID sender = msg.getSender();
                    // Change name from musicLib@101.2.186.224:1099/JADE to musicLib@101.2.186.224-1099#JADE to support creation of dir by name
                    String senderName = sender.getName().replace( ":", "-" ).replace( "/", "#" );
                    String savedFileName = msg.getConversationId();
                    String fileText = msg.getContent();

                    //write to agent's dir
                    File senderDataDir = new File( senderName );
                    if( !senderDataDir.exists() )
                    {
                        if( senderDataDir.mkdirs() )
                        {
                            // save to file
                            outputToFile( senderName, savedFileName, fileText );
                        }
                        else
                        {
                            System.out.println( ">>>>>>> ERROR IN SAVING DATA TO FILE BY DJ ::: " + senderName + " : " + senderDataDir.getAbsolutePath() );
                        }
                    }
                    else
                    {
                        // save to file
                        outputToFile( senderName, savedFileName, fileText );
                    }

                    if( !connectedListeners.containsKey( senderName ) )
                    {
                        Listener listener = new Listener( senderName );
                        listener.setLibrary( sender );
                        connectedListeners.put( senderName, listener );
                    }
                    Listener connectedListner = connectedListeners.get( senderName );
                    connectedListner.incrementReceivedFiles();
                    if( connectedListner.getReceivedFiles() == FileDb.TOTAL_FILE_COUNT_FOR_DB )
                    {
                        List<SongCollection> musicLibrary = FileDb.getSongCollections( senderName );
                        // TODO: AVOID RESETTING THE CURRENT VALUES
                        connectedListner.setMusicLibraryCollection( musicLibrary );
                        connectedListner.setReceivedFiles( 0 );

                    }

                }
                else
                {
                    block();
                }
            }

        } );

        // Select song to play
        addBehaviour( new TickerBehaviour( this, (long) ( 0.5 * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                // Adjust collections
                for( Map.Entry<String, Listener> connectedListenerEntry : connectedListeners.entrySet() )
                {
                    Listener connectedListener = connectedListenerEntry.getValue();
                    for( SongCollection collection : connectedListener.getEnabledMusicLibraryCollection() )
                    {
                        // UPDATE THE NOT PLAYED SONG LIST IN THE COLLECTION AND REUSE IN THIS CALCULATION TICK ROUND
                        List<CollectionSong> notPlayedSongsList = collection.getNotPlayedSongsList();       // Get not played and features extracted songs

                        double[][] collectionFeatureValues = new double[Constants.CALCULATION_USED_FEATURES.length][notPlayedSongsList.size()];
                        for( int songNo = 0; songNo < notPlayedSongsList.size(); songNo++ )
                        {
                            CollectionSong collectionSong = notPlayedSongsList.get( songNo );
                            Song originalSong = collectionSong.getDelegatedSong();
                            double[] calculationUsedFeatureValArr = originalSong.getCalculationUsedFeatureValArr();

                            for( int featureNo = 0; featureNo < Constants.CALCULATION_USED_FEATURES.length; featureNo++ )
                            {
                                collectionFeatureValues[featureNo][songNo] = calculationUsedFeatureValArr[featureNo];
                            }
                        }

                        // Get MEAN and SD
                        double[] featureMeans = Calculations.getMean( collectionFeatureValues );
                        double[] featureSDs = Calculations.getSD( collectionFeatureValues );

                        // Set adjusted feature values for collection in the CollectionSong obj
                        for( CollectionSong collectionSong : notPlayedSongsList )
                        {
                            Song originalSong = collectionSong.getDelegatedSong();
                            double[] calculationUsedFeatureValArr = originalSong.getCalculationUsedFeatureValArr();

                            for( int featureNo = 0; featureNo < Constants.CALCULATION_USED_FEATURES.length; featureNo++ )
                            {
                                String calculationUsedFeatureName = Constants.CALCULATION_USED_FEATURES[featureNo];
                                Feature collectionFeature = collectionSong.getFeatures().get( calculationUsedFeatureName );
                                Feature originalFeature = originalSong.getFeatures().get( calculationUsedFeatureName );
                                double adustedFeatureVal = Calculations.featureAdjustment( originalFeature.getVal(), featureMeans[featureNo], featureSDs[featureNo] );
                                collectionFeature.setVal( adustedFeatureVal );
                            }
                        }
                    }
                }

                // Update Listener models(avgs)
                for( Map.Entry<String, Listener> connectedListenerEntry : connectedListeners.entrySet() )
                {
                    Listener connectedListener = connectedListenerEntry.getValue();
                    // Update the model(averages of the songs of the listener)
                    connectedListener.updateSongPreference();
                }


                // Update feature value minima and maxima
                for( int i = 0; i < Constants.CALCULATION_USED_FEATURES.length; i++ )
                {
                    featureMaxValues[i] = -Double.MAX_VALUE;
                    featureMinValues[i] = Double.MAX_VALUE;
                }
                for( Map.Entry<String, Listener> connectedListenerEntry : connectedListeners.entrySet() )
                {
                    Listener connectedListener = connectedListenerEntry.getValue();
                    log( Constants.LOG_IMPORTANT, getName(), connectedListener.getLibraryName(), " MSL : ", String.valueOf( connectedListener.getMSL() ) );

                    for( SongCollection collection : connectedListener.getEnabledMusicLibraryCollection() )
                    {
                        List<CollectionSong> notPlayedSongsList = collection.getNotPlayedSongsList();   // Get not played and features extracted songs
                        for( CollectionSong collectionSong : notPlayedSongsList )
                        {
                            for( int featureNo = 0; featureNo < Constants.CALCULATION_USED_FEATURES.length; featureNo++ )
                            {
                                String featureName = Constants.CALCULATION_USED_FEATURES[featureNo];
                                Feature feature = collectionSong.getFeatures().get( featureName );
                                if( featureMaxValues[featureNo] < feature.getVal() )
                                {
                                    featureMaxValues[featureNo] = feature.getVal();
                                }
                                if( featureMinValues[featureNo] > feature.getVal() )
                                {
                                    featureMinValues[featureNo] = feature.getVal();
                                }
                            }

                        }
                    }
                }

                double selectedMslDistance = Long.MAX_VALUE;
                Song selectedSong = null;
                AID selectedSongsLibraryAgent = null;

                // For each collection, find the winner song  (OLD---For each song find total distance to all the listeners--)
                for( Map.Entry<String, Listener> connectedListenerEntry : connectedListeners.entrySet() )
                {
                    Listener connectedListener = connectedListenerEntry.getValue();

                    for( SongCollection collection : connectedListener.getEnabledMusicLibraryCollection() )
                    {
                        for( CollectionSong collectionSong : collection.getNotPlayedSongsList() )       // Get not played and features extracted songs
                        {
                            Song song = collectionSong;
                            double totMslDistances = 0.0D;
                            double[] mslDistances = new double[connectedListeners.size()];
                            int listenerNo = 0;
                            for( Listener listener : connectedListeners.values() )
                            {
                                double distanceFromSongToListener = Calculations.calculateEuclideanDistance(
                                        /*Updated in round 1*/listener.getSongPreferenceFeatureModel(), song.getCalculationUsedFeatureValArr(), featureMaxValues, featureMinValues );
                                double mslDistance = distanceFromSongToListener * listener.getMSL();
                                mslDistances[listenerNo] = mslDistance;
                                totMslDistances += mslDistance;
                                //                                log( getName(), "For ", listener.getLibraryName(), " totMslDistances : ", String.valueOf( totMslDistances ) );
                                listenerNo++;
                            }
                            log( getName(), "Song ", song.getName(), " of ", connectedListener.getLibrary().getName(), " with features ", Arrays.toString( song.getCalculationUsedFeatureValArr() ), " has total distance ", String.valueOf( totMslDistances ), " = ", Arrays.toString( mslDistances ), " for listners ", Arrays.toString( connectedListeners.keySet().toArray() ) );
                            //                    log( Constants.LOG_IMPORTANT, getLibraryName(), "Total Distances ", Arrays.toString( distances ), " = ", String.valueOf( totDistances ), " for : ", libraryEntry.getKey().substring( libraryEntry.getKey().lastIndexOf( "\\" ) ) );


                            // IGNORE PLAYEDSONGS IN CALCULATIONS AS WELL
                            if( selectedMslDistance > totMslDistances )  // ALSO USE STANDARD DEVIATION(LESS MEANS EVERYONE LIKES IT MORE THAN JUST ONE LIKE IT A LOT)
                            {
                                selectedMslDistance = totMslDistances;
                                selectedSong = song;
                                selectedSongsLibraryAgent = connectedListenerEntry.getValue().getLibrary();
                                log( Constants.LOG_IMPORTANT, getName(), /*"Total Distances ", Arrays.toString( distances ), " = ",*/
                                        " Selected ", selectedSong.getPath(), " of ", selectedSongsLibraryAgent.getName(), "totMslDistances ", String.valueOf( totMslDistances ) );
                            }

                        }
                    }
                }

                if( selectedSong != null )
                {
                    for( Listener connectedListener : connectedListeners.values() )
                    {
                        // Send play request
                        if( selectedSongsLibraryAgent == connectedListener.getLibrary() )
                        {
                            selectedSong.setPlayedNumber( ++playingSongsNo );
                            log( getName(), "Playing selected song ", selectedSong.getPath(), " of ", selectedSongsLibraryAgent.getName() );
                            //                    if( playerAgents != null && playerAgents.length != 0 )
                            {
                                ACLMessage newSongInform = new ACLMessage( ACLMessage.REQUEST );
                                newSongInform.addReceiver( selectedSongsLibraryAgent );
                                newSongInform.setContent( selectedSong.getPath() );   // Can also send byte arrays, serializable objects
                                newSongInform.setConversationId( Constants.PLAY_REQUEST );
                                newSongInform.setReplyWith( Constants.PLAY_REQUEST + System.currentTimeMillis() );
                                myAgent.send( newSongInform );
                                log( myAgent.getName(), "Sent request to play ", selectedSong.getPath(), " to ", selectedSongsLibraryAgent.getName() );

                                String mslLog = "";
                                for( Listener listener : connectedListeners.values() )
                                {
                                    mslLog += listener.getMSL();
                                    mslLog += ",";
                                }
                                String songFullPath = selectedSong.getPath();
                                int lastSeperator = songFullPath.lastIndexOf( "\\" );
                                int secondLastSeperator = songFullPath.lastIndexOf( "\\", lastSeperator - 1 );
                                System.out.println( ">>>>>>> " + lastSeperator + " ::" + secondLastSeperator );
                                mslLog += songFullPath.substring( lastSeperator + 1 );
                                mslLog += ",";
                                mslLog += songFullPath.substring( secondLastSeperator + 1, lastSeperator );
                                mstLog( mslLog );
                            }
                        }
                        // Send stop playing request to other agents
                        else
                        {
                            ACLMessage stopSongRequest = new ACLMessage( ACLMessage.REQUEST );
                            stopSongRequest.addReceiver( connectedListener.getLibrary() );
                            stopSongRequest.setContent( "" );   // Can also send byte arrays, serializable objects
                            stopSongRequest.setConversationId( Constants.STOP_REQUEST );
                            stopSongRequest.setReplyWith( Constants.STOP_REQUEST + System.currentTimeMillis() );
                            myAgent.send( stopSongRequest );
                            log( myAgent.getName(), "Sent request to stop to ", connectedListener.getLibrary().getName() );
                        }
                    }


                    // Update satisfaction levels of listeners
                    double maxMSLVal = Double.MIN_VALUE;
                    for( Listener listener : connectedListeners.values() )
                    {
                        double listenerMSL = listener.getMSL();
                        double distanceFromSongToListener = Calculations.calculateEuclideanDistance( listener.getSongPreferenceFeatureModel(), selectedSong.getCalculationUsedFeatureValArr(), featureMaxValues, featureMinValues );
                        listener.setMSL( listenerMSL + ( 1 / 1 + distanceFromSongToListener ) );        // ( Math.pow( listenerMSL, 2 ) / distanceFromSongToListener )
                        if( listener.getMSL() > maxMSLVal )
                        {
                            maxMSLVal = listener.getMSL();
                        }
                        log( Constants.LOG_IMPORTANT, getName(), "Updating MSL for ", listener.getLibraryName(), " MSL value: ", String.valueOf( listenerMSL ), " / ", String.valueOf( distanceFromSongToListener ), " = ", String.valueOf( listener.getMSL() ) );

                    }
                    // Rearrange MSL values to be 0 - 1 with max val as 1 (but don't change min val to 0)
                    for( Listener listener : connectedListeners.values() )
                    {
                        double listenerMSL = listener.getMSL();
                        listener.setMSL( listenerMSL / maxMSLVal );
                        log( Constants.LOG_IMPORTANT, getName(), "Normalized MSL for ", listener.getLibraryName(), " MSL value: ", String.valueOf( listener.getMSL() ) );
                    }
                }

            }
        } );
    }

    private void outputToFile( String dir, String fileName, String text )
    {
        PrintStream out = null;
        String filePath = ( dir != null && !dir.isEmpty() ? dir + File.separator + fileName : fileName );
        try
        {
            out = new PrintStream( new FileOutputStream( filePath ) );
            out.print( text );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            if( out != null )
            {
                out.close();
            }
        }
    }

    public static AID getAgents( Agent callFromAgent )
    {
        AID[] djAgents = MusicUsAgent.getAgents( getAgentTypeCode(), callFromAgent );
        return djAgents != null && djAgents.length > 0 ? djAgents[0] : null;
    }


    class ConnectedPlatForm
    {
        private AID library;
        private int receivedFiles;
        private List<SongCollection> musicLibraryCollection;

        AID getLibrary()
        {
            return library;
        }

        void setLibrary( AID library )
        {
            this.library = library;
        }

        int getReceivedFiles()
        {
            return receivedFiles;
        }

        void setReceivedFiles( int receivedFiles )
        {
            this.receivedFiles = receivedFiles;
        }

        void incrementReceivedFiles()
        {
            this.receivedFiles++;
        }

        List<SongCollection> getMusicLibraryCollection()
        {
            if( musicLibraryCollection == null )
            {
                musicLibraryCollection = new ArrayList<SongCollection>();
            }
            return musicLibraryCollection;
        }

        void setMusicLibraryCollection( List<SongCollection> musicLibraryCollection )
        {
            this.musicLibraryCollection = musicLibraryCollection;
        }
    }
}
