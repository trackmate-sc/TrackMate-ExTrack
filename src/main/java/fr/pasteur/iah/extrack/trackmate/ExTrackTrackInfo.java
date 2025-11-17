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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.features.track.TrackAnalyzer;

public class ExTrackTrackInfo implements TrackAnalyzer
{

	/** The key for this analyzer. */
	public static final String KEY = "ExTrack track info";

	public static final String EXTRACK_TRACKID = "EXTRACK_TRACKID";

	public static final List< String > FEATURES = new ArrayList<>( 1 );

	public static final Map< String, String > FEATURE_NAMES = new HashMap<>( 1 );

	public static final Map< String, String > FEATURE_SHORT_NAMES = new HashMap<>( 1 );

	public static final Map< String, Dimension > FEATURE_DIMENSIONS = new HashMap<>( 1 );

	public static final Map< String, Boolean > IS_INT = new HashMap<>( 2 );

	static
	{
		FEATURES.add( EXTRACK_TRACKID );
		FEATURE_NAMES.put( EXTRACK_TRACKID, "ExTrack track ID" );
		FEATURE_SHORT_NAMES.put( EXTRACK_TRACKID, "ExTrackID" );
		FEATURE_DIMENSIONS.put( EXTRACK_TRACKID, Dimension.NONE );
		IS_INT.put( EXTRACK_TRACKID, Boolean.TRUE );
	}

	@Override
	public boolean isLocal()
	{
		return true;
	}

	@Override
	public void process( final Collection< Integer > trackIDs, final Model model )
	{}

	@Override
	public long getProcessingTime()
	{
		return 0l;
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

	/**
	 * Ignored. This analyzer is single-threaded.
	 */
	@Override
	public void setNumThreads()
	{}

	/**
	 * Ignored. This analyzer is single-threaded.
	 */
	@Override
	public void setNumThreads( final int numThreads )
	{}

	/**
	 * Ignore. This analyzer is single-threaded.
	 */
	@Override
	public int getNumThreads()
	{
		return 1;
	}

	@Override
	public String getInfoText()
	{
		return null;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return KEY;
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
}
