package com.flegler.fritzrestart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * (c) Oliver Flegler (oliver@flegler.com)
 */
public class FritzRestart {

	private static File configFile;
	private static String fritzIp;
	private static String password;

	public static void main(String[] args) {
		parseConfiguration(args);
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(configFile));
		} catch (IOException e) {
			System.err.println("Unable to load configfile '"
					+ configFile.getName() + "'");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		fritzIp = prop.getProperty("fritzIp");
		password = prop.getProperty("password");

		WebCrawler fritzWebCrawler = new WebCrawler(fritzIp, password);
		if (fritzWebCrawler.restartFritzBox()) {
			System.exit(0);
		} else {
			System.exit(1);
		}
	}

	private static void parseConfiguration(String[] args) {
		Options options = new Options();
		options.addOption("h", "help", false, "Print this help");
		options.addOption("c", "config", true,
				"Config file that will be read in");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error while parsing the arguments");
			System.err.println(e.getMessage());
			System.exit(1);
		}

		Boolean help = cmd.hasOption("h");
		if (help) {
			printHelp(options);
		}

		String cfgFile = cmd.getOptionValue("c");
		if (cfgFile == null) {
			cfgFile = "/etc/fritzrestart.cfg";
		}
		configFile = new File(cfgFile);
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar jpostgrey.jar", options);
		System.exit(0);
	}
}