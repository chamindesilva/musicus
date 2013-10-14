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
    public static double calculateEuclideanDistance( double[] featureList1, List<Double> featureList2, double[] featureMaxValues, double[] featureMinValues )
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
            double diff = ( featureList1[i] - featureList2.get( i ) ) / ( maxMinusMin != 0 ? maxMinusMin : 1.0 );
            //            System.out.println( ">>>> "+ featureList1[i] + " - " + featureList2.get( i ) + " = " + diff );
            tot += ( diff * diff );
        }

        return Math.sqrt( tot );
    }
}
