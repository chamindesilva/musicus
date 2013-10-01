package com.musicus.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/30/13
 * Time: 11:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class Savable
{
    public static final int NEW             = 2;
    public static final int MODIFIED        = 3;
    public static final int DELETED         = 4;
    public static final int UNCHANGED       = 5;

    public int getStatus()
    {
        return -1;
    }

    public void setStatus( int i )
    {

    }

    public void save( Connection con ) throws SQLException
    {
        throw new SQLException( "Needs Implementation" );
    }

    public void load( ResultSet rs, Connection con, int level ) throws SQLException
    {
        throw new SQLException( "Needs Implementation" );
    }
}
