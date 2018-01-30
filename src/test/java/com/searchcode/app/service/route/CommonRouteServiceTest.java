package com.searchcode.app.service.route;

import junit.framework.TestCase;

import java.util.Calendar;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


public class CommonRouteServiceTest extends TestCase {
    public void testGetPhotoId() {

        int photoId =  CommonRouteService.getPhotoId(Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        assertThat(photoId).isBetween(1, 42);

        for (int i = 0; i < 10000; i++) {
            photoId = CommonRouteService.getPhotoId(i);
            assertThat(photoId).isBetween(1, 42);
        }
    }
}
