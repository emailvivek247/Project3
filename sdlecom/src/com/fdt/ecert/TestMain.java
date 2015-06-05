package com.fdt.ecert;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.javacoding.xsearch.api.Document;
import net.javacoding.xsearch.api.FacetChoice;
import net.javacoding.xsearch.api.FacetCount;
import net.javacoding.xsearch.api.Field;
import net.javacoding.xsearch.api.Result;
import net.javacoding.xsearch.api.SearchConnection;
import net.javacoding.xsearch.api.SearchQuery;

import com.fdt.common.util.adapter.DocumentManagementSystemAdapter;
import com.fdt.common.util.adapter.SDLDMSDocument;


public class TestMain {
	
	public static void main(String[] args) throws IOException {
		SearchConnection searchConnection = new SearchConnection("http://localhost:3001/sdlecom/").setIndex("test");
	       SearchQuery q = new SearchQuery("Venkata").setDebug(true)
	                          .setHighlightTag("<span style=\"color:#666\">", "</span>")
	                           .setFacetCountLimit(20);
	       Result result = searchConnection.search(q);
	       System.out.println("total:"+result.getTotal());
	       System.out.println("doc count:"+result.getDocList().size());
	       System.out.println("Search time:"+result.getSearchTime());
	       for(Document d : result.getDocList()) {
	    	   System.out.println(d.getString("EMAIL_ID"));
	           System.out.println("---------------------------");
	           for(Field f : d.getFieldList()) {
	               System.out.println(f.getName()+"("+f.getType()+")"+f.getValue());
	           }
	       }
	       /*result.getD
	       System.out.println("*********************");*/
	       for(FacetChoice fChoice : result.getFacetChoiceList()) {
	           System.out.println("Narrow By " + fChoice.getColumn());
	           for(FacetCount fc : fChoice.getFacetCountList()) {
	               System.out.println("  " + fc.getValue() + (fc.getEndValue().length()==0? "" : ","+fc.getEndValue()) + " ~ " + fc.getCount());
	           }
	       }
	}
	
	public static void main1(String[] args) throws Exception  {
		DocumentManagementSystemAdapter onbaseAdapter = new DocumentManagementSystemAdapter();
		SDLDMSDocument sdldmsDocument = onbaseAdapter.getDocumentByDMSID("14127");
		String strFilePath = "C:\\Projects\\AMCAD\\Development\\demo.tiff";
		try {
			byte[] byteArr = sdldmsDocument.getFile();
			if (byteArr != null) {
				FileOutputStream fos = new FileOutputStream(strFilePath);
				fos.write(byteArr);
				fos.close();
			} else {
				System.out.println("Nulll");
			}
		} catch (FileNotFoundException ex) {
			System.out.println("FileNotFoundException : " + ex);
		} catch (IOException ioe) {	
			System.out.println("IOException : " + ioe);
		}
	}

}