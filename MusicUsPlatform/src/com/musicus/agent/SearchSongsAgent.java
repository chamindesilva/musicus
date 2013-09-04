package com.musicus.agent;

import com.musicus.agent.behaviour.SearchUpdatesInLibBehaviour;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/2/13
 * Time: 10:33 PM
 * java -cp lib\jade.jar;classes jade.Boot -gui -agents songAnal:com.musicus.agent.SearchSongsAgent
 * java -cp lib\jade.jar;MusicUs\MusicUsPlatform.jar jade.Boot -gui -agents songAnal:com.musicus.agent.SearchSongsAgent
 * java -cp lib\jade.jar;MusicUs\MusicUsPlatform.jar jade.Boot -gui -agents songAnal:com.musicus.agent.SearchSongsAgent;musicLib:com.musicus.agent.MusicLibraryAgent
 */
public class SearchSongsAgent extends MusicUsAgent
{
    @Override protected void addBehaviours()
    {
        addBehaviour( new SearchUpdatesInLibBehaviour( this, 60 * 1000 ) );
    }

    @Override protected String getAgentType()
    {
        return Constants.MUSIC_SEARCH;
    }


}
