/*-
 * #%L
 * TrackMate interface for the ExTrack track analysis software.
 * %%
 * Copyright (C) 2020 - 2025 Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fr.pasteur.iah.extrack.trackmate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.features.spot.SpotAnalyzer;
import fiji.plugin.trackmate.features.spot.SpotAnalyzerFactory;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin( type = SpotAnalyzerFactory.class )
public class ExTrackProbabilitiesFeature< T extends RealType< T > & NativeType< T > > implements SpotAnalyzerFactory< T >
{

	public static final String P_STUCK = "EXTRACK_P_STUCK";

	public static final String P_DIFFUSIVE = "EXTRACK_P_DIFFUSIVE";

	public static final String KEY = "EXTRACK_PROBABILITIES";

	static final List< String > FEATURES = new ArrayList<>( 2 );

	static final Map< String, String > FEATURE_SHORT_NAMES = new HashMap<>( 2 );

	static final Map< String, String > FEATURE_NAMES = new HashMap<>( 2 );

	static final Map< String, Dimension > FEATURE_DIMENSIONS = new HashMap<>( 2 );

	static final Map< String, Boolean > IS_INT = new HashMap<>( 1 );

	static final String INFO_TEXT = "<html>A dummy analyzer for the feature that stores probabilities calculated by the ExTrack algorithm.</html>";

	static final String NAME = "ExTrack probabilities";

	static
	{
		FEATURES.add( P_STUCK );
		FEATURE_SHORT_NAMES.put( P_STUCK, "P stuck" );
		FEATURE_NAMES.put( P_STUCK, "Probability stuck" );
		FEATURE_DIMENSIONS.put( P_STUCK, Dimension.NONE );
		IS_INT.put( P_STUCK, Boolean.FALSE );

		FEATURES.add( P_DIFFUSIVE );
		FEATURE_SHORT_NAMES.put( P_DIFFUSIVE, "P diffusive" );
		FEATURE_NAMES.put( P_DIFFUSIVE, "Probability diffusive" );
		FEATURE_DIMENSIONS.put( P_DIFFUSIVE, Dimension.NONE );
		IS_INT.put( P_DIFFUSIVE, Boolean.FALSE );
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public List< String > getFeatures()
	{
		return FEATURES;
	}

	@Override
	public Map< String, String > getFeatureShortNames()
	{
		return FEATURE_SHORT_NAMES;
	}

	@Override
	public Map< String, String > getFeatureNames()
	{
		return FEATURE_NAMES;
	}

	@Override
	public Map< String, Dimension > getFeatureDimensions()
	{
		return FEATURE_DIMENSIONS;
	}

	@Override
	public String getInfoText()
	{
		return INFO_TEXT;
	}

	@Override
	public Map< String, Boolean > getIsIntFeature()
	{
		return IS_INT;
	}

	@Override
	public boolean isManualFeature()
	{
		return true;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public SpotAnalyzer< T > getAnalyzer( final ImgPlus< T > img, final int frame, final int channel )
	{
		return new SpotAnalyzer.DummySpotAnalyzer<>();
	}
}
