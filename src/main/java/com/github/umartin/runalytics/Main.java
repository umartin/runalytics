package com.github.umartin.runalytics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Main {

	public static void main(String... args) throws IOException {
		System.out.println("akkasd");
		for (String arg : args) {
			Path path = Paths.get(arg);
			handlePath(path);
		}
	}

	private static void handlePath(Path path) throws IOException {
		if (Files.isRegularFile(path)) {
			handleFile(path);
		} else if (Files.isDirectory(path)) {
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
				for (Path dirElement : directoryStream) {
					handlePath(dirElement);
				}
			}
		}
	}

	private static void handleFile(Path path) throws IOException {
		if (path.getFileName().toString().endsWith(".gpx")) {
			try (InputStream is = Files.newInputStream(path)) {
				parseAndStore(path.getFileName().toString(), is);
			}
		} else if (path.getFileName().toString().endsWith("zip")) {
			try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(path))) {

				ZipEntry ze;
				while ((ze = zis.getNextEntry()) != null) {
					if (ze.getName().endsWith(".gpx")) {
						parseAndStore(ze.getName(), zis);
					}
				}
			}
		} else {
			throw new RuntimeException("unknown filetype: " + path.getFileName());
		}
	}

	private static void parseAndStore(String filename, InputStream is) {
		System.out.println(String.format("Parsing %s", filename));
		Activity activity = GPXParser.parseFile(is);

		Gson gson = Converters.registerAll(new GsonBuilder()).create();

		String json = gson.toJson(activity);

		new PostgresStorage().storeFile(filename, json);
	}
}
