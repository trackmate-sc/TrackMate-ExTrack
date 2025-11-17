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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import Jama.Matrix;
import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fr.pasteur.iah.extrack.numpy.NumPyReader;
import fr.pasteur.iah.extrack.util.ExTrackUtil;

public class ComputeExTrackTestDrive
{
	public static void main( final String[] args ) throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		/*
		 * Load data.
		 */

		final String trackFile = "samples/tracks.npy";
		final Map< Integer, Matrix > tracks = NumPyReader.readTracks( trackFile );

		/*
		 * Launch GUI.
		 */

		final ExTrackComputeAction action = new ExTrackComputeAction();
		action.setLogger( Logger.DEFAULT_LOGGER );
		final Model model = ExTrackUtil.toModel( tracks );
		final Settings settings = new Settings();
		final TrackMate trackmate = new TrackMate( model , settings  );
		action.execute( trackmate, null, null, null );
	}
}
