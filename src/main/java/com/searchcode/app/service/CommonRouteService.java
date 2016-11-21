package com.searchcode.app.service;

import com.searchcode.app.App;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;

public class CommonRouteService {
    public static String getLogo() {
        if(App.ISCOMMUNITY) {
            return Values.EMPTYSTRING;
        }

        Data data = Singleton.getData();
        return data.getDataByName(Values.LOGO, Values.EMPTYSTRING);
    }
}
