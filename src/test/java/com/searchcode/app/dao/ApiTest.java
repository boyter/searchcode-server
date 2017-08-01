package com.searchcode.app.dao;

import com.searchcode.app.config.SQLiteMemoryDatabaseConfig;
import com.searchcode.app.model.ApiResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ApiTest extends TestCase {

    private Api api;

    public void setUp() throws Exception {
        super.setUp();
        this.api = new Api(new SQLiteMemoryDatabaseConfig(), new Helpers());
        this.api.createTableIfMissing();
    }

    public void testMultipleCreateTable() {
        for (int i = 0; i < 200; i++) {
            this.api.createTableIfMissing();
        }
    }

    public void testSaveDelete() {
        String randomApiString = this.getRandomString();

        this.api.saveApi(new ApiResult(0, randomApiString, "privateKey", "", ""));
        this.api.deleteApiByPublicKey(randomApiString);
    }

    public void testSaveRetrieve() {
        String randomApiString = this.getRandomString();

        this.api.saveApi(new ApiResult(0, randomApiString, "privateKey", "", ""));
        Optional<ApiResult> apiByPublicKey = this.api.getApiByPublicKey(randomApiString);

        assertThat(apiByPublicKey.get().getPublicKey()).isEqualTo(randomApiString);
        assertThat(apiByPublicKey.get().getPrivateKey()).isEqualTo("privateKey");

        this.api.deleteApiByPublicKey(randomApiString);
    }

    public void testMultipleRetrieveCache() {
        String randomApiString = this.getRandomString();

        this.api.saveApi(new ApiResult(0, randomApiString, "privateKey", "", ""));

        for(int i=0; i < 500; i++) {
            Optional<ApiResult> apiByPublicKey = this.api.getApiByPublicKey(randomApiString);

            assertThat(apiByPublicKey.get().getPublicKey()).isEqualTo(randomApiString);
            assertThat(apiByPublicKey.get().getPrivateKey()).isEqualTo("privateKey");
        }

        this.api.deleteApiByPublicKey(randomApiString);
    }

    public void testGetAllApi() {
        String randomApi1 = this.getRandomString();
        String randomApi2 = this.getRandomString();

        this.api.saveApi(new ApiResult(0, randomApi1, "privateKey", "", ""));
        this.api.saveApi(new ApiResult(0, randomApi2, "privateKey", "", ""));

        assertThat(this.api.getAllApi().size()).isEqualTo(2);

        this.api.deleteApiByPublicKey(randomApi1);
        this.api.deleteApiByPublicKey(randomApi2);
    }

    private String getRandomString() {
        Random random = new Random();
        return RandomStringUtils.randomAscii(random.nextInt(20) + 20);
    }
}
