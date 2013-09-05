package com.musicus.agent.behaviour;

import com.musicus.agent.Constants;
import com.musicus.agent.MusicUsAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/3/13
 * Time: 1:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class SearchUpdatesInLibBehaviour extends TickerBehaviour
{
    List<File> musicFolders = new ArrayList<File>();

    public SearchUpdatesInLibBehaviour( Agent a, long period )
    {
        super( a, period );
//        musicFolders.add( new File( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\VH1s_100_Greatest_Songs_Of_The_90s_-_KoD-2008" ) );
//        musicFolders.add( new File( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\VH1 100 Greatest Songs of 80s" ) );
//        musicFolders.add( new File( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\Billboard Top 100 Songs of Decade 2000-2009" ) );
        musicFolders.add( new File( "F:\\Ent\\ForPrabu\\ChaminClassics" ) );
    }

    @Override protected void onTick()
    {
        MusicUsAgent.log( myAgent.getAID().getName(), "Checking for new songs" );
        // find music library
        AID[] musicLibraries = MusicUsAgent.getAgents( MusicLibraryAgent.get );

        if( musicLibraries.length != 0 )
        {
            // find new songs
            for( File musicFolder : musicFolders )
            {
                MusicUsAgent.log( myAgent.getAID().getName(), "Checking music in ", musicFolder.getAbsolutePath() );
                FilenameFilter mp3FileFilter = new FilenameFilter()
                {
                    @Override public boolean accept( File dir, String name )
                    {
                        return name.toLowerCase().endsWith( Constants.MP3_EXTENTION );
                    }
                };
                for( File file : musicFolder.listFiles( mp3FileFilter ) )
                {
                    String newMusicFile = file.getAbsolutePath();
                    MusicUsAgent.log( myAgent.getAID().getName(), "Detected file : ", newMusicFile );

                    sendMessageMusicLibrary( musicLibraries, newMusicFile );
                }
            }
        }
        else
        {
            MusicUsAgent.log( myAgent.getAID().getName(), "No music libraries found!!!" );
        }
    }

    private void sendMessageMusicLibrary( AID[] musicLibraries, String newMusicFile )
    {
        ACLMessage newSongInform = new ACLMessage( ACLMessage.INFORM );
        for( AID musicLibrary : musicLibraries )
        {
            newSongInform.addReceiver( musicLibrary );
        }
        newSongInform.setContent( newMusicFile );   // Can also send byte arrays, serializable objects
        newSongInform.setConversationId( Constants.NEW_MUSIC_INFORM );
        newSongInform.setReplyWith( Constants.NEW_MUSIC_INFORM + System.currentTimeMillis() );
        myAgent.send( newSongInform );
    }
}
