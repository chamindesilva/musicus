package com.musicus.agent;

import jAudioFeatureExtractor.ACE.DataTypes.Batch;
import jAudioFeatureExtractor.ACE.XMLParsers.XMLDocumentParser;
import jAudioFeatureExtractor.CommandLineThread;
import jAudioFeatureExtractor.DataModel;
import jAudioFeatureExtractor.DataTypes.RecordingInfo;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/5/13
 * Time: 3:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class FeatureExtractorAgent extends MusicUsAgent
{
    // TODO : Music library maintains a list(queue) of songs to be feature extracted.
    // Feature extract agents request and get a song to analyse and submit results to lib one after the other.
    // then there can be mainy extract agents in the network processing library's queue.


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
                    String extractedFeatures = "";
                    try
                    {
                        extractedFeatures = getFeaturesExtracted( new String[]{song} );
                    }
                    catch( Exception e )
                    {
                        e.printStackTrace();
                    }
                    log( myAgent.getName(), "##### Calling GC" );
                    System.gc();
                    // TODO: get the stream and send to parser
                    log( myAgent.getName(), extractedFeatures );

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative( ACLMessage.INFORM );
                    reply.setContent( extractedFeatures );
                    myAgent.send( reply );
                    /*try
                    {
                        DataSet[] dataSets = ExtendedDataSet.parseDataSetXml( extractedFeatures );
                        for( DataSet dataSet : dataSets )
                        {
                            System.out.println( "identifier : " + dataSet.identifier );
                            System.out.println( "feature_names : " + dataSet.feature_names );
                            System.out.println( "feature_values : " + dataSet.feature_values );
                        }
                    }
                    catch( Exception e )
                    {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }*/

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
        super.init();
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

    /**
     * Only extracts features of one song at a time
     * @param songList
     * @return
     */
    private String getFeaturesExtracted( String[] songList )
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
                o = (Object[]) XMLDocumentParser.parseXMLDocument( batchFilePath, Constants.BATCH_FILE );
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
                        String path = songList[row];
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

                    return new String( ( (ByteArrayOutputStream) dm.featureValue ).toByteArray(), "UTF-8" );
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
