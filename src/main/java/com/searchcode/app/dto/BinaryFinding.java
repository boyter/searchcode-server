package com.searchcode.app.dto;

/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

public class BinaryFinding {
    private boolean isBinary;
    private String reason;

    public BinaryFinding(boolean isBinary, String reason) {
        this.setIsBinary(isBinary);
        this.setReason(reason);
    }

    public boolean isBinary() {
        return isBinary;
    }

    public void setIsBinary(boolean isBinary) {
        this.isBinary = isBinary;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
