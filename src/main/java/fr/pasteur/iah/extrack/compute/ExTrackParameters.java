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

public class ExTrackParameters
{
	public final double localizationError;

	public final double diffusionLength0;

	public final double diffusionLength1;

	public final double F0;

	public final double probabilityOfUnbinding;

	public final int nbSubteps;

	public final int nFrames;

	private ExTrackParameters(
			final double localizationError,
			final double diffusionLength0,
			final double diffusionLength1,
			final double F0,
			final double probabilityOfUnbinding,
			final int nbSubteps,
			final int nFrames )
	{
		this.localizationError = localizationError;
		this.diffusionLength0 = diffusionLength0;
		this.diffusionLength1 = diffusionLength1;
		this.F0 = F0;
		this.probabilityOfUnbinding = probabilityOfUnbinding;
		this.nbSubteps = nbSubteps;
		this.nFrames = nFrames;
	}

	public double[] optimParamstoArray()
	{
		return new double[] {
				localizationError,
				diffusionLength0,
				diffusionLength1,
				F0,
				probabilityOfUnbinding };
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() );
		str.append( String.format( "\n%-40s: %-8.3g", " - Localization error", localizationError ) );
		str.append( String.format( "\n%-40s: %-8.3g", " - Diffusion length for diffusive state", diffusionLength0 ) );
		str.append( String.format( "\n%-40s: %-8.3g", " - Diffusion length for bound state", diffusionLength1 ) );
		str.append( String.format( "\n%-40s: %-8.3g", " - Fraction in diffusive state", F0 ) );
		str.append( String.format( "\n%-40s: %-8.3g", " - Probability of unbinding", probabilityOfUnbinding ) );
		str.append( String.format( "\n%-40s: %d", " - Number of sub-steps for optimization", nbSubteps ) );
		str.append( String.format( "\n%-40s: %d\n", " - Number of frames for optimization", nFrames ) );
		return str.toString();
	}

	public static final Builder create()
	{
		return new Builder();
	}

	public static final ExTrackParameters ESTIMATION_START_POINT = new ExTrackParameters( 0.3, 0.08, 0.08, 0.1, 0.9, 2, 5 );

	public static class Builder
	{
		private double localizationError = 0.1;

		private double diffusionLength0 = 0.5;

		private double diffusionLength1 = 0.01;

		private double F0 = 0.5;

		private double probabilityOfUnbinding = 0.9;

		private int nbSubSteps = 1;

		private int nFrames = 6;

		public Builder localizationError( final double localizationError )
		{
			this.localizationError = localizationError;
			return this;
		}

		public Builder diffusionLength0( final double diffusionLength0 )
		{
			this.diffusionLength0 = diffusionLength0;
			return this;
		}

		public Builder diffusionLength1( final double diffusionLength1 )
		{
			this.diffusionLength1 = diffusionLength1;
			return this;
		}

		public Builder F0( final double F0 )
		{
			this.F0 = F0;
			return this;
		}

		public Builder probabilityOfUnbinding( final double probabilityOfUnbinding )
		{
			this.probabilityOfUnbinding = probabilityOfUnbinding;
			return this;
		}

		public Builder nbSubSteps( final int nbSubSteps )
		{
			this.nbSubSteps = nbSubSteps;
			return this;
		}

		public Builder nFrames( final int nFrames )
		{
			this.nFrames = nFrames;
			return this;
		}

		public ExTrackParameters build()
		{
			return new ExTrackParameters(
					localizationError,
					diffusionLength0,
					diffusionLength1,
					F0,
					probabilityOfUnbinding,
					nbSubSteps,
					nFrames );
		}
	}
}
