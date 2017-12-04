package com.github.umartin.runalytics;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Point;


public class GPXParser {

	static class GPXContentHandler extends DefaultHandler {

		static class Path {

			List<String> tags = new ArrayList<>();

			public void push(String tagsName) {
				tags.add(tagsName);
			}

			public void pop() {
				tags.remove(tags.size() - 1);
			}

			@Override
			public String toString() {
				return tags.stream().collect(Collectors.joining("/"));
			}
		}

		Path path = new Path();
		StringBuilder value = new StringBuilder();

		Activity activity = new Activity();
		Trackpoint currentTrackPoint = new Trackpoint();
		int i;


		@Override
		public void startElement(String uri, String localName, String qName, Attributes atrbts) throws SAXException {
			path.push(qName);
			if (value.length() > 0) {
				value = new StringBuilder();
			}
			if ("trkpt".equals(qName)) {
				currentTrackPoint = new Trackpoint();
				currentTrackPoint.latitude = Double.parseDouble(atrbts.getValue("lat"));
				currentTrackPoint.longitude = Double.parseDouble(atrbts.getValue("lon"));
			}
		}

		@Override
		public void characters(char[] chars, int start, int length) throws SAXException {
			value.append(chars, start, length);
		}

		@Override
		public void endElement(String string, String string1, String string2) throws SAXException {
			switch (path.toString()) {
				case "gpx/trk/time":
					activity.time = Instant.parse(value.toString());
					break;
				case "gpx/trk/trkseg/trkpt":
					activity.trackpoints.add(currentTrackPoint);
					break;
				case "gpx/trk/trkseg/trkpt/ele":
					currentTrackPoint.elevation = Double.parseDouble(value.toString());
					break;
				case "gpx/trk/trkseg/trkpt/time":
					currentTrackPoint.time = Instant.parse(value.toString());
					break;
				case "gpx/trk/trkseg/trkpt/extensions/gpxtpx:TrackPointExtension/gpxtpx:hr":
					currentTrackPoint.heartRate = Double.parseDouble(value.toString());
					break;

			}
			path.pop();
			if (value.length() > 0) {
				value = new StringBuilder();
			}
		}
	}

	static Activity parseFile(InputStream inputStream) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			
			GPXContentHandler gpxContentHandler = new GPXContentHandler();
			xmlReader.setContentHandler(gpxContentHandler);
			xmlReader.parse(new InputSource(new BufferedInputStream(inputStream) {
				@Override
				public void close() throws IOException {
					// do nothing. SAX-bug.
				}

			}));

			List<Point> points = gpxContentHandler.activity.trackpoints.stream()
					.map(trkpnt -> GeometryUtil.createWGS84Point(trkpnt.latitude, trkpnt.longitude))
					.collect(Collectors.toList());
			
			gpxContentHandler.activity.distance = GeometryUtil.calculateDistanceMeters(points);

			return gpxContentHandler.activity;
		} catch (IOException | SAXException | ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}
}
