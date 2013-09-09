package com.musicus.agent.Utils;

import com.musicus.agent.Constants;
import jAudioFeatureExtractor.ACE.DataTypes.DataSet;
import jAudioFeatureExtractor.ACE.XMLParsers.XMLDocumentParser;

import java.io.Reader;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/7/13
 * Time: 7:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExtendedDataSet extends DataSet
{

    public static DataSet[] parseDataSetXml( Reader dataXmlStream )
            throws Exception
    {
        // Parse the file
        Object[] results = (Object[]) ExtendedXMLDocumentParser.parseXMLDocument( dataXmlStream, Constants.FEATURE_VECTOR_FILE );
        DataSet[] parse_results = new DataSet[results.length];
        for (int i = 0; i < parse_results.length; i++)
            parse_results[i] = (DataSet) results[i];

        // Return the results
        return parse_results;
    }

    public static DataSet[] parseDataSetXml(String dataXml )
            throws Exception
    {
        // Parse the file
        Object[] results = (Object[]) ExtendedXMLDocumentParser.parseXMLDocument( dataXml, Constants.FEATURE_VECTOR_FILE );
        DataSet[] parse_results = new DataSet[results.length];
        for (int i = 0; i < parse_results.length; i++)
            parse_results[i] = (DataSet) results[i];

        // Return the results
        return parse_results;
    }
}
