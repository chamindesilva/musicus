package com.musicus.agent;

import com.musicus.Utils.Calculations;
import com.musicus.db.FileDb;
import com.musicus.db.SongCollection;
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

    private double[] featureMaxValues = new double[Constants.ANALYSED_FEATURES_COUNT];      // used to normalize data
    private double[] featureMinValues = new double[Constants.ANALYSED_FEATURES_COUNT];      // used to normalize data

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
                    String senderName = sender.getName();
                    String savedFileName = msg.getConversationId();
                    String fileText = msg.getContent();

                    //write to agent's dir
                    File senderDataDir = new File( senderName );
                    if( !senderDataDir.exists() )
                    {
                        if( senderDataDir.mkdirs() )
                        {
                            // save to file
                            outputToFile( savedFileName, fileText );
                        }
                        else
                        {
                            System.out.println( ">>>>>>> ERROR IN SAVING DATA TO FILE BY DJ ::: " + senderName + " : " + senderDataDir.getAbsolutePath() );
                        }
                    }
                    else
                    {
                        // save to file
                        outputToFile( savedFileName, fileText );
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
                    for( SongCollection collection : connectedListener.getMusicLibraryCollection() )
                    {
                        for( Song song : collection.getSongsList() )
                        {
                            if( song.getFeatures().isEmpty() )        // Not extracted files
                            {
                                continue;
                            }


                        }
                    }
                }

                double selectedDistance = Long.MAX_VALUE;
                Map.Entry<String, List<Double>> selectedSong = null;

                // For each song find total distance to all the listeners
                for( Map.Entry<String, Listener> connectedListenerEntry : connectedListeners.entrySet() )
                {
                    Listener connectedListener = connectedListenerEntry.getValue();
                    connectedListener.updateSongPreference();       // update the model(averages of the songs of the listener)
                    log( Constants.LOG_IMPORTANT, getName(), connectedListener.getLibraryName(), " MSL : ", String.valueOf( connectedListener.getMSL() ) );

                    for( SongCollection collection : connectedListener.getMusicLibraryCollection() )
                    {
                        for( Song song : collection.getSongsList() )
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
                                double distanceFromSongToListener =
                                        Calculations.calculateEuclideanDistance( listener.getSongPreferenceFeatureModel(),
                                                song.getFeatures(), featureMaxValues, featureMinValues );
                                distances[listenerNo] = distanceFromSongToListener;
                                totDistances += ( distanceFromSongToListener * listener.getMSL() );
                                log( getName(), "For ", listener.getLibraryName(), " totDistance : ", String.valueOf( totDistances ) );
                                listenerNo++;
                            }
                            //                    log( Constants.LOG_IMPORTANT, getLibraryName(), "Total Distances ", Arrays.toString( distances ), " = ", String.valueOf( totDistances ), " for : ", libraryEntry.getKey().substring( libraryEntry.getKey().lastIndexOf( "\\" ) ) );

                            if( selectedDistance > totDistances && !lastPlayedQueue.contains( connectedListenerEntry.getKey() ) )
                            {
                                log( Constants.LOG_IMPORTANT, getName(), "Total Distances ", Arrays.toString( distances ), " = ", String.valueOf( totDistances ), " for : ", connectedListenerEntry.getKey().substring( connectedListenerEntry.getKey().lastIndexOf( "\\" ) ) );
                                selectedDistance = totDistances;
                                selectedSong = connectedListenerEntry;
                                log( Constants.LOG_IMPORTANT, getName(), "Selected song ", selectedSong.getKey() );
                            }

                        }
                    }

                    if( selectedSong != null )
                    {
                        // Play song
                        lastPlayedQueue.add( selectedSong.getKey() );
                        log( getName(), "Playing selected song ", selectedSong.getKey() );
                        if( playerAgents != null && playerAgents.length != 0 )
                        {
                            ACLMessage newSongInform = new ACLMessage( ACLMessage.REQUEST );
                            newSongInform.addReceiver( playerAgents[0] );
                            newSongInform.setContent( selectedSong.getKey() );   // Can also send byte arrays, serializable objects
                            newSongInform.setConversationId( Constants.PLAY_REQUEST );
                            newSongInform.setReplyWith( Constants.PLAY_REQUEST + System.currentTimeMillis() );
                            myAgent.send( newSongInform );
                            log( myAgent.getName(), "Sent request to play ", selectedSong.getKey() );

                            String mslLog = "";
                            for( Listener listener : listeners )
                            {
                                mslLog += listener.getMSL();
                                mslLog += ",";
                            }
                            String songFullPath = selectedSong.getKey();
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
                        for( Listener listener : listeners )
                        {
                            double listenerMSL = listener.getMSL();
                            double distanceFromSongToListener = Calculations.calculateEuclideanDistance( listener.getSongPreferenceFeatureModel(), selectedSong.getValue(), featureMaxValues, featureMinValues );
                            listener.setMSL( listenerMSL / distanceFromSongToListener );        // ( Math.pow( listenerMSL, 2 ) / distanceFromSongToListener )
                            if( listener.getMSL() > maxMSLVal )
                            {
                                maxMSLVal = listener.getMSL();
                            }
                            log( Constants.LOG_IMPORTANT, getName(), "Updating MSL for ", listener.getLibraryName(), " MSL value: ", String.valueOf( listenerMSL ), " / ", String.valueOf( distanceFromSongToListener ), " = ", String.valueOf( listener.getMSL() ) );

                        }
                        // Rearrange MSL values to be 0 - 1 with max val as 1 (but don't change min val to 0)
                        for( Listener listener : listeners )
                        {
                            double listenerMSL = listener.getMSL();
                            listener.setMSL( listenerMSL / maxMSLVal );
                            log( Constants.LOG_IMPORTANT, getName(), "Normalized MSL for ", listener.getLibraryName(), " MSL value: ", String.valueOf( listener.getMSL() ) );
                        }
                    }

                }
            }

            );
        }

    private void outputToFile( String fileName, String text )
    {
        PrintStream out = null;
        try
        {
            out = new PrintStream( new FileOutputStream( fileName ) );
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
