package com.musicus.agent;

import jade.core.Agent;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/5/13
 * Time: 3:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class FeatureExtractorAgent extends MusicUsAgent
{
    // Use FeatureExtractor MainFrame @ songsRebuildBtnActionPerformed()
    // change to dm.featureKey = new ByteArrayOutputStream();


    @Override protected void setup()
    {
        System.out.println( "Hello! FeatureExtractorAgent " + getAID().getName() + " is ready." );


    }
}
