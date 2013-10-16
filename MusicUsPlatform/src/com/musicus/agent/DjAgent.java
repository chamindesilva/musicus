package com.musicus.agent;

import com.musicus.Utils.Calculations;
import com.musicus.db.FileDb;
import com.musicus.db.SongCollection;
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
                    String senderName = sender.getName().replace( ":" ,"-").replace( "/" ,"#");
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
                // Update feature value minima and maxima
                for( int i = 0; i < Constants.CALCULATION_USED_FEATURES.length; i++ )
                {
                    featureMaxValues[i] = Double.MIN_VALUE;
                    featureMinValues[i] = Double.MAX_VALUE;
                }
                for( Map.Entry<String, Listener> connectedListenerEntry : connectedListeners.entrySet() )
                {
                    Listener connectedListener = connectedListenerEntry.getValue();
                    for( SongCollection collection : connectedListener.getEnabledMusicLibraryCollection() )
                    {
                        for( Song song : collection.getNotPlayedSongsList() )
                        {
                            //                            double[] calculationUsedFeatureValArr = song.getCalculationUsedFeatureValArr();
                            for( int featureNo = 0; featureNo < Constants.CALCULATION_USED_FEATURES.length; featureNo++ )
                            {
                                String featureName = Constants.CALCULATION_USED_FEATURES[featureNo];
                                Feature feature = song.getFeatures().get( featureName );
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

                double selectedDistance = Long.MAX_VALUE;
                Song selectedSong = null;
                AID selectedSongsLibraryAgent = null;

                // For each song find total distance to all the listeners
                for( Map.Entry<String, Listener> connectedListenerEntry : connectedListeners.entrySet() )
                {
                    Listener connectedListener = connectedListenerEntry.getValue();
                    log( Constants.LOG_IMPORTANT, getName(), connectedListener.getLibraryName(), " MSL : ", String.valueOf( connectedListener.getMSL() ) );

                    // Update the model(averages of the songs of the listener)
                    connectedListener.updateSongPreference();

                    for( SongCollection collection : connectedListener.getEnabledMusicLibraryCollection() )
                    {
                        for( Song song : collection.getNotPlayedSongsList() )
                        {
                            if( song.getFeatures().isEmpty() )        // Not extracted files
                            {
                                continue;
                            }
                            double totDistances = 0.0D;
                            double[] distances = new double[connectedListeners.size()];
                            int listenerNo = 0;
                            for( Listener listener : connectedListeners.values() )
                            {
                                double distanceFromSongToListener = Calculations.calculateEuclideanDistance(
                                        listener.getSongPreferenceFeatureModel(), song.getCalculationUsedFeatureValArr(), featureMaxValues, featureMinValues );
                                distances[listenerNo] = distanceFromSongToListener;
                                totDistances += ( distanceFromSongToListener * listener.getMSL() );
                                log( getName(), "For ", listener.getLibraryName(), " totDistance : ", String.valueOf( totDistances ) );
                                listenerNo++;
                            }
                            //                    log( Constants.LOG_IMPORTANT, getLibraryName(), "Total Distances ", Arrays.toString( distances ), " = ", String.valueOf( totDistances ), " for : ", libraryEntry.getKey().substring( libraryEntry.getKey().lastIndexOf( "\\" ) ) );


                            // IGNORE PLAYEDSONGS IN CALCULATIONS AS WELL
                            if( selectedDistance > totDistances )
                            {
                                selectedDistance = totDistances;
                                selectedSong = song;
                                selectedSongsLibraryAgent = connectedListenerEntry.getValue().getLibrary();
                                log( Constants.LOG_IMPORTANT, getName(), "Total Distances ", Arrays.toString( distances ), " = ",
                                        String.valueOf( totDistances ), " for : ", selectedSong.getPath(), " of ", selectedSongsLibraryAgent.getName() );
                            }

                        }
                    }
                }

                if( selectedSong != null )
                {
                    // Play song
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


                    // Update satisfaction levels of listeners
                    double maxMSLVal = Double.MIN_VALUE;
                    for( Listener listener : connectedListeners.values() )
                    {
                        double listenerMSL = listener.getMSL();
                        double distanceFromSongToListener = Calculations.calculateEuclideanDistance(
                                listener.getSongPreferenceFeatureModel(), selectedSong.getCalculationUsedFeatureValArr(), featureMaxValues, featureMinValues );
                        listener.setMSL( listenerMSL / distanceFromSongToListener );        // ( Math.pow( listenerMSL, 2 ) / distanceFromSongToListener )
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
        });
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
