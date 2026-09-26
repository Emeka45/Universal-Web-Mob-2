package com.coeric.universalwebmob2.web;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class StartPagesTest {
    @Test public void workflowUrlsAreHttps() {
        assertTrue(StartPages.CHATGPT.startsWith("https://"));
        assertTrue(StartPages.CLOUDFLARE.startsWith("https://"));
        assertTrue(StartPages.GITHUB.startsWith("https://"));
    }

    @Test public void homeUrlIsInternal() {
        assertTrue(StartPages.HOME.startsWith("about:"));
    }
}
