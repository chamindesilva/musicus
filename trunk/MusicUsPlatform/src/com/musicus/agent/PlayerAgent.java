package com.musicus.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/10/13
 * Time: 11:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerAgent extends MusicUsAgent
{
    SourceDataLine line = null;
    AudioInputStream din = null;

    // some lock somewhere...
    Object lock = new Object();
    // some paused variable
    volatile boolean paused = false;
    volatile boolean stopped = true;

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
        return Constants.SONG_PLAYER;
    }

    @Override protected void addBehaviours()
    {
        addBehaviour( new CyclicBehaviour()
        {
            @Override public void action()
            {
                // Check messages for new songs
                ACLMessage msg = myAgent.receive();
                if( msg != null )       // TODO : get the last entry in the queue and play
                {
                    String audioToPlay;
                    audioToPlay = msg.getContent();
                    if( audioToPlay != null && !audioToPlay.isEmpty() )
                    {
                        log( getName(), ">>> Playing :", audioToPlay );
                        System.out.println( ">>> Playing :" + audioToPlay );
                        stop();
                        newPlay( audioToPlay );
                    }
                }
                else
                {
                    block();
                }
            }
        } );
    }

    public static AID[] getAgents( Agent callFromAgent )
    {
        return MusicUsAgent.getAgents( getAgentTypeCode(), callFromAgent );
    }

    @Override protected void takeDown()
    {
        super.takeDown();
        stop();
    }

    private void newPlay( final String audioFile )
    {
        stopped = false;
        Thread playThread = new Thread()
        {
            @Override public void run()
            {
                super.run();    //To change body of overridden methods use File | Settings | File Templates.
                newPlayLoop( audioFile );
            }
        };
        playThread.start();
    }

    private void newPlayLoop( String audioFile )
    {
        try
        {
            File file = new File( audioFile );
            AudioInputStream in = AudioSystem.getAudioInputStream( file );
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false );
            din = AudioSystem.getAudioInputStream( decodedFormat, in );
            DataLine.Info info = new DataLine.Info( SourceDataLine.class, decodedFormat );
            line = (SourceDataLine) AudioSystem.getLine( info );
            if( line != null )
            {
                line.open( decodedFormat );
                byte[] data = new byte[4096];
                // Start
                line.start();

                int nBytesRead;
                synchronized( lock )
                {
                    while( !stopped && ( nBytesRead = din.read( data, 0, data.length ) ) != -1 )
                    {
                        while( paused )
                        {
                            if( line.isRunning() )
                            {
                                line.stop();
                            }
                            try
                            {
                                lock.wait();
                            }
                            catch( InterruptedException e )
                            {
                            }
                        }

                        if( !line.isRunning() )
                        {
                            line.start();
                        }
                        line.write( data, 0, nBytesRead );
                    }
                }
            }

        }
        catch( Exception e )
        {
            e.printStackTrace();
            stop();
        }
    }

    private void stop()
    {
        stopped = true;
        try
        {
            // Stop
            if( line != null )
            {
                line.drain();
                line.stop();
                line.close();
            }
        }
        finally
        {
            if( din != null )
            {
                try
                {
                    din.close();
                }
                catch( IOException e )
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    // on the user thread:
    private void userPressedPause()
    {
        paused = true;
    }

    public void userPressedPlay()
    {
        synchronized( lock )
        {
            paused = false;
            lock.notifyAll();
        }
    }
}
