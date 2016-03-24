/**
 * 
 */
package be.ua.mbarbier.rese.registration;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortBlitter;

import java.util.LinkedHashMap;

//import mpicbg.imagefeatures.Feature;
//import mpicbg.imagefeatures.FloatArray2DSIFT;

import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import bunwarpj.Param;

import be.ua.mbarbier.external.SIFT_ExtractPointRoi;
import be.ua.mbarbier.rese.image.LibUtilities;

/**
 * @author mbarbie1
 *
 */
public class LibRegistration {

	ImagePlus imp1;
	ImagePlus imp2;

	float initialSigma = 1.60f;
	int steps = 3;
	int minOctaveSize = 64;
	int maxOctaveSize = 1024;
	int fdSize = 4;
	int fdBins = 8;
	float rod = 0.92f;
	float maxEpsilon = 25.0f;
	float minInlierRatio = 0.05f;
	int modelIndex = 2;

	int accuracy_mode = 1;
	int img_subsamp_fact = 0;
	int  min_scale_deformation = 1;
	int max_scale_deformation = 2;
	double divWeight = 0.1;
	double curlWeight = 0.1;
	double landmarkWeight = 1.0;
	double imageWeight = 1.0;
	double consistencyWeight = 10.0;
	double stopThreshold = 0.01;

	public static LinkedHashMap<String, Float> siftParamDefault() {
		LinkedHashMap<String, Float> paramDefault = new LinkedHashMap<String, Float>(); 
		paramDefault.put( "initialSigma", 1.60f );
		paramDefault.put( "steps", 3.0f );
		paramDefault.put( "minOctaveSize", 64.0f );
		paramDefault.put( "maxOctaveSize", 1024.0f );
		paramDefault.put("fdSize",4.0f );
		paramDefault.put("fdBins",8.0f );
		paramDefault.put("rod",0.92f );
		paramDefault.put("maxEpsilon",25.0f );
		paramDefault.put("minInlierRatio",0.05f );
		paramDefault.put("modelIndex",2.0f );

		return paramDefault;
	}

	public static LinkedHashMap<String, Double> bunwarpjParamDefault() {
		LinkedHashMap<String, Double> paramDefault = new LinkedHashMap<String, Double>(); 
		paramDefault.put( "accuracy_mode", 1.0);
		paramDefault.put( "img_subsamp_fact", 0.0);
		paramDefault.put( "min_scale_deformation", 1.0);
		paramDefault.put( "max_scale_deformation", 2.0);
		paramDefault.put( "divWeight", 0.0);
		paramDefault.put( "curlWeight",	0.0);
		paramDefault.put( "landmarkWeight", 0.0);
		paramDefault.put( "imageWeight", 1.0);
		paramDefault.put( "consistencyWeight", 10.0);
		paramDefault.put( "stopThreshold", 0.01);

		return paramDefault;
	}

	public static LinkedHashMap<String, Roi> siftSingle( ImagePlus impSource, ImagePlus impTarget, LinkedHashMap param ) {
		SIFT_ExtractPointRoi t = new SIFT_ExtractPointRoi();
		t.exec(
			impTarget, 
			impSource, 
			(float) param.get("initialSigma"),
			(int) (param.get("steps")),
			(int) (param.get("minOctaveSize")),
			(int) (param.get("maxOctaveSize")),
			(int) (param.get("fdSize")),
			(int) (param.get("fdBins")),
			(float) (param.get("rod")),
			(float) (param.get("maxEpsilon")),
			(float) (param.get("minInlierRatio")),
			(int) (param.get("modelIndex"))
		);
		Roi roiSource = impSource.getRoi();
		Roi roiTarget = impTarget.getRoi();
		LinkedHashMap<String, Roi> out = new LinkedHashMap<String, Roi>();
		out.put( "roiSource", roiSource );
		out.put( "roiTarget", roiTarget );

		return out;
	}

	public static Transformation bunwarpj_param( ImagePlus impSource, ImagePlus impTarget, LinkedHashMap param ) {
		impSource.setTitle("bunwarpj_source");
		impTarget.setTitle("bunwarpj_target");
		ImageProcessor targetMskIp = LibUtilities.mask( impTarget.getProcessor().duplicate(), 0.0, 0.5 ).getProcessor();
		ImageProcessor sourceMskIp = LibUtilities.mask( impSource.getProcessor().duplicate(), 0.0, 0.5 ).getProcessor();
		//FloatProcessor tmp = new FloatProcessor( impTarget.getWidth(), impTarget.getHeight() );
		//tmp.setValue(1.0);
		//tmp.fill();
		//ImageProcessor targetMskIp = tmp.duplicate();
		//ImageProcessor sourceMskIp = tmp.duplicate();
		param = bunwarpjParamDefault();
		Transformation transfo = bUnwarpJ_.computeTransformationBatch(
			impTarget, 
			impSource, 
			targetMskIp, 
			sourceMskIp, 
			(int) (param.get("accuracy_mode")),
			(int) (param.get("img_subsamp_fact")),
			(int) (param.get("min_scale_deformation")),
			(int) (param.get("max_scale_deformation")),
			(double) (param.get("divWeight")),
			(double) (param.get("curlWeight")),
			(double) (param.get("landmarkWeight")),
			(double) (param.get("imageWeight")),
			(double) (param.get("consistencyWeight")),
			(double) (param.get("stopThreshold"))
		);

		return transfo;
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

from jarray import array
def transformRoi( transfo, roi ):
	""" 
		Transform the points in an imagej Roi using a bunwarpj registration transformation object
		(note to self: remember the jarray java jython array)
	"""
	transfoPoint =[]
	xt = []
	yt = []
	# Get ROI points
	polygon = roi.getPolygon()
	n_points = polygon.npoints
	x = polygon.xpoints
	y = polygon.ypoints
	xyF = array([ 30.0, 40.0 ], "d")
	for i in range(0, len(x)):
		l = transfo.transform( float( x[i] ), float( y[i] ), xyF, 1 )
		transfoPoint.append(xyF)
		xt.append(xyF[0])
		yt.append(xyF[1])
	transfoRoi = PolygonRoi( xt, yt, len(xt), PolygonRoi.FREEROI)
	transfoRoi.setName( roi.getName() )
	transfoRoi.setStrokeColor( roi.getStrokeColor() )
	return transfoRoi


def siftParamDefault():
	#
	#void exec(final ImagePlus imp1, final ImagePlus imp2,
	#			 final float initialSigma, final int steps,
	#			 final int minOctaveSize, final int maxOctaveSize,
	#			 final int fdSize, final int fdBins,
	#			 final float rod, final float maxEpsilon,
	#			 final float minInlierRatio, final int modelIndex) {
	#
	#public void exec(final ImagePlus imp1, final ImagePlus imp2, final int mode)
	#
	#param modelIndex: 0=Translation, 1=Rigid, 2=Similarity, 3=Affine 
	param_default = dict( {
		"initialSigma": 	1.60,
		"steps": 			3,
		"minOctaveSize": 	64,
		"maxOctaveSize": 	1024,
		"fdSize": 			4,
		"fdBins": 			8,
		"rod": 				0.92,
		"maxEpsilon": 		25.0,
		"minInlierRatio":	0.05,
		"modelIndex": 		2
	} )
	return param_default

def bunwarpjParamDefault():
	param_default = dict( {
		"accuracy_mode":			1,
		"img_subsamp_fact":			0,
		"min_scale_deformation":	1,
		"max_scale_deformation":	2,
		"divWeight":				0.0,
		"curlWeight":				0.0,
		"landmarkWeight":			0.0,
		"imageWeight":				1.0,
		"consistencyWeight":		10,
		"stopThreshold":			0.01
	} )
	return param_default


def siftSingle(impSource, impTarget, param):
	""" perform SIFT registration for one image """
	t = SIFT_ExtractPointRoi()
	t.exec(
		impTarget, 
		impSource, 
		param["initialSigma"],
		param["steps"],
		param["minOctaveSize"],
		param["maxOctaveSize"],
		param["fdSize"],
		param["fdBins"],
		param["rod"],
		param["maxEpsilon"],
		param["minInlierRatio"],
		param["modelIndex"]
	)
	roiSource = impSource.getRoi()
	roiTarget = impTarget.getRoi()
	return [roiSource, roiTarget]

def bunwarpj_par6(impRef, impTarget, landMarks, divWeight, curlWeight, consistencyWeight):
	impRef.setTitle('bunwarpj_source')
	impTarget.setTitle('bunwarpj_target')
	tmp = FloatProcessor(impTarget.getWidth(), impTarget.getHeight())
	tmp.setValue(1.0)
	tmp.fill()
	targetMskIp = tmp.duplicate()
	sourceMskIp = tmp.duplicate()
	accuracy_mode = 1
	img_subsamp_fact = 0
	min_scale_deformation = 1
	max_scale_deformation = 2
	#divWeight = 0.0
	#curlWeight = 0.0
	#consistencyWeight = 4
	stopThreshold = 0.01
	landmarkWeight = landMarks
	imageWeight = 1 - landMarks
	transfo = bUnwarpJ_.computeTransformationBatch(
		impTarget, impRef, targetMskIp, sourceMskIp, accuracy_mode, img_subsamp_fact, min_scale_deformation, max_scale_deformation, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight, stopThreshold)
	return transfo

def bunwarpj_param(impRef, impTarget, param):
	impRef.setTitle('bunwarpj_source')
	impTarget.setTitle('bunwarpj_target')
	tmp = FloatProcessor(impTarget.getWidth(), impTarget.getHeight())
	tmp.setValue(1.0)
	tmp.fill()
	targetMskIp = tmp.duplicate()
	sourceMskIp = tmp.duplicate()
	param = bunwarpjParamDefault()
	transfo = bUnwarpJ_.computeTransformationBatch(
		impTarget, 
		impRef, 
		targetMskIp, 
		sourceMskIp, 
		param["accuracy_mode"], 
		param["img_subsamp_fact"], 
		param["min_scale_deformation"],
		param["max_scale_deformation"],
		param["divWeight"],
		param["curlWeight"],
		param["landmarkWeight"],
		param["imageWeight"],
		param["consistencyWeight"],
		param["stopThreshold"]
	)
	return transfo


""" START SCRIPT TESTING """

"""

def getOverlayImage( rois, imp ):
	impRois = ImagePlus( "Image with rois", imp.getProcessor().duplicate() )
	overlay = Overlay()
	for i in range( 0, len( rois ) ):
		currentRoi = rois[i]
		overlay.add( currentRoi )
	overlay.drawNames( True )
	overlay.drawLabels( True )
	overlay.setLabelFont( Font( "fontName", Font.PLAIN, int( impRois.getWidth()/100.0 ) ) )
	impRois.setOverlay( overlay )
	return impRois


param = siftParamDefault()
sourceFilePath = "/media/usb0/2016_01_22/input/Stack_not_registered_grey.tif"
imp = IJ.openImage( sourceFilePath )
stack = imp.getStack()
outStack = stack.duplicate()
sizeZ = imp.getStackSize()
target = stack.getProcessor(1)
impTarget = ImagePlus('Sift_target', target)
impTarget.show()

for i in range(1, sizeZ):
	source = stack.getProcessor(i)
	impSource = ImagePlus('Sift_source', source)
	[roiSource, roiTarget] = siftSingle(impSource, impTarget, param)
	overlay = Overlay()
	overlay.add( roiSource )
	overlay.drawNames( True )
	overlay.drawLabels( True )
	impSource.setOverlay( overlay )
	

source = stack.getProcessor(13)
impSource = ImagePlus('Sift_source', source)
sigmas = [1.6, 3.2, 6.4]
fdSizes = [4, 8, 16]
for s in sigmas:
	for fdSize in fdSizes:
		param["initialSigma"] = s
		param["fdSize"] = fdSize
		#param["fdBins"] = 8
		[roiSource, roiTarget] = siftSingle(impSource, impTarget, param)
		impSource.setTitle("sigma:" + str(s) + "_fdSize:" + str(fdSize) )
		roiSource.setName("sigma:" + str(s) + "_fdSize:" + str(fdSize) )
		impSource.setRoi( roiSource )
		overlay = Overlay()
		overlay.add( roiSource )
		overlay.drawNames( True )
		overlay.drawLabels( True )
		impSource.setOverlay( overlay )
		impSource.duplicate().show()

"""

""" END SCRIPT TESTING """
*/
