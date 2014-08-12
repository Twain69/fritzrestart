package com.flegler.fritzrestart;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebCrawler {

	private static final String USERAGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36";

	private final String fritzIp;
	private final String baseUrl;
	private final String password;

	public WebCrawler(String fritzIp, String password) {
		this.fritzIp = fritzIp;
		this.password = password;
		this.baseUrl = "http://" + fritzIp;
	}

	public boolean restartFritzBox() {
		String uiResp = createUiResp(getChallenge(), password);
		System.out.println("uiResp: " + uiResp);

		String sid = getSid(uiResp);

		System.out.println("sid: " + sid);

		return reboot(sid);
	}

	public String createUiResp(String g_challenge, String password) {
		String response = g_challenge + "-" + password;
		String uiResp = null;
		try {
			uiResp = g_challenge + "-"
					+ DigestUtils.md5Hex(response.getBytes("UTF-16LE"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return uiResp;
	}

	private String getChallenge() {
		Document elements = null;
		try {
			System.out.print("Fetching " + baseUrl + "/login.lua ... ");
			Long start = System.currentTimeMillis();
			elements = Jsoup.connect(baseUrl + "/login.lua")
					.userAgent(USERAGENT).get();
			Long end = System.currentTimeMillis();
			Long durationSeconds = end - start;
			System.out.println("done (" + durationSeconds + " milliseconds)");
		} catch (IOException e) {
			e.printStackTrace();
		}

		String challenge = null;

		Pattern g_challengePattern = Pattern
				.compile("g_challenge = \"([a-zA-Z0-9]*)\"");

		for (Element element : elements.getAllElements()) {
			if (challenge != null) {
				break;
			}
			Matcher m = g_challengePattern.matcher(element.toString());
			while (m.find()) {
				if (!m.group(1).trim().equals("")) {
					challenge = m.group(1).trim();
					System.out.println("challenge: " + challenge);
				}
			}
		}
		if (challenge == null) {
			System.err.println("Could not find g_challenge on login page");
			System.exit(1);
		}

		return challenge;
	}

	private String getSid(String uiResp) {
		try {
			System.out.print("Logging in ... ");
			Long start = System.currentTimeMillis();
			Response resp = Jsoup.connect(baseUrl + "/login.lua")
					.userAgent(USERAGENT).data("response", uiResp)
					.followRedirects(false).execute();
			Long end = System.currentTimeMillis();
			Long duration = end - start;
			System.out.println("done (" + duration + " milliseconds)");
			String param = resp.headers().get("Location").split("\\?")[1];
			String sid = param.split("=")[1];

			return sid;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean reboot(String sid) {
		try {
			System.out.print("Sending reboot command .., ");
			Long start = System.currentTimeMillis();

			Connection con = Jsoup.connect(baseUrl + "/system/reboot.lua")
					.userAgent(USERAGENT).header("Origin", baseUrl)
					.data("sid", sid).data("reboot", "").followRedirects(false)
					.method(Method.POST);
			Response resp = con.execute();

			Long end = System.currentTimeMillis();
			Long duration = end - start;
			System.out.println("done (" + duration + " milliseconds)");

			System.out.println(resp.headers());

			System.out.print("Sending second reboot command .., ");
			start = System.currentTimeMillis();
			Connection con2 = Jsoup.connect(baseUrl + "/reboot.lua?sid=" + sid)
					.userAgent(USERAGENT).header("Origin", baseUrl)
					.referrer(baseUrl + "/system/reboot.lua?sid=" + sid)
					.followRedirects(false).method(Method.GET);

			Response resp2 = con2.execute();

			end = System.currentTimeMillis();
			duration = end - start;
			System.out.println("done (" + duration + " milliseconds)");

			System.out.println(resp2.headers());
			// System.out.println("Status: " + resp.statusCode());
			// System.out.println(resp.parse().getAllElements());
			// System.out.println(doc2.select("html"));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}
}