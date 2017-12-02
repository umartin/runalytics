package com.github.umartin.runalytics;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import org.junit.Test;

import static org.junit.Assert.*;

public class GPXParserTest {

	@Test
	public void testParseFile() throws Exception {
		try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("with_hr.gpx")) {
			Activity activity = GPXParser.parseFile(resourceAsStream);

			assertEquals(Instant.parse("2017-10-07T18:31:05Z"), activity.time);
			assertEquals(755, activity.trackpoints.size());
			assertEquals(124.0D, activity.trackpoints.get(0).heartRate, 0.1D);
			assertEquals(26.0D, activity.trackpoints.get(0).elevation, 0.1D);
			assertEquals(11.958370000D, activity.trackpoints.get(0).longitude, 0.1D);
			assertEquals(57.703038000D, activity.trackpoints.get(0).latitude, 0.1D);
			assertEquals(Instant.parse("2017-10-07T18:31:05Z"), activity.trackpoints.get(0).time);

//			assertEquals(8080.0D, activity.distance, 100.0D);
		}
	}
}
