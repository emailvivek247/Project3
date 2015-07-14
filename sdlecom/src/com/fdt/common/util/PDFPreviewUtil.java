package com.fdt.common.util;

import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.io.RandomAccessSource;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;

public class PDFPreviewUtil {

    private static Logger logger = LoggerFactory.getLogger(PDFPreviewUtil.class.getName());

    public static boolean preparePdf(byte[] tiffFile, String waterMark, int waterMarkColorRed,
            int waterMarkColorGreen, int waterMarkBlue, int fontSize, int numberOfPages, boolean isPrintingAllowed,
            OutputStream outputStream, boolean isFillWaterMark, float opacity, boolean keepOriginalPageSize) {

        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        RandomAccessFileOrArray randomAcessFileOrArray = null;
        PdfWriter pdfWriter = null;
        boolean isFileConverted = false;

        try {

            RandomAccessSourceFactory randomAccessSourceFactory =  new RandomAccessSourceFactory();
            RandomAccessSource randomAccessSource = randomAccessSourceFactory.createSource(tiffFile);
            randomAcessFileOrArray = new RandomAccessFileOrArray(randomAccessSource);
            PdfReader pdfReader = new PdfReader(randomAcessFileOrArray, null);

            int actualPageCount = pdfReader.getNumberOfPages();
            if (numberOfPages == 0 || numberOfPages > actualPageCount) {
                numberOfPages = actualPageCount;
            }
            if (fontSize == 0) {
                fontSize = 100;
            }

            pdfWriter = PdfWriter.getInstance(document, outputStream);

            if (!StringUtils.isBlank(waterMark)) {
                BaseColor waterMarkColor = new BaseColor(waterMarkColorRed, waterMarkColorGreen, waterMarkBlue);
                pdfWriter.setPageEvent(new Watermark(waterMark, waterMarkColor, fontSize, isFillWaterMark, opacity, keepOriginalPageSize));
            }
            if (!isPrintingAllowed) {
                /** This setting is done not to allow Printing **/
                pdfWriter.setEncryption(null, null, 0, PdfWriter.ENCRYPTION_AES_256);
            }

            pdfWriter.setViewerPreferences(PdfWriter.FitWindow) ;
            pdfWriter.setStrictImageSequence(true);

            document.setPageSize(pdfReader.getPageSize(1));
            document.open();

            PdfContentByte pdfContentByte = pdfWriter.getDirectContent();
            long startTime = System.currentTimeMillis();
            for (int page = 1; page <= numberOfPages; ++page) {
                PdfImportedPage pdfImportedPage = pdfWriter.getImportedPage(pdfReader, page);
                pdfContentByte.addTemplate(pdfImportedPage, new AffineTransform());
            }

            isFileConverted = true;

            long endTime =  System.currentTimeMillis();
            logger.debug("The time taken to convert the TIFF to PDF file is : " + (endTime - startTime) + "(ms)");

        } catch(Exception docExp) {
            logger.error("Error converting the TIFF file to pdf file the message", docExp);
        } catch (OutOfMemoryError outOfMemoryError) {
            logger.error("Out of Memory Error", outOfMemoryError);
        }
        finally {
            try {
                if (document != null) {
                    document.close();
                }
                if (pdfWriter != null) {
                    pdfWriter.close();
                }
                if (randomAcessFileOrArray != null) {
                    randomAcessFileOrArray.close();
                }
            } catch (Exception e) {
                logger.error("Error Closing the Stream", e);
            }
        }
        return isFileConverted;
    }
}
