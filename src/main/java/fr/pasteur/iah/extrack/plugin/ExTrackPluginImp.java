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
package fr.pasteur.iah.extrack.plugin;

import javax.swing.JFrame;

import org.scijava.util.VersionUtils;

import fr.pasteur.iah.extrack.trackmate.ExTrackImporter;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class ExTrackPluginImp implements PlugIn
{

	@Override
	public void run( final String arg )
	{
		final ImagePlus imp = WindowManager.getCurrentImage();
		if ( imp == null )
		{
			IJ.error( "Please open an image before running " + ExTrackImporter.PLUGIN_NAME );
			return;
		}

		final JFrame frame = new JFrame( ExTrackImporter.PLUGIN_NAME + " v" + VersionUtils.getVersion( getClass() ) );
		frame.setIconImage( ExTrackImporterPanel.ICON.getImage() );

		frame.getContentPane().add( new ExTrackImporterPanel(
				imp,
				ExTrackImporterPanel.lastDataPath,
				ExTrackImporterPanel.lastRadius ) );

		frame.pack();
		frame.setVisible( true );
	}

	public static void main( final String[] args )
	{
		ImageJ.main( args );
		IJ.openImage( "samples/img.tif" ).show();
		ExTrackImporterPanel.lastDataPath = "samples/tracks.npy";
		new ExTrackPluginImp().run( "" );
	}
}
