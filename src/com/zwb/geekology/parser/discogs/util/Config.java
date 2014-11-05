package com.zwb.geekology.parser.discogs.util;

import com.zwb.config.api.ConfigurationFactory;
import com.zwb.config.api.IConfiguration;
import com.zwb.geekology.parser.api.parser.IGkParsingSource;
import com.zwb.geekology.parser.impl.GkParsingResult;
import com.zwb.geekology.parser.impl.GkParsingSource;

public class Config
{
    private static final String SOURCE_STRING = "discogs.com";
    
    private static final String CONFIG_NAME = "discogs.config";

    private static final String CONFIG_KEY_API_KEY = "access.apikey";
    private static final String CONFIG_KEY_API_USER_AGENT = "access.useragent";
    private static final String CONFIG_KEY_API_USE_OAUTH = "access.useoauth";
    private static final boolean CONFIG_DEFAULT_USE_OAUTH = false;
    private static final String CONFIG_DEFAULT_API_USER_AGENT = "foo";
    private static final String CONFIG_KEY_API_CONSUMER_KEY = "access.oauth.consumerkey";
    private static final String CONFIG_KEY_API_CONSUMER_SECRET = "access.oauth.consumersecret";
    private static final String CONFIG_KEY_API_VERIFICATION_CODE = "access.oauth.verificationcode";
    
    private static final String CONFIG_KEY_SEARCH_DEPTH = "search.depth";
    private static final int CONFIG_DEFAULT_SEARCH_DEPTH = 15;
    private static final String CONFIG_KEY_SEARCH_THRESHOLD = "search.threshold";
    private static final double CONFIG_DEFAULT_SEARCH_THRESHOLD = 0.8;
    private static final String CONFIG_KEY_SEARCH_THRESHOLD_ALBUM = "search.threshold.album";
    private static final double CONFIG_DEFAULT_SEARCH_THRESHOLD_ALBUM = 0.9;
    
    private static IConfiguration config = ConfigurationFactory.getBufferedConfiguration(CONFIG_NAME);
    
    public static String getSourceString()
    {
	return SOURCE_STRING;
    }
    
    public static String getApiKey()
    {
	return config.getString(CONFIG_KEY_API_KEY, "");
    }
    
    public static String getUserAgent()
    {
	return config.getString(CONFIG_KEY_API_USER_AGENT, CONFIG_DEFAULT_API_USER_AGENT);
    }
    
    public static String getConsumerKey()
    {
	return config.getString(CONFIG_KEY_API_CONSUMER_KEY, "");
    }
    
    public static String getConsumerSecret()
    {
	return config.getString(CONFIG_KEY_API_CONSUMER_SECRET, "");
    }
    
    public static String getVerificationCode()
    {
	return config.getString(CONFIG_KEY_API_VERIFICATION_CODE, "");
    }
    
    public static boolean getUseOAuth()
    {
	return config.getBool(CONFIG_KEY_API_USE_OAUTH, CONFIG_DEFAULT_USE_OAUTH);
    }
    
    public static int getSearchDepth()
    {
	return config.getInt(CONFIG_KEY_SEARCH_DEPTH, CONFIG_DEFAULT_SEARCH_DEPTH);
    }
    
    public static double getSearchTreshold()
    {
	return config.getDouble(CONFIG_KEY_SEARCH_THRESHOLD, CONFIG_DEFAULT_SEARCH_THRESHOLD);
    }
    
    public static double getSearchTresholdViaAlbum()
    {
	return config.getDouble(CONFIG_KEY_SEARCH_THRESHOLD_ALBUM, CONFIG_DEFAULT_SEARCH_THRESHOLD_ALBUM);
    }
    
}
