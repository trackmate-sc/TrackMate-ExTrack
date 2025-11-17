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

import java.util.Map;
import java.util.function.Consumer;

import org.scijava.Cancelable;

import Jama.Matrix;
import fiji.plugin.trackmate.Logger;

public class ExTrackParameterOptimizer implements Runnable, Cancelable
{

	private final Logger logger;

	private final ExTrackParameters startPoint;

	private final Map< Integer, Matrix > trackMatrices;

	/*
	 * Perform optimization. Optimizer is Powell optimizer updated by Brent.
	 */
	private final ConjugateDirectionSearch optimizer;

	public ExTrackParameterOptimizer(
			final ExTrackParameters startPoint,
			final Map< Integer, Matrix > trackMatrices,
			final Logger logger,
			final Consumer< double[] > valueWatcher )
	{
		this.startPoint = startPoint;
		this.trackMatrices = trackMatrices;
		this.logger = logger;
		this.optimizer = new ConjugateDirectionSearch( logger, valueWatcher );
	}

	@Override
	public void run()
	{
		final int nbSubSteps = startPoint.nbSubteps;
		final int frameLen = startPoint.nFrames;
		final boolean doFrame = true;
		// No doPred for optimization.
		final boolean doPred = false;

		final double[] parameters = startPoint.optimParamstoArray();
		final double tolfx = 1e-6;
		final double tolx = 1e-6;

		final NegativeLikelihoodFunction fun = new NegativeLikelihoodFunction( trackMatrices, nbSubSteps, doFrame, frameLen, doPred );
		optimizer.optimize(
				fun,
				parameters,
				tolfx, tolx );

		logger.log( "\n\n-------------------------------------------------------------------------\n", Logger.BLUE_COLOR );
		logger.log( String.format( "%40s: %8.3g\n", "Localization error", parameters[ 0 ] ), Logger.BLUE_COLOR );
		logger.log( String.format( "%40s: %8.3g\n", "Diffusion length for diffusive state", parameters[ 1 ] ), Logger.BLUE_COLOR );
		logger.log( String.format( "%40s: %8.3g\n", "Diffusion length for bound state", parameters[ 2 ] ), Logger.BLUE_COLOR );
		logger.log( String.format( "%40s: %8.3g\n", "Fraction in diffusive state", parameters[ 3 ] ), Logger.BLUE_COLOR );
		logger.log( String.format( "%40s: %8.3g\n", "Probability of unbinding", parameters[ 4 ] ), Logger.BLUE_COLOR );
	}

	@Override
	public boolean isCanceled()
	{
		return optimizer.isCanceled();
	}

	@Override
	public void cancel( final String reason )
	{
		optimizer.cancel( reason );
	}

	@Override
	public String getCancelReason()
	{
		return optimizer.getCancelReason();
	}

	public ExTrackParameters getParameters()
	{
		final double[] array = optimizer.getCurrentValue();
		return ExTrackParameters.create()
				.localizationError( array[ 0 ] )
				.diffusionLength0( array[ 1 ] )
				.diffusionLength1( array[ 2 ] )
				.F0( array[ 3 ] )
				.probabilityOfUnbinding( array[ 4 ] )
				.nbSubSteps( startPoint.nbSubteps )
				.nFrames( startPoint.nFrames )
				.build();
	}
}
