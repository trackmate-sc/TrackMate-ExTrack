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

import javax.swing.JFrame;

import org.scijava.util.VersionUtils;

import fr.pasteur.iah.extrack.trackmate.ExTrackImporter;
import ij.ImageJ;
import ij.plugin.PlugIn;

public class ExTrackPluginImageFile implements PlugIn
{

	@Override
	public void run( final String arg )
	{
		final JFrame frame = new JFrame( ExTrackImporter.PLUGIN_NAME + " v" + VersionUtils.getVersion( getClass() ) );
		frame.setIconImage( ExTrackImporterPanel.ICON.getImage() );

		frame.getContentPane().add( new ExTrackImporterPanel(
				ExTrackImporterPanel.lastImagePath,
				ExTrackImporterPanel.lastDataPath,
				ExTrackImporterPanel.lastPizelSize,
				ExTrackImporterPanel.lastRadius,
				ExTrackImporterPanel.lastSpatialUnits,
				ExTrackImporterPanel.lastFrameInterval,
				ExTrackImporterPanel.lastTimeUnits ) );

		frame.pack();
		frame.setVisible( true );
	}

	public static void main( final String[] args )
	{
		ImageJ.main( args );
		ExTrackImporterPanel.lastDataPath = "samples/tracks.npy";
		ExTrackImporterPanel.lastImagePath = "samples/img.tif";
		new ExTrackPluginImageFile().run( "" );
	}
}
