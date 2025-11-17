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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Jama.Matrix;
import net.imglib2.algorithm.MultiThreaded;
import pal.math.MultivariateFunction;

public class NegativeLikelihoodFunction implements MultivariateFunction, MultiThreaded
{

	private static final int N_ARGS = 5;

	private final Map< Integer, Matrix > Cs;

	private final int nbSubSteps;

	private final boolean doFrame;

	private final int frameLen;

	private final double[] lowerBound;

	private final double[] upperBound;

	private final boolean doPred;

	private int numThreads;

	private ExecutorService executorService;

	public NegativeLikelihoodFunction(
			final Map< Integer, Matrix > Cs,
			final int nbSubSteps,
			final boolean doFrame,
			final int frameLen,
			final boolean doPred )
	{
		this.Cs = Cs;
		this.nbSubSteps = nbSubSteps;
		this.doFrame = doFrame;
		this.frameLen = frameLen;
		this.doPred = doPred;
		this.lowerBound = new double[ N_ARGS ];
		this.upperBound = new double[ N_ARGS ];
		setNumThreads();

		/*
		 * 0. localizationError
		 */

		lowerBound[ 0 ] = 0.005;
		upperBound[ 0 ] = 100.; // um

		/*
		 * 1. diffusionLengths0
		 */

		lowerBound[ 1 ] = 1e-100;
		upperBound[ 1 ] = 10.; // um

		/*
		 * 2. diffusionLengths1
		 */

		lowerBound[ 2 ] = lowerBound[ 1 ];
		upperBound[ 2 ] = upperBound[ 1 ];

		/*
		 * 3. F0.
		 */

		lowerBound[ 3 ] = 0.01;
		upperBound[ 3 ] = 0.99;

		/*
		 * 4. probabilityOfUnbinding
		 */

		lowerBound[ 4 ] = 0.01;
		upperBound[ 4 ] = 0.99;
	}

	@Override
	public double evaluate( final double[] argument )
	{
		return evalFun( argument, Cs, nbSubSteps, doFrame, frameLen, doPred, executorService );
	}

	@Override
	public int getNumArguments()
	{
		return N_ARGS;
	}

	@Override
	public double getLowerBound( final int n )
	{
		return lowerBound[ n ];
	}

	@Override
	public double getUpperBound( final int n )
	{
		return upperBound[ n ];
	}

	public static final double evalFun(
			final double[] params,
			final Map< Integer, Matrix > tracks,
			final int nbSubSteps,
			final boolean doFrame,
			final int frameLen,
			final boolean doPred,
			final ExecutorService executorService )
	{
		final double localizationError = params[ 0 ];
		final double diffusionLength0 = params[ 1 ];
		final double diffusionLength1 = params[ 2 ];
		final double F0 = params[ 3 ];
		final double probabilityOfUnbindingContinuous = params[ 4 ];

		double sumLogProbas = 0.; // all tracks
		final TrackState state = new TrackState(
				localizationError,
				diffusionLength0,
				diffusionLength1,
				F0,
				probabilityOfUnbindingContinuous,
				nbSubSteps,
				doFrame,
				frameLen,
				doPred );
		
		
		final List< Future< Double > > futures = new ArrayList<>( tracks.size() );
		for ( final Integer trackID : tracks.keySet() )
		{
			final Future< Double > future = executorService.submit( new Callable< Double >()
			{

				@Override
				public Double call() throws Exception
				{
					final Matrix track = tracks.get( trackID );
					final Matrix[] vals = state.eval( track );
					final Matrix probabilities = vals[ 0 ];

					double sumProba = 0.; // one track
					for ( int r = 0; r < probabilities.getRowDimension(); r++ )
						sumProba += probabilities.get( r, 0 );

					return Double.valueOf( sumProba );
				}
			} );
			futures.add( future );
		}

		for ( final Future< Double > future : futures )
		{
			Double val;
			try
			{
				val = future.get();
				sumLogProbas += Math.log( val.doubleValue() );
			}
			catch ( InterruptedException | ExecutionException e )
			{
				e.printStackTrace();
			}
		}
		return -sumLogProbas;
	}

	@Override
	public void setNumThreads()
	{
		setNumThreads( Runtime.getRuntime().availableProcessors() / 2 );
	}

	@Override
	public void setNumThreads( final int numThreads )
	{
		this.numThreads = Math.max( 1, numThreads );
		this.executorService = Executors.newFixedThreadPool( numThreads );
	}

	@Override
	public int getNumThreads()
	{
		return numThreads;
	}
}
