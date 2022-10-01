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

import java.awt.Frame;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.AbstractTMAction;
import fiji.plugin.trackmate.action.TrackMateAction;
import fiji.plugin.trackmate.action.TrackMateActionFactory;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fr.pasteur.iah.extrack.plugin.ExTrackActionController;
import fr.pasteur.iah.extrack.plugin.ExTrackImporterPanel;
import fr.pasteur.iah.extrack.util.ExTrackUtil;

public class ExTrackComputeAction extends AbstractTMAction
{

	public static final String INFO_TEXT = "<html>"
			+ "<b>ExTrack</b>"
			+ "<p>"
			+ "ExTrack allows for the estimation of diffusion coefficient and transition rates as "
			+ "well as consecutive states of tracks, even when diffusion per step is less than "
			+ "an order of magnitude higher than localization error."
			+ "<p>"
			+ "ExTrack determines the "
			+ "diffusion coefficients, localization error and transition rates of particles which "
			+ "transition between two diffusion states (<i>e.g.</i> immobile <i>vs</i> diffusive). "
			+ "Once these parameters are determined, ExTrack computes the probability of being in "
			+ "a given state at every time point of each track. These states probabilities can "
			+ "then be visualized using spot and edge features."
			+ "</html>";

	public static final String KEY = "COMPUTE_EXTRACK_PROBABILITIES";

	public static final ImageIcon ICON = ExTrackUtil.scaleImage( ExTrackImporterPanel.ICON, 16, 16 );

	public static final String NAME = "Compute ExTrack probabilities";

	@Override
	public void execute( final TrackMate trackmate, final SelectionModel selectionModel, final DisplaySettings displaySettings, final Frame parent )
	{
		final ExTrackActionController controller = new ExTrackActionController( trackmate, logger );
		controller.show();
	}

	@Plugin( type = TrackMateActionFactory.class )
	public static class Factory implements TrackMateActionFactory
	{

		@Override
		public String getInfoText()
		{
			return INFO_TEXT;
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public TrackMateAction create()
		{
			return new ExTrackComputeAction();
		}

		@Override
		public ImageIcon getIcon()
		{
			return ICON;
		}

		@Override
		public String getName()
		{
			return NAME;
		}
	}
}
