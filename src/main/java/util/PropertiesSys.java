package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jiawei.liu on 11/11/15.
 */
public class
        PropertiesSys {
    public static java.util.Properties properties;

    static {
        properties = new java.util.Properties();

        InputStream input = null;

        try {
            try {
                String filePath = new File("").getAbsolutePath();
                input = new FileInputStream(filePath + "/src/main/resources/config");
            } catch (Exception e) {
                input = PropertiesSys.class.getClassLoader().getResourceAsStream("config");
            }

            properties.load(input);

        } catch (IOException ex) {
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
