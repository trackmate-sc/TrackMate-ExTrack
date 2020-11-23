/*-
 * #%L
 * TrackMate interface for the ExTrack track analysis software.
 * %%
 * Copyright (C) 2020 Institut Pasteur.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import Jama.Matrix;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.TrackMateGUIController;
import fiji.plugin.trackmate.gui.descriptors.ConfigureViewsDescriptor;
import fiji.plugin.trackmate.providers.EdgeAnalyzerProvider;
import fiji.plugin.trackmate.providers.SpotAnalyzerProvider;
import fiji.plugin.trackmate.providers.TrackAnalyzerProvider;
import fiji.plugin.trackmate.visualization.ViewUtils;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import fr.pasteur.iah.extrack.numpy.NumPyReader;
import fr.pasteur.iah.extrack.util.ExTrackUtil;
import ij.ImageJ;

public class ExTrackImporterTestDrive
{
	public static void main( final String[] args ) throws FileNotFoundException, IOException
	{
		ImageJ.main( args );

		final String trackFile = "samples/tracks.npy";
		final Map< Integer, Matrix > tracks = NumPyReader.readTracks( trackFile );

		/*
		 * Create the model.
		 */

		final double frameInterval = 0.02; // s.

		final Model model = ExTrackUtil.toModel( tracks );

		/*
		 * Create the settings object.
		 */

		final Settings settings = new Settings();
		settings.dt = frameInterval;
		settings.imp = ViewUtils.makeEmpytImagePlus( model );
		settings.setFrom( settings.imp );
		settings.imp.show();

		/*
		 * TrackMate object.
		 */

		addAllAnalyzers( settings );
		final TrackMate trackmate = new TrackMate( model, settings );
//		trackmate.computeSpotFeatures( false );
		trackmate.computeEdgeFeatures( false );
		trackmate.computeTrackFeatures( false );

		/*
		 * Launch controller.
		 */

		final TrackMateGUIController controller = new TrackMateGUIController( trackmate );
		controller.setGUIStateString( ConfigureViewsDescriptor.KEY );
		GuiUtils.positionWindow( controller.getGUI(), settings.imp.getWindow() );

		final HyperStackDisplayer view = new HyperStackDisplayer( model, controller.getSelectionModel(), settings.imp );
		final Map< String, Object > displaySettings = controller.getGuimodel().getDisplaySettings();
		controller.getGuimodel().addView( view );
		for ( final String key : displaySettings.keySet() )
			view.setDisplaySettings( key, displaySettings.get( key ) );

		view.render();
	}

	private static final void addAllAnalyzers( final Settings settings )
	{
		settings.clearSpotAnalyzerFactories();
		final SpotAnalyzerProvider spotAnalyzerProvider = new SpotAnalyzerProvider();
		final List< String > spotAnalyzerKeys = spotAnalyzerProvider.getKeys();
		for ( final String key : spotAnalyzerKeys )
			settings.addSpotAnalyzerFactory( spotAnalyzerProvider.getFactory( key ) );

		settings.clearEdgeAnalyzers();
		final EdgeAnalyzerProvider edgeAnalyzerProvider = new EdgeAnalyzerProvider();
		final List< String > edgeAnalyzerKeys = edgeAnalyzerProvider.getKeys();
		for ( final String key : edgeAnalyzerKeys )
			settings.addEdgeAnalyzer( edgeAnalyzerProvider.getFactory( key ) );

		settings.clearTrackAnalyzers();
		final TrackAnalyzerProvider trackAnalyzerProvider = new TrackAnalyzerProvider();
		final List< String > trackAnalyzerKeys = trackAnalyzerProvider.getKeys();
		for ( final String key : trackAnalyzerKeys )
			settings.addTrackAnalyzer( trackAnalyzerProvider.getFactory( key ) );
	}

}
