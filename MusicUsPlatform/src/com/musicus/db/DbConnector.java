package com.musicus.db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/30/13
 * Time: 9:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DbConnector
{
    private static Connection con;
    private static boolean connected;

    public static Connection getConnection()
    {
        if( !connected )
        {
             reconnectDb();
        }
        return con;
    }

    public static void reconnectDb()
    {
        try
        {
            Class.forName( "org.sqlite.JDBC" );
            con = DriverManager.getConnection( "jdbc:sqlite:musicus.db" );
            connected = true;
        }
        catch( Exception e )
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println( "Opened database connection successfully" );
    }
}
