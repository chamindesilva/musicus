package com.musicus.agent;

import com.musicus.Utils.Calculations;
import com.musicus.Utils.LimitedQueue;
import com.musicus.db.DbConnector;
import com.musicus.db.FileDb;
import com.musicus.db.SongCollection;
import com.musicus.gui.MainFrame;
import com.musicus.model.Feature;
import com.musicus.model.Listener;
import com.musicus.model.Song;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/3/13
 * Time: 7:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicLibraryAgent extends MusicUsAgent
{
//    public static final String DATA_TAG = "@DATA";
//    public static final String EXTRACTED_SONGS_FILE = "ExtractedSongsFile.txt";
//    public static final String EXTRACTED_FEATURE_VALUES_FILE = "ExtractedFeatureValues.arff";
    //    private Map<String, List<Double>> musicLibrary;   // use a hash table for order preservation. Object is a Song object of features list
    private List<SongCollection> musicLibrary = new ArrayList<SongCollection>();
    private AID[] featureExtractorAgents;
    private AID playerAgent;
    private AID djAgent;
//    private List<Listener> listeners = new ArrayList<Listener>();
//    private LimitedQueue<String> lastPlayedQueue = new LimitedQueue<String>( 30 );
//    private double[] featureMaxValues = new double[Constants.ANALYSED_FEATURES_COUNT];      // used to normalize data
//    private double[] featureMinValues = new double[Constants.ANALYSED_FEATURES_COUNT];      // used to normalize data
    private boolean libraryUpdatesToBeSentToDj = true;

    @Override protected void init() throws Exception
    {
        // Create sub agents to support this library agent
        ContainerController containerController = getContainerController();
        System.out.println( "Launching the local agents in the container" + containerController.getContainerName() + " under " + getName() );
        String agentUUID = String.valueOf( UUID.randomUUID() ).split( "-" )[0];
        AID featureExtractorAgentId = createAgent( containerController, "featExtractor" + agentUUID, FeatureExtractorAgent.class.getCanonicalName(), new Object[0] );
        featureExtractorAgents = new AID[]{featureExtractorAgentId};
        playerAgent = createAgent( containerController, "musicPlayer" + agentUUID, PlayerAgent.class.getCanonicalName(), new Object[0] );

        // Load data from files
        musicLibrary = FileDb.getSongCollections();

        // LOAD GUI
        // init gui
        // set this in  to gui
        MainFrame.startGui( this );
        System.out.println( ">>>>>>>>>> GUI started" );

        DbConnector.getConnection();
        super.init();
        djAgent = DjAgent.getAgents( this );
        //        musicLibrary = new HashMap<String, List<Double>>();
        //        featureExtractorAgents = FeatureExtractorAgent.getAgents( this );
        //        playerAgent = PlayerAgent.getAgents( this );
        //        List<String> musicFolders = new ArrayList<String>();
        //        musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\VH1 100 Greatest Songs of 80s" ) );
        //        musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\VH1s_100_Greatest_Songs_Of_The_90s_-_KoD-2008" ) );
        //        musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\Billboard Top 100 Songs of Decade 2000-2009" ) );
        //        //            musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\classics" ) );
        //        musicFolders.add( new String( "D:\\shared music\\mp3-3" ) );
        //        musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\ChartHitz" ) );
        //        musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\#English DJ Song" ) );
        //        //        musicFolders.add( new File( "F:\\Ent\\ForPrabu\\ChaminClassics" ) );
        //
        //        // setup listners in the environment
        //        addListener( "Listener80", 0, musicFolders );
        //        addListener( "Listener90", 1, musicFolders );
        //        addListener( "Listener00", 2, musicFolders );
        //        addListener( "ListenerCollection", 3, musicFolders );
        //        addListener( "ListenerChartHitz", 4, musicFolders );
        //        addListener( "ListenerDJCollection", 5, musicFolders );


        /*for( int i = 0; i < Constants.CALCULATION_USED_FEATURES.length; i++ )
        {
            featureMaxValues[i] = Double.MIN_VALUE;
            featureMinValues[i] = Double.MAX_VALUE;
        }
        // Load existing data to library
        try
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
        }

        updateListenerModels();*/

    }

    public String[] saveMusicLibrary()
    {
        return FileDb.saveSongCollection( musicLibrary );
    }

    /**
     * Check for new songs to be sent to extractor
     */
    @Override protected void addBehaviours()
    {

        final Set<String> songsSentToExtraction = new HashSet<String>();
        // Handle proposals on possible new song information messages
        // TODO: Receive new songs and collection messages from GUI instead of GUI directly adding songs to library by sharing lib obj
        addBehaviour( new CyclicBehaviour()
        {
            // Look for new songs that features are not extracted and
            @Override public void action()
            {
                for( SongCollection songCollection : musicLibrary )
                {
                    //                    if( songCollection.getSongsList() != null )
                    //                    {
                    for( Song song : songCollection.getSongsList() )
                    {
                        if( ( song.getFeatures() == null || song.getFeatures().isEmpty() ) && !songsSentToExtraction.contains( song.getPath() ) )
                        {
                            // send to feature extractor
                            System.out.println( "Sending to feature extractor : " + song.getName() + ", " + song.getPath() );
                            songsSentToExtraction.add( song.getPath() );

                            ACLMessage newSongInform = new ACLMessage( ACLMessage.REQUEST );
                            newSongInform.addReceiver( featureExtractorAgents[0] );
                            newSongInform.setContent( song.getPath() );   // Can also send byte arrays, serializable objects
                            newSongInform.setConversationId( Constants.FEATURE_EXTRACTION_PROPOSAL );
                            newSongInform.setReplyWith( song.getPath() );
                            myAgent.send( newSongInform );
                            log( myAgent.getName(), "Sent request to extract features from ", song.getPath() );
                        }
                    }
                    //                    }
                }
                // Check messages for new songs
                /*MessageTemplate newSongProposalMt = MessageTemplate.MatchPerformative( ACLMessage.PROPOSE );
                ACLMessage msg = myAgent.receive( newSongProposalMt );
                if( msg != null )
                {
                    // Message received. Process it
                    String song = msg.getContent();
                    MusicUsAgent.log( getAID().getLibraryName(), "MusicLibraryAgent ", getAID().getLibraryName(), " received song ", song );

                    // if message contains a song that library doesn't contain, send it to analyser
                    if( !musicLibrary.containsKey( song ) )
                    {
                        musicLibrary.put( song, null );

                        if( featureExtractorAgents.length != 0 )
                        {
                            ACLMessage newSongInform = new ACLMessage( ACLMessage.REQUEST );
                            newSongInform.addReceiver( featureExtractorAgents[0] );
                            newSongInform.setContent( song );   // Can also send byte arrays, serializable objects
                            newSongInform.setConversationId( Constants.FEATURE_EXTRACTION_PROPOSAL );
                            newSongInform.setReplyWith( song );
                            myAgent.send( newSongInform );
                            log( myAgent.getLibraryName(), "Sent request to extract features from ", song );
                        }
                        else
                        {
                            // Response back with error
                        }
                    }
                }
                else
                {
                    block();
                }*/
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
                    String songPath = msg.getInReplyTo();
                    String featureArff = msg.getContent();
                    log( getName(), "Received features for ", songPath );

                    List<String> attributesList = new ArrayList<String>();
                    if( featureArff != null && !featureArff.isEmpty() )
                    {
                        boolean dataTagFound = false;

                        String[] featureArffRows = featureArff.split( "\n" );
                        for( String featureArffRow : featureArffRows )
                        {
                            if( featureArffRow.startsWith( "@ATTRIBUTE" ) )
                            {
                                int featureNameStart = featureArffRow.indexOf( "\"" );
                                String featureName = featureArffRow.substring( featureNameStart + 1, featureArffRow.indexOf( "\"", featureNameStart + 1 ) );
                                // get attributes
                                attributesList.add( featureName );
                            }

                            if( dataTagFound )
                            {
                                if( !featureArffRow.isEmpty() )
                                {
                                    Song song = Song.getSongInstance( songPath );
                                    String[] featureVals = featureArffRow.trim().split( "," );
                                    for( int featureValNo = 0; featureValNo < featureVals.length; featureValNo++ )
                                    {
                                        String featureVal = featureVals[featureValNo];
                                        Feature feature = new Feature( songPath, attributesList.get( featureValNo ), Double.parseDouble( !"NaN".equals( featureVal ) ? featureVal : "0.0" ) );
                                        song.getFeatures().put( feature.getName(), feature );
                                    }

                                    libraryUpdatesToBeSentToDj = true;      // Send library to DJ with new features
                                }
                            }

                            if( featureArffRow.startsWith( "@DATA" ) )
                            {
                                dataTagFound = true;
                            }
                        }
                    }


                    // get data
                    //                    int dataStart = featureArff.indexOf( DATA_TAG );
                    //                    System.out.println( "dataStart >" + dataStart );
                    //                    if( dataStart > -1 )
                    //                    {
                    //                        String data = featureArff.substring( dataStart + DATA_TAG.length() ).trim().replaceAll( "\n", "" );
                    //                        System.out.println( ">>>>>Data list>>" + data );
                    //
                    //                        // append to song file
                    //                        try
                    //                        {
                    //                            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( EXTRACTED_SONGS_FILE, true ) ) );
                    //                            out.println( songPath );
                    //                            out.close();
                    //                        }
                    //                        catch( IOException e )
                    //                        {
                    //                            e.printStackTrace();
                    //                        }
                    //
                    //                        // append to feature file
                    //                        try
                    //                        {
                    //                            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( EXTRACTED_FEATURE_VALUES_FILE, true ) ) );
                    //                            out.println( data );
                    //                            out.close();
                    //                        }
                    //                        catch( IOException e )
                    //                        {
                    //                            e.printStackTrace();
                    //                        }
                    //                    }
                }

            }
        } );

        // Print Library
        //        addBehaviour( new TickerBehaviour( this, (long) ( Constants.LIBRARY_SCAN_INTERVAL * 60 * 1000 ) )
        //        {
        //            @Override protected void onTick()
        //            {
        //                printLibrary();
        //            }
        //        } );

        // If lib is updated, send the lib to DJ
        addBehaviour( new TickerBehaviour( this, (long) ( 1 * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                if( libraryUpdatesToBeSentToDj && djAgent != null )
                {
                    // locally save music library by serializing to files
                    String[] fileNames = saveMusicLibrary();

                    for( String savedFileName : fileNames )
                    {
                        if( savedFileName != null && !savedFileName.isEmpty() )
                        {
                            String fileText = "";
                            try
                            {
                                fileText = new String( Files.readAllBytes( Paths.get( savedFileName ) ) );
                            }
                            catch( IOException e )
                            {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }


                            // send lib file to DJ
                            ACLMessage updatedLibraryDataFileMsg = new ACLMessage( ACLMessage.INFORM );
                            updatedLibraryDataFileMsg.addReceiver( djAgent );
                            updatedLibraryDataFileMsg.setConversationId( savedFileName );
                            updatedLibraryDataFileMsg.setContent( fileText );   // Can also send byte arrays, serializable objects
                            updatedLibraryDataFileMsg.setReplyWith( savedFileName + System.currentTimeMillis() );
                            myAgent.send( updatedLibraryDataFileMsg );
                        }
                    }

                    libraryUpdatesToBeSentToDj = false;
                }
            }
        } );

        // scan for feature extract agents
//        addBehaviour( new TickerBehaviour( this, (long) ( Constants.SCAN_FOR_AGENTS_INTERVAL * 60 * 1000 ) )
//        {
//            @Override protected void onTick()
//            {
//                // No need of featureExtractorAgents to be accessed with synchronized even though it is been accessed by multiple behaviours.
//                // As those behaviours are within same agent, all of those behaviours run in same thread. So no concurrency violence.
//                featureExtractorAgents = FeatureExtractorAgent.getAgents( myAgent );
//            }
//        } );

        // scan for player agent
//        addBehaviour( new TickerBehaviour( this, (long) ( Constants.SCAN_FOR_AGENTS_INTERVAL * 60 * 1000 ) )
//        {
//            @Override protected void onTick()
//            {
//                playerAgent = PlayerAgent.getAgents( myAgent );
//            }
//        } );

        // scan for dj agent
        addBehaviour( new TickerBehaviour( this, (long) ( Constants.SCAN_FOR_AGENTS_INTERVAL * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                djAgent = DjAgent.getAgents( myAgent );
            }
        } );

        // Update listner models
//        addBehaviour( new TickerBehaviour( this, (long) ( Constants.LISTNER_MODEL_UPDATE_INTERVAL * 60 * 1000 ) )
//        {
//            @Override protected void onTick()
//            {
//                updateListenerModels();
//            }
//        } );

        // Parse PLAY request from DJ to lib's player agent
        addBehaviour( new CyclicBehaviour()
        {
            @Override public void action()
            {
                MessageTemplate performativeMt = MessageTemplate.MatchPerformative( ACLMessage.REQUEST );
                MessageTemplate conversationIdMt = MessageTemplate.MatchConversationId( Constants.PLAY_REQUEST );
                MessageTemplate performativeConversationIdMt = MessageTemplate.and( performativeMt, conversationIdMt );
                ACLMessage msg = myAgent.receive( conversationIdMt );
                if( msg != null )       // TODO : get the last entry in the queue and play
                {
                    ACLMessage newSongInform = new ACLMessage( ACLMessage.REQUEST );
                    newSongInform.addReceiver( playerAgent );
                    newSongInform.setContent( msg.getContent() );   // Can also send byte arrays, serializable objects
                    newSongInform.setConversationId( Constants.PLAY_REQUEST );
                    newSongInform.setReplyWith( msg.getInReplyTo() );
                    myAgent.send( newSongInform );
                    log( myAgent.getName(), "Sent request to play ", msg.getContent(), " to ", playerAgent.getName() );

                }
                else
                {
                    block();
                }
            }
        } );

        // Parse STOP request from DJ to lib's player agent
        addBehaviour( new CyclicBehaviour()
        {
            @Override public void action()
            {
                MessageTemplate performativeMt = MessageTemplate.MatchPerformative( ACLMessage.REQUEST );
                MessageTemplate conversationIdMt = MessageTemplate.MatchConversationId( Constants.STOP_REQUEST );
                MessageTemplate performativeConversationIdMt = MessageTemplate.and( performativeMt, conversationIdMt );
                ACLMessage msg = myAgent.receive( conversationIdMt );
                if( msg != null )       // TODO : get the last entry in the queue and play
                {
                    ACLMessage stopSongRequest = new ACLMessage( ACLMessage.REQUEST );
                    stopSongRequest.addReceiver( playerAgent );
                    stopSongRequest.setContent( msg.getContent() );   // Can also send byte arrays, serializable objects
                    stopSongRequest.setConversationId( Constants.STOP_REQUEST );
                    stopSongRequest.setReplyWith( msg.getInReplyTo() );
                    myAgent.send( stopSongRequest );
                    log( myAgent.getName(), "Sent request to stop ", msg.getContent(), " to ", playerAgent.getName() );

                }
                else
                {
                    block();
                }
            }
        } );

        // Play songs
        /*addBehaviour( new TickerBehaviour( this, (long) ( 0.5 * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                double selectedDistance = Long.MAX_VALUE;
                Map.Entry<String, List<Double>> selectedSong = null;
                for( Listener listener : listeners )
                {
                    log( Constants.LOG_IMPORTANT, getLibraryName(), listener.getLibraryName(), " MSL : ", String.valueOf( listener.getMSL() ) );
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
                        log( getLibraryName(), "For ", listener.getLibraryName(), " totDistance : ", String.valueOf( totDistances ) );
                        listenerNo++;
                    }
//                    log( Constants.LOG_IMPORTANT, getLibraryName(), "Total Distances ", Arrays.toString( distances ), " = ", String.valueOf( totDistances ), " for : ", libraryEntry.getKey().substring( libraryEntry.getKey().lastIndexOf( "\\" ) ) );

                    if( selectedDistance > totDistances && !lastPlayedQueue.contains( libraryEntry.getKey() ) )
                    {
                        log( Constants.LOG_IMPORTANT, getLibraryName(), "Total Distances ", Arrays.toString( distances ), " = ", String.valueOf( totDistances ), " for : ", libraryEntry.getKey().substring( libraryEntry.getKey().lastIndexOf( "\\" ) ) );
                        selectedDistance = totDistances;
                        selectedSong = libraryEntry;
                        log( Constants.LOG_IMPORTANT, getLibraryName(), "Selected song ", selectedSong.getKey() );
                    }
                }

                if( selectedSong != null )
                {
                    // Play song
                    lastPlayedQueue.add( selectedSong.getKey() );
                    log( getLibraryName(), "Playing selected song ", selectedSong.getKey() );
                    if( playerAgents != null && playerAgents.length != 0 )
                    {
                        ACLMessage newSongInform = new ACLMessage( ACLMessage.REQUEST );
                        newSongInform.addReceiver( playerAgents[0] );
                        newSongInform.setContent( selectedSong.getKey() );   // Can also send byte arrays, serializable objects
                        newSongInform.setConversationId( Constants.PLAY_REQUEST );
                        newSongInform.setReplyWith( Constants.PLAY_REQUEST + System.currentTimeMillis() );
                        myAgent.send( newSongInform );
                        log( myAgent.getLibraryName(), "Sent request to play ", selectedSong.getKey() );

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
                        log( Constants.LOG_IMPORTANT, getLibraryName(), "Updating MSL for ", listener.getLibraryName(), " MSL value: ", String.valueOf( listenerMSL ), " / ",  String.valueOf( distanceFromSongToListener ), " = ", String.valueOf( listener.getMSL() ) );

                    }
                    // Rearrange MSL values to be 0 - 1 with max val as 1 (but don't change min val to 0)
                    for( Listener listener : listeners )
                    {
                        double listenerMSL = listener.getMSL();
                        listener.setMSL( listenerMSL / maxMSLVal );
                        log( Constants.LOG_IMPORTANT, getLibraryName(), "Normalized MSL for ", listener.getLibraryName(), " MSL value: ", String.valueOf( listener.getMSL() ) );
                    }
                }

            }
        } );*/
    }

//    private void updateListenerModels()
//    {
//        /*for( Listener listener : listeners )
//        {
//            listener.updateListenersLibrary( musicLibrary );
//            listener.updateSongPreference();
//        } */
//    }

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
        /*MusicUsAgent.log( getAID().getLibraryName(), "############ Music Library ############" );
        for( Map.Entry<String, List<Double>> stringListEntry : musicLibrary.entrySet() )
        {
            StringBuilder logSb = new StringBuilder();
            logSb.append( stringListEntry.getKey() );
            for( Double featureVal : stringListEntry.getValue() )
            {
                logSb.append( "," );
                logSb.append( featureVal );
            }
            MusicUsAgent.log( getAID().getLibraryName(), logSb.toString() );
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

    public boolean isLibraryUpdatesToBeSentToDj()
    {
        return libraryUpdatesToBeSentToDj;
    }

    public void setLibraryUpdatesToBeSentToDj( boolean libraryUpdatesToBeSentToDj )
    {
        this.libraryUpdatesToBeSentToDj = libraryUpdatesToBeSentToDj;
    }
}
