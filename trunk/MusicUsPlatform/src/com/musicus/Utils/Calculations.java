package com.musicus.Utils;

import java.util.List;

/**
 * <DESCRIPTION>
 *
 * @author: Chamin De Silva
 * @version: 1.0
 * Date: 10/09/13
 * Time: 16:14
 */
public class Calculations
{
    public static double calculateEuclideanDistance( Double[] featureList1, List<Double> featureList2 )
    {
        double tot = 0.0D;
        for( int i = 0; i < featureList1.length; i++)
        {
            double diff = (featureList1[i] - featureList2.get( i ));
            tot += (diff * diff);
        }

        return Math.sqrt( tot );
    }
}
