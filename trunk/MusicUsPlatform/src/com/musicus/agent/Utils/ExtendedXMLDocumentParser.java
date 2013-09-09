package com.musicus.agent.Utils;

import com.musicus.agent.Constants;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import jAudioFeatureExtractor.ACE.XMLParsers.FeatureListHandler;
import jAudioFeatureExtractor.ACE.XMLParsers.ParseBatchJobHandler;
import jAudioFeatureExtractor.ACE.XMLParsers.ParseClassificationsFileHandler;
import jAudioFeatureExtractor.ACE.XMLParsers.ParseDataSetFileHandler;
import jAudioFeatureExtractor.ACE.XMLParsers.ParseFeatureDefinitionsFileHandler;
import jAudioFeatureExtractor.ACE.XMLParsers.ParseFileHandler;
import jAudioFeatureExtractor.ACE.XMLParsers.ParseSaveSettings;
import jAudioFeatureExtractor.ACE.XMLParsers.ParseTaxonomyFileHandler;
import jAudioFeatureExtractor.ACE.XMLParsers.ParsingXMLErrorHandler;
import jAudioFeatureExtractor.ACE.XMLParsers.XMLDocumentParser;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;

/**
 * Extend XML file parser functionality of ACE package for Strings and for streams
 * User: Chamin
 * Date: 9/7/13
 * Time: 7:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExtendedXMLDocumentParser extends XMLDocumentParser
{

    public static Object parseXMLDocument( String xmlContent, String documentType ) throws Exception
    {
        return parseXMLDocument(  new StringReader( xmlContent ), documentType );
    }

    public static Object parseXMLDocument( Reader xmlContentStream, String documentType ) throws Exception
    {
        // Prepare the XML parser with the validation feature on and the error handler
        // set to throw exceptions on all warnings and errors
        XMLReader reader = new SAXParser();
        reader.setFeature( "http://xml.org/sax/features/validation", true );
        reader.setErrorHandler( new ParsingXMLErrorHandler() );
        ParseFileHandler handler;

        // Choose the correct type handler based on the type of XML file
        if( documentType.equals( Constants.FEATURE_VECTOR_FILE ) )
        {
            handler = new ParseDataSetFileHandler();
        }
        else if( documentType.equals( Constants.FEATURE_KEY_FILE ) )
        {
            handler = new ParseFeatureDefinitionsFileHandler();
        }
        else if( documentType.equals( Constants.TAXONOMY_FILE ) )
        {
            handler = new ParseTaxonomyFileHandler();
        }
        else if( documentType.equals( Constants.CLASSIFICATIONS_FILE ) )
        {
            handler = new ParseClassificationsFileHandler();
        }
        else if( documentType.equals( Constants.SAVE_SETTINGS ) )
        {
            handler = new ParseSaveSettings();
        }
        else if( documentType.equals( Constants.BATCH_FILE ) )
        {
            handler = new ParseBatchJobHandler();
        }
        else if( documentType.equals( Constants.FEATURE_LIST ) )
        {
            handler = new FeatureListHandler();
        }

        // Throw an exception if an unknown type of XML file is specified
        else
        {
            throw new Exception( new String( "Invalid type of XML file specified. The XML file type " + documentType + " is not known." ) );
        }

        // Parse the file so that the contents are available in the parsed_file_contents field of the handler
        reader.setContentHandler( handler );
        try
        {
            reader.parse( new InputSource( xmlContentStream ) );
        }
        catch( SAXParseException e ) // throw an exception if the file is not a valid XML file
        {
            throw new Exception( "The xml stream is not a valid XML file.\n\nDetails of the problem: " + e.getMessage() +
                    "\n\nThis error is likely in the region of line " + e.getLineNumber() + "." );
        }
        catch( SAXException e ) // throw an exception if the file is not an XML file of the correct type
        {
            throw new Exception( "The xml stream must be of type " + documentType + "." + e.getMessage() );
        }
        catch( Exception e ) // throw an exception if the file is not an XML file of the correct type
        {
            throw new Exception( "The xml stream is not formatted properly.\n\nDetails of the problem: " + e.getMessage() );
        }

        // Return the contents of the parsed file
        return handler.parsed_file_contents;
    }
}
