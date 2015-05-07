package com.fdt.ecert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

public class Certification {

	static class Watermark extends PdfPageEventHelper {

		String watermarkText;

		public void setWatermarkText(String watermarkText) {
			this.watermarkText = watermarkText;
		}

		public void onEndPage(PdfWriter writer, Document document) {
        	PdfGState gstate = new PdfGState();
    		gstate.setFillOpacity(0.5f);
    		gstate.setStrokeOpacity(0.5f);
    		PdfContentByte contentunder = writer.getDirectContentUnder();
    		contentunder.saveState();
    		contentunder.setGState(gstate);
    		contentunder.setColorFill(new GrayColor(0.75f));
    		contentunder.beginText();
    		BaseFont bf;
			try {
				bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
				contentunder.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_STROKE);
				contentunder.setFontAndSize(bf, 80);
			} catch (DocumentException | IOException e) {
				e.printStackTrace();
			}
    		contentunder.showTextAligned(Element.ALIGN_CENTER, watermarkText,  297.5f, 421, 45);
    		contentunder.endText();
    		contentunder.restoreState();
        }
	}

    static class Footer extends PdfPageEventHelper {

    	String footerText;

    	//PdfTemplate total;

		public void setFooterText(String footer) {
			this.footerText = footer;
		}
		/*public void onOpenDocument(PdfWriter writer, Document document) {
             total = writer.getDirectContent().createTemplate(30, 16);
        }*/
		public void onEndPage(PdfWriter writer, Document document) {
          	Font helvetica = new Font(FontFamily.HELVETICA, 8);
			Phrase phrase = new Phrase(footerText, helvetica);
			PdfContentByte cb = writer.getDirectContent();
			ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, phrase, 300, 37, 0);
        }
    }

	public static void createCoverPage(ByteArrayOutputStream coverPage, String roaInformation, byte[] stamp, String noteOfAuthentication,
			 byte[] signature, String signatureFooter, String footerText, String urlVerification) throws DocumentException, IOException {

		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, coverPage);

		/* For Footer */
		Footer footer = new Footer();
		footer.setFooterText(footerText);
		writer.setPageEvent(footer);

		document.open();

		/* For Adding an Stamp */
		Image image = Image.getInstance(stamp);
		image.setAbsolutePosition(225, 505);
		image.scaleToFit(150, 150);
		writer.getDirectContent().addImage(image);

		/* For Note Of Authenticity and Clerk's Signature. */

		Image signatureImage = Image.getInstance(signature);
		signatureImage.setAbsolutePosition(100, 245);
		//signatureImage.scaleToFit(100, 150);
		writer.getDirectContent().addImage(signatureImage);

		PdfGState gstate = new PdfGState();
		/*gstate.setFillOpacity(0.9f);
		gstate.setStrokeOpacity(0.6f);*/
	    PdfContentByte canvas = writer.getDirectContent();
	    canvas.saveState();
	    canvas.setGState(gstate);
	    BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
	    canvas.setFontAndSize(baseFont, 8);
	    ColumnText columnText1 = new ColumnText(canvas);
        ColumnText columnText2 = new ColumnText(canvas);
        ColumnText columnText3 = new ColumnText(canvas);
        ColumnText columnText4 = new ColumnText(canvas);

        Rectangle rectangle1 = new Rectangle(100, 770f, 480f, 704f);
        Rectangle rectangle2 = new Rectangle(100, 320f, 480f, 500f);
        Rectangle rectangle3 = new Rectangle(100, 150f, 480f, 250f);
        Rectangle rectangle4 = new Rectangle(100, 40f, 480f, 140f);

        Chunk chunk = new Chunk(roaInformation);
        Paragraph paragraph1 = new Paragraph(chunk);
        Paragraph paragraph2 = new Paragraph(noteOfAuthentication);
        Paragraph paragraph3 = new Paragraph(signatureFooter);
        Paragraph paragraph4 = new Paragraph(urlVerification);

        columnText2.setSimpleColumn(rectangle2);
        columnText1.setSimpleColumn(rectangle1);
        columnText3.setSimpleColumn(rectangle3);
        columnText4.setSimpleColumn(rectangle4);

        columnText1.setText(paragraph1);
        columnText2.setText(paragraph2);
        columnText3.setText(paragraph3);
        columnText4.setText(paragraph4);

        columnText2.setAlignment(Element.ALIGN_JUSTIFIED);
        columnText1.setAlignment(Element.ALIGN_CENTER);
        columnText3.setAlignment(Element.ALIGN_LEFT);
        columnText4.setAlignment(Element.ALIGN_JUSTIFIED);

        columnText1.go();
        columnText2.go();
        columnText3.go();
        columnText4.go();

        canvas.restoreState();
		document.close();
	 }

	public static void createInternalPage(ByteArrayOutputStream outputStream, String footerText)
			throws DocumentException, IOException {

		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);

		/* For Footer */
		Footer footer = new Footer();
		footer.setFooterText(footerText);
		writer.setPageEvent(footer);

		document.open();
		document.add(new Paragraph("  "));
		document.close();

	}

	public static void stampTheDocument(ByteArrayOutputStream certifiedDocumentInternal, byte[] convertedTiffImage, byte[] internalPage)
			throws IOException, DocumentException {
		 PdfReader convertedDocumentReader = new PdfReader(convertedTiffImage);
		 PdfReader internalPageReader = new PdfReader(internalPage);
         PdfStamper pdfStamper = new PdfStamper(convertedDocumentReader, certifiedDocumentInternal);
         PdfImportedPage page = pdfStamper.getImportedPage(internalPageReader, 1);
         for(int i=1; i<=convertedDocumentReader.getNumberOfPages(); i++) {
        	 PdfContentByte content = pdfStamper.getOverContent(i);
        	 content.addTemplate(page, 1f, 0, 0, 1, 0, 0);
         }
         pdfStamper.close();
	}

	public static void _concatenate(String coverPage, String certifiedDocumentInternal, OutputStream outputStream)
			throws IOException, DocumentException {

		String[] files = { coverPage, certifiedDocumentInternal };
		Document document = new Document();
		PdfCopy copy = new PdfCopy(document, outputStream);
		document.open();
		PdfReader reader;
		PdfImportedPage page;
		PdfCopy.PageStamp stamp;
		int n;

		PdfReader internalPageReader = new PdfReader(certifiedDocumentInternal);
		int totalPages = internalPageReader.getNumberOfPages();

		for (int i = 0; i < files.length; i++) {
			reader = new PdfReader(files[i]);
			n = reader.getNumberOfPages();
			for (int j = 1; j <= n; j++) {
				page = copy.getImportedPage(reader, j);
				if(i == 0) {
					stamp = copy.createPageStamp(page);
					Font helvetica = new Font(FontFamily.HELVETICA, 12);
					Phrase phrase = new Phrase(String.format("Total Pages: %d", totalPages), helvetica);
					ColumnText.showTextAligned(stamp.getOverContent(), Element.ALIGN_LEFT,
							phrase, 100, 355, 0);
		            stamp.alterContents();
				}
				if(i == 1) {
					stamp = copy.createPageStamp(page);
					Font helvetica = new Font(FontFamily.HELVETICA, 8);
					Phrase phrase = new Phrase(String.format("-  Page %d of %d", j, n), helvetica);
					ColumnText.showTextAligned(stamp.getOverContent(), Element.ALIGN_CENTER,
							phrase, 453.5f, 37, 0);
		            stamp.alterContents();
				}
				copy.addPage(page);
			}
			copy.freeReader(reader);
			reader.close();
		}
		document.close();
	}

	public static void concatenate(byte[] coverPage, byte[] certifiedDocumentInternal, OutputStream outputStream)
			throws IOException, DocumentException {

		Document document = new Document();
		PdfCopy copy = new PdfCopy(document, outputStream);
		document.open();
		PdfReader reader;
		PdfImportedPage page;
		PdfCopy.PageStamp stamp;
		int n;

		PdfReader internalPageReader = new PdfReader(certifiedDocumentInternal);
		int totalPages = internalPageReader.getNumberOfPages();

		reader = new PdfReader(coverPage);
		n = reader.getNumberOfPages();
		for (int j = 1; j <= n; j++) {
			page = copy.getImportedPage(reader, j);
			stamp = copy.createPageStamp(page);
			Font helvetica = new Font(FontFamily.HELVETICA, 12);
			Phrase phrase = new Phrase(String.format("Total Pages: %d", totalPages), helvetica);
			ColumnText.showTextAligned(stamp.getOverContent(), Element.ALIGN_LEFT,
					phrase, 100, 355, 0);
            stamp.alterContents();
			copy.addPage(page);
		}
		copy.freeReader(reader);
		reader.close();

		reader = new PdfReader(certifiedDocumentInternal);
		n = reader.getNumberOfPages();
		for (int j = 1; j <= n; j++) {
			page = copy.getImportedPage(reader, j);
			stamp = copy.createPageStamp(page);
			Font helvetica = new Font(FontFamily.HELVETICA, 8);
			Phrase phrase = new Phrase(String.format("-  Page %d of %d", j, n), helvetica);
			ColumnText.showTextAligned(stamp.getOverContent(), Element.ALIGN_CENTER,
					phrase, 453.5f, 37, 0);
            stamp.alterContents();
			copy.addPage(page);
		}
		copy.freeReader(reader);
		reader.close();
		document.close();
	}


}
