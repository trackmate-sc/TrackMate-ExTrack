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

import java.io.IOException;

import fr.pasteur.iah.extrack.numpy.NumPyReader;

public class NumPyReaderTestDrive
{

	public static void main( final String[] args ) throws IOException
	{
		final String file = "samples/tracks.npy";
//		final String file = "samples/params.npy";

		System.out.println( "Reading NumPy file: " + file );
		final long start = System.currentTimeMillis();
		final double[][] out = NumPyReader.readFile( file );
		final long end = System.currentTimeMillis();
		System.out.println( "Read file in " + ( end - start ) + " ms." );
		System.out.println( String.format( "Matrix size: %d x %d ", out[ 0 ].length, out.length ) );

		for ( int r = 0; r < out[ 0 ].length; r++ )
		{
			System.out.println();
			for ( int c = 0; c < out.length - 1; c++ )
				System.out.print( String.format( "%7.2f, ", out[ c ][ r ] ) );

			System.out.print( String.format( "%7.2f ", out[ out.length - 1 ][ r ] ) );
		}
	}
}
