/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.0
 */

package com.searchcode.app.util;

import com.searchcode.app.service.Singleton;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * Small helper used to load the properties file
 */
public class Properties {

    private static final LoggerWrapper LOGGER = Singleton.getLogger();

    private static java.util.Properties properties = null;

    public static java.util.Properties getProperties() {
        if(properties == null) {
            properties = new java.util.Properties();
            try {
                properties.load(new FileInputStream("searchcode.properties"));
            } catch (IOException e) {
                LOGGER.severe("Unable to load 'searchcode.properties' file. Will resort to defaults for all values.");
            }
        }

        return properties;
    }
}
