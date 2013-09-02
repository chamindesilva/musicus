package com.musicus.agent.behaviour;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/3/13
 * Time: 1:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class SearchUpdatesInLibBehaviour extends TickerBehaviour
{
    public SearchUpdatesInLibBehaviour( Agent a, long period )
    {
        super( a, period );
    }

    @Override protected void onTick()
    {
        System.out.println("Tick");
    }
}
