package com.searchcode.app.dao.searchcode;

import com.searchcode.app.model.searchcode.CodeResult;
import junit.framework.TestCase;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CodeTest extends TestCase {
    public void testGetCodeBetween() {
        Code code = new Code();
        List<CodeResult> codeBetween = code.getCodeBetween(0, 100);
        assertThat(codeBetween).hasAtLeastOneElementOfType(CodeResult.class);
    }

    public void testGetMaxId() {
        Code code = new Code();
        int maxId = code.getMaxId();
        assertThat(maxId).isEqualTo(200);
    }
}
