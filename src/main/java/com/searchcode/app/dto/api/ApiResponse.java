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


package com.searchcode.app.dto.api;


public class ApiResponse {
    private boolean sucessful;
    private String message;

    public ApiResponse() {}

    public ApiResponse(boolean sucessful, String message) {
        this.setSucessful(sucessful);
        this.setMessage(message);
    }

    public boolean isSucessful() {
        return sucessful;
    }

    public void setSucessful(boolean sucessful) {
        this.sucessful = sucessful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
