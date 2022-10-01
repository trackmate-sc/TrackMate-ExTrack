/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
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
package fr.pasteur.iah.extrack;

import java.io.File;
import java.io.IOException;
import java.util.NavigableSet;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.io.TmXmlReader;
import fr.pasteur.iah.extrack.compute.ExTrackParameters;

public class ExTrackScriptingJava
{

	public static void main( final String[] args )
	{

		// Path to the TrackMate file containing your tracks.
		final String path = "samples/tracks.xml";

		// Load them.
		System.out.println( "Loading " + path );
		final TmXmlReader reader = new TmXmlReader( new File( path ) );
		final Model model = reader.getModel();
		if ( !reader.isReadingOk() )
		{
			System.err.println( "Problem reading the file:" );
			System.err.println( reader.getErrorMessage() );
			System.err.println( "Aborting." );
			return;
		}
		System.out.println( "Loading done." );

		// Create an ExTrack object.
//		final ExTrack extrack = new ExTrack( model );
		final ExTrack extrack = new ExTrack( model, Logger.VOID_LOGGER );

		// We only estimate parameters if we do not have them already saved.
		final String parent = new File( path ).getParent();
		final File savefile = new File( parent, "extrack-params.json" );
		if ( savefile.exists() )
		{
			System.out.println( "\nFound an existing save-file for parameters. "
					+ "Skipping parameter estimation." );
		}
		else
		{
			// Estimate motility parameters.
			System.out.println( "\nEstimating motility parameters (can be long)..." );
			// Specify the starting point.
			final ExTrackParameters startpoint = ExTrackParameters.create()
					.localizationError( 0.02 )
					.diffusionLength0( 0.001 ) // stuck
					.diffusionLength1( 0.1 ) // diffusive
					.F0( 0.5 ) // mobile fraction
					.probabilityOfUnbinding( 0.1 )
					.nbSubSteps( 1 )
					.nFrames( 6 )
					.build();
			System.out.println( "Using the following as starting point:" );
			System.out.println( startpoint.toString() );

			final long start = System.currentTimeMillis();
			final ExTrackParameters optimum = extrack.estimateParameters( startpoint );
			final long end = System.currentTimeMillis();
			System.out.println( String.format( "Estimation done in %.1f seconds.\n"
					+ "Found the following optimum:",
					( end - start ) / 1000. ) );
			System.out.println( optimum.toString() );

			// Save.
			System.out.println( "\nSaving the parameters to a JSon file." );

			try
			{
				ExTrack.saveParameters( optimum, savefile.getAbsolutePath() );
			}
			catch ( final IOException e )
			{
				System.err.println( "Problem while saving to " + savefile );
				System.err.println( e.getMessage() );
				System.err.println( "Aborting." );
				return;
			}
			System.out.println( "Saved to " + savefile.getAbsolutePath() );
		}

		// Load.
		System.out.println( "\nLoading the parameters from a JSon file." );
		ExTrackParameters loadedparams = null;
		try
		{
			loadedparams = ExTrack.loadParameters( savefile.getAbsolutePath() );
			System.out.println( "Loaded from " + savefile );
			System.out.println( "Parameters loaded:" );
		}
		catch ( final IOException e )
		{
			System.err.println( "Problem while loading from " + savefile );
			System.err.println( e.getMessage() );
			System.err.println( "Aborting." );
			return;
		}
		System.out.println( loadedparams );

		// Predict probabilities.
		System.out.println();
		System.out.println( "Predicting diffusive & stuck probabilities..." );
		extrack.computeProbabilities( loadedparams );
		System.out.println( "Done." );

		// Print probabilities.
		System.out.println();
		System.out.println( "Content of model features now:" );
		System.out.println( "-----------------------------------------------------------------" );
		System.out.println( String.format( "| %-25s | %-15s | %-15s |", "", "P stuck", "P diffusive" ) );
		System.out.println( "-----------------------------------------------------------------" );
		final SpotCollection allspots = model.getSpots();
		final NavigableSet< Integer > frames = allspots.keySet();
		for ( final Integer frame : frames )
		{
			System.out.println( "Frame " + frame + ":" );
			final Iterable< Spot > spots = allspots.iterable( frame.intValue(), true );
			for ( final Spot spot : spots )
			{
				System.out.println( String.format( "| %-25s | %-15.3g | %-15.3g |",
						spot.getName(),
						spot.getFeature( "EXTRACK_P_STUCK" ).doubleValue(),
						spot.getFeature( "EXTRACK_P_DIFFUSIVE" ).doubleValue() ) );
			}
			System.out.println( "-----------------------------------------------------------------" );
			break;
		}
	}
}
