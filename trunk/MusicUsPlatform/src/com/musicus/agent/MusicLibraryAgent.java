package com.musicus.agent;

import com.musicus.agent.behaviour.SearchUpdatesInLibBehaviour;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/3/13
 * Time: 7:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicLibraryAgent extends Agent
{
    private Map<String, Object> musicLibrary;   // use a hash table for order preservation. Object is a Song object of features list

    @Override protected void setup()
    {
        System.out.println( "Hello! MusicLibraryAgent " + getAID().getName() + " is ready." );
        musicLibrary = new HashMap<String, Object>();

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName( getAID() );
        ServiceDescription sd = new ServiceDescription();
        sd.setType( Constants.MUSIC_LIBRARY );
        sd.setName( Constants.MUSIC_LIBRARY + getAID().getName() );
        dfd.addServices( sd );

        try
        {
            DFService.register( this, dfd );
        }
        catch( FIPAException fe )
        {
            fe.printStackTrace();
        }

        addBehaviour( new CyclicBehaviour( )
        {
            @Override public void action()
            {
                // Check messages for new songs
                ACLMessage msg = myAgent.receive();
                if( msg != null )
                {
                    // Message received. Process it
                    String song = msg.getContent();
                    System.out.println( "MusicLibraryAgent " + getAID().getName() + " received song " + song );

                    // if message contains a song that library doesn't contain, send it to analyser
                    if( !musicLibrary.containsKey( song ) )
                    {
                        musicLibrary.put( song, null );
                    }
                }
                else
                {
                    block();
                }
            }
        } );

        addBehaviour( new TickerBehaviour( this, 2* 60 * 1000)
        {
            @Override protected void onTick()
            {
                System.out.println( "############ Music Library ############" );
                for( Map.Entry<String, Object> musicEntry : musicLibrary.entrySet() )
                {
                    System.out.println( "##" + musicEntry.getKey() );
                }
            }
        } );
    }

    @Override protected void takeDown()
    {
        try
        {
            DFService.deregister( this );
        }
        catch( FIPAException fe )
        {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println( "Seller - agent " + getAID().getName() + "terminating." );
    }
}
