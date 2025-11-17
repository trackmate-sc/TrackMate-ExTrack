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
package fr.pasteur.iah.extrack.compute;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;
import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackModel;
import fr.pasteur.iah.extrack.trackmate.ExTrackProbabilitiesFeature;

public class ExTrackDoPredictions implements Runnable
{

	private final ExTrackParameters params;

	private final Logger logger;

	private final Model model;

	public ExTrackDoPredictions(
			final ExTrackParameters params,
			final Model model,
			final Logger logger )
	{
		this.params = params;
		this.model = model;
		this.logger = logger;
	}

	@Override
	public void run()
	{
		final int nbSubSteps = params.nbSubteps;
		final int frameLen = params.nFrames;
		final boolean doFrame = true;
		final boolean doPred = true;

		final TrackState trackState = new TrackState(
				params.localizationError,
				params.diffusionLength0,
				params.diffusionLength1,
				params.F0,
				params.probabilityOfUnbinding,
				nbSubSteps,
				doFrame,
				frameLen,
				doPred );

		final TrackModel trackModel = model.getTrackModel();
		final int nTracks = trackModel.nTracks( true );
		int index = 0;
		for ( final Integer trackID : trackModel.trackIDs( true ) )
		{
			final List< Spot > track = new ArrayList<>( trackModel.trackSpots( trackID ) );
			track.sort( Spot.frameComparator );

			final Matrix C = new Matrix( track.size(), 2 );
			for ( int r = 0; r < track.size(); r++ )
			{
				final Spot spot = track.get( r );
				C.set( r, 0, spot.getDoublePosition( 0 ) );
				C.set( r, 1, spot.getDoublePosition( 1 ) );
			}

			final Matrix[] matrices = trackState.eval( C );
			final Matrix predictions = matrices[ 1 ];

			for ( int r = 0; r < track.size(); r++ )
			{
				final double stuckProba = predictions.get( r, 0 );
				final double diffusiveProba = predictions.get( r, 1 );

				final Spot spot = track.get( r );
				spot.putFeature( ExTrackProbabilitiesFeature.P_DIFFUSIVE, diffusiveProba );
				spot.putFeature( ExTrackProbabilitiesFeature.P_STUCK, stuckProba );
			}
			logger.setProgress( ( double ) ( ++index ) / nTracks );
		}
	}
}
