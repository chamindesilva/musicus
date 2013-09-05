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

    @Override protected void init()
    {
        musicLibrary = new HashMap<String, Map<String, Double>>();
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

                        ACLMessage newSongInform = new ACLMessage( ACLMessage.INFORM );
                        for( AID musicLibrary : musicLibrary )
                        {
                            newSongInform.addReceiver( musicLibrary );
                        }
                        newSongInform.setContent( newMusicFile );   // Can also send byte arrays, serializable objects
                        newSongInform.setConversationId( Constants.NEW_MUSIC_INFORM );
                        newSongInform.setReplyWith( Constants.NEW_MUSIC_INFORM + System.currentTimeMillis() );
                        myAgent.send( newSongInform );
                    }
                }
                else
                {
                    block();
                }
            }
        } );

        //
        addBehaviour( new TickerBehaviour( this, (long) ( 1.0001 * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                MusicUsAgent.log( getAID().getName(), "############ Music Library ############" );
                for( Map.Entry<String, Object> musicEntry : musicLibrary.entrySet() )
                {
                    MusicUsAgent.log( getAID().getName(), "##", musicEntry.getKey() );
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
}
