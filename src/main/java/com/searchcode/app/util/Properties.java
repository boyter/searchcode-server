/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.util;

import com.searchcode.app.config.Values;
import com.searchcode.app.service.Singleton;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;


/**
 * Small helper used to load the properties file
 */
public class Properties {

    private static java.util.Properties properties = null;

    public static synchronized java.util.Properties getProperties() {
        if (properties == null) {
            properties = new java.util.Properties();
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(Values.PROPERTIES_FILE_NAME);
                properties.load(fileInputStream);
            } catch (IOException ex) {
                // As this is a static method the use of singleton here is fine
                Singleton.getLogger().severe(String.format("deb3c728::error in class %s exception %s unable to load searchcode.properties file will use defaults for all values", ex.getClass(), ex.getMessage()));
            }
            finally {
                IOUtils.closeQuietly(fileInputStream);
            }
        }

        return properties;
    }
}
