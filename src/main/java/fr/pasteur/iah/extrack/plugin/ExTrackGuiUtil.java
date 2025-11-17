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
package fr.pasteur.iah.extrack.plugin;

import java.awt.Image;

import javax.swing.ImageIcon;

public class ExTrackGuiUtil
{
	public static final ImageIcon ICON = new ImageIcon( ExTrackGuiUtil.class.getResource( "TrackMateExTrack-logo.png" ) );

	public static ImageIcon getIcon()
	{
		final int w = 200;
		final int h = 200;
		return getIcon( w, h );
	}

	public static ImageIcon getIcon( final int w, final int h )
	{

		final Image image = ICON.getImage();
		int nw = ICON.getIconWidth();
		int nh = ICON.getIconHeight();

		if ( ICON.getIconWidth() > w )
		{
			nw = w;
			nh = ( nw * ICON.getIconHeight() ) / ICON.getIconWidth();
		}

		if ( nh > h )
		{
			nh = h;
			nw = ( ICON.getIconWidth() * nh ) / ICON.getIconHeight();
		}
		final Image newimg = image.getScaledInstance( nw, nh, java.awt.Image.SCALE_SMOOTH );
		return new ImageIcon( newimg );
	}

}
