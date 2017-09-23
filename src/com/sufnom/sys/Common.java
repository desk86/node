package com.sufnom.sys;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Common {
    public static String readFile(String fileAbsolutePath){
        InputStream stream = null;
        String result = null;
        try {
            File file = new File(fileAbsolutePath);
            stream = new FileInputStream(file);
            result = new String(IOUtils.toByteArray(stream));
        }
        catch (IOException e){e.printStackTrace();}
        finally {
            if (stream != null){
                try { stream.close(); }
                catch (IOException e){e.printStackTrace();}
            }
        }
        return result;
    }
}
