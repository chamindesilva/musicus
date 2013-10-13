package com.musicus.agent;

import com.musicus.db.FileDb;
import com.musicus.db.SongCollection;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 10/13/13
 * Time: 7:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class DjAgent extends MusicUsAgent
{
    Map<String, ConnectedPlatForm> connectedLibraries = new HashMap<String, ConnectedPlatForm>();

    @Override protected String getAgentType()
    {
        return getAgentTypeCode();
    }

    public static String getAgentTypeCode()
    {
        return Constants.DJ;
    }

    @Override protected void addBehaviours()
    {
        // receive data files froom connected platforms and update libraries
        addBehaviour( new CyclicBehaviour()
        {
            @Override public void action()
            {
                MessageTemplate updatedLibFileInfo = MessageTemplate.MatchPerformative( ACLMessage.INFORM );
                ACLMessage msg = myAgent.receive( updatedLibFileInfo );
                if( msg != null )
                {
                    AID sender = msg.getSender();
                    String senderName = sender.getName();
                    String savedFileName = msg.getConversationId();
                    String fileText = msg.getContent();

                    //write to agent's dir
                    File senderDataDir = new File( senderName );
                    if( !senderDataDir.exists() )
                    {
                        if( senderDataDir.mkdirs() )
                        {
                            // save to file
                            outputToFile( savedFileName, fileText );
                        }
                        else
                        {
                            System.out.println( ">>>>>>> ERROR IN SAVING DATA TO FILE BY DJ ::: " + senderName + " : " + senderDataDir.getAbsolutePath() );
                        }
                    }
                    else
                    {
                        // save to file
                        outputToFile( savedFileName, fileText );
                    }

                    if( !connectedLibraries.containsKey( senderName ) )
                    {
                        ConnectedPlatForm connectedPlatForm = new ConnectedPlatForm();
                        connectedPlatForm.setLibrary( sender );
                        connectedLibraries.put( senderName, connectedPlatForm );
                    }
                    ConnectedPlatForm connectedPlatForm = connectedLibraries.get( senderName );
                    connectedPlatForm.incrementReceivedFiles();
                    if( connectedPlatForm.getReceivedFiles() == FileDb.TOTAL_FILE_COUNT_FOR_DB )
                    {
                        List<SongCollection> musicLibrary = FileDb.getSongCollections( senderName );
                        connectedPlatForm.setMusicLibraryCollection( musicLibrary );
                    }

                }
                else
                {
                    block();
                }
            }

        } );

        // Select song to play
        addBehaviour( new TickerBehaviour( this, (long) ( 0.5 * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {

            }
        });
    }

    private void outputToFile( String fileName, String text )
    {
        PrintStream out = null;
        try
        {
            out = new PrintStream( new FileOutputStream( fileName ) );
            out.print( text );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            if( out != null )
            {
                out.close();
            }
        }
    }

    public static AID getAgents( Agent callFromAgent )
    {
        AID[] djAgents = MusicUsAgent.getAgents( getAgentTypeCode(), callFromAgent );
        return djAgents != null && djAgents.length > 0 ? djAgents[0] : null;
    }


    class ConnectedPlatForm
    {
        private AID library;
        private int receivedFiles;
        private List<SongCollection> musicLibraryCollection;

        AID getLibrary()
        {
            return library;
        }

        void setLibrary( AID library )
        {
            this.library = library;
        }

        int getReceivedFiles()
        {
            return receivedFiles;
        }

        void setReceivedFiles( int receivedFiles )
        {
            this.receivedFiles = receivedFiles;
        }

        void incrementReceivedFiles()
        {
            this.receivedFiles++;
        }

        List<SongCollection> getMusicLibraryCollection()
        {
            return musicLibraryCollection;
        }

        void setMusicLibraryCollection( List<SongCollection> musicLibraryCollection )
        {
            this.musicLibraryCollection = musicLibraryCollection;
        }
    }
}
