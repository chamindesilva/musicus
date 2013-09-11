package com.musicus.agent;

/**
 * Created with IntelliJ IDEA.
 * User: Chamin
 * Date: 9/3/13
 * Time: 11:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class Constants
{
    public static final String NEW_MUSIC_PROPOSAL = "newMusicProposal";
    public static final String FEATURE_EXTRACTION_PROPOSAL = "featureExtractionProposal";
    public static final String PLAY_REQUEST = "playRequest";

    public static final String MUSIC_SEARCH     = "music_search";
    public static final String MUSIC_LIBRARY    = "music-library";
    public static final String FEATURE_EXTRACTOR = "feature-extractor";
    public static final String SONG_PLAYER = "song_player";

    public static final String MP3_EXTENTION = ".mp3";
    public static final int SCAN_FOR_AGENTS_INTERVAL = 5;
    public static final double LIBRARY_SCAN_INTERVAL = 4;
    public static final String JAUDIO_BATCH_FILE_XML = ".\\JAudioBatchFile.xml";
    public static final String FEATURE_VECTOR_FILE = "feature_vector_file";
    public static final String FEATURE_KEY_FILE = "feature_key_file";
    public static final String TAXONOMY_FILE = "taxonomy_file";
    public static final String CLASSIFICATIONS_FILE = "classifications_file";
    public static final String SAVE_SETTINGS = "save_settings";
    public static final String BATCH_FILE = "batchFile";
    public static final String FEATURE_LIST = "feature_list";
    public static final int LISTNER_MODEL_UPDATE_INTERVAL = 3;
    public static final int ANALYSED_FEATURES_COUNT = 20;
    public static final boolean DEBUG_MODE = false;
    public static final int DEBUG_DELAY_SEC = 10 * 1000;

    //************ LOG LEVELS ******
    public static final boolean LOG_WRITE_TO_FILE = true;
    public static final String LOG_FILE = "log.txt";
    public static final int LOG_WARNING         = 3;
    public static final int LOG_IMPORTANT       = 4;
    public static final int LOG_INFO            = 5;


    public static final int LOG_FILTER_LEVEL    = 4;

    // Cross-fade controls
    public static final int CROSSFADE_BLOCKS_COUNT = 50;
    public static final float CROSSFADE_REDUCTION_STEP_SIZE = -5.0f;
}
