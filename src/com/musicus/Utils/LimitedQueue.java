package com.musicus.Utils;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/12/13
 * Time: 2:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class LimitedQueue<E> extends LinkedList<E>
{
    private int limit;

    public LimitedQueue( int limit )
    {
        this.limit = limit;
    }

    @Override
    public boolean add( E o )
    {
        super.add( o );
        while( size() > limit )
        {
            super.remove();
        }
        return true;
    }
}
