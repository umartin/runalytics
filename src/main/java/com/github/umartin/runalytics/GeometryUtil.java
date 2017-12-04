package com.github.umartin.runalytics;

import java.util.List;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;


public final class GeometryUtil {

	private static final int DEFAULT_PRECISION = 7;
	private static final int RT90 = 3021;
	private static final int WGS84 = 4326;

	private GeometryUtil() {
	}

	public static Point createWGS84Point(double  lon,double lat) {
		return wgs84toRT90(getGeometryFactory().createPoint(new Coordinate(lat, lon)));
	}

	public static double calculateDistanceMeters(List<Point> points) {
		Coordinate[] coordinates = points.stream().map(Point::getCoordinate).toArray(Coordinate[]::new);
		LineString lineString = getGeometryFactory().createLineString(coordinates);
		return lineString.getLength();
	}

	public static Point wgs84toRT90(Point wgs84) {
		try {
			CoordinateReferenceSystem crsDest = CRS.decode("EPSG:" + RT90, true);
			CoordinateReferenceSystem crsSrc = CRS.decode("EPSG:" + WGS84,true);
			MathTransform transform = CRS.findMathTransform(crsSrc, crsDest);
			return (Point) JTS.transform(wgs84, transform);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (TransformException ex) {
			throw new RuntimeException(ex);
		} catch (FactoryException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static GeometryFactory getGeometryFactory() {
		return new GeometryFactory(new PrecisionModel(DEFAULT_PRECISION), WGS84);
	}
}
