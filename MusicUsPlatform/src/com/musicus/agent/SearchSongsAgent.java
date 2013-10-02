package com.musicus.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/2/13
 * Time: 10:33 PM
 */
public class SearchSongsAgent extends MusicUsAgent
{
    private AID[] musicLibraries;

    @Override protected void addBehaviours()
    {
        addBehaviour( new SearchUpdatesInLibBehaviour( this, 30 * 1000 ) );

        addBehaviour( new TickerBehaviour( this, (long) ( Constants.SCAN_FOR_AGENTS_INTERVAL * 60 * 1000 ) )
        {
            @Override protected void onTick()
            {
                // No need of featureExtractorAgents to be accessed with synchronized even though it is been accessed by multiple behaviours.
                // As those behaviours are within same agent, all of those behaviours run in same thread. So no concurrency violence.
                updateMusicLibraryAgents();
            }
        } );
    }

    @Override protected void init()
    {
        super.init();
        updateMusicLibraryAgents();
    }

    @Override protected String getAgentType()
    {
        return getAgentTypeCode();
    }

    public static String getAgentTypeCode()
    {
        return Constants.MUSIC_SEARCH;
    }

    public static AID[] getAgents( Agent callFromAgent )
    {
        return MusicUsAgent.getAgents( getAgentTypeCode(), callFromAgent );
    }

    public AID[] getMusicLibraries()
    {
        return musicLibraries;
    }

    private void updateMusicLibraryAgents()
    {
        musicLibraries = MusicLibraryAgent.getAgents( this );
    }

    public void setMusicLibraries( AID[] musicLibraries )
    {
        this.musicLibraries = musicLibraries;
    }

    class SearchUpdatesInLibBehaviour extends TickerBehaviour
    {
        private List<File> musicFolders = new ArrayList<File>();
        private FilenameFilter mp3FileFilter = new FilenameFilter()
        {
            @Override public boolean accept( File dir, String name )
            {
                return name.toLowerCase().endsWith( Constants.MP3_EXTENTION );
            }
        };

        public SearchUpdatesInLibBehaviour( Agent a, long period )
        {
            super( a, period );
//            musicFolders.add( new File( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\VH1s_100_Greatest_Songs_Of_The_90s_-_KoD-2008" ) );
//            musicFolders.add( new File( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\VH1 100 Greatest Songs of 80s" ) );
//            musicFolders.add( new File( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\Billboard Top 100 Songs of Decade 2000-2009" ) );
//            musicFolders.add( new File( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\classics" ) );
            musicFolders.add( new File( "D:\\shared music\\mp3-3" ) );
            musicFolders.add( new File( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\ChartHitz" ) );
            musicFolders.add( new File( "D:\\shared music\\AUDIO SONGS\\ENGLISH\\#English DJ Song" ) );
            //        musicFolders.add( new File( "F:\\Ent\\ForPrabu\\ChaminClassics" ) );
        }

        @Override protected void onTick()
        {
            MusicUsAgent.log( myAgent.getAID().getName(), "Checking for new songs" );

            /*if( musicLibraries.length != 0 )
            {
                // find new songs
                if( !musicFolders.isEmpty() )
                {
                    for( File musicFolder : musicFolders )
                    {
                        MusicUsAgent.log( myAgent.getAID().getName(), "Checking music in ", musicFolder.getAbsolutePath() );

                        for( File file : musicFolder.listFiles( mp3FileFilter ) )
                        {
                            String newMusicFile = file.getAbsolutePath();
//                            MusicUsAgent.log( myAgent.getAID().getName(), "Detected file : ", newMusicFile );

                            sendMessageMusicLibrary( musicLibraries, newMusicFile );
                        }
                    }
                }
                else
                {
                    MusicUsAgent.log( myAgent.getAID().getName(), "No music directories found!!!" );
                }
            }
            else
            {
                MusicUsAgent.log( myAgent.getAID().getName(), "No music libraries found!!!" );
            }  */
        }

        private void sendMessageMusicLibrary( AID[] musicLibraries, String newMusicFile )
        {
            // music lib/contoller should send CFP to SearchSongAgent with what to search for(eg: dir, iTunes lib, playlists)
            // then search agent search in those and reply with proposals for new songs, playlists
            ACLMessage newSongInform = new ACLMessage( ACLMessage.PROPOSE );
            for( AID musicLibrary : musicLibraries )
            {
                newSongInform.addReceiver( musicLibrary );
            }
            newSongInform.setContent( newMusicFile );   // Can also send byte arrays, serializable objects
            newSongInform.setConversationId( Constants.NEW_MUSIC_PROPOSAL );
            newSongInform.setReplyWith( Constants.NEW_MUSIC_PROPOSAL + System.currentTimeMillis() );
            myAgent.send( newSongInform );
        }
    }
}
