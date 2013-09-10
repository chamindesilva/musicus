package com.musicus.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/5/13
 * Time: 4:09 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MusicUsAgent extends Agent
{

    @Override protected void setup()
    {
        if( Constants.DEBUG_MODE )
        try
        {
            Thread.sleep( Constants.DEBUG_DELAY_SEC );
        }
        catch( InterruptedException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        log( getAID().getName(), "Hello! ", getAID().getName(), " is starting." );
        registerAgent( getAgentType() );
        init();     // init check for other agent lists by registration, so agents should be completed registration before init()
        addBehaviours();
        log( getAID().getName(), "Hello! ", getAID().getName(), " is started." );
    }

    protected abstract void init();

    protected abstract String getAgentType();

    protected abstract void addBehaviours();


    protected void registerAgent( String agentType )
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName( getAID() );
        ServiceDescription sd = new ServiceDescription();
        sd.setType( agentType );
        sd.setName( agentType + getAID().getName() );
        dfd.addServices( sd );

        try
        {
            DFService.register( this, dfd );
        }
        catch( FIPAException fe )
        {
            fe.printStackTrace();
        }
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
        log( getAID().getName(), "Agent ", getAID().getName(), "terminating." );
    }

    public static AID[] getAgents( String agentCodeToSearch, Agent callFromAgent )
    {
        MusicUsAgent.log( callFromAgent.getName(), "############ Looking for ", agentCodeToSearch, " ############" );
        AID[] foundAgents = null;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType( agentCodeToSearch );
        template.addServices( sd );
        try
        {
            DFAgentDescription[] result = DFService.search( callFromAgent, template );
            foundAgents = new AID[result.length];
            for( int i = 0; i < result.length; ++i )
            {
                foundAgents[i] = result[i].getName();
                MusicUsAgent.log( callFromAgent.getName(), "## Found ", agentCodeToSearch,  " : ", foundAgents[i].getName() );
            }
        }
        catch( FIPAException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return foundAgents;
    }

    public static void log( String agentName, String... logParts )
    {
        /*if( logParts != null && logParts.length != 0 )
        {
            StringBuilder logSb = new StringBuilder();
            logSb.append( agentName );
            //            logSb.append( "\t:" );
            //            logSb.append( agentId );
            logSb.append( "\t:::" );
            for( String logPart : logParts )
            {
                logSb.append( logPart );
            }

            System.out.println( logSb.toString() );
        }*/
    }
}
