package utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author vietan
 */
public class IOUtils {

    
    public static ZipOutputStream getZipOutputStream(String outptuFile) throws Exception {
        File f = new File(outptuFile);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        return out;
    }

    public static ZipInputStream getZipInputStream(String inputFile) throws Exception {
        File f = new File(inputFile);
        ZipInputStream in = new ZipInputStream(new FileInputStream(f));
        return in;
    }

    public static BufferedReader getBufferedReader(String filepath)
            throws FileNotFoundException, UnsupportedEncodingException {
//        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF-8"));
    	BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "GBK"));
        return in;
    }

    public static BufferedReader getBufferedReader(File file)
            throws FileNotFoundException, UnsupportedEncodingException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        return in;
    }

    public static BufferedReader getBufferedReader(String zipFilePath, String zipEntry) throws Exception {
        ZipFile zipFile = new ZipFile(zipFilePath);
        ZipEntry modelEntry = zipFile.getEntry(zipEntry);
        return getBufferedReader(zipFile, modelEntry);
    }

    public static BufferedReader getBufferedReader(ZipFile zipFile, ZipEntry modelEntry)
            throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(modelEntry), "UTF-8"));
        return reader;
    }

    public static BufferedWriter getBufferedWriter(String filepath)
            throws FileNotFoundException, UnsupportedEncodingException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath), "UTF-8"));
        return out;
    }

    public static BufferedWriter getBufferedWriter(File file)
            throws FileNotFoundException, UnsupportedEncodingException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        return out;
    }

    public static BufferedWriter getBufferedWriterAppend(File file)
            throws FileNotFoundException, UnsupportedEncodingException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
    }

    public static BufferedWriter getBufferedWriterAppend(String filepath)
            throws FileNotFoundException, UnsupportedEncodingException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath, true), "UTF-8"));
        return out;
    }

}
