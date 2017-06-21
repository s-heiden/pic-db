package BL;

import BIF.SWE2.interfaces.*;
import BIF.SWE2.interfaces.models.*;
import DAL.DBTable;
import DAL.SQLiteDAL;
import helpers.Helpers;
import static helpers.Helpers.deleteFromPicturePath;
import static helpers.Helpers.existsInPicturePath;
import static helpers.Helpers.getRandomString;
import Models.*;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import helpers.Constants;
import static helpers.Constants.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

/**
 * The BusinessLayer implementation used by the application.
 */
public final class BL implements BusinessLayer {

    private static BL blInstance;
    private static SQLiteDAL dal;
<<<<<<< HEAD
     
=======
    
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
    /**
     * Returns the single static instance.
     */
    public static BL getInstance() {
        if (blInstance == null) {
            blInstance = new BL();
        }
        return blInstance;
    }

    private BL() {
        if (dal == null) {
            dal = SQLiteDAL.getInstance();
        }
        sync();
    }

    @Override
    public Collection<PictureModel> getPictures() {
        try {
            return dal.getPictures(
                    "",
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public Collection<PictureModel> getPictures(
            String searchString,
            PhotographerModel photographerParts,
            IPTCModel iptcParts,
            EXIFModel exifParts
    ) throws Exception {
        if (photographerParts != null || iptcParts != null || exifParts != null) {
            throw new UnsupportedOperationException("Detail search not implemented yet, "
                    + "please use argument searchString for the search term");
        }        
        return dal.getPictures(searchString, null, null, null);
    }

    @Override
    public PictureModel getPicture(int ID) {
        try {
            return dal.getPicture(ID);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public void save(PictureModel picture) throws Exception {
        if (picture.getID() < 0) { // checks if no valid ID has been set yet
            picture.setID(((SQLiteDAL) dal).nextIdFor(DBTable.PICTURES));
        }
        dal.save(picture);
    }

    @Override
    public void deletePicture(int ID) {
        String filename = getPicture(ID).getFileName();
        deleteFromPicturePath(filename);
        try {
            dal.deletePicture(ID);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void sync() {
        File pictureDirectory = new File(PICTURE_PATH);
        if (pictureDirectory.isDirectory()) {
            List<String> dirListing = Arrays.asList(pictureDirectory.list());
            List<String> filenamesInModel = new ArrayList<>();

            // if a picture does not exist in the directory but in the model + db
            for (PictureModel picture : getPictures()) {
                filenamesInModel.add(picture.getFileName());
                if (!dirListing.contains(picture.getFileName())) {
                    deletePicture(picture.getID());
                }
            }
            // add a picture to the model/database if it exists in the directory but not in the model/db
            for (String picDirFilename : dirListing) {
                // TODO: check the file suffix for a image file
                if (!filenamesInModel.contains(picDirFilename)) {
                    PictureModel picture = new Picture();
                    picture.setFileName(picDirFilename);
                    try {
                        picture.setEXIF(extractEXIF(picture.getFileName()));
                        picture.setIPTC(extractIPTC(picture.getFileName()));
                        save(picture);
                    } catch (Exception e) {
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public Collection<PhotographerModel> getPhotographers() throws Exception {
        return dal.getPhotographers();
    }

    @Override
    public PhotographerModel getPhotographer(int ID) throws Exception {
        return dal.getPhotographer(ID);
    }

    @Override
    public void save(PhotographerModel photographer) throws Exception {
        if (photographer.getID() < 0) {
            photographer.setID(((SQLiteDAL) dal).nextIdFor(DBTable.PHOTOGRAPHERS));
            System.out.println("Trying to save photographer: " + photographer.getID());
        }
        dal.save(photographer);
    }

    @Override
    public void deletePhotographer(int ID) throws Exception {
        dal.deletePhotographer(ID);
    }

    @Override
    public IPTCModel extractIPTC(String filename) throws FileNotFoundException {
        if (existsInPicturePath(filename)) {
            String randomString = getRandomString(5);
            IPTCModel iptcModel = new Iptc();
            iptcModel.setByLine("byLine_" + randomString);
            iptcModel.setKeywords("keywords_" + randomString);
            iptcModel.setCopyrightNotice(Constants.COPYRIGHT_NOTICES.toArray()[new Random().nextInt(Constants.COPYRIGHT_NOTICES.size())].toString());
            iptcModel.setHeadline("headline_" + randomString);
            iptcModel.setCaption("caption_" + randomString);
            return iptcModel;
        } else {
            throw new FileNotFoundException("Filename " + filename + " does not exist in " + PICTURE_PATH);
        }
    }

    @Override
    public EXIFModel extractEXIF(String filename) throws FileNotFoundException {
        if (Helpers.existsInPicturePath(filename)) {
            EXIFModel exifModel = new Exif();
            exifModel.setExposureProgram(Helpers.randomEnum(ExposurePrograms.class));
            exifModel.setExposureTime(Constants.EXAMPLE_EXPOSURE_TIMES[new Random().nextInt(Constants.EXAMPLE_EXPOSURE_TIMES.length)]);
            exifModel.setFNumber(Constants.EXAMPLE_F_NUMBERS[new Random().nextInt(Constants.EXAMPLE_F_NUMBERS.length)]);
            exifModel.setFlash(Math.random() > 0.5);
            exifModel.setISOValue(Constants.EXAMPLE_ISO_VALUES[new Random().nextInt(Constants.EXAMPLE_ISO_VALUES.length)]);
            exifModel.setMake((Math.random() > 0.5 ? "Leica " : "Polaroid "));
            return exifModel;
        } else {
            throw new FileNotFoundException("Filename " + filename + " does not exist in " + PICTURE_PATH);
        }
    }

    @Override
    public void writeIPTC(String filename, IPTCModel iptc) throws Exception {
        if (existsInPicturePath(filename)) {
            Thread.sleep(50);
        } else {
            throw new FileNotFoundException();
        }
    }

    @Override
    public Collection<CameraModel> getCameras() {
        return dal.getCameras();
    }

    @Override
    public CameraModel getCamera(int ID) {
        return dal.getCamera(ID);
    }
    
<<<<<<< HEAD
    /**
     * makes a directory, if it does not exist yet.
     * 
     * @param dirPath
     * @throws SecurityException if it fails to make the directory
     */
=======
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
    public void makeDir(String dirPath) throws SecurityException {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            try {
                dir.mkdir();
            } catch (SecurityException e) {
                throw new SecurityException("Could not make directory " + dirPath + ".");
            }   
        }
    }
     
<<<<<<< HEAD
    /**
     * generates a report pdf-file in the reports directory
     * which lists all keywords that occur and the number of their occurrences.
     */
=======
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
    public void generateTagReport() {
        Map<String, Integer> keywordStrings = dal.getKeywordStrings();
        Map<String, Integer> keywordCount = new HashMap<>();
        String[] splitString;
        for (String keywordString : keywordStrings.keySet()) {
<<<<<<< HEAD
            splitString = keywordString.split("[,;\\s]");
=======
            splitString = keywordString.split("[,;\r\n]");
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
            for (String keyword : splitString) {
                keyword = keyword
                        .toUpperCase()
                        .replaceAll("[^-_0-9a-zA-Z]", "");
                if (keywordCount.containsKey(keyword)) {
<<<<<<< HEAD
                    keywordCount.put(keyword, keywordCount.get(keyword) + keywordStrings.get(keywordString));
                } else {
                    keywordCount.put(keyword, keywordStrings.get(keywordString));
=======
                    keywordCount.put(keyword, keywordCount.get(keyword) + 1);
                } else {
                    keywordCount.put(keyword, 1);
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
                }
            }
        }
        
        Document document = new Document();
        try {
            makeDir(REPORT_PATH);
            PdfWriter.getInstance(document, new FileOutputStream(REPORT_PATH + "/"
                    + TAG_REPORT_FILENAME + ".pdf"));
            document.open();
            document.addTitle("Tag-Report");
            Font textFont = new Font(Font.getFamily(TEXT_FONT_FAMILY),
                    TEXT_FONT_SIZE, Font.getStyleValue(TEXT_FONT_STYLE));
            Font headlineFont = new Font(Font.getFamily(HEADLINE_FONT_FAMILY),
                    HEADLINE_FONT_SIZE, Font.getStyleValue(HEADLINE_FONT_STYLE));
            PdfPTable table = new PdfPTable(2);
            table.addCell(new Phrase("Keyword / Tag", headlineFont));
            table.addCell(new Phrase("Anzahl der Fotos", headlineFont));
            for (String keyword : keywordCount.keySet()) {
                table.addCell(keyword);
                table.addCell(keywordCount.get(keyword).toString());
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
    /**
     * generates an report pdf-file of the active Image, including the picture,
     * the EXIF- and IPTC-Information and the Photographer, if known.
     * @param ID  - must be a valid picture-ID
     */
=======
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
    public void generateImageReport(int ID) {
        PictureModel picture = getPicture(ID);
        Document document = new Document();
        try {
            makeDir(REPORT_PATH);
            PdfWriter.getInstance(document, new FileOutputStream(REPORT_PATH + "/"
<<<<<<< HEAD
                    + IMG_REPORT_FILENAME + "_"
                    + picture.getFileName().toString() + ".pdf"));
=======
                    + IMG_REPORT_FILENAME + ".pdf"));
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
            document.open();
            document.addTitle("Image-Report");
            Image image = Image.getInstance(PICTURE_PATH + "/"
                    + picture.getFileName().toString());
            image.scaleToFit(525f, 750f);
            document.add(image);
            
<<<<<<< HEAD
            System.out.println(document.toString() + picture.toString());
            
=======
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
            Font textFont = new Font(Font.getFamily(TEXT_FONT_FAMILY),
                    TEXT_FONT_SIZE, Font.getStyleValue(TEXT_FONT_STYLE));
            Font headlineFont = new Font(Font.getFamily(HEADLINE_FONT_FAMILY),
                    HEADLINE_FONT_SIZE, Font.getStyleValue(HEADLINE_FONT_STYLE));
            
            Paragraph EXIFPara = new Paragraph();
            EXIFPara.setSpacingBefore(20.0f);
            EXIFPara.setFirstLineIndent(10.0f);
            Paragraph IPTCPara = (Paragraph)EXIFPara.clone();
            Paragraph PhotographerPara = (Paragraph)EXIFPara.clone();
            
            EXIFPara.add(new Chunk("EXIFPara-Informationen:\n\n", headlineFont));
            EXIFPara.add(new Chunk(parseListFormatted(picture.getEXIF()), textFont).setLineHeight(15.0f));
            document.add(EXIFPara);
            
<<<<<<< HEAD
            IPTCPara.add(new Chunk("IPTC-Informationen:\n\n", headlineFont));
            IPTCPara.add(new Chunk(parseListFormatted(picture.getIPTC()), textFont).setLineHeight(15.0f));
            document.add(IPTCPara);

=======
             IPTCPara.add(new Chunk("IPTC-Informationen:\n\n", headlineFont));
            IPTCPara.add(new Chunk(parseListFormatted(picture.getIPTC()), textFont).setLineHeight(15.0f));
            document.add(IPTCPara);

            PhotographerModel m = picture.getPhotographer();
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
            String photogr = parseListFormatted(picture.getPhotographer());
            PhotographerPara.add(new Chunk("Fotograf:\n\n", headlineFont));
            PhotographerPara.add(new Chunk(photogr != null ? photogr : "Fotograf is unbekannt.", textFont).setLineHeight(15.0f));
            document.add(PhotographerPara);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
    
<<<<<<< HEAD
    /**
     * converts an Object with JSON-like toString() functions into a
     * line by line paragraph relating keys an values
     * @param obj - must have JSON like toString() output; must not be null.
     * @return a formatted multi-line string
     * @throws IllegalArgumentException 
     */
    public String parseListFormatted(Object obj) throws IllegalArgumentException{
        if (obj != null) {
            return obj.toString()
=======
    public String parseListFormatted(Object o) throws NullPointerException{
        if (o != null) {
            return o.toString()
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
                    .replaceAll(".*\\{", "")
                    .replaceAll("}", "")
                    .replaceAll(", ", "\n");
        } else {
<<<<<<< HEAD
            throw new IllegalArgumentException("Input string was null.");
=======
            //throw new NullPointerException("Parsed String was null.");
            return "";
>>>>>>> c4e4f5eceab46d5640d26e8b62fda3ade1f01586
        }
    }
}
