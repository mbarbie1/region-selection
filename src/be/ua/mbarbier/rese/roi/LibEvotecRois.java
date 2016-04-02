package be.ua.mbarbier.rese.roi;

import ij.gui.Roi;
import java.awt.Polygon;
import java.io.File;
import java.util.ArrayList;

public class LibEvotecRois {

	/**
	 * Convert the imagej rois to evotec rois
	 *	use the imagej roi to evotec roi function
	 *
	 * @return Evotec roi string
 	 */
	public static String roiToEvotecRegion(Roi roi, int w, int h, float Xlt, float Ylt, float Xrb, float Yrb) {
		String text = "[";
		String typeRegion = "IN-spline";
		String headerRegion = "[" + typeRegion + ";" +
			Integer.toString(w) + "," +	Integer.toString(h) + ";" +
			Float.toString(Xlt) + "," + Float.toString(Ylt) + ";" + Float.toString(Xrb) + "," + Float.toString(Yrb) + "]";
		text = text + headerRegion;
		if ( roi != null ) {
			// Get ROI points
			Polygon polygon = roi.getPolygon();
			int[] x = polygon.xpoints;
			int[] y = polygon.ypoints;
			for ( int i = 0; i < x.length; i++ ) {
				text = text + Float.toString( x[i] ) + "," + Float.toString( y[i] );
				if ( i < (x.length-1) ) {
					text = text + ";";
				}
			}
			text = text + "]";
		}

		return text;
	}

	
	public static String getEvotecHeaderOut( String header_prefix, String[] name_rois ) {
		String header = header_prefix;
		ArrayList<String> header_out = new ArrayList<String>();
		header_out.add(header);
		header_out.add("overviewbasename");
		for (String s: name_rois) {
			header_out.add( "EVT_Regions_" + s );
		}
		String text = ""; 
		for ( int i = 1; i < header_out.size(); i++ ) {
			text = text + header_out.get(i);
			if ( i < (header_out.size()-1) ) {
				text = text + "\t";
			}
		}
		
		return text;
	}

	public static String getEvotecHeader( String header_prefix ) {
		String header = header_prefix;

		return header;
	}
	
	public static String getEvotecOverviewFileName( String plateName, String measurementDate, String wellName, String overviewfileExt ) {
		return plateName + "__" + measurementDate.replaceAll("[^a-zA-Z0-9.-]","") + "__" + wellName + "." + overviewfileExt;
	}

	public static String getEvotecOverviewFilePath( String plateName, String measurementDate, String wellName, String overviewfileDir, String overviewfileExt ) {
		String overviewbasename = getEvotecOverviewFileName( plateName, measurementDate, wellName, overviewfileExt );
		File file = new File( overviewfileDir, overviewbasename );

		return file.getAbsolutePath();
	}

	public static String getEvotecRow( String plateName, String measurementDate, String wellName, String overviewfilename, int binning, float xltUnit, float yltUnit, float xrbUnit, float yrbUnit, int sizeX, int sizeY ) {
		String row = plateName + '\t' + measurementDate + '\t' + wellName + '\t' + overviewfilename + '\t' + Integer.toString(binning) + '\t' +
				Float.toString(xltUnit) + '\t' + Float.toString(yltUnit)  + '\t' + Float.toString(xrbUnit) + '\t' + Float.toString(yrbUnit) + '\t' + 
				Integer.toString(sizeX) + '\t' + Integer.toString(sizeY);
	
		return row;
	}
		
	public static String getEvotecRowOut( String plateName, String measurementDate, String wellName, String overviewfilename, String overviewbasename, int binning, float xltUnit, float yltUnit, float xrbUnit, float yrbUnit, int sizeX, int sizeY, String[] rois_Evotec ) {
		String row = getEvotecRow( plateName, measurementDate, wellName, overviewfilename, binning, xltUnit, yltUnit, xrbUnit, yrbUnit, sizeX, sizeY );
		ArrayList<String> row_out = new ArrayList<String>();
		row_out.add( row );
		row_out.add( overviewbasename );
		for (String s: rois_Evotec) {
			row_out.add( s );
		}
		String text = "";
		for ( int i = 1; i < row_out.size(); i++ ) {
			text = text + row_out.get(i);
			if ( i < (row_out.size()-1) ) {
				text = text + "\t";
			}
		}
	
		return text;
	}

}


/*
from ij import IJ, ImagePlus, ImageStack
from ij.process import ImageProcessor, Blitter, FloatProcessor, ShortBlitter, ImageStatistics as IS
from ij.plugin import CanvasResizer, RoiScaler
from ij.plugin.filter import GaussianBlur
from ij.plugin.frame import RoiManager
from ij.gui import PointRoi, Roi, Wand, PolygonRoi, Overlay, ShapeRoi
from ij.measure import ResultsTable
from util.opencsv import CSVReader
from math import sqrt
from jarray import zeros, array
import SIFT_ExtractPointRoi
from bunwarpj import Transformation, bUnwarpJ_, Param
from mpicbg.ij import SIFT
from mpicbg.imagefeatures import Feature, FloatArray2DSIFT
from java.io import FileReader, File
from java.util import Stack, ArrayList
from java.awt import Point, Font, Color, Rectangle
import re
import os
import csv

from libRegistration import transformRoi 


""" 
	Convert the imagej rois to evotec rois
		- use the imagej roi to evotec roi function
"""

def roiToEvotecRegion(roi, w, h, Xlt, Ylt, Xrb, Yrb):
	text = "["
	typeRegion = "IN-spline"
	headerRegion = "[" + typeRegion + ";"+ str(w) + "," + str(h) + ";" + str(Xlt) + "," + str(Ylt) + ";" +str(Xrb)+","+str(Yrb) + "]"
	text = text + headerRegion
	if roi is not None:
		# Get ROI points
		polygon = roi.getPolygon()
		n_points = polygon.npoints
		x = polygon.xpoints
		y = polygon.ypoints
		for i in range(0, len(x)):
			text = text + str(x[i]) + "," + str(y[i])
			if i < (len(x)-1):
				text = text + ";"
		text = text + "]"
	return(text)

"""
	Generate the output table
"""

#platename	measurementdate			wellname	overviewfilename									binning	xlt				ylt				xrb				yrb				sizeX	sizeY	overviewbasename					EVT_Regions_Cx	EVT_Regions_Hip	EVT_Regions_Th	EVT_Regions_Bs	EVT_Regions_Mb
#S235 		2015-06-17T09:59:55Z	A1			T:/Data/Regions/S235__2015-06-17T095955Z__A1.png	8		-666.835638593	-666.835638593	11456.8356386	6655.83563859	2318	1400	S235__2015-06-17T095955Z__A1.png	[[IN-spline;2318,1400;0.000,0.000;2318.000,1400.000]1260.000,942.000;1232.000,976.000;1222.000,998.000;1218.000,1026.000;1218.000,1064.000;1216.000,1086.000;1216.000,1102.000;1164.000,1200.000;1154.000,1238.000;1160.000,1250.000;1194.000,1260.000;1306.000,1264.000;1390.000,1248.000;1388.000,1246.000;1574.000,1146.000;1906.000,892.000;2068.000,716.000;2130.000,598.000;2146.000,506.000;2130.000,448.000;2082.000,398.000;1996.000,366.000;1942.000,358.000;1886.000,354.000;1882.000,338.000;1874.000,314.000;1862.000,294.000;1846.000,270.000;1812.000,266.000;1798.000,274.000;1800.000,300.000;1802.000,316.000;1800.000,336.000;1784.000,342.000;1760.000,354.000;1750.000,372.000;1740.000,404.000;1736.000,432.000;1738.000,460.000;1752.000,482.000;1788.000,518.000;1794.000,550.000;1784.000,588.000;1748.000,644.000;1704.000,694.000;1636.000,766.000;1598.000,802.000;1560.000,866.000;1522.000,918.000;1480.000,964.000;1416.000,1006.000;1360.000,1038.000;1338.000,1038.000;1310.000,1030.000;1284.000,1010.000;1272.000,986.000;1266.000,956.000]	[[IN-spline;2318,1400;0.000,0.000;2318.000,1400.000]1468.000,722.000;1428.000,732.000;1406.000,744.000;1396.000,756.000;1380.000,772.000;1362.000,784.000;1344.000,792.000;1332.000,800.000;1320.000,816.000;1306.000,834.000;1294.000,854.000;1278.000,874.000;1276.000,894.000;1280.000,906.000;1286.000,922.000;1298.000,938.000;1326.000,962.000;1368.000,958.000;1412.000,950.000;1454.000,926.000;1488.000,878.000;1528.000,838.000;1540.000,792.000;1544.000,758.000;1538.000,738.000;1520.000,730.000;1492.000,724.000]	[[IN-spline;2318,1400;0.000,0.000;2318.000,1400.000]1040.000,442.000;1008.000,472.000;998.000,498.000;1010.000,526.000;1034.000,562.000;1070.000,594.000;1116.000,632.000;1154.000,656.000;1168.000,674.000;1206.000,704.000;1258.000,736.000;1282.000,770.000;1298.000,792.000;1316.000,810.000;1342.000,788.000;1358.000,764.000;1394.000,738.000;1420.000,724.000;1452.000,718.000;1484.000,694.000;1498.000,640.000;1504.000,578.000;1496.000,526.000;1480.000,478.000;1438.000,446.000;1418.000,428.000;1366.000,416.000;1310.000,410.000;1264.000,412.000;1228.000,420.000;1200.000,438.000;1178.000,446.000;1154.000,452.000;1130.000,450.000;1108.000,446.000;1078.000,440.000;1050.000,442.000]	[[IN-spline;2318,1400;0.000,0.000;2318.000,1400.000]710.000,904.000;732.000,882.000;760.000,854.000;786.000,820.000;814.000,776.000;836.000,736.000;852.000,686.000;872.000,622.000;884.000,574.000;898.000,510.000;898.000,460.000;888.000,424.000;856.000,398.000;824.000,388.000;786.000,370.000;740.000,366.000;690.000,366.000;620.000,398.000;558.000,438.000;504.000,458.000;446.000,474.000;396.000,520.000;350.000,580.000;316.000,648.000;314.000,702.000;314.000,740.000;312.000,784.000;316.000,816.000;354.000,838.000;404.000,840.000;462.000,848.000;500.000,866.000;542.000,872.000;584.000,872.000;630.000,870.000;654.000,876.000;682.000,896.000]	[[IN-spline;2318,1400;0.000,0.000;2318.000,1400.000]970.000,352.000;1000.000,348.000;1022.000,366.000;1040.000,394.000;1042.000,424.000;1032.000,446.000;1016.000,472.000;996.000,490.000;998.000,514.000;1002.000,540.000;1022.000,580.000;1044.000,608.000;1076.000,644.000;1112.000,662.000;1158.000,690.000;1198.000,724.000;1226.000,740.000;1248.000,764.000;1262.000,790.000;1272.000,828.000;1274.000,856.000;1262.000,886.000;1256.000,892.000;1240.000,920.000;1220.000,952.000;1210.000,976.000;1200.000,1006.000;1192.000,1032.000;1192.000,1056.000;1188.000,1084.000;1180.000,1112.000;1168.000,1140.000;1152.000,1170.000;1132.000,1196.000;1116.000,1222.000;1092.000,1232.000;1034.000,1252.000;990.000,1258.000;938.000,1260.000;838.000,1300.000;806.000,1288.000;786.000,1262.000;762.000,1232.000;758.000,1198.000;764.000,1162.000;778.000,1120.000;778.000,1084.000;780.000,1040.000;804.000,1012.000;818.000,982.000;810.000,952.000;802.000,910.000;790.000,874.000;778.000,846.000;792.000,814.000;810.000,786.000;832.000,754.000;854.000,722.000;874.000,690.000;916.000,640.000;938.000,604.000;942.000,548.000;944.000,502.000;940.000,464.000;928.000,440.000;922.000,412.000;928.000,382.000;946.000,364.000]


def getEvotecHeader( header_prefix, name_rois ):
	header = header_prefix
	header_out = [header]
	header_out.append('overviewbasename')
	for s in name_rois:
		header_out.append( "EVT_Regions_" + s )
	header_out = '\t'.join( header_out )
	return [header, header_out]


def getEvotecOverviewfilename( plateName, measurementDate, wellName, overviewfileDir, overviewfileExt ):
	overviewbasename = plateName + "__" + re.sub(r"[^a-zA-Z0-9.-]", "", measurementDate) + "__" + wellName + "." + overviewfileExt
	overviewfilename = os.path.join( overviewfileDir, overviewbasename )
	return [overviewfilename, overviewbasename]

def getEvotecRow( plateName, measurementDate, wellName, overviewfilename, overviewbasename, xltUnit, yltUnit, xrbUnit, yrbUnit, sizeX, sizeY, rois_Evotec ):
	row = [ plateName, measurementDate, wellName, overviewfilename, binning, xltUnit, yltUnit, xrbUnit, yrbUnit, sizeX, sizeY ]
	row_out = list(row)
	row_out.append(overviewbasename)
	for i in range(0,len(rois_Evotec)):
		row_out.append( rois_Evotec[i] )
	row = '\t'.join( map( str, row ) )
	row_out = '\t'.join( map( str, row_out ) )
	return [row, row_out]

def writeTextFile(text, saveDirectory, ext, fileName):
	""" Write a text file """
	filePath = os.path.join(saveDirectory.getAbsolutePath(), fileName + '.' + ext)
	try:
		f = open(filePath,'w')
		f.write(text)
	finally:
		f.close()


""" 
	Generate the output png
		- use the merged file and overlay with the roi's
		- label the roi's
"""

def getOverlayImage( rois, imp ):
	impRois = ImagePlus( "Image with rois", imp.getProcessor().duplicate() )
	overlay = Overlay()
	for i in range( 0, len( rois ) ):
		currentRoi = rois[i]
		overlay.add( currentRoi )
	overlay.drawNames( True )
	overlay.drawLabels( True )
	#overlay.setLabelFont( Font( "fontName", Font.PLAIN, int( impRois.getWidth()/50.0 ) ) )
	impRois.setOverlay( overlay )
	return impRois

def saveOverviewImageEvotec( imp, filePath ):
	impTemp = imp.duplicate()
	# TODO: derive saturated pixel ratio from histogram of the reference image
	IJ.run( impTemp, "Enhance Contrast", "saturated=0.35" )
	# Should we also save the image with rois?
	IJ.run( impTemp, "RGB Color", "" );
	IJ.save( impTemp, filePath )

from javax.xml.xpath import XPathFactory
from javax.xml.xpath import XPath
from javax.xml.xpath import XPathConstants
from javax.xml.xpath import XPathExpressionException
from javax.xml.parsers import DocumentBuilder;
from javax.xml.parsers import DocumentBuilderFactory;

def loadXMLFromFile( xmlPath ):
	db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	doc = db.parse( xmlPath );

	return doc;

def EvotecXMLRegionToRois( xmlPath ):

	doc = loadXMLFromFile( xmlPath )
	xpath = XPathFactory.newInstance().newXPath()
	expressionNodes = "//Value"
	nodeList = xpath.evaluate( expressionNodes, doc, XPathConstants.NODESET )
	rois = []
	for i in range( 0, nodeList.getLength()):
		node = nodeList.item(i)
		nodeRoiStr = node.getTextContent()
		nodeRoiName = node.getParentNode().getAttributes().item(0).getNodeValue()
		rois.append( EvotecStringRegionToRoi(nodeRoiStr, nodeRoiName) )

	return rois

def EvotecStringRegionToRoi(roiStr, roiName):
	xp = []
	yp = []
	roiStrSplit = re.split( "\]", roiStr )
	evtHeaderStr = re.split( ";", roiStrSplit[0] )
	evtHeaderStr = evtHeaderStr[3:]
	evtCoordStrList = re.split( ";", roiStrSplit[1] )
	for s in evtCoordStrList:
		xy = re.split( ",", s )
		xp.append( float(xy[0]) )
		yp.append( float(xy[1]) )
	roi = PolygonRoi(xp, yp, len(xp), PolygonRoi.FREEROI)
	roi.setName( roiName )

	return roi

def roiColor():
	return dict( [ 
			( "EVT_Regions_Bg", Color.gray ),
			( "EVT_Regions_Cx", Color.red ),
			( "EVT_Regions_Hip", Color.green ),
			( "EVT_Regions_Th", Color.blue ),
			( "EVT_Regions_Bs", Color.magenta ),
			( "EVT_Regions_Mb", Color.yellow ),
			( "EVT_Regions_Cb", Color.cyan )
	] )

def scaleRois(rois, s, offset):
	for i in range( 0, len(rois) ):
		roi = rois[i]
		if s != 1.0:
			roi = RoiScaler.scale( roi, s, s, 0 )
		rec = roi.getBounds()
		roi.setLocation( int( rec.getX() ) + offset[0], int( rec.getY() ) + offset[1] )
		roi.setName( rois[i].getName() )
		roi.setStrokeColor( roiColor()[ rois[i].getName() ] )
		roi.setStrokeWidth( 2 )
		rois[i] = roi
	return rois

def getEvotecRoiImage( imp, impOffset, s, evtPath, sEvtRoi):
	rois = EvotecXMLRegionToRois( evtPath )
	rois = scaleRois(rois, s / sEvtRoi, impOffset )
	impRois = getOverlayImage( rois, imp )
	impRois.setTitle("Evotec Rois")
	return [impRois, rois]

def getTransformedEvotecRoiImage( imp, rois, transfo):
	trois = []
	for roi in rois:
		trois.append( transformRoi( transfo, roi ) )
	impRois = getOverlayImage( trois, imp )
	impRois.setTitle("Transformed Evotec Rois")
	return [impRois, trois]

*/