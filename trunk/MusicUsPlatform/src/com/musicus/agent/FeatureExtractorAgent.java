package com.musicus.agent;

import jAudioFeatureExtractor.ACE.DataTypes.Batch;
import jAudioFeatureExtractor.ACE.XMLParsers.XMLDocumentParser;
import jAudioFeatureExtractor.CommandLineThread;
import jAudioFeatureExtractor.DataModel;
import jAudioFeatureExtractor.DataTypes.RecordingInfo;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

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
                    log( myAgent.getName(), "##### Extracting features from song : ", song );
                    String extractedFeatures = getFuturesExtracted( new String[]{song} );
                    log( myAgent.getName(), extractedFeatures );
                }
                else
                {
                    block();
                }

            }
        } );
    }

    @Override protected void init()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override protected String getAgentType()
    {
        return getAgentTypeCode();
    }

    public static String getAgentTypeCode()
    {
        return Constants.FEATURE_EXTRACTOR;
    }

    public static AID[] getAgents( Agent callFromAgent )
    {
        return MusicUsAgent.getAgents( getAgentTypeCode(), callFromAgent );
    }

    private String getFuturesExtracted( String[] songList )
    {

        String batchFilePath = Constants.JAUDIO_BATCH_FILE_XML;
        File batch = new File( batchFilePath );
        if( !batch.exists() )
        {
            System.out.println( "Batch file '" + batchFilePath + "' does not exist" );
            //            System.exit( 2 );
        }
        else
        {

            Object[] o = new Object[]{};
            try
            {
                o = (Object[]) XMLDocumentParser.parseXMLDocument( batchFilePath, "batchFile" );
            }
            catch( Exception e )
            {
                System.out.println( "Error parsing the batch file" );
                System.out.println( e.getMessage() );
                System.exit( 3 );
            }
            for( int i = 0; i < o.length; ++i )
            {
                Batch b = (Batch) o[i];
                DataModel dm = new DataModel( "jAudioFeatures.xml", null );
                try
                {
                    int rowCount = songList.length;
                    RecordingInfo[] recordingInfos = new RecordingInfo[rowCount];
                    for( int row = 0; row < rowCount; row++ )
                    {
                        String path = songList[ row ];
                        RecordingInfo recordingInfo = new RecordingInfo( path );
                        recordingInfos[row] = recordingInfo;
                    }
                    b.setRecording( recordingInfos );

                    //                    dm.featureKey = new FileOutputStream( new File( b.getDestinationFK() ) );
                    //                    dm.featureValue = new FileOutputStream( new File( b.getDestinationFV() ) );
                    dm.featureKey = new ByteArrayOutputStream();
                    dm.featureValue = new ByteArrayOutputStream();
                    b.setDataModel( dm );
                    CommandLineThread clt = new CommandLineThread( b );
                    clt.start();
                    while( clt.isAlive() )
                    {
                        if( System.in.available() > 0 )
                        {
                            clt.cancel();
                        }
                        clt.join( 1000 );
                    }

                    return new String(((ByteArrayOutputStream)dm.featureValue).toByteArray(), "UTF-8");
                }
                catch( Exception e )
                {
                    System.out.println( "Error in execution - skipping this batch (" + b.getName() + ")" );
                    e.printStackTrace();
                }
            }
        }

        return "";
    }
}
