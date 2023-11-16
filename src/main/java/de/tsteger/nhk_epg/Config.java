package de.tsteger.nhk_epg;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;
import org.json.JSONTokener;

public class Config {
	private final static String API_URL_KEY = "apiUrl";
	private final static String DAYS_KEY = "days";
	private final static String OUTPUTFILE_KEY = "outputFile";
	
	private String apiUrl;
	private int days;
	private String outputFile;
	
	private Config() {
		super();
		
		this.apiUrl = "https://nwapi.nhk.jp/nhkworld/epg/v7b/world/s%d-e%d.json";
		this.days = 1;
		this.outputFile = "out.xml";
	}
	
	public static Config getInstance() {
		return new Config();
	}
	
	public static Config load(String filename) {
		
		Config retValue = null; 
		try (final InputStream is = new FileInputStream(filename)){
			retValue = new Config();
			final JSONObject json = new JSONObject(new JSONTokener(is));
			
			if(json.has(API_URL_KEY)) {
				retValue.apiUrl = json.getString(API_URL_KEY);
			}
			if(json.has(DAYS_KEY)) {
				retValue.days = json.getInt(DAYS_KEY);
			}
			if(json.has(OUTPUTFILE_KEY)) {
				retValue.outputFile = json.getString(OUTPUTFILE_KEY);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return retValue;
	}


	public String getApiUrl() {
		return apiUrl;
	}

	public int getDays() {
		return days;
	}

	public String getOutputFile() {
		return outputFile;
	}
	
}
