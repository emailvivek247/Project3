package com.fdt.ecert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import com.fdt.common.util.TIFFToPDFConverter;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.itextpdf.text.DocumentException;

/** 
 * Test to create the certified document.
 * This test expects the files  to be present in the BASE_DIRECTORY below.
 * 
 * 1 signature.png
 * 2 stamp.jpg
 * 3 test.tif
 * 
 * 
 * 
 * @author APatel
 *
 */
public class TestMain {

		// Base directory for all the files.
		public static String BASE_DIRECTORY = "c:\\docs\\test\\";

	 public static void main(String[] args) throws DocumentException, IOException {
		 String certifiedDocument = BASE_DIRECTORY + "CertifiedDocument.pdf";


		 Date date = new Date();
		 String watermarkText = "CERTIFIED COPY";
		 String inputFormat = "EEE MMM dd HH:mm:ss zzz yyyy";
		 String footerDateFormat = "MM/dd/yyyy HH:mm:ss zzz";
		 String attestedDateFormat = "MMMMMMMMMMMMMMMMM dd, yyyy";
		 String urlVerification = "Verify this document within 90 days from the date of this certification at:\nhttps://AZeCertify.com";
		 String authentcityNumber = UUID.randomUUID().toString().toUpperCase().substring(5, 23);
		 String footerText = watermarkText.concat(" - ").concat(" ").concat(authentcityNumber).concat(" -").
				 concat(" ").concat(PageStyleUtil.format(date.toString(), inputFormat, footerDateFormat));
		 String roaInformation = "Certification Page For: \nCase No. CR20010 - State Vs. Doe - Sentencing";
		 String clerkName = "Clark C. Clerical";
		 String desigination ="Clerk of the Superior Court";
		 String state = "State of Arizona";
		 String countyName = "County of Huachuca";
		 String signatureFooter = clerkName.toUpperCase().concat("\n").concat(desigination).concat("\n").concat(state).concat("\n").concat(countyName);
		 String sealOfAuthenticity = BASE_DIRECTORY + "stamp.JPG" ;
		 String signature = BASE_DIRECTORY + "signature.png";
		 String noteOfAuthenticity = "I, ".concat(clerkName).concat(", ").concat(desigination).concat(" of the ").concat(state).concat(", in and for the ").concat(countyName).concat(", " +
		 		"do hereby certify that this is a full, true and correct copy of the original on file with this office and that this " +
		 		"image is certified with a computer generated seal pursuant to Arizona Revised Statutes §1-215.36 and §12-282(D)." +
		 		"  \n\nAttest: ".concat(PageStyleUtil.format(date.toString(), inputFormat, attestedDateFormat)).concat("\nCertified Document Number:  " + authentcityNumber));

		ByteArrayOutputStream convertedTiffImage = new ByteArrayOutputStream();
		ByteArrayOutputStream coverPage = new ByteArrayOutputStream();
		ByteArrayOutputStream internalPage = new ByteArrayOutputStream();
		ByteArrayOutputStream certifiedDocumentInternal = new ByteArrayOutputStream();


		 System.out.println("The File Exist" + TIFFToPDFConverter.convert(BASE_DIRECTORY + "test.tif", null, 0,0,0,0,0, true, convertedTiffImage, false, 0.5f, false));

		 Certification.createCoverPage(coverPage, roaInformation, getBytesFromImage(sealOfAuthenticity), noteOfAuthenticity,
				 getBytesFromImage(signature), signatureFooter, footerText, urlVerification);

		 Certification.createInternalPage(internalPage, footerText);

		Certification.stampTheDocument(certifiedDocumentInternal, convertedTiffImage.toByteArray(), internalPage.toByteArray());

         //Certification.concatenate(certifiedDocument, coverPage, new FileOutputStream(certifiedDocumentInternal));
 		Certification.concatenate(coverPage.toByteArray(), certifiedDocumentInternal.toByteArray(), new FileOutputStream(certifiedDocument));

	}
	 
	public static byte[] getBytesFromImage(String fileName){
		FileInputStream fis = null;
		try{
			File file = new File(fileName);
			byte[] bytes = new byte[(int)file.length()];
			fis = new FileInputStream(file);
			fis.read(bytes);
			fis.close();
			return bytes;
		}catch(Exception e){}
		finally { 
			try { if(fis != null){ fis.close(); }}catch(Exception ignore){}
		}
		return null;
	}
}
