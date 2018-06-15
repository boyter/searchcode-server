/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
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
            } catch (IOException e) {
                // TODO Use second 'stdout' logger here, because ctor LoggerWrapper call this method
                Singleton.getLogger().severe("Unable to load 'searchcode.properties' file. Will resort to defaults for all values.");
            }
            finally {
                IOUtils.closeQuietly(fileInputStream);
            }
        }

        return properties;
    }
}
