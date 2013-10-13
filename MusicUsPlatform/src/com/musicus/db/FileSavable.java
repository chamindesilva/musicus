package com.musicus.db;

import com.musicus.model.Song;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
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
    //    public abstract FileSavable getInstance();

    public abstract FileSavable load( String[] dbValues, String fileDirPath );

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

    public static List<FileSavable> loadData( FileSavable loadType, String dirPath )
    {
        List<FileSavable> fileSavableList = new ArrayList<FileSavable>();
        BufferedReader br = null;
        try
        {
            String dbFileName = loadType.getDbFileName();
            String filePath;
            if( dirPath != null && !dirPath.isEmpty() )
            {
                filePath = dirPath + File.separator + dbFileName;
            }
            else
            {
                filePath = dbFileName;
            }
            java.io.File f = new java.io.File( filePath );
            if( f.exists() )
            {
                FileInputStream fstream = new FileInputStream( filePath );
                DataInputStream in = new DataInputStream( fstream );
                br = new BufferedReader( new InputStreamReader( in ) );
                String dbValsStr;
                //Read File Line By Line
                while( ( dbValsStr = br.readLine() ) != null )
                {
                    if( dbValsStr.length() > 0 )
                    {
                        String[] dbValsArr = dbValsStr.split( "," );
                        FileSavable fileSavable = loadType.load( dbValsArr, dirPath );
                        if( fileSavable != null )
                        {
                            fileSavableList.add( fileSavable );
                        }
                    }
                }
            }
            else
            {
                System.out.println( " >>>>>>>>>>>>>> ERROR LOADING DATA PERSISTANCE FILE, COULD  NOT FIND : " + filePath );
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

    public static String persistData( Collection<? extends FileSavable> dataList, String dirPath )
    {
        String fileNameSaved  = null;
        if( dataList != null && !dataList.isEmpty() )
        {
            BufferedWriter writer = null;
            try
            {

                boolean openedFile = false;
                for( FileSavable fileSavable : dataList )
                {
                    // open file at first row read only
                    if( !openedFile )
                    {
                        String fileName = fileSavable.getDbFileName();
                        String filePath;
                        if( dirPath != null && !dirPath.isEmpty() )
                        {
                            File dir = new File( dirPath );
                            if( !dir.exists() )
                            {
                                if( !dir.mkdirs() )
                                {
                                    System.out.println( " >>>>>>>>>>>>>> ERROR IN CREATING DATA PERSISTANCE DIRS : " + dir.getAbsolutePath() );
                                    return null;
                                }
                                else
                                {
                                    filePath = dirPath + File.separator + fileName;
                                }
                            }
                            else
                            {
                                filePath = dirPath + File.separator + fileName;
                            }
                        }
                        else
                        {
                            filePath = fileName;
                        }
                        writer = new BufferedWriter( new FileWriter( filePath ) );
                        writer.write( "" );         // empty the file
                        openedFile = true;
                        fileNameSaved = filePath;
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

        return fileNameSaved;
    }

    public static List<Song> getFullSongsList( String fileDirPath )
    {
        List<Song> songList = new ArrayList<Song>();
        List<FileSavable> fileSavables = FileSavable.loadData( new Song( null, null ), fileDirPath );
        for( FileSavable fileSavable : fileSavables )
        {
            Song song = (Song) fileSavable;
            songList.add( song );
        }

        return songList;
    }
}
