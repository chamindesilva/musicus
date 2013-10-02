package com.musicus.db;

import com.musicus.model.Song;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 10/1/13
 * Time: 10:35 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FileSavable
{
    public abstract FileSavable getInstance();

    public abstract boolean load( String[] dbValues );

    public abstract String getDbString();

    public String getDbFileName()
    {
        return getClass().getName();
    }

    public abstract String getCompositePrimaryKey();

    @Override public int hashCode()
    {
        return getCompositePrimaryKey().hashCode();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override public boolean equals( Object obj )
    {
        if( this == obj )
        {
            return true;
        }
        else if( obj instanceof FileSavable )
        {
            return this.getCompositePrimaryKey().equals( ( (FileSavable) obj ).getCompositePrimaryKey() );
        }
        else
        {
            return false;
        }
    }

    public static List<FileSavable> loadData( FileSavable loadType )
    {
        List<FileSavable> fileSavableList = new ArrayList<FileSavable>();
        BufferedReader br = null;
        try
        {
            String dbFileName = loadType.getDbFileName();
            java.io.File f = new java.io.File( dbFileName );
            if( f.exists() )
            {
                FileInputStream fstream = new FileInputStream( dbFileName );
                DataInputStream in = new DataInputStream( fstream );
                br = new BufferedReader( new InputStreamReader( in ) );
                String dbValsStr;
                //Read File Line By Line
                while( ( dbValsStr = br.readLine() ) != null )
                {
                    if( dbValsStr.length() > 0 )
                    {
                        String[] dbValsArr = dbValsStr.split( "," );
                        FileSavable fileSavable = loadType.getInstance();
                        if( fileSavable.load( dbValsArr ) )
                        {
                            fileSavableList.add( fileSavable );
                        }
                    }
                }
            }
        }
        catch( FileNotFoundException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch( IOException e )
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally
        {
            if( br != null )
            {
                try
                {
                    br.close();
                }
                catch( IOException e )
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }


        return fileSavableList;
    }

    public static void persistData( Collection<? extends FileSavable> dataList )
    {
        if( dataList != null && !dataList.isEmpty() )
        {
            BufferedWriter writer = null;
            try
            {

                boolean openedFile = false;
                for( FileSavable fileSavable : dataList )
                {
                    if( !openedFile )
                    {
                        String fileName = fileSavable.getDbFileName();
                        writer = new BufferedWriter( new FileWriter( fileName ) );
                        writer.write( "" );         // empty the file
                        openedFile = true;
                    }
                    writer.append( fileSavable.getDbString() );
                    writer.append( "\n" );
                }
            }
            catch( IOException e )
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            finally
            {
                if( writer != null )
                {
                    try
                    {
                        writer.close();
                    }
                    catch( IOException e )
                    {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }
    }

    public static List<Song> getFullSongsList()
    {
        List<Song> songList = new ArrayList<Song>();
        List<FileSavable> fileSavables = FileSavable.loadData( new Song() );
        for( FileSavable fileSavable : fileSavables )
        {
            Song song = (Song) fileSavable;
            songList.add( song );
        }

        return songList;
    }
}
