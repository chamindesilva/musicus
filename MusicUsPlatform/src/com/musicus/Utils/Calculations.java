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
    public static double calculateEuclideanDistance( double[] featureList1, double[] featureList2, double[] featureMaxValues, double[] featureMinValues )
    {
        // TODO : Normalize values + use a weight for each feature
        double tot = 0.0D;
        for( int i = 0; i < featureList1.length; i++ )
        {
            //            if( featureList1 == null || featureList2 ==null || featureList2.get( i )==null)
            //            {
            //                System.out.println("error");
            //            }
            double maxMinusMin = featureMaxValues[i] - featureMinValues[i];             // ??
            double diff = ( featureList1[i] - featureList2[i] ) / ( maxMinusMin != 0 ? maxMinusMin : 1.0 );
            //            System.out.println( ">>>> "+ featureList1[i] + " - " + featureList2.get( i ) + " = " + diff );
            tot += ( diff * diff );
        }

        return Math.sqrt( tot / featureList1.length );
    }

    /**
     * get means for ALL FEATURES for all songs
     *
     * @param dataSets
     * @return
     */
    public static double[] getMean( double[][] dataSets )
    {
        double[] means = new double[dataSets.length];   // no of features
        for( int setNo = 0; setNo < dataSets.length; setNo++ )
        {
            means[setNo] = getMean( dataSets[setNo] );
        }

        return means;
    }

    /**
     * Get mean for A FEATURE for all songs
     *
     * @param dataSet
     * @return
     */
    public static double getMean( double[] dataSet )
    {
        double tot = 0.0D;
        for( double data : dataSet )
        {
            tot += data;
        }

        return tot / dataSet.length;        // divide by no of songs
    }

    /**
     * get Standard Deviation for ALL FEATURES for all songs
     *
     * @param dataSets
     * @return
     */
    public static double[] getSD( double[][] dataSets )
    {
        double[] sd = new double[dataSets.length];   // no of features
        for( int setNo = 0; setNo < dataSets.length; setNo++ )
        {
            sd[setNo] = getSD( dataSets[setNo] );
        }

        return sd;
    }

    /**
     * Get Standard Deviation for A FEATURE for all songs
     *
     * @param dataSet
     * @return
     */
    public static double getSD( double[] dataSet )
    {
        return Math.sqrt( getVariance( dataSet ) );
    }

    public static double getVariance( double[] dataSet )
    {
        double mean = getMean( dataSet );
        double tot = 0.0D;
        for( double data : dataSet )
        {
            tot += Math.pow( ( data - mean ), 2.0 );    // power of 2
        }

        return tot / dataSet.length;        // divide by no of songs
    }

    public static double featureAdjustment( double originalVal, double mean, double sd )
    {
        return originalVal - ( ( originalVal - mean ) / ( 1 + sd ) );
    }
}
