package com.musicus.model;

import com.musicus.db.FileSavable;
import com.musicus.db.Savable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/30/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class Feature extends FileSavable
{
    private String owner;
    private String name;
    private double val;
    private String compositeKey;

    public Feature()
    {
    }

    public Feature( String owner, String name, double val )
    {
        this.owner = owner;
        this.name = name;
        this.val = val;
    }

    //    @Override public FileSavable getInstance()
//    {
//        return new Feature();
//    }

    @Override public FileSavable load( String[] dbValues, String fileDirPath )
    {
        FileSavable loadedObj = null;
        if( dbValues.length == 3 )
        {
            String owner = dbValues[0];
            String name = dbValues[1];
            double val = Double.parseDouble( dbValues[2] );
            loadedObj =  new Feature(owner, name, val);
        }

        return loadedObj;
    }

    @Override public String getDbString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( owner );
        sb.append( "," );
        sb.append( name );
        sb.append( "," );
        sb.append( val );

        return sb.toString();
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner( String owner )
    {
        this.owner = owner;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public double getVal()
    {
        return val;
    }

    public void setVal( double val )
    {
        this.val = val;
    }

    public String getCompositePrimaryKey()
    {
        if( compositeKey == null )
        {
            compositeKey = owner + "##" + name;
        }
        return compositeKey;
    }
}
