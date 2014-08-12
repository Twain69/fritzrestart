package com.flegler.fritzrestart;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WebCrawlerTest {

	private static final String FRITZIP = "fritz.box";
	private static final String PASSWORD = "äbc";

	private WebCrawler crawler;

	@Before
	public void init() {
		this.crawler = new WebCrawler(FRITZIP, PASSWORD);
	}

	@Test
	public void testCreateUiResp() {
		String uiResp = crawler.createUiResp("1234567z", PASSWORD);
		Assert.assertEquals("1234567z-9e224a41eeefa284df7bb0f26c2913e2", uiResp);
	}

	@Test
	public void testMd5Working() {
		String md5 = DigestUtils.md5Hex("abc");
		Assert.assertEquals("900150983cd24fb0d6963f7d28e17f72", md5);
	}
}
