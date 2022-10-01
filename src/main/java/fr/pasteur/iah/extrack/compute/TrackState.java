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
package fr.pasteur.iah.extrack.compute;

import Jama.Matrix;

public class TrackState
{

	private final double localizationError;

	private final double diffusionLength0;

	private final double diffusionLength1;

	private final double F0;

	private final double F1;

	private final int nbSubSteps;

	private final boolean doFrame;

	private final int frameLen;

	private final boolean doPred;

	private final double probabilityOfUnbinding;

	private final double probabilityOfBinding;

	public TrackState(
			final double localizationError,
			final double diffusionLength0,
			final double diffusionLength1,
			final double F0,
			final double probabilityOfUnbindingContinuous,
			final int nbSubSteps,
			final boolean doFrame,
			final int frameLen,
			final boolean doPred )
	{
		this.localizationError = localizationError;
		this.diffusionLength0 = diffusionLength0;
		this.diffusionLength1 = diffusionLength1;
		this.F0 = F0;
		this.F1 = 1. - F0;
		this.nbSubSteps = nbSubSteps;
		this.doFrame = doFrame;
		this.frameLen = frameLen;
		this.doPred = doPred;

		// Compute probabilityOfBindingContinuous.
		final double probabilityOfBindingContinuous = F0 / F1 * probabilityOfUnbindingContinuous;

		// Correct input probabilities from continuous to discrete.
		this.probabilityOfUnbinding = 1. - Math.exp( -probabilityOfUnbindingContinuous / nbSubSteps );
		this.probabilityOfBinding = 1. - Math.exp( -probabilityOfBindingContinuous / nbSubSteps );
	}

	/**
	 * Returns:
	 * <ol start="0">
	 * <li>the matrix of probabilities P
	 * <li>the matrix of state prediction pred
	 * </ol>
	 *
	 * @param track
	 *            the track to evaluate.
	 * @return a new array of matrices.
	 */
	public Matrix[] eval( final Matrix track )
	{

		/*
		 * Initialize.
		 */

		final Matrix TrMat = initTransitionMatrix( probabilityOfUnbinding, probabilityOfBinding );
		final Matrix currBs0 = createCurrBs( nbSubSteps + 1 );
		final Matrix currDs0 = initDiffusionLengthMatrix( currBs0, diffusionLength0, diffusionLength1 );
		final Matrix LT0 = initLogTransitionProbaMatrix( currBs0, TrMat );

		final Matrix currC0 = getDetection( track, track.getRowDimension() - 1 );
		Matrix LP = initLogProbabilityMatrix( LT0 );
		Matrix Km = initLocalizationDensityProbabilityM( currC0, currDs0 );
		Matrix Ks = initLocalizationDensityProbabilityS( localizationError, currDs0 );

		/*
		 * Iterate.
		 */

		int currentStep = 2;
		int removeStep = 0;
		Matrix currStates = null;
		final int nbLocs = track.getRowDimension();
		final int nbSubLocs = ( nbLocs - 1 ) * nbSubSteps + 1;
		final Matrix pred = ( doPred ) ? new Matrix( nbSubLocs, 2, -1. ) : null;
		Matrix currBs = null;

		while ( currentStep <= nbLocs - 1 )
		{
			currBs = createCurrBs( currentStep * nbSubSteps + 1 - removeStep );
			currStates = createStateMatrix( currBs, nbSubSteps );
			final Matrix currDs = initDiffusionLengthMatrix( currStates, diffusionLength0, diffusionLength1 );
			final Matrix LT = initLogTransitionProbaMatrix( currStates, TrMat );

			Km = iterate( Km, nbSubSteps );
			Ks = iterate( Ks, nbSubSteps );
			LP = iterate( LP, nbSubSteps );

			final int detectionRow = nbLocs - currentStep;
			final Matrix currC = getDetection( track, detectionRow );

			// Iterate via log integral diff.
			// Can we make an object we would iterate?
			final Matrix[] K = logIntegralDiff(
					currC,
					localizationError,
					currDs,
					Km,
					Ks );
			Km = K[ 0 ];
			Ks = K[ 1 ];
			final Matrix LC = K[ 2 ];
			LP = LP.plus( LT ).plus( LC );

			int currNbBs = currBs.getRowDimension();
			if ( doFrame && currentStep < nbLocs - 1 )
			{
				while ( currNbBs >= ( int ) Math.pow( 2, frameLen ) )
				{
					if ( doPred )
					{
						/*
						 * Compute new value for KsLoop.
						 */

						final Matrix KsPred = new Matrix( Ks.getRowDimension(), 1 );
						for ( int r = 0; r < Ks.getRowDimension(); r++ )
						{
							final double ks = Ks.get( r, 0 );
							final double val = Math.sqrt( ks * ks + localizationError * localizationError );
							KsPred.set( r, 0, val );
						}

						/*
						 * Compute real value of probability.
						 */

						final Matrix logIntegratedTerm = new Matrix( KsPred.getRowDimension(), 1 );
						for ( int r = 0; r < KsPred.getRowDimension(); r++ )
						{
							final double ks = KsPred.get( r, 0 );
							double sumC = 0.;
							for ( int c = 0; c < track.getColumnDimension(); c++ )
							{
								final double km = Km.get( r, c );
								final double t = track.get( nbLocs - currentStep, c );
								final double dx = ( t - km );
								sumC += dx * dx;
							}
							final double val = -Math.log( 2. * Math.PI * ks * ks ) - sumC / ( 2. * ks * ks );
							logIntegratedTerm.set( r, 0, val );
						}

						/*
						 * Log of probabilities to be bound.
						 */

						final Matrix LFPred = new Matrix( currStates.getRowDimension(), 1 );
						for ( int r = 0; r < LFPred.getRowDimension(); r++ )
						{
							final double val = currStates.get( r, 0 ) == 0. ? F0 : F1;
							LFPred.set( r, 0, Math.log( val ) );
						}

						/*
						 * Update LPloop.
						 */

						final Matrix LPPred = LP.plus( logIntegratedTerm ).plus( LFPred );

						final Matrix PPred = new Matrix( LPPred.getRowDimension(), LPPred.getColumnDimension() );
						for ( int r = 0; r < LPPred.getRowDimension(); r++ )
							for ( int c = 0; c < LPPred.getColumnDimension(); c++ )
								PPred.set( r, c, Math.exp( LPPred.get( r, c ) ) );

						for ( int state = 0; state < 2; state++ )
						{

							// Conditional sum & global sum of P.
							double conditionalSumPPred = 0.;
							double sumPPred = 0.;
							for ( int r = 0; r < PPred.getRowDimension(); r++ )
							{
								final double p = PPred.get( r, 0 );
								sumPPred += p;
								final double stateID = currBs.get( r, currBs.getColumnDimension() - 1 );
								if ( state == stateID )
									conditionalSumPPred += p;
							}

							final double val = conditionalSumPPred / sumPPred;
//							pred.set( nbLocs - currentStep + frameLen - 2, state, val );
							pred.set( pred.getRowDimension() - ( removeStep + 1 ), state, val );
						}
					}

					/*
					 * Update currBs.
					 */

					final Matrix currBsLoopTmp = new Matrix(
							currBs.getRowDimension() / 2,
							currBs.getColumnDimension() - 1 );
					for ( int r = 0; r < currBsLoopTmp.getRowDimension(); r++ )
						for ( int c = 0; c < currBsLoopTmp.getColumnDimension(); c++ )
							currBsLoopTmp.set( r, c, currBs.get( r, c ) );
					currBs = currBsLoopTmp;

					final Matrix[] Kloop2 = fuseTracks(
							Km,
							Ks,
							LP );
					Km = Kloop2[ 0 ];
					Ks = Kloop2[ 1 ];
					LP = Kloop2[ 2 ];

					currNbBs = currBs.getRowDimension();
					removeStep += 1;

					currStates = createStateMatrix( currBs, nbSubSteps );
				}
			}

			/*
			 * Iterate.
			 */

			currentStep++;

		}

		/*
		 * Compute new value for KsLoop.
		 */

		final Matrix KsLoopTmp = new Matrix( Ks.getRowDimension(), 1 );
		for ( int r = 0; r < Ks.getRowDimension(); r++ )
		{
			final double ks = Ks.get( r, 0 );
			final double val = Math.sqrt( ks * ks + localizationError * localizationError );
			KsLoopTmp.set( r, 0, val );
		}
		Ks = KsLoopTmp;

		/*
		 * Compute real value of probability.
		 */

		final Matrix logIntegratedTerm = new Matrix( Ks.getRowDimension(), 1 );
		for ( int r = 0; r < Ks.getRowDimension(); r++ )
		{
			final double ks = Ks.get( r, 0 );
			double sumC = 0.;
			for ( int c = 0; c < track.getColumnDimension(); c++ )
			{
				final double km = Km.get( r, c );
				final double t = track.get( 0, c );
				final double dx = ( t - km );
				sumC += dx * dx;
			}
			final double val = -Math.log( 2. * Math.PI * ks * ks ) - sumC / ( 2. * ks * ks );
			logIntegratedTerm.set( r, 0, val );
		}

		/*
		 * Log of probabilities to be bound.
		 */

		final Matrix LF = new Matrix( currStates.getRowDimension(), 1 );
		for ( int r = 0; r < LF.getRowDimension(); r++ )
		{
			final double val = currStates.get( r, 0 ) == 0. ? F0 : F1;
			LF.set( r, 0, Math.log( val ) );
		}
		/*
		 * Update LPloop.
		 */

		LP = LP.plus( logIntegratedTerm ).plus( LF );

		final Matrix P = new Matrix( LP.getRowDimension(), 1 );
		for ( int r = 0; r < LP.getRowDimension(); r++ )
			P.set( r, 0, Math.exp( LP.get( r, 0 ) ) );

		/*
		 * Update predictions.
		 */

		if ( doPred )
		{
			for ( int state = 0; state < 2; state++ )
			{
//				for ( int rowPred = 0; rowPred < Math.min( frameLen, pred.getRowDimension() ); rowPred++ )
				for ( int rowPred = 0; rowPred < currBs.getColumnDimension(); rowPred++ )
				{

					// Conditional sum & global sum of P.
					double conditionalSumPPred = 0.;
					double sumPPred = 0.;
					for ( int r = 0; r < P.getRowDimension(); r++ )
					{
						final double p = P.get( r, 0 );
						sumPPred += p;
						final double stateID = currBs.get( r, rowPred );
						if ( state == stateID )
							conditionalSumPPred += p;
					}

					final double val = conditionalSumPPred / sumPPred;
					pred.set( rowPred, state, val );
				}
			}
		}

		/*
		 * Cherry-pick pred. Make a smaller matrix, jumping over nbSubSteps so
		 * that outPred has the same size that of locs.
		 */

		final Matrix outPred;
		if ( doPred )
		{
			outPred = new Matrix( nbLocs, pred.getColumnDimension() );
			for ( int rowOutPred = 0; rowOutPred < outPred.getRowDimension(); rowOutPred++ )
			{
				final int rowPred = rowOutPred * nbSubSteps;
				for ( int state = 0; state < outPred.getColumnDimension(); state++ )
				{
					final double val = pred.get( rowPred, state );
					outPred.set( rowOutPred, state, val );
				}
			}
		}
		else
		{
			outPred = null;
		}

		return new Matrix[] { P, outPred };
	}

	private static Matrix[] fuseTracks( final Matrix Km, final Matrix Ks, final Matrix LP )
	{
		final int currNbBs = LP.getRowDimension();
		final int i = currNbBs / 2;

		final Matrix LP0 = LP.getMatrix( 0, i - 1, 0, 0 );
		final Matrix LP1 = LP.getMatrix( i, currNbBs - 1, 0, 0 );

		final Matrix maxLP = new Matrix( LP0.getRowDimension(), 1 );
		for ( int r = 0; r < LP0.getRowDimension(); r++ )
		{
			final double lp0 = LP0.get( r, 0 );
			final double lp1 = LP1.get( r, 0 );
			maxLP.set( r, 0, Math.max( lp0, lp1 ) );
		}

		final Matrix P0 = new Matrix( LP0.getRowDimension(), 1 );
		for ( int r = 0; r < P0.getRowDimension(); r++ )
		{
			final double lp0 = LP0.get( r, 0 );
			final double mlp = maxLP.get( r, 0 );
			final double val = Math.exp( lp0 - mlp );
			P0.set( r, 0, val );
		}

		final Matrix P1 = new Matrix( LP1.getRowDimension(), 1 );
		for ( int r = 0; r < P1.getRowDimension(); r++ )
		{
			final double lp1 = LP1.get( r, 0 );
			final double mlp = maxLP.get( r, 0 );
			final double val = Math.exp( lp1 - mlp );
			P1.set( r, 0, val );
		}

		final Matrix SP = P0.plus( P1 );
		final Matrix A0 = P0.arrayRightDivide( SP );
		final Matrix A1 = P1.arrayRightDivide( SP );

		final Matrix Km0 = Km.getMatrix( 0, i - 1, 0, Km.getColumnDimension() - 1 );
		final Matrix Km1 = Km.getMatrix( i, Km.getRowDimension() - 1, 0, Km.getColumnDimension() - 1 );

		final Matrix Ks0 = Ks.getMatrix( 0, i - 1, 0, Ks.getColumnDimension() - 1 );
		final Matrix Ks1 = Ks.getMatrix( i, Ks.getRowDimension() - 1, 0, Ks.getColumnDimension() - 1 );

		final Matrix KmNew = new Matrix( Km0.getRowDimension(), Km0.getColumnDimension() );
		for ( int r = 0; r < KmNew.getRowDimension(); r++ )
		{
			for ( int c = 0; c < KmNew.getColumnDimension(); c++ )
			{
				final double a0 = A0.get( r, 0 );
				final double km0 = Km0.get( r, c );
				final double a1 = A1.get( r, 0 );
				final double km1 = Km1.get( r, c );
				KmNew.set( r, c, a0 * km0 + a1 * km1 );
			}
		}
		final Matrix KsNew = new Matrix( A0.getRowDimension(), A0.getColumnDimension() );
		for ( int r = 0; r < KsNew.getRowDimension(); r++ )
		{
			for ( int c = 0; c < KsNew.getColumnDimension(); c++ )
			{
				final double a0 = A0.get( r, c );
				final double a1 = A1.get( r, c );
				final double ks0 = Ks0.get( r, c );
				final double ks1 = Ks1.get( r, c );
				final double val = Math.sqrt( a0 * ks0 * ks0 + a1 * ks1 * ks1 );
				KsNew.set( r, c, val );
			}
		}

		final Matrix LPNew = new Matrix( LP0.getRowDimension(), 1 );
		for ( int r = 0; r < LP0.getRowDimension(); r++ )
		{
			final double sp = SP.get( r, 0 );
			final double mlp = maxLP.get( r, 0 );
			final double val = mlp + Math.log( sp );
			LPNew.set( r, 0, val );
		}

		return new Matrix[] { KmNew, KsNew, LPNew };
	}

	private static Matrix[] logIntegralDiff(
			final Matrix currCLoop,
			final double localizationError,
			final Matrix currDs3Loop,
			final Matrix KmLoop,
			final Matrix KsLoop )
	{
		final int nbDims = currCLoop.getColumnDimension();

		final Matrix Km = new Matrix( KmLoop.getRowDimension(), KmLoop.getColumnDimension() );
		final Matrix Ks = new Matrix( KsLoop.getRowDimension(), 1 );
		final Matrix LC = new Matrix( KsLoop.getRowDimension(), 1 );

		for ( int r = 0; r < KmLoop.getRowDimension(); r++ )
		{
			for ( int c = 0; c < KmLoop.getColumnDimension(); c++ )
			{
				final double km = KmLoop.get( r, c );
				final double ks = KsLoop.get( r, 0 );
				final double val = ( km * localizationError * localizationError + currCLoop.get( 0, c ) * ks * ks )
						/ ( localizationError * localizationError + ks * ks );
				Km.set( r, c, val );
			}
		}

		for ( int r = 0; r < KsLoop.getRowDimension(); r++ )
		{
			final double ks = KsLoop.get( r, 0 );
			final double cd = currDs3Loop.get( r, 0 );
			final double val = Math.sqrt(
					( cd * cd * localizationError * localizationError
							+ cd * cd * ks * ks
							+ localizationError * localizationError * ks * ks )
							/ ( localizationError * localizationError + ks * ks ) );
			Ks.set( r, 0, val );
		}

		for ( int r = 0; r < LC.getRowDimension(); r++ )
		{
			final double ks = KsLoop.get( r, 0 );
			final double ksOut = Ks.get( r, 0 );
			final double cd = currDs3Loop.get( r, 0 );

			double sumKm = 0.;
			for ( int c = 0; c < KmLoop.getColumnDimension(); c++ )
			{
				final double cc = currCLoop.get( 0, c );
				final double kmOut = Km.get( r, c );
				final double km = KmLoop.get( r, c );
				sumKm += ( kmOut * kmOut / ( 2 * ksOut * ksOut )
						- ( km * km * localizationError * localizationError + ks * ks * cc * cc + ( km - cc ) * ( km - cc ) * cd * cd )
								/ ( 2 * ksOut * ksOut * ( localizationError * localizationError + ks * ks ) ) );
			}

			LC.set( r, 0, sumKm + nbDims * Math.log( 1. /
					( Math.sqrt( 2 * Math.PI * ( localizationError * localizationError + ks * ks ) ) ) ) );

		}

		return new Matrix[] { Km, Ks, LC };
	}

	private static Matrix iterate( Matrix K, final int nbSubSteps )
	{
		K = repeatLines( K, ( int ) Math.pow( 2, nbSubSteps ) );
		return K;
	}

	private final Matrix createStateMatrix( final Matrix currBs, final int nbSubSteps )
	{
		final Matrix currStates = new Matrix( currBs.getRowDimension(), nbSubSteps + 1 );
		for ( int r = 0; r < currStates.getRowDimension(); r++ )
			for ( int c = 0; c < nbSubSteps + 1; c++ )
				currStates.set( r, c, currBs.get( r, c ) );
		return currStates;
	}

	private static Matrix initLocalizationDensityProbabilityS( final double localizationError, final Matrix currDs )
	{
		final Matrix Ks = new Matrix( currDs.getRowDimension(), 1 );
		for ( int r = 0; r < currDs.getRowDimension(); r++ )
		{
			final double valCurrDs = currDs.get( r, 0 );
			final double val = Math.sqrt( localizationError * localizationError + valCurrDs * valCurrDs );
			Ks.set( r, 0, val );
		}
		return Ks;
	}

	private static Matrix initLocalizationDensityProbabilityM( final Matrix currC, final Matrix currDs )
	{
		final Matrix Km = new Matrix( currDs.getRowDimension(), currC.getColumnDimension() );
		for ( int r = 0; r < currDs.getRowDimension(); r++ )
			for ( int c = 0; c < currC.getColumnDimension(); c++ )
				Km.set( r, c, currC.get( 0, c ) );
		return Km;
	}

	private static Matrix initDiffusionLengthMatrix( final Matrix currStates, final double diffusionLength0, final double diffusionLength1 )
	{
		final Matrix currDs = new Matrix( currStates.getRowDimension(), currStates.getColumnDimension() );
		for ( int r = 0; r < currDs.getRowDimension(); r++ )
		{
			for ( int c = 0; c < currDs.getColumnDimension(); c++ )
			{
				final double val = currStates.get( r, c ) == 0. ? diffusionLength0 : diffusionLength1;
				currDs.set( r, c, val );
			}
		}

		/*
		 * Iterate currDs.
		 */

		final Matrix currDs2 = new Matrix( currDs.getRowDimension(), currDs.getColumnDimension() - 1 );
		for ( int r = 0; r < currDs2.getRowDimension(); r++ )
		{
			for ( int c = 0; c < currDs2.getColumnDimension(); c++ )
			{
				final double val1 = currDs.get( r, c );
				final double val2 = currDs.get( r, c + 1 );
				currDs2.set( r, c, Math.sqrt( ( val1 * val1 + val2 * val2 ) / 2. ) );
			}
		}

		/*
		 * Iterate currDs second time.
		 */

		final Matrix currDs3 = new Matrix( currDs2.getRowDimension(), 1 );
		for ( int r = 0; r < currDs2.getRowDimension(); r++ )
		{
			double sumSq = 0.;
			for ( int c = 0; c < currDs2.getColumnDimension(); c++ )
			{
				final double val = currDs2.get( r, c );
				sumSq += val * val;
			}
			final double meanSqRootSumSq = Math.sqrt( sumSq / currDs2.getColumnDimension() );
			currDs3.set( r, 0, meanSqRootSumSq );
		}

		return currDs3;
	}

	private static Matrix initLogProbabilityMatrix( final Matrix LT )
	{
		return LT.copy();
	}

	private static Matrix initLogTransitionProbaMatrix( final Matrix currStates, final Matrix TrMat )
	{
		final Matrix LTtemp = new Matrix( currStates.getRowDimension(), currStates.getColumnDimension() - 1 );
		for ( int r = 0; r < currStates.getRowDimension(); r++ )
		{
			for ( int c = 0; c < currStates.getColumnDimension() - 1; c++ )
			{
				final int val1 = ( int ) currStates.get( r, c );
				final int val2 = ( int ) currStates.get( r, c + 1 );

				final double val = TrMat.get( val1, val2 );
				LTtemp.set( r, c, val );
			}
		}

		final Matrix LT = new Matrix( currStates.getRowDimension(), 1 );
		for ( int r = 0; r < LT.getRowDimension(); r++ )
		{
			double sum = 0.;
			for ( int c = 0; c < LTtemp.getColumnDimension(); c++ )
				sum += Math.log( LTtemp.get( r, c ) );

			LT.set( r, 0, sum );
		}
		return LT;
	}

	private static Matrix createCurrBs( final int nbSubSteps )
	{
		Matrix allBs = new Matrix( 2, 1 );
		allBs.set( 0, 0, 0. );
		allBs.set( 1, 0, 1. );

		for ( int i = 0; i < nbSubSteps - 1; i++ )
		{
			final Matrix nAllBs = new Matrix( 2 * allBs.getRowDimension(), allBs.getColumnDimension() + 1 );

			for ( int r = 0; r < allBs.getRowDimension(); r++ )
			{
				final int nr = 2 * r;
				// 1st column -> set to 0.
				nAllBs.set( nr, 0, 0. );
				nAllBs.set( nr + 1, 0, 1. );

				// Other columns: we copy the previous row.
				for ( int c = 0; c < allBs.getColumnDimension(); c++ )
				{
					nAllBs.set( nr, 1 + c, allBs.get( r, c ) );
					nAllBs.set( nr + 1, 1 + c, allBs.get( r, c ) );
				}
			}
			allBs = nAllBs;
		}

		return allBs;
	}

	private static Matrix initTransitionMatrix( final double probabilityOfUnbinding, final double probabilityOfBinding )
	{
		final Matrix TrMat = new Matrix( 2, 2 );
		TrMat.set( 0, 0, 1. - probabilityOfUnbinding );
		TrMat.set( 0, 1, probabilityOfUnbinding );
		TrMat.set( 1, 0, probabilityOfBinding );
		TrMat.set( 1, 1, 1. - probabilityOfBinding );
		return TrMat;
	}

	private static Matrix repeatLines( final Matrix M, final int n )
	{
		final Matrix N = new Matrix( n * M.getRowDimension(), M.getColumnDimension() );
		for ( int c = 0; c < M.getColumnDimension(); c++ )
		{
			for ( int r = 0; r < M.getRowDimension(); r++ )
			{
				final int nr = n * r;
				for ( int inc = 0; inc < n; inc++ )
				{
					final double val = M.get( r, c );
					N.set( nr + inc, c, val );
				}
			}
		}
		return N;
	}

	private static Matrix getDetection( final Matrix track, final int n )
	{
		final Matrix row = new Matrix( 1, track.getColumnDimension() );
		for ( int c = 0; c < track.getColumnDimension(); c++ )
			row.set( 0, c, track.get( n, c ) );
		return row;
	}
}
