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
package fr.pasteur.iah.extrack;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Jama.Matrix;
import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fr.pasteur.iah.extrack.compute.ExTrackDoPredictions;
import fr.pasteur.iah.extrack.compute.ExTrackParameterOptimizer;
import fr.pasteur.iah.extrack.compute.ExTrackParameters;
import fr.pasteur.iah.extrack.util.ExTrackUtil;

/**
 * User oriented class, that can call ExTrack feature in a simple manner. This
 * class is mainly aimed for scripting purposes.
 * 
 * @author Jean-Yves Tinevez
 */
public class ExTrack
{

	private final Model model;

	private final Logger logger;

	public ExTrack( final Model model )
	{
		this( model, Logger.IJ_LOGGER );
	}

	public ExTrack( final Model model, final Logger logger )
	{
		this.model = model;
		this.logger = logger;
	}

	/**
	 * Computes the probabilities for all spots in the specified model to be
	 * stuck or diffusive, using the specified parameters for particle motility.
	 * <p>
	 * The results of the probabilities predictions are stored:
	 * <ul>
	 * <li>for spots, in the two features named
	 * '<code>EXTRACK_P_DIFFUSIVE</code>' and
	 * '<code>EXTRACK_P_STUCK</code>'.</li>
	 * <li>for edges that are the target of these spots, the same values are
	 * stored in the two edge features '<code>EXTRACK_EDGE_P_DIFFUSIVE</code>'
	 * and '<code>EXTRACK_EDGE_P_STUCK</code>'.
	 * </ul>
	 * 
	 * @param parameters
	 *            the motility parameters, possibly estimated with
	 *            <code>estimateParameters()</code>.
	 */
	public void computeProbabilities( final ExTrackParameters parameters )
	{
		final ExTrackDoPredictions predictions = new ExTrackDoPredictions( parameters, model, logger );
		predictions.run();
	}

	/**
	 * Estimates the motility parameters from the tracks in the model. This
	 * estimation can take several minutes or be very long depending on the
	 * value of <code>nbSubSteps</code> and <code>nFrames</code> in the
	 * estimation parameters you set.
	 * <p>
	 * The estimation relies on the Brent's modification of a conjugate
	 * direction search method proposed by Powell, that we adapted from the PAL
	 * library.
	 * 
	 * @param startPoint
	 *            the estimation parameters to use.
	 * @return a new set of motility parameters.
	 */
	public ExTrackParameters estimateParameters( final ExTrackParameters startPoint )
	{
		final Map< Integer, Matrix > tracks = ExTrackUtil.toMatrix( model );
		final Consumer< double[] > valueWatcher = e -> {}; // do nothing.
		final ExTrackParameterOptimizer optimizer = new ExTrackParameterOptimizer( startPoint, tracks, logger, valueWatcher );
		optimizer.run();
		final ExTrackParameters optimum = optimizer.getParameters();
		return optimum;
	}

	/**
	 * Save parameters to a JSon file.
	 * 
	 * @param params
	 *            the parameters to save.
	 * @param path
	 *            the path to save them to. Ideally ends in '.json'
	 * @throws IOException
	 *             if something wrong happens while saving.
	 */
	public static final void saveParameters( final ExTrackParameters params, final String path ) throws IOException
	{
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (final FileWriter file = new FileWriter( path ))
		{
			final String serialized = gson.toJson( params );
			file.write( serialized );
			file.flush();
		}
	}

	/**
	 * Load parameters from a JSon file.
	 * 
	 * @param path
	 *            the path to the JSon file.
	 * @return the loaded parameters.
	 * @throws IOException
	 *             is something wrong happens while loading.
	 */
	public static final ExTrackParameters loadParameters( final String path ) throws IOException
	{
		final Gson gson = new Gson();
		final String content = new String( Files.readAllBytes( Paths.get( path ) ) );
		final ExTrackParameters params = gson.fromJson( content, ExTrackParameters.class );
		return params;
	}
}
