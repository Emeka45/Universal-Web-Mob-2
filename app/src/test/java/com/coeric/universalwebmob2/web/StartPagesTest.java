package com.coeric.universalwebmob2.web;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class StartPagesTest {
    @Test public void homeUrlIsInternal() {
        assertTrue(StartPages.HOME.startsWith("about:"));
    }
}
