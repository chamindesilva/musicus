package com.musicus.agent;

import com.musicus.Utils.LimitedQueue;
import com.musicus.db.DbConnector;
import com.musicus.db.FileDb;
import com.musicus.db.SongCollection;
import com.musicus.gui.MainFrame;
import com.musicus.model.Listener;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/3/13
 * Time: 7:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicLibraryAgent extends MusicUsAgent
{
    public static final String DATA_TAG = "@DATA";
    public static final String EXTRACTED_SONGS_FILE = "ExtractedSongsFile.txt";
    public static final String EXTRACTED_FEATURE_VALUES_FILE = "ExtractedFeatureValues.arff";
//    private Map<String, List<Double>> musicLibrary;   // use a hash table for order preservation. Object is a Song object of features list
    private List<SongCollection> musicLibrary = new ArrayList<SongCollection>(  );
    private AID[] featureExtractorAgents;
    private AID[] playerAgents;
    private List<Listener> listeners = new ArrayList<Listener>();
    private LimitedQueue<String> lastPlayedQueue = new LimitedQueue<String>( 30 );
    private double[] featureMaxValues = new double[Constants.ANALYSED_FEATURES_COUNT];      // used to normalize data
    private double[] featureMinValues = new double[Constants.ANALYSED_FEATURES_COUNT];      // used to normalize data

    @Override protected void init()
    {
        musicLibrary = FileDb.getSongCollections();

        // test data
        SongCollection collection = new SongCollection();
        collection.setName( "test" );
        collection.setEnabled( true );
        collection.setSequenced( false );
        musicLibrary.add( collection );

        // LOAD GUI
        // init gui
        // set this in  to gui
        MainFrame.startGui( this );
        System.out.println( ">>>>>>>>>> GUI started");

        DbConnector.getConnection();
        super.init();
//        musicLibrary = new HashMap<String, List<Double>>();
        featureExtractorAgents = FeatureExtractorAgent.getAgents( this );
        playerAgents = PlayerAgent.getAgents( this );
        List<String> musicFolders = new ArrayList<String>();
        musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\VH1 100 Greatest Songs of 80s" ) );
        musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\VH1s_100_Greatest_Songs_Of_The_90s_-_KoD-2008" ) );
        musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\Billboard Top 100 Songs of Decade 2000-2009" ) );
        //            musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\classics" ) );
        musicFolders.add( new String( "D:\\shared music\\mp3-3" ) );
        musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\ChartHitz" ) );
        musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\#English DJ Song" ) );
        //        musicFolders.add( new File( "F:\\Ent\\ForPrabu\\ChaminClassics" ) );

        // setup listners in the environment
        addListener( "Listener80", 0, musicFolders );
        addListener( "Listener90", 1, musicFolders );
        addListener( "Listener00", 2, musicFolders );
        addListener( "ListenerCollection", 3, musicFolders );
        addListener( "ListenerChartHitz", 4, musicFolders );
        addListener( "ListenerDJCollection", 5, musicFolders );


        for(int i = 0; i < Constants.ANALYSED_FEATURES_COUNT; i++)
        {
            featureMaxValues[i]=Double.MIN_VALUE;
            featureMinValues[i]=Double.MAX_VALUE;
        }
        // Load existing data to library
        /*try
        {
            List<String> extractedSongsList = readFileToList( EXTRACTED_SONGS_FILE );
            List<String> extractedFeatureList = readFileToList( EXTRACTED_FEATURE_VALUES_FILE );
            for( int songNo = 0; songNo < extractedSongsList.size(); songNo++ )
            {
                String featureLineStr = extractedFeatureList.get( songNo );
                String[] featureStrArr = featureLineStr.split( "," );
                List<Double> features = new ArrayList<Double>();
                int featureNo = 0;
                for( String featureVal : featureStrArr )
                {
                    features.add( new Double( !"NaN".equals( featureVal ) ? featureVal : "0.0" ) );
                    if( featureMaxValues[featureNo] < features.get( featureNo ))
                    {
                        featureMaxValues[featureNo] = features.get( featureNo);
                    }
                    if( featureMinValues[featureNo] > features.get( featureNo ))
                    {
                        featureMinValues[featureNo] = features.get( featureNo);
                    }
                    featureNo++;
                }
                musicLibrary.put( extractedSongsList.get( songNo ), features );
            }

            // print library
            printLibrary();
        }
        catch( IOException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/

        updateListenerModels();

    }

    private void addListener( String listnerName, int musicFolderIndex, List<String> musicFolders )
    {
        Listener listner = new Listener( listnerName );
        listner.getSongDirs().add( musicFolders.get( musicFolderIndex ) );
        listeners.add( listner );
    }

    @Override protected void addBehaviours()
    {
        // Handle proposals on possible new song information messages
        addBehaviour( new CyclicBehaviour()
        {
            @Override public void action()
            {
                // Check messages for new songs
                MessageTemplate newSongProposalMt = MessageTemplate.MatchPerformative( ACLMessage.PROPOSE );
                ACLMessage msg = myAgent.receive( newSongProposalMt );
                if( msg != null )
                {
                    // Message received. Process it
                    String song = msg.getContent();
                    MusicUsAgent.log( getAID().getName(), "MusicLibraryAgent ", getAID().getName(), " received song ", song );

                    // if message contains a song that library doesn't contain, send it to analyser
//                    if( !musicLibrary.containsKey( song ) )
//                    {
//                        musicLibrary.put( song, null );
//
//                        if( featureExtractorAgents.length != 0 )
//                        {
//                            ACLMessage newSongInform = new ACLMessage( ACLMessage.REQUEST );
//                            newSongInform.addReceiver( featureExtractorAgents[0] );
//                            newSongInform.setContent( song );   // Can also send byte arrays, serializable objects
//                            newSongInform.setConversationId( Constants.FEATURE_EXTRACTION_PROPOSAL );
//                            newSongInform.setReplyWith( song );
//                            myAgent.send( newSongInform );
//                            log( myAgent.getName(), "Sent request to extract features from ", song );
//                        }
//                        else
//                        {
//                            // Response back with error
//                        }
//                    }
                }
                else
                {
                    block();
                }
            }
        } );

        // Handle information on extracted features of songs
        addBehaviour( new CyclicBehaviour()
        {
            @Override public void action()
            {
                MessageTemplate songFeatureMt = MessageTemplate.MatchPerformative( ACLMessage.INFORM );
                ACLMessage msg = myAgent.receive( songFeatureMt );
                if( msg != null )
                {
                    String song = msg.getInReplyTo();
                    String featureArff = msg.getContent();
                    log( getName(), "Received features for ", song );

                    // get data
                    int dataStart = featureArff.indexOf( DATA_TAG );
                    System.out.println( "dataStart >" + dataStart );
                    if( dataStart > -1 )
                    {
                        String data = featureArff.substring( dataStart + DATA_TAG.length() ).trim().replaceAll( "\n", "" );
                        System.out.println( ">>>>>Data list>>" + data );

                        // append to song file
                        try
                        {
                            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( EXTRACTED_SONGS_FILE, true ) ) );
                            out.println( song );
                            out.close();
                        }
                        catch( IOException e )
                        {
                            e.printStackTrace();
                        }

                        // append to feature file
                        try
                        {
                            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( EXTRACTED_FEATURE_VALUES_FILE, true ) ) );
                            out.println( data );
                            out.close();
                        }
                        catch( IOException e )
                        {
                            e.printStackTrace();
                        }
                    }
                }

            }
        } );

        // Print Library
        addBehaviour( new TickerBehaviour( this, (long) ( Constants.LIBRARY_SCAN_INTERVAL * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                printLibrary();
            }
        } );

        // scan for feature extract agents
        addBehaviour( new TickerBehaviour( this, (long) ( Constants.SCAN_FOR_AGENTS_INTERVAL * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                // No need of featureExtractorAgents to be accessed with synchronized even though it is been accessed by multiple behaviours.
                // As those behaviours are within same agent, all of those behaviours run in same thread. So no concurrency violence.
                featureExtractorAgents = FeatureExtractorAgent.getAgents( myAgent );
            }
        } );

        // scan for player agent
        addBehaviour( new TickerBehaviour( this, (long) ( Constants.SCAN_FOR_AGENTS_INTERVAL * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                playerAgents = PlayerAgent.getAgents( myAgent );
            }
        } );

        // Update listner models
        addBehaviour( new TickerBehaviour( this, (long) ( Constants.LISTNER_MODEL_UPDATE_INTERVAL * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                updateListenerModels();
            }
        } );

        // Play songs
        addBehaviour( new TickerBehaviour( this, (long) ( 0.5 * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                /*double selectedDistance = Long.MAX_VALUE;
                Map.Entry<String, List<Double>> selectedSong = null;
                for( Listener listener : listeners )
                {
                    log( Constants.LOG_IMPORTANT, getName(), listener.getName(), " MSL : ", String.valueOf( listener.getMSL() ) );
                }


                for( Map.Entry<String, List<Double>> libraryEntry : musicLibrary.entrySet() )
                {
                    if( libraryEntry.getValue() == null)        // Not extracted files
                    {
                        continue;
                    }
                    double totDistances = 0.0D;
                    double[] distances = new double[listeners.size()];
                    int listenerNo = 0;
                    for( Listener listener : listeners )
                    {
                        double distanceFromSongToListener = Calculations.calculateEuclideanDistance( listener.getSongPreferenceFeatureModel(), libraryEntry.getValue(), featureMaxValues, featureMinValues );
                        distances[listenerNo] = distanceFromSongToListener;
                        totDistances += ( distanceFromSongToListener * listener.getMSL() );
                        log( getName(), "For ", listener.getName(), " totDistance : ", String.valueOf( totDistances ) );
                        listenerNo++;
                    }
//                    log( Constants.LOG_IMPORTANT, getName(), "Total Distances ", Arrays.toString( distances ), " = ", String.valueOf( totDistances ), " for : ", libraryEntry.getKey().substring( libraryEntry.getKey().lastIndexOf( "\\" ) ) );

                    if( selectedDistance > totDistances && !lastPlayedQueue.contains( libraryEntry.getKey() ) )
                    {
                        log( Constants.LOG_IMPORTANT, getName(), "Total Distances ", Arrays.toString( distances ), " = ", String.valueOf( totDistances ), " for : ", libraryEntry.getKey().substring( libraryEntry.getKey().lastIndexOf( "\\" ) ) );
                        selectedDistance = totDistances;
                        selectedSong = libraryEntry;
                        log( Constants.LOG_IMPORTANT, getName(), "Selected song ", selectedSong.getKey() );
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
                        int secondLastSeperator = songFullPath.lastIndexOf( "\\" , lastSeperator-1 );
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
                        if( listener.getMSL() > maxMSLVal)
                        {
                            maxMSLVal = listener.getMSL();
                        }
                        log( Constants.LOG_IMPORTANT, getName(), "Updating MSL for ", listener.getName(), " MSL value: ", String.valueOf( listenerMSL ), " / ",  String.valueOf( distanceFromSongToListener ), " = ", String.valueOf( listener.getMSL() ) );

                    }
                    // Rearrange MSL values to be 0 - 1 with max val as 1 (but don't change min val to 0)
                    for( Listener listener : listeners )
                    {
                        double listenerMSL = listener.getMSL();
                        listener.setMSL( listenerMSL / maxMSLVal );
                        log( Constants.LOG_IMPORTANT, getName(), "Normalized MSL for ", listener.getName(), " MSL value: ", String.valueOf( listener.getMSL() ) );
                    }
                }   */

            }
        } );
    }

    private void updateListenerModels()
    {
        /*for( Listener listener : listeners )
        {
            listener.updateListenersLibrary( musicLibrary );
            listener.updateSongPreference();
        } */
    }

    @Override protected String getAgentType()
    {
        return getAgentTypeCode();
    }

    public static String getAgentTypeCode()
    {
        return Constants.MUSIC_LIBRARY;
    }

    public static AID[] getAgents( Agent callFromAgent )
    {
        return MusicUsAgent.getAgents( getAgentTypeCode(), callFromAgent );
    }

    private void printLibrary()
    {
        /*MusicUsAgent.log( getAID().getName(), "############ Music Library ############" );
        for( Map.Entry<String, List<Double>> stringListEntry : musicLibrary.entrySet() )
        {
            StringBuilder logSb = new StringBuilder();
            logSb.append( stringListEntry.getKey() );
            for( Double featureVal : stringListEntry.getValue() )
            {
                logSb.append( "," );
                logSb.append( featureVal );
            }
            MusicUsAgent.log( getAID().getName(), logSb.toString() );
        }*/
    }

    private List<String> readFileToList( String fileToRead ) throws IOException
    {
        List<String> readLines = new ArrayList<String>();
        BufferedReader br = null;
        try
        {
            br = new BufferedReader( new FileReader( fileToRead ) );
            String strLine;

            while( ( strLine = br.readLine() ) != null )
            {
                if( !strLine.isEmpty() )
                {
                    readLines.add( strLine );
                }
            }
        }
        finally
        {
            if( br != null )
            {
                br.close();
            }
        }

        return readLines;
    }

    public List<SongCollection> getMusicLibrary()
    {
        return musicLibrary;
    }

    public void setMusicLibrary( List<SongCollection> musicLibrary )
    {
        this.musicLibrary = musicLibrary;
    }
}
