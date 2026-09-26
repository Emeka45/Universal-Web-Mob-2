package com.coeric.universalwebmob2;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PackageConfigurationTest {
    @Test public void applicationIdIsCorrect() {
        assertEquals("com.coeric.universalwebmob2", BuildConfig.APPLICATION_ID);
    }
}
