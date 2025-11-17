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
package fr.pasteur.iah.extrack.util;

import java.awt.Image;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import Jama.Matrix;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;

public class ExTrackUtil
{

	public static final DecimalFormat FORMAT = new DecimalFormat( "0.#####E0" );

	public static final Map< Integer, Matrix > toMatrix( final Model model )
	{
		final Map< Integer, Matrix > Cs = new HashMap<>( model.getTrackModel().nTracks( true ) );
		for ( final Integer trackID : model.getTrackModel().trackIDs( true ) )
		{
			final List< Spot > track = new ArrayList<>( model.getTrackModel().trackSpots( trackID ) );
			track.sort( Spot.frameComparator );

			final Matrix C = new Matrix( track.size(), 2 );
			for ( int r = 0; r < track.size(); r++ )
			{
				final Spot spot = track.get( r );
				C.set( r, 0, spot.getDoublePosition( 0 ) );
				C.set( r, 1, spot.getDoublePosition( 1 ) );
			}
			Cs.put( trackID, C );
		}
		return Cs;
	}

	public static final Model toModel( final Map< Integer, Matrix > tracks )
	{
		final Model model = new Model();
		model.setPhysicalUnits( "µm", "s" );

		final double radius = 0.25; // µm.
		final double quality = 1.;
		final double frameInterval = 0.02; // s.

		model.beginUpdate();
		try
		{
			for ( final Integer trackID : tracks.keySet() )
			{
				// Offset the tracks because they all start at x=0, y=0.

				Spot previous = null;
				final Matrix track = tracks.get( trackID );
				int frame = 0;
				for ( int r = 0; r < track.getRowDimension(); r++ )
				{
					final double x = track.get( r, 0 );
					final double y = track.get( r, 1 );
					final double z = 0.;

					final Spot spot = new Spot( x, y, z, radius, quality );
					spot.putFeature( Spot.POSITION_T, frame * frameInterval );
					model.addSpotTo( spot, frame );

					if ( null != previous )
						model.addEdge( previous, spot, 1. );

					frame++;
					previous = spot;
				}
			}
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			model.endUpdate();
		}
		return model;
	}

    public static final ImageIcon scaleImage(final ImageIcon icon, final int w, final int h)
    {
        int nw = icon.getIconWidth();
        int nh = icon.getIconHeight();

        if(icon.getIconWidth() > w)
        {
          nw = w;
          nh = (nw * icon.getIconHeight()) / icon.getIconWidth();
        }

        if(nh > h)
        {
          nh = h;
          nw = (icon.getIconWidth() * nh) / icon.getIconHeight();
        }

        return new ImageIcon(icon.getImage().getScaledInstance(nw, nh, Image.SCALE_DEFAULT));
    }
}
