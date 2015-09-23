package com.fdt.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.RectangleReadOnly;
import com.itextpdf.text.io.RandomAccessSource;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;

public class TIFFToPDFConverter {

    private static Logger logger = LoggerFactory.getLogger(TIFFToPDFConverter.class.getName());

    public static boolean convert(byte[] tiffFile, String waterMark, int waterMarkColorRed,
            int waterMarkColorGreen, int waterMarkBlue, int fontSize, int numerOfPages, boolean isPrintingAllowed,
            OutputStream outputStream, boolean isFillWaterMark, float opacity, boolean keepOriginalPageSize) {
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        RandomAccessFileOrArray randomAcessFileOrArray = null;
        PdfWriter writer = null;
        boolean isFileConverted = false;
        try {
            RandomAccessSourceFactory randomAccessSourceFactory =  new RandomAccessSourceFactory();
            RandomAccessSource randomAccessSource = randomAccessSourceFactory.createSource(tiffFile);
            randomAcessFileOrArray = new RandomAccessFileOrArray(randomAccessSource);
            int actualPageCount = 0;
            actualPageCount = TiffImage.getNumberOfPages(randomAcessFileOrArray);
            if (numerOfPages == 0 || numerOfPages > actualPageCount) {
                numerOfPages = actualPageCount;
            }
            if (fontSize == 0) {
                fontSize = 100;
            }
            writer = PdfWriter.getInstance(document, outputStream);
            if (!StringUtils.isBlank(waterMark)) {
                BaseColor waterMarkColor = new BaseColor(waterMarkColorRed, waterMarkColorGreen, waterMarkBlue);
                writer.setPageEvent(new Watermark(waterMark, waterMarkColor, fontSize, isFillWaterMark, opacity, keepOriginalPageSize));
            }
            if (!isPrintingAllowed) {
                /** This setting is done not to allow Printing **/
                writer.setEncryption(null, null, 0, PdfWriter.ENCRYPTION_AES_256);
            }
            writer.setViewerPreferences(PdfWriter.FitWindow) ;
            writer.setStrictImageSequence(true);
            document.open();
            PdfContentByte pdfContentByte = writer.getDirectContent();
            long startTime = System.currentTimeMillis();
            for (int page = 1; page <= numerOfPages; ++page) {
                Image img = TiffImage.getTiffImage(randomAcessFileOrArray, page);
                if (img != null) {
                	if (keepOriginalPageSize) {
                		Rectangle rect = new RectangleReadOnly(PageSize.A4);
                		if (img.getWidth() < 14400 && img.getHeight() < 14400) {
                			rect = new RectangleReadOnly(img.getWidth(), img.getHeight());
                		} else {
                			if (img.getWidth() > 14400) {
                				float factor = 14400 /img.getWidth();
                				rect = new RectangleReadOnly((img.getWidth() * factor), img.getHeight() * factor);
                			} else if  (img.getHeight() > 14400) {
                				float factor = 14400 /img.getHeight();
                				rect = new RectangleReadOnly((img.getWidth() * factor), img.getHeight() * factor);
                			}
                		}
                		document.setPageSize(rect);
                	} else {
                		 if (img.getWidth() > img.getHeight()) {
                			 document.setPageSize(PageSize.A4);
 	                        img.setInitialRotation(1.571f);
                		 } else {
                			document.setPageSize(PageSize.A4);
 	                        img.setInitialRotation(0.0f);
                		 }
                	}
                    img.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
                    document.setMargins(0, 0, 0, 0);
                    document.newPage();
                    img.setAbsolutePosition(0, 0);
                    pdfContentByte.addImage(img);
                }
            }
            isFileConverted =true;
            long endTime =  System.currentTimeMillis();
            logger.debug("The time taken to convert the TIFF to PDF file is : " + (endTime - startTime) + "(ms)");
        } catch(Exception docExp) {
            logger.error("Error converting the TIFF file to pdf file the message", docExp);
        } catch (OutOfMemoryError outOfMemoryError) {
            logger.error("Out of Memory Error", outOfMemoryError);
        }
        finally {
            try {
                if (randomAcessFileOrArray != null)randomAcessFileOrArray.close();
                if (document != null)document.close();
                if (writer != null)writer.close();
            } catch (Exception e) {
                logger.error("Error Closing the Stream", e);
            }
        }
        return isFileConverted;
    }

    public static boolean convert(List<String> tiffFileNameList, String waterMark, int waterMarkColorRed, int waterMarkColorGreen, int waterMarkBlue, int fontSize,
    		boolean isAllowPrinting, OutputStream outputStream, boolean isFillWaterMark, float opacity, boolean keepOriginalPageSize) {
    	if ((tiffFileNameList != null) && (tiffFileNameList.size() > 0)) {
        logger.debug("tiffFileNameList : " + tiffFileNameList);
        String[] tiffFileNameArray = (String[])tiffFileNameList.toArray(new String[0]);
        logger.debug("tiffFileNameArray : " + tiffFileNameArray);
        return convert(tiffFileNameArray, waterMark, waterMarkColorRed, waterMarkColorGreen, waterMarkBlue, fontSize,
          isAllowPrinting, outputStream, isFillWaterMark, opacity, keepOriginalPageSize);
	      }
	      return false;
    }

    public static boolean convert(String[] tiffFileNames, String waterMark, int waterMarkColorRed,
            int waterMarkColorGreen, int waterMarkBlue,	int fontSize, boolean isAllowPrinting, OutputStream outputStream,
                boolean isFillWaterMark, float opacity, boolean keepOriginalPageSize) {
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        RandomAccessFileOrArray randomAcessFileOrArray = null;
        PdfWriter writer = null;
        boolean isFileConverted = false;
        File file = null;
        if (fontSize == 0) {
            fontSize = 100;
        }
        try {
            writer = PdfWriter.getInstance(document, outputStream);
            if (!StringUtils.isBlank(waterMark)) {
                BaseColor waterMarkColor = new BaseColor(waterMarkColorRed, waterMarkColorGreen, waterMarkBlue);
                writer.setPageEvent(new Watermark(waterMark, waterMarkColor));
            }
            writer.setViewerPreferences(PdfWriter.FitWindow) ;
            writer.setStrictImageSequence(true);
            if (!isAllowPrinting) {
                writer.setEncryption(null, null, 0, PdfWriter.ENCRYPTION_AES_256);
            }
            if (!StringUtils.isBlank(waterMark)) {
                BaseColor waterMarkColor = new BaseColor(waterMarkColorRed, waterMarkColorGreen, waterMarkBlue);
                writer.setPageEvent(new Watermark(waterMark, waterMarkColor, fontSize, isFillWaterMark, opacity, keepOriginalPageSize));
            }
            document.open();
            PdfContentByte pdfContentByte = writer.getDirectContent();
            long startTime = System.currentTimeMillis();
            for (String tiffFileName : tiffFileNames) {
                file = new File(tiffFileName);
                if (!file.exists()) {
                    logger.warn("The File does not Exists in this location" + tiffFileName);
                    return false;
                }
                RandomAccessSourceFactory randomAccessSourceFactory =  new RandomAccessSourceFactory();
                RandomAccessSource randomAccessSource = randomAccessSourceFactory.createBestSource(tiffFileName);
                randomAcessFileOrArray = new RandomAccessFileOrArray(randomAccessSource);
                logger.debug("The TIFF File name is " + tiffFileName);
                int noOfPages = 0;
                noOfPages = TiffImage.getNumberOfPages(randomAcessFileOrArray);
                for (int page = 1; page <= noOfPages; ++page) {
                    Image img = TiffImage.getTiffImage(randomAcessFileOrArray, page);
                    if (img != null) {
                    	if (keepOriginalPageSize) {
                    		Rectangle rect = new RectangleReadOnly(PageSize.A4);
                    		if (img.getWidth() < 14400 && img.getHeight() < 14400) {
                    			rect = new RectangleReadOnly(img.getWidth(), img.getHeight());
                    		} else {
                    			if (img.getWidth() > 14400) {
                    				float factor = 14400 /img.getWidth();
                    				rect = new RectangleReadOnly((img.getWidth() * factor), img.getHeight() * factor);
                    			} else if  (img.getHeight() > 14400) {
                    				float factor = 14400 /img.getHeight();
                    				rect = new RectangleReadOnly((img.getWidth() * factor), img.getHeight() * factor);
                    			}
                    		}
                    		document.setPageSize(rect);
                    	} else {
                    		 if (img.getWidth() > img.getHeight()) {
                    			 document.setPageSize(PageSize.A4);
     	                        img.setInitialRotation(1.571f);
                    		 } else {
                    			document.setPageSize(PageSize.A4);
     	                        img.setInitialRotation(0.0f);
                    		 }
                    	}
                        img.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
                        document.setMargins(0, 0, 0, 0);
                        document.newPage();
                        img.setAbsolutePosition(0, 0);
                        pdfContentByte.addImage(img);
                    }
                }
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
                if (randomAcessFileOrArray != null)randomAcessFileOrArray.close();
                if (document != null)document.close();
                if (writer != null)writer.close();
            } catch (Exception e) {
                logger.error("Error Closing the Stream", e);
            }
        }
        return isFileConverted;
    }

    public static boolean convert(String tiffFileOrFolder, String waterMark, int waterMarkColorRed,
            int waterMarkColorGreen, int waterMarkBlue, int fontSize, int numberOfPages, boolean isPrintingAllowed,
                OutputStream outputStream, boolean isFillWaterMark, float opacity, boolean keepOriginalPageSize) {
        File fileOrDirectory = new File(tiffFileOrFolder);
        if (fileOrDirectory.isDirectory()) {
            return convertFolder(tiffFileOrFolder, waterMark, waterMarkColorRed,
                waterMarkColorGreen, waterMarkColorGreen, fontSize, isPrintingAllowed, outputStream, isFillWaterMark, opacity, keepOriginalPageSize);
        } else {
            return convertFile(tiffFileOrFolder, waterMark, waterMarkColorRed,
                waterMarkColorGreen, waterMarkColorGreen, fontSize, numberOfPages, isPrintingAllowed, outputStream,
                    isFillWaterMark, opacity, keepOriginalPageSize);
        }
    }

    private static boolean convertFile(String tiffFileName, String waterMark, int waterMarkColorRed,
            int waterMarkColorGreen, int waterMarkBlue, int fontSize, int numerOfPages, boolean isPrintingAllowed,
            OutputStream outputStream, boolean isFillWaterMark, float opacity, boolean keepOriginalPageSize) {
        logger.debug("The TIFF File name is " + tiffFileName);
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        RandomAccessFileOrArray randomAcessFileOrArray = null;
        PdfWriter writer = null;
        boolean isFileConverted = false;
        File file = null;
        try {
            file = new File(tiffFileName);
            if (!file.exists()) {
                logger.warn("The File does not Exists in this location" + tiffFileName);
                return false;
            }
            RandomAccessSourceFactory randomAccessSourceFactory =  new RandomAccessSourceFactory();
            RandomAccessSource randomAccessSource = randomAccessSourceFactory.createBestSource(tiffFileName);
            randomAcessFileOrArray = new RandomAccessFileOrArray(randomAccessSource);
            int actualPageCount = 0;
            actualPageCount = TiffImage.getNumberOfPages(randomAcessFileOrArray);
            if (numerOfPages == 0 || numerOfPages > actualPageCount) {
                numerOfPages = actualPageCount;
            }
            if (fontSize == 0) {
                fontSize = 100;
            }
            writer = PdfWriter.getInstance(document, outputStream);
            if (!StringUtils.isBlank(waterMark)) {
                BaseColor waterMarkColor = new BaseColor(waterMarkColorRed, waterMarkColorGreen, waterMarkBlue);
                writer.setPageEvent(new Watermark(waterMark, waterMarkColor, fontSize, isFillWaterMark, opacity, keepOriginalPageSize));
            }
            if (!isPrintingAllowed) {
                writer.setEncryption(null, null, 0, PdfWriter.ENCRYPTION_AES_256);
            }
            writer.setViewerPreferences(PdfWriter.FitWindow) ;
            writer.setStrictImageSequence(true);
            document.open();
            PdfContentByte pdfContentByte = writer.getDirectContent();
            long startTime = System.currentTimeMillis();
            for (int page = 1; page <= numerOfPages; ++page) {
                Image img = TiffImage.getTiffImage(randomAcessFileOrArray, page);
                if (img != null) {
                	if (keepOriginalPageSize) {
                		Rectangle rect = new RectangleReadOnly(PageSize.A4);
                		if (img.getWidth() < 14400 && img.getHeight() < 14400) {
                			rect = new RectangleReadOnly(img.getWidth(), img.getHeight());
                		} else {
                			if (img.getWidth() > 14400) {
                				float factor = 14400 /img.getWidth();
                				rect = new RectangleReadOnly((img.getWidth() * factor), img.getHeight() * factor);
                			} else if  (img.getHeight() > 14400) {
                				float factor = 14400 /img.getHeight();
                				rect = new RectangleReadOnly((img.getWidth() * factor), img.getHeight() * factor);
                			}
                		}
                		document.setPageSize(rect);
                	} else {
                		 if (img.getWidth() > img.getHeight()) {
                			 document.setPageSize(PageSize.A4);
 	                        img.setInitialRotation(1.571f);
                		 } else {
                			document.setPageSize(PageSize.A4);
 	                        img.setInitialRotation(0.0f);
                		 }
                	}
                    img.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
                    document.setMargins(0, 0, 0, 0);
                    document.newPage();
                    img.setAbsolutePosition(0, 0);
                    pdfContentByte.addImage(img);
                }
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
                if (randomAcessFileOrArray != null)randomAcessFileOrArray.close();
                if (document != null)document.close();
                if (writer != null)writer.close();
            } catch (Exception e) {
                logger.error("Error Closing the Stream", e);
            }
        }
        return isFileConverted;
    }

    private static boolean convertFolder(String tiffFolderName, String waterMark, int waterMarkColorRed,
            int waterMarkColorGreen, int waterMarkBlue, int fontSize, boolean isPrintingAllowed, OutputStream outputStream,
            boolean isFillWaterMark, float opacity, boolean keepOriginalPageSize) {
        String fileName;
        File folder = new File(tiffFolderName);
        File[] listOfFiles = folder.listFiles();
        List<String> listOfTiffFiles =  new ArrayList<String>();
        for (int i = 0; i < listOfFiles.length; i++) {
           if (listOfFiles[i].isFile()) {
               fileName = listOfFiles[i].getPath();
               if (fileName.endsWith(".tif")) {
                   listOfTiffFiles.add(fileName);
              }
           }
        }
        return convert(listOfTiffFiles.toArray(new String[0]), waterMark, waterMarkColorRed,
                waterMarkColorGreen, waterMarkBlue, fontSize, isPrintingAllowed, outputStream, isFillWaterMark, opacity, keepOriginalPageSize);
    }

    public static boolean isImageAvailable(String tiffFileName) {
        logger.debug("The TIFF File name is " + tiffFileName);
        boolean isImageAvailable = true;
        File file = null;
        file = new File(tiffFileName);
        if (!file.exists()) {
            logger.debug("The File does not Exists in this location" + tiffFileName);
            file = new File(tiffFileName + ".burned");
            if (file.exists()) {
                isImageAvailable = true;
            } else {
                isImageAvailable = false;
            }
        }
        return isImageAvailable;
    }

    public static void main(String[] args) throws FileNotFoundException {
        OutputStream outputStream = new FileOutputStream("C:\\temp\\nw3.pdf");
        System.out.println("The File Exist" + convert("C:\\temp\\nw3.tif",
        										"UNOFFICIAL", 0,0,0,0,0, true, outputStream, false, 0.5f, true));
//		OutputStream outputStream = new FileOutputStream("C:\\Sample\\Merge.pdf");
//		String[] fileNames = {"C:\\sample\\Bexar\\00000001_00000001.tif" ,
//							  "C:\\sample\\Bexar\\00000001_00000002.tif",
//							  "C:\\sample\\Bexar\\00000001_00000003.tif"};
//		System.out.println("The File Exist" + convert(fileNames, "UNOFFICIAL",  0,0,0, outputStream));

//		OutputStream outputStream = new FileOutputStream("C:\\sample\\Bexar\\Merge.pdf");
//		System.out.println("The File Exist" + convert("C:\\sample\\Bexar\\", "UNOFFICIAL",  0,0,0, outputStream));

//		System.out.println("The File Exist " + isImageAvailable("C:\\sample\\20070022865.tif"));

    }
}

class Watermark extends PdfPageEventHelper {

	private static Logger logger = LoggerFactory.getLogger(PdfPageEventHelper.class.getName());

    /** The Graphics State for the watermark. **/
    protected PdfGState gstate;

    /** Stores the Water mark Text **/
    private String waterMark = null;

    /** Stores the Water mark Color **/
    private BaseColor waterMarkColor = null;

    /** Stores the Font Size **/
    private int fontSize = 100;

    /** Stores Watermark type fill or outline **/
    private boolean isFillWaterMark = false;

    /** Identifies whether to convert the pages of the document to letter or leave them at original size **/
    private boolean keepOriginalPageSize = false;

    /** Stores Watermark type fill or outline **/
    private float opacity = 0.5f;

    /** The font that will be used. */
    protected BaseFont watermarkFont;

    /** The Water mark message. */
    public Watermark(String waterMark, BaseColor waterMarkColor) {
        this.waterMark = waterMark;
        this.waterMarkColor = waterMarkColor;
        try {
			watermarkFont = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
		} catch (DocumentException | IOException e) {
			logger.error("The Font is either Invalid or the font file could not be read");
		}
    }

    /** The Water mark message. */
    public Watermark(String waterMark, BaseColor waterMarkColor, int fontSize) {
        this.waterMark = waterMark;
        this.waterMarkColor = waterMarkColor;
        this.fontSize = fontSize;
        try {
            watermarkFont = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
		} catch (DocumentException | IOException e) {
			logger.error("The Font is either Invalid or the font file could not be read");
		}
    }

    /** The Water mark message. */
    public Watermark(String waterMark, BaseColor waterMarkColor, int fontSize, boolean isFillWaterMark, boolean keepOriginalPageSize) {
        this.waterMark = waterMark;
        this.waterMarkColor = waterMarkColor;
        this.fontSize = fontSize;
        this.isFillWaterMark = isFillWaterMark;
        try {
            watermarkFont = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
		} catch (DocumentException | IOException e) {
			logger.error("The Font is either Invalid or the font file could not be read");
		}
    }

    /** The Water mark message. */
    public Watermark(String waterMark, BaseColor waterMarkColor, int fontSize, boolean isFillWaterMark, float opacity, boolean keepOriginalPageSize) {
        this.waterMark = waterMark;
        this.waterMarkColor = waterMarkColor;
        this.fontSize = fontSize;
        this.isFillWaterMark = isFillWaterMark;
        this.opacity = opacity;
        this.keepOriginalPageSize = keepOriginalPageSize;
        try {
            watermarkFont = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
		} catch (DocumentException | IOException e) {
			logger.error("The Font is either Invalid or the font file could not be read");
		}
    }

    public void onOpenDocument(PdfWriter writer, Document document) {
        gstate = new PdfGState();
        gstate.setFillOpacity(this.opacity);
        gstate.setStrokeOpacity(this.opacity);
    }

    /* (non-Javadoc)
     * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
     */
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte contentunder = writer.getDirectContent();
        contentunder.saveState();
        contentunder.setGState(gstate);
        contentunder.beginText();
        /**USE FILL TO FILL THE TEXT AND STROKE TO JUST OUTLINE THE TEXT. FILL HOWEVER WILL CAUSE DISTORTION OF THE IMAGE
        WHILE PRINTING **/
        if (isFillWaterMark) {
            contentunder.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);
            contentunder.setColorFill(this.waterMarkColor);
        } else {
            contentunder.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_STROKE);
            contentunder.setColorStroke(this.waterMarkColor);
        }

        float docWidth = document.getPageSize().getWidth();
        float docHeight = document.getPageSize().getHeight();

        String trimmedWaterMark = waterMark.trim();
        int waterMarkWidth = watermarkFont.getWidth(trimmedWaterMark);

        int calculatedFontSize = (int) ((1000.0f / (float) waterMarkWidth) * docWidth);
        int maxFontSize = (int) ((document.getPageSize().getWidth() / 310.0f) * fontSize);
        
        int finalFontSize = Math.min(maxFontSize, calculatedFontSize);

        float ascentPoint = watermarkFont.getAscentPoint(trimmedWaterMark, finalFontSize);
        float descentPoint = watermarkFont.getDescentPoint(trimmedWaterMark, finalFontSize);
        float waterMarkHeight = ascentPoint - descentPoint;

        contentunder.setFontAndSize(watermarkFont, finalFontSize * 1.2f);
        contentunder.showTextAligned(Element.ALIGN_CENTER, waterMark.trim(), (docWidth / 2) + (waterMarkHeight / 4),
                (docHeight / 2) - (waterMarkHeight / 4), 45);
        contentunder.endText();
        contentunder.restoreState();
    }
}
