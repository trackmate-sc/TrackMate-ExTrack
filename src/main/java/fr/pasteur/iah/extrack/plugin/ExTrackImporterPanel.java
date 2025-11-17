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

import static fiji.plugin.trackmate.gui.Icons.TRACKMATE_ICON;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettingsIO;
import fiji.plugin.trackmate.gui.wizard.TrackMateWizardSequence;
import fiji.plugin.trackmate.gui.wizard.descriptors.ConfigureViewsDescriptor;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.plugin.trackmate.visualization.TrackMateModelView;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import fr.pasteur.iah.extrack.trackmate.ExTrackImporter;
import fr.pasteur.iah.extrack.trackmate.ExTrackImporterFromImp;
import fr.pasteur.iah.extrack.util.FileChooser;
import fr.pasteur.iah.extrack.util.FileChooser.DialogType;
import fr.pasteur.iah.extrack.util.FileChooser.SelectionMode;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class ExTrackImporterPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public static final ImageIcon ICON = new ImageIcon( ExTrackImporterPanel.class.getResource( "TrackMateExTrack-logo.png" ) );

	// Used to 'remember values between runs.'
	static String lastDataPath = System.getProperty( "user.home" );

	static String lastImagePath = System.getProperty( "user.home" );

	static double lastRadius = 0.3;

	static double lastPizelSize = 0.08;

	static double lastFrameInterval = 0.1;

	static String lastSpatialUnits = "µm";

	static String lastTimeUnits = "s";

	private final JTextField textFieldDataPath;

	private final JTextField textFieldImgPath;

	private static File path = new File( System.getProperty( "user.home" ) );

	/**
	 * Creates a panel from an opened image.
	 * 
	 * @param imp
	 * @param dataPath
	 * @param radius
	 */
	public ExTrackImporterPanel(
			final ImagePlus imp,
			final String dataPath,
			final double radius )
	{
		this(
				imp,
				null,
				dataPath,
				imp.getCalibration().pixelWidth,
				radius,
				imp.getCalibration().getUnit(),
				imp.getCalibration().frameInterval,
				imp.getCalibration().getTimeUnit() );
	}

	/**
	 * Creates an image from a path to an image file.
	 * 
	 * @param imagePath
	 * @param dataPath
	 * @param pixelSize
	 * @param radius
	 * @param spaceUnits
	 * @param frameInterval
	 * @param timeUnits
	 */
	public ExTrackImporterPanel(
			final String imagePath,
			final String dataPath,
			final double pixelSize,
			final double radius,
			final String spaceUnits,
			final double frameInterval,
			final String timeUnits )
	{
		this(
				null,
				imagePath,
				dataPath,
				pixelSize,
				radius,
				spaceUnits,
				frameInterval,
				timeUnits );
	}

	private ExTrackImporterPanel(
			final ImagePlus imp,
			final String imagePath,
			final String dataPath,
			final double pixelSize,
			final double radius,
			final String spaceUnits,
			final double frameInterval,
			final String timeUnits )
	{
		// Number format.
		final NumberFormat nf = NumberFormat.getNumberInstance( Locale.US );
		final DecimalFormat format = ( DecimalFormat ) nf;
		format.setMaximumFractionDigits( 3 );
		format.setGroupingUsed( false );
		format.setDecimalSeparatorAlwaysShown( true );

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblIcon = new JLabel( getIcon() );
		final GridBagConstraints gbc_lblIcon = new GridBagConstraints();
		gbc_lblIcon.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblIcon.insets = new Insets( 5, 5, 5, 0 );
		gbc_lblIcon.gridx = 0;
		gbc_lblIcon.gridy = 0;
		add( lblIcon, gbc_lblIcon );

		final JLabel lblDataFile = new JLabel( "Data file:" );
		final GridBagConstraints gbc_lblDataFile = new GridBagConstraints();
		gbc_lblDataFile.insets = new Insets( 5, 5, 0, 5 );
		gbc_lblDataFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDataFile.gridx = 0;
		gbc_lblDataFile.gridy = 1;
		add( lblDataFile, gbc_lblDataFile );

		textFieldDataPath = new JTextField( dataPath );
		final GridBagConstraints gbc_textFieldDataPath = new GridBagConstraints();
		gbc_textFieldDataPath.insets = new Insets( 0, 5, 0, 5 );
		gbc_textFieldDataPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldDataPath.gridx = 0;
		gbc_textFieldDataPath.gridy = 2;
		add( textFieldDataPath, gbc_textFieldDataPath );
		textFieldDataPath.setColumns( 10 );

		final JButton btnBrowseDataFile = new JButton( "Browse" );
		final GridBagConstraints gbc_btnBrowseDataFile = new GridBagConstraints();
		gbc_btnBrowseDataFile.insets = new Insets( 0, 5, 5, 5 );
		gbc_btnBrowseDataFile.anchor = GridBagConstraints.EAST;
		gbc_btnBrowseDataFile.gridx = 0;
		gbc_btnBrowseDataFile.gridy = 3;
		add( btnBrowseDataFile, gbc_btnBrowseDataFile );

		final JLabel lblImgFile = new JLabel( ( imp == null ) ? "Image file:" : "Image:" );
		final GridBagConstraints gbc_lblImgFile = new GridBagConstraints();
		gbc_lblImgFile.insets = new Insets( 0, 5, 5, 5 );
		gbc_lblImgFile.anchor = GridBagConstraints.WEST;
		gbc_lblImgFile.gridx = 0;
		gbc_lblImgFile.gridy = 4;
		add( lblImgFile, gbc_lblImgFile );

		textFieldImgPath = new JTextField( ( imp == null ) ? imagePath : imp.getTitle() );
		textFieldImgPath.setEnabled( ( imp == null ) );
		final GridBagConstraints gbc_textFieldImgPath = new GridBagConstraints();
		gbc_textFieldImgPath.insets = new Insets( 0, 5, 0, 5 );
		gbc_textFieldImgPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldImgPath.gridx = 0;
		gbc_textFieldImgPath.gridy = 5;
		add( textFieldImgPath, gbc_textFieldImgPath );
		textFieldImgPath.setColumns( 10 );

		final JButton btnBrowseImgFile = new JButton( "Browse" );
		btnBrowseImgFile.setVisible( ( imp == null ) );
		final GridBagConstraints gbc_btnBrowseImgFile = new GridBagConstraints();
		gbc_btnBrowseImgFile.insets = new Insets( 0, 5, 5, 5 );
		gbc_btnBrowseImgFile.anchor = GridBagConstraints.EAST;
		gbc_btnBrowseImgFile.gridx = 0;
		gbc_btnBrowseImgFile.gridy = 6;
		add( btnBrowseImgFile, gbc_btnBrowseImgFile );

		final JPanel panelUnits = new JPanel();
		final GridBagConstraints gbc_panelUnits = new GridBagConstraints();
		gbc_panelUnits.insets = new Insets( 5, 5, 5, 5 );
		gbc_panelUnits.anchor = GridBagConstraints.NORTH;
		gbc_panelUnits.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelUnits.gridx = 0;
		gbc_panelUnits.gridy = 7;
		add( panelUnits, gbc_panelUnits );
		panelUnits.setLayout( new BoxLayout( panelUnits, BoxLayout.X_AXIS ) );

		final JLabel lblUnits = new JLabel( "Spatial units:" );
		lblUnits.setVisible( imp == null );
		panelUnits.add( lblUnits );

		final Component hg1 = Box.createHorizontalGlue();
		panelUnits.add( hg1 );

		final JComboBox< String > comboBoxSpaceUnits = new JComboBox<>();
		comboBoxSpaceUnits.setMaximumSize( new Dimension( 150, 32767 ) );
		comboBoxSpaceUnits.setModel( new DefaultComboBoxModel<>( new String[] { "µm", "nm" } ) );
		comboBoxSpaceUnits.setVisible( imp == null );
		panelUnits.add( comboBoxSpaceUnits );

		final JPanel panelPixelSize = new JPanel();
		final GridBagConstraints gbc_panelPixelSize = new GridBagConstraints();
		gbc_panelPixelSize.insets = new Insets( 5, 5, 5, 5 );
		gbc_panelPixelSize.fill = GridBagConstraints.BOTH;
		gbc_panelPixelSize.gridx = 0;
		gbc_panelPixelSize.gridy = 8;
		add( panelPixelSize, gbc_panelPixelSize );
		panelPixelSize.setLayout( new BoxLayout( panelPixelSize, BoxLayout.X_AXIS ) );

		final JLabel lblPixelSize = new JLabel( "Pixel size:" );
		lblPixelSize.setPreferredSize( new Dimension( 150, 16 ) );
		panelPixelSize.add( lblPixelSize );

		final Component horizontalGlue = Box.createHorizontalGlue();
		panelPixelSize.add( horizontalGlue );

		final JFormattedTextField ftfPixelSize = new JFormattedTextField( format );
		ftfPixelSize.setValue( Double.valueOf( pixelSize ) );
		ftfPixelSize.setHorizontalAlignment( SwingConstants.CENTER );
		ftfPixelSize.setPreferredSize( new Dimension( 80, 26 ) );
		ftfPixelSize.setEnabled( imp == null );
		panelPixelSize.add( ftfPixelSize );

		final Component hs2 = Box.createHorizontalStrut( 5 );
		panelPixelSize.add( hs2 );

		final JLabel lblPixelSizeUnits = new JLabel( "  " );
		panelPixelSize.add( lblPixelSizeUnits );

		final JPanel panelTimeUnits = new JPanel();
		final GridBagConstraints gbc_panelTimeUnits = new GridBagConstraints();
		gbc_panelTimeUnits.insets = new Insets( 5, 5, 5, 5 );
		gbc_panelTimeUnits.fill = GridBagConstraints.BOTH;
		gbc_panelTimeUnits.gridx = 0;
		gbc_panelTimeUnits.gridy = 9;
		add( panelTimeUnits, gbc_panelTimeUnits );
		panelTimeUnits.setLayout( new BoxLayout( panelTimeUnits, BoxLayout.X_AXIS ) );

		final JLabel lblTimeUnits = new JLabel( "Time units:" );
		lblTimeUnits.setVisible( imp == null );
		panelTimeUnits.add( lblTimeUnits );

		final Component hg2 = Box.createHorizontalGlue();
		panelTimeUnits.add( hg2 );

		final JComboBox< String > comboBoxTimeUnits = new JComboBox<>();
		comboBoxTimeUnits.setMaximumSize( new Dimension( 150, 32767 ) );
		comboBoxTimeUnits.setModel( new DefaultComboBoxModel<>( new String[] { "s", "ms" } ) );
		comboBoxTimeUnits.setVisible( imp == null );
		panelTimeUnits.add( comboBoxTimeUnits );

		final JPanel panelFrameInterval = new JPanel();
		final GridBagConstraints gbc_panelFrameInterval = new GridBagConstraints();
		gbc_panelFrameInterval.insets = new Insets( 5, 5, 5, 5 );
		gbc_panelFrameInterval.fill = GridBagConstraints.BOTH;
		gbc_panelFrameInterval.gridx = 0;
		gbc_panelFrameInterval.gridy = 10;
		add( panelFrameInterval, gbc_panelFrameInterval );
		panelFrameInterval.setLayout( new BoxLayout( panelFrameInterval, BoxLayout.X_AXIS ) );

		final JLabel lblFrameInterval = new JLabel( "Frame interval:" );
		lblFrameInterval.setPreferredSize( new Dimension( 150, 16 ) );
		panelFrameInterval.add( lblFrameInterval );

		final JFormattedTextField ftfFrameInterval = new JFormattedTextField( format );
		ftfFrameInterval.setValue( Double.valueOf( frameInterval ) );
		ftfFrameInterval.setHorizontalAlignment( SwingConstants.CENTER );
		ftfFrameInterval.setEnabled( imp == null );
		panelFrameInterval.add( ftfFrameInterval );

		final Component hs4 = Box.createHorizontalStrut( 5 );
		panelFrameInterval.add( hs4 );

		final JLabel lblFrameIntervalUnits = new JLabel( "  " );
		panelFrameInterval.add( lblFrameIntervalUnits );

		final JPanel panelDetectionRadius = new JPanel();
		final GridBagConstraints gbc_panelDetectionRadius = new GridBagConstraints();
		gbc_panelDetectionRadius.insets = new Insets( 15, 5, 5, 5 );
		gbc_panelDetectionRadius.fill = GridBagConstraints.BOTH;
		gbc_panelDetectionRadius.gridx = 0;
		gbc_panelDetectionRadius.gridy = 11;
		add( panelDetectionRadius, gbc_panelDetectionRadius );
		panelDetectionRadius.setLayout( new BoxLayout( panelDetectionRadius, BoxLayout.X_AXIS ) );

		final JLabel lblDetectionRadius = new JLabel( "Detection radius:" );
		lblDetectionRadius.setPreferredSize( new Dimension( 150, 16 ) );
		panelDetectionRadius.add( lblDetectionRadius );

		final Component hg3 = Box.createHorizontalGlue();
		panelDetectionRadius.add( hg3 );

		final JFormattedTextField ftfDetectionRadius = new JFormattedTextField( format );
		ftfDetectionRadius.setValue( Double.valueOf( radius ) );
		ftfDetectionRadius.setHorizontalAlignment( SwingConstants.CENTER );
		ftfDetectionRadius.setPreferredSize( new Dimension( 80, 26 ) );
		panelDetectionRadius.add( ftfDetectionRadius );

		final Component hs3 = Box.createHorizontalStrut( 5 );
		panelDetectionRadius.add( hs3 );

		final JLabel lblDetectionRadiusUnits = new JLabel( "  " );
		panelDetectionRadius.add( lblDetectionRadiusUnits );

		final JButton btnImport = new JButton( "Import" );
		final GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnImport.gridx = 0;
		gbc_btnImport.gridy = 12;
		add( btnImport, gbc_btnImport );

		/*
		 * LISTENERS.
		 */

		btnBrowseDataFile.addActionListener( l -> browseDataFile() );
		btnBrowseImgFile.addActionListener( l -> browseImgFile() );
		comboBoxSpaceUnits.addActionListener( l -> {
			final String units = ( String ) comboBoxSpaceUnits.getSelectedItem();
			lblDetectionRadiusUnits.setText( units );
			lblPixelSizeUnits.setText( units );
		} );
		comboBoxSpaceUnits.setSelectedIndex( 0 );
		comboBoxSpaceUnits.setEditable( true );
		comboBoxSpaceUnits.setSelectedItem( spaceUnits );
		comboBoxSpaceUnits.setEditable( false );

		comboBoxTimeUnits.addActionListener( l -> lblFrameIntervalUnits.setText( ( String ) comboBoxTimeUnits.getSelectedItem() ) );
		comboBoxTimeUnits.setSelectedIndex( 0 );
		comboBoxTimeUnits.setEditable( true );
		comboBoxTimeUnits.setSelectedItem( timeUnits );
		comboBoxTimeUnits.setEditable( false );

		btnImport.addActionListener( l -> runImport(
				imp,
				textFieldImgPath.getText(),
				textFieldDataPath.getText(),
				( ( Number ) ftfPixelSize.getValue() ).doubleValue(),
				( ( Number ) ftfDetectionRadius.getValue() ).doubleValue(),
				( String ) comboBoxSpaceUnits.getSelectedItem(),
				( ( Number ) ftfFrameInterval.getValue() ).doubleValue(),
				( String ) comboBoxTimeUnits.getSelectedItem() ) );

	}

	private void runImport(
			final ImagePlus imp,
			final String imagePath,
			final String dataPath,
			final double pixelSize,
			final double radius,
			final String spaceUnits,
			final double frameInterval,
			final String timeUnits )
	{
		final EverythingDisablerAndReenabler disabler = new EverythingDisablerAndReenabler( getParent(), new Class[] { JLabel.class } );
		disabler.disable();

		lastImagePath = imagePath;
		lastDataPath = dataPath;
		lastPizelSize = pixelSize;
		lastRadius = radius;
		lastFrameInterval = frameInterval;
		lastSpatialUnits = spaceUnits;
		lastTimeUnits = timeUnits;

		new Thread( "TrackMate-ExTrack importer thread" )
		{
			@Override
			public void run()
			{
				try
				{
					final StringBuilder logText = new StringBuilder();
					logText.append( "ExTrack import started on " + TMUtils.getCurrentTimeString() + '\n' );
					if ( imp == null )
						logText.append( " - Image file: " + imagePath + '\n' );
					else
						logText.append( " - Image: " + imp.getTitle() + '\n' );
					logText.append( " - NumPy datafile: " + dataPath + '\n' );
					logText.append( " - Pixel size: " + pixelSize + " " + spaceUnits + '\n' );
					logText.append( " - Frame interval: " + frameInterval + " " + timeUnits + '\n' );
					logText.append( " - Detection radius: " + radius + " " + spaceUnits + '\n' );

					final ExTrackImporter importer;
					if ( imp == null )
						importer = new ExTrackImporter(
								imagePath,
								dataPath,
								radius,
								spaceUnits,
								frameInterval,
								timeUnits );
					else
						importer = new ExTrackImporterFromImp(
								imp,
								dataPath,
								radius );

					if ( !importer.checkInput() || !importer.process() )
					{
						IJ.error( importer.getErrorMessage() );
						return;
					}
					final TrackMate trackmate = importer.getResult();

					if ( null == imp )
					{
						// Fine-tune image.
						final Settings ls = trackmate.getSettings();
						final Settings settings = trackmate.getSettings().copyOn( ls.imp );
						final ImagePlus imp = settings.imp;
						imp.show();
						imp.getCalibration().setUnit( spaceUnits );
						imp.getCalibration().pixelWidth = pixelSize;
						imp.getCalibration().pixelHeight = pixelSize;
						imp.getCalibration().setTimeUnit( timeUnits );
						imp.getCalibration().frameInterval = frameInterval;
						/*
						 * If we do not have a time-lapse, assume we have and
						 * permute Z with T.
						 */
						if ( settings.imp.getNFrames() == 1 )
						{
							final int nSlices = settings.imp.getNSlices();
							final int nChannels = settings.imp.getNChannels();
							settings.imp.setDimensions( nChannels, 1, nSlices );
						}
						settings.imp.setOpenAsHyperStack( true );

						// Resave the image.
						final String tifImagePath = imagePath.substring( 0, imagePath.lastIndexOf( '.' ) ) + ".tif";
						final boolean resaveOk = IJ.saveAsTiff( imp, tifImagePath );
						final String saveMsg = ( resaveOk )
								? "Resaving image succesful."
								: "Problem resaving the image to TIF file. Saved TrackMate file might not reload properly.";
						logText.append( saveMsg );
					}

					// Main objects.
					final Settings settings = trackmate.getSettings();
					final Model model = trackmate.getModel();
					final SelectionModel selectionModel = new SelectionModel( model );
					final DisplaySettings displaySettings = DisplaySettingsIO.readUserDefault();

					// Main view.
					final TrackMateModelView displayer = new HyperStackDisplayer( model, selectionModel, imp, displaySettings );
					displayer.render();

					// Wizard.
					final TrackMateWizardSequence sequence = new TrackMateWizardSequence( trackmate, selectionModel, displaySettings );
					sequence.setCurrent( ConfigureViewsDescriptor.KEY );
					final JFrame frame = sequence.run( "TrackMate on " + settings.imp.getShortTitle() );
					frame.setIconImage( TRACKMATE_ICON.getImage() );
					GuiUtils.positionWindow( frame, settings.imp.getWindow() );
					frame.setVisible( true );

					// Log all of this.
					model.getLogger().log( logText.toString() );
				}
				finally
				{
					disabler.reenable();
				}
			};
		}.start();
	}

	private void browseImgFile()
	{
		final File selectedFile = FileChooser.chooseFile(
				this,
				path.getAbsolutePath(),
				null,
				"Select image file",
				DialogType.LOAD,
				SelectionMode.FILES_ONLY );
		if ( selectedFile == null )
			return;

		path = selectedFile;
		textFieldImgPath.setText( selectedFile.getAbsolutePath() );
	}

	private void browseDataFile()
	{
		final File selectedFile = FileChooser.chooseFile(
				this,
				path.getAbsolutePath(),
				new FileNameExtensionFilter( "NumPy files", "npy" ),
				"Select NumPy data file",
				DialogType.LOAD,
				SelectionMode.FILES_ONLY );
		if ( selectedFile == null )
			return;

		path = selectedFile;
		textFieldDataPath.setText( selectedFile.getAbsolutePath() );
	}

	private static ImageIcon getIcon()
	{
		final int w = 200;
		final int h = 200;
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

	public static void main( final String[] args )
	{
		ImageJ.main( args );
		final JFrame frame = new JFrame( "ExTrack importer" );
		frame.setIconImage( ICON.getImage() );

//		frame.getContentPane().add( new ExTrackImporterPanel(
//				"samples/realdata/GFP-100mW-60msExp-FTo-bleaching-10s-tirf_6_MMStack_Pos0.ome.tif",
//				"samples/realdata/tracks.npy",
//				0.08, 0.12, "µm", 0.1, "s" ) );

//		final ImagePlus imp = IJ.openImage( "samples/img.tif" );
//		imp.show();
//		frame.getContentPane().add( new ExTrackImporterPanel(
//				imp,
//				"samples/tracks.npy",
//				0.12 ) );

		final ImagePlus imp = IJ.openImage( "samples/realdata/GFP-100mW-60msExp-FTo-bleaching-10s-tirf_6_MMStack_Pos0.tif" );
		imp.show();
		frame.getContentPane().add( new ExTrackImporterPanel(
				imp,
				"samples/realdata/tracks.npy",
				0.12 ) );

		frame.pack();
		frame.setVisible( true );
	}
}
