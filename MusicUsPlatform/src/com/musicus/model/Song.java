package com.musicus.model;

import com.musicus.db.FileSavable;
import com.musicus.db.Savable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/30/13
 * Time: 11:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class Song extends FileSavable
{
    private String path;        // Primary key
    private String name;
    private Set<Feature> features;
    private int status;

    public Song()
    {
    }

    @Override public FileSavable getInstance()
    {
        return new Song();
    }

    @Override public boolean load( String... dbValues )
    {
        boolean loadSuccess = false;
        if( dbValues.length == 2 )
        {
            path = dbValues[0];
            name = dbValues[1];
            List<FileSavable> fileSavables = FileSavable.loadData( new Feature() );
            List<Feature> featureList = new ArrayList<Feature>(  );
            for( FileSavable fileSavable : fileSavables )
            {
                Feature feature = (Feature) fileSavable;
                if( path.equals( feature.getOwner() ))
                {
                    featureList.add( feature );
                }
            }
            loadSuccess = true;
        }
        else
        {
            loadSuccess = false;
        }

        return loadSuccess;
    }

    @Override public String getDbString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( path );
        sb.append( "," );
        sb.append( name );

        return sb.toString();
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public Set<Feature> getFeatures()
    {
        return features;
    }

    public void setFeatures( Set<Feature> features )
    {
        this.features = features;
    }

    public String getCompositePrimaryKey()
    {
        return path;
    }
}
