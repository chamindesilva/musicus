package com.musicus.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
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
    private Map<String, Map<String, Double>> musicLibrary;   // use a hash table for order preservation. Object is a Song object of features list
    private AID[] featureExtractorAgents;

    @Override protected void init()
    {
        musicLibrary = new HashMap<String, Map<String, Double>>();
        featureExtractorAgents = FeatureExtractorAgent.getAgents( this );
    }

    @Override protected void addBehaviours()
    {
        // Handle information on possible new song information messages
        addBehaviour( new CyclicBehaviour()
        {
            @Override public void action()
            {
                // Check messages for new songs
                ACLMessage msg = myAgent.receive();
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
                            newSongInform.setReplyWith( Constants.FEATURE_EXTRACTION_PROPOSAL + System.currentTimeMillis() );
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

        //
        addBehaviour( new TickerBehaviour( this, (long) ( Constants.LIBRARY_SCAN_INTERVAL * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                MusicUsAgent.log( getAID().getName(), "############ Music Library ############" );
                for( Map.Entry<String, Map<String, Double>> musicEntry : musicLibrary.entrySet() )
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
}
