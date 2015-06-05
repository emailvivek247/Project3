package net.javacoding.xsearch.indexer.textfilter;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.status.CSVReader;
import net.javacoding.xsearch.utility.U;

public class ZipToGeoConvertor {
    
    public static class GeoPosition{
        float latitude;
        float longitude;
        public float getLatitude() {
            return latitude;
        }
        public void setLatitude(float latitude) {
            this.latitude = latitude;
        }
        public float getLongitude() {
            return longitude;
        }
        public void setLongitude(float longitude) {
            this.longitude = longitude;
        }
        public GeoPosition(float longitude, float latitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        @Override
        public String toString() {
            return "["+longitude+","+latitude+"]";
        }
    }

    static boolean loaded = false;
    static Map<String,GeoPosition> zipToGeoMap = null;
    static String fileName = (WebserverStatic.getIsServer() ? WebserverStatic.getRootDirectory()+"WEB-INF/" : "" )+ "data/geo/zip2position.csv";
    public static synchronized void load() {
        if(loaded) return;
        if(zipToGeoMap==null) {
            zipToGeoMap = new HashMap<String,GeoPosition>(43000);
            try {
                String[] loadLine = null;
                CSVReader csv = new CSVReader(new FileReader(fileName), ',');
                while ((loadLine = csv.getLine()) != null) {
                    if(loadLine.length>=3) {
                        zipToGeoMap.put(loadLine[0], new GeoPosition(U.getFloat(loadLine[1], 0),U.getFloat(loadLine[2], 0)));
                    }
                }
                csv.close();
            } catch (IOException e) {
                e.printStackTrace();
            }           
            loaded=true;
        }
    }
    public static GeoPosition lookup(String zip) {
        if(!loaded) {
            load();
        }
        return zipToGeoMap.get(zip);
    }

    public static void main(String[] args) {
        System.out.println(new ZipToGeoConvertor().lookup(null));
        System.out.println(new ZipToGeoConvertor().lookup(""));
        System.out.println(new ZipToGeoConvertor().lookup("94002"));
    }
}
