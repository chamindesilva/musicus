package com.musicus.agent;

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

    @Override protected void addBehaviours()
    {
//        addBehaviour( new SearchUpdatesInLibBehaviour( this, 60 * 1000 ) );
    }

    @Override protected String getAgentType()
    {
        return getAgentTypeCode();
    }

    public static String getAgentTypeCode()
    {
        return Constants.FEATURE_EXTRACTOR;
    }
}
