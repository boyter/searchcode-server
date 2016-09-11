/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 *
 * After the following date 27 August 2019 this software version '1.2.3' or '1.2.4' is dual licenced under the
 * Fair Source Licence included in the LICENSE.txt file or under the GNU General Public License Version 3 with terms
 * specified at https://www.gnu.org/licenses/gpl-3.0.txt
 */

package com.searchcode.app.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecentCache<A, B> extends LinkedHashMap<A, B> {
    private final int maxLength;

    public RecentCache(final int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
        return this.size() > maxLength;
    }
}
