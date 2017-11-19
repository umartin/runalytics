package com.github.umartin.runalytics;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.*;

public class GPXParserTest {

	@Test
	public void testParseFile() throws IOException {
		try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("with_hr.gpx")) {
			Activity activity = GPXParser.parseFile(resourceAsStream);

			assertEquals(754, activity.trackpoints.size());
			assertEquals(8080.0D, activity.distance, 100.0D);
		}
	}
}