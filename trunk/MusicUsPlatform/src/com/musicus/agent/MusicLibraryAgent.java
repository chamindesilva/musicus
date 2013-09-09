package com.musicus.agent;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, List<Double>> musicLibrary;   // use a hash table for order preservation. Object is a Song object of features list
    private AID[] featureExtractorAgents;
    private List<Listener> listeners = new ArrayList<Listener>();

    @Override protected void init()
    {
//        musicLibrary = new HashMap<String, Map<String, Double>>();
        musicLibrary = new HashMap<String, List<Double>>(  );
        featureExtractorAgents = FeatureExtractorAgent.getAgents( this );
        List<String> musicFolders = new ArrayList<String>(  );
        //            musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\VH1 100 Greatest Songs of 80s" ) );
        //            musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\VH1s_100_Greatest_Songs_Of_The_90s_-_KoD-2008" ) );
        //            musicFolders.add( new String( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\Billboard Top 100 Songs of Decade 2000-2009" ) );
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


        // Load existing data
        try
        {
            List<String> extractedSongsList = readFileToList( EXTRACTED_SONGS_FILE );
            List<String> extractedFeatureList = readFileToList( EXTRACTED_FEATURE_VALUES_FILE );
            for(int songNo = 0; songNo < extractedSongsList.size() ; songNo++)
            {
                String featureLineStr = extractedFeatureList.get( songNo );
                String[] featureStrArr = featureLineStr.split( "," );
                List<Double> features = new ArrayList<Double>(  );
                for( String featureVal : featureStrArr )
                {
                    features.add( new Double( featureVal ) );
                }
                musicLibrary.put( extractedSongsList.get( songNo ), features );
            }

            for( Map.Entry<String, List<Double>> stringListEntry : musicLibrary.entrySet() )
            {
                System.out.print( "\n" + stringListEntry.getKey() );
                for( Double aDouble : stringListEntry.getValue() )
                {
                    System.out.print(","+aDouble);
                }
            }
        }
        catch( IOException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

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
                            log( myAgent.getName(), "Sent request to extract features from ", song );
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
                            //oh noes!
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
                            //oh noes!
                        }
                    }
                }

            }
        } );

        //
        addBehaviour( new TickerBehaviour( this, (long) ( Constants.LIBRARY_SCAN_INTERVAL * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                MusicUsAgent.log( getAID().getName(), "############ Music Library ############" );
                for( Map.Entry<String, List<Double>> musicEntry : musicLibrary.entrySet() )
                {
                    MusicUsAgent.log( getAID().getName(), "##", musicEntry.getKey() );
                }
            }
        } );

        addBehaviour( new TickerBehaviour( this, (long) ( Constants.SCAN_FOR_AGENTS_INTERVAL * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                // No need of featureExtractorAgents to be accessed with synchronized even though it is been accessed by multiple behaviours.
                // As those behaviours are within same agent, all of those behaviours run in same thread. So no concurrency violence.
                featureExtractorAgents = FeatureExtractorAgent.getAgents( myAgent );
            }
        } );

        addBehaviour( new TickerBehaviour( this, (long) ( Constants.LISTNER_MODEL_UPDATE_INTERVAL * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                for( Listener listener : listeners )
                {
                    listener.updateListenersLibrary( musicLibrary );
                    listener.updateSongPreference();
                }
            }
        } );
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
}
