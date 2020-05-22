package fr.pasteur.iah.extrack.gui;

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
import java.util.Map;

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
import fiji.plugin.trackmate.gui.TrackMateGUIController;
import fiji.plugin.trackmate.gui.descriptors.ConfigureViewsDescriptor;
import fiji.plugin.trackmate.util.DefaultFileFilter.ImageFileFilter;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.plugin.trackmate.visualization.SpotColorGenerator;
import fiji.plugin.trackmate.visualization.TrackColorGenerator;
import fiji.plugin.trackmate.visualization.TrackMateModelView;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import fr.pasteur.iah.extrack.trackmate.ExTrackEdgeFeatures;
import fr.pasteur.iah.extrack.trackmate.ExTrackImporter;
import fr.pasteur.iah.extrack.trackmate.ExTrackProbabilitiesFeature;
import fr.pasteur.iah.extrack.util.FileChooser;
import fr.pasteur.iah.extrack.util.FileChooser.DialogType;
import fr.pasteur.iah.extrack.util.FileChooser.SelectionMode;
import ij.IJ;
import ij.ImagePlus;

public class ExTrackImporterPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public static final ImageIcon ICON = new ImageIcon( ExTrackImporterPanel.class.getResource( "TrackMateExTrack-logo.png" ) );

	private JTextField textFieldDataPath;

	private JTextField textFieldImgPath;

	private static File path = new File( System.getProperty( "user.home" ) );

	public ExTrackImporterPanel()
	{
		this( path.getAbsolutePath(), path.getAbsolutePath(), 0.08, 0.12, "µm" );
	}

	public ExTrackImporterPanel(
			final String imagePath,
			final String dataPath,
			final double pixelSize,
			final double radius,
			final String spaceUnits )
	{
		// Number format.
		final NumberFormat nf = NumberFormat.getNumberInstance( Locale.US );
		final DecimalFormat format = ( DecimalFormat ) nf;
		format.setMaximumFractionDigits( 3 );
		format.setGroupingUsed( false );
		format.setDecimalSeparatorAlwaysShown( true );

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblIcon = new JLabel( getIcon() );
		final GridBagConstraints gbc_lblIcon = new GridBagConstraints();
		gbc_lblIcon.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblIcon.insets = new Insets( 5, 5, 5, 5 );
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

		final JLabel lblImgFile = new JLabel( "Image file:" );
		final GridBagConstraints gbc_lblImgFile = new GridBagConstraints();
		gbc_lblImgFile.insets = new Insets( 5, 5, 0, 5 );
		gbc_lblImgFile.anchor = GridBagConstraints.WEST;
		gbc_lblImgFile.gridx = 0;
		gbc_lblImgFile.gridy = 4;
		add( lblImgFile, gbc_lblImgFile );

		textFieldImgPath = new JTextField( imagePath );
		final GridBagConstraints gbc_textFieldImgPath = new GridBagConstraints();
		gbc_textFieldImgPath.insets = new Insets( 0, 5, 0, 5 );
		gbc_textFieldImgPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldImgPath.gridx = 0;
		gbc_textFieldImgPath.gridy = 5;
		add( textFieldImgPath, gbc_textFieldImgPath );
		textFieldImgPath.setColumns( 10 );

		final JButton btnBrowseImgFile = new JButton( "Browse" );
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
		panelUnits.add( lblUnits );

		final Component hg1 = Box.createHorizontalGlue();
		panelUnits.add( hg1 );

		final JComboBox< String > comboBoxUnits = new JComboBox<>();
		comboBoxUnits.setMaximumSize( new Dimension( 150, 32767 ) );
		comboBoxUnits.setModel( new DefaultComboBoxModel<>( new String[] { "µm", "nm" } ) );
		comboBoxUnits.setSelectedItem( spaceUnits );
		panelUnits.add( comboBoxUnits );

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

		final JFormattedTextField ftfPixelSize = new JFormattedTextField( Double.valueOf( pixelSize ) );
		ftfPixelSize.setHorizontalAlignment( SwingConstants.CENTER );
		ftfPixelSize.setPreferredSize( new Dimension( 80, 26 ) );
		panelPixelSize.add( ftfPixelSize );

		final Component hs2 = Box.createHorizontalStrut( 5 );
		panelPixelSize.add( hs2 );

		final JLabel lblPixelSizeUnits = new JLabel( "  " );
		panelPixelSize.add( lblPixelSizeUnits );

		final JPanel panelDetectionRadius = new JPanel();
		final GridBagConstraints gbc_panelDetectionRadius = new GridBagConstraints();
		gbc_panelDetectionRadius.insets = new Insets( 5, 5, 5, 5 );
		gbc_panelDetectionRadius.fill = GridBagConstraints.BOTH;
		gbc_panelDetectionRadius.gridx = 0;
		gbc_panelDetectionRadius.gridy = 9;
		add( panelDetectionRadius, gbc_panelDetectionRadius );
		panelDetectionRadius.setLayout( new BoxLayout( panelDetectionRadius, BoxLayout.X_AXIS ) );

		final JLabel lblDetectionRadius = new JLabel( "Detection radius:" );
		lblDetectionRadius.setPreferredSize( new Dimension( 150, 16 ) );
		panelDetectionRadius.add( lblDetectionRadius );

		final Component hg3 = Box.createHorizontalGlue();
		panelDetectionRadius.add( hg3 );

		final JFormattedTextField ftfDetectionRadius = new JFormattedTextField( Double.valueOf( radius ) );
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
		gbc_btnImport.gridy = 10;
		add( btnImport, gbc_btnImport );

		/*
		 * LISTENERS.
		 */

		btnBrowseDataFile.addActionListener( l -> browseDataFile() );
		btnBrowseImgFile.addActionListener( l -> browseImgFile() );
		comboBoxUnits.addActionListener( l -> {
			final String units = ( String ) comboBoxUnits.getSelectedItem();
			lblDetectionRadiusUnits.setText( units );
			lblPixelSizeUnits.setText( units );
		} );
		comboBoxUnits.setSelectedIndex( 0 );

		btnImport.addActionListener( l -> runImport(
				textFieldImgPath.getText(),
				textFieldDataPath.getText(),
				( ( Number ) ftfPixelSize.getValue() ).doubleValue(),
				( ( Number ) ftfDetectionRadius.getValue() ).doubleValue(),
				( String ) comboBoxUnits.getSelectedItem() ) );

	}

	private void runImport(
			final String imagePath,
			final String dataPath,
			final double pixelSize,
			final double radius,
			final String spaceUnits )
	{
		final EverythingDisablerAndReenabler disabler = new EverythingDisablerAndReenabler( getParent(), new Class[] { JLabel.class } );
		disabler.disable();

		new Thread( "TrackMate-ExTrack importer thread" )
		{
			@Override
			public void run()
			{
				try
				{
					final StringBuilder logText = new StringBuilder();
					logText.append( "ExTrack import started on " + TMUtils.getCurrentTimeString() + '\n' );
					logText.append( " - Image file: " + imagePath + '\n' );
					logText.append( " - NumPy datafile: " + dataPath + '\n' );
					logText.append( " - Pixel size: " + pixelSize + " " + spaceUnits + '\n' );
					logText.append( " - Detection radius: " + radius + " " + spaceUnits + '\n' );

					final ExTrackImporter importer = new ExTrackImporter( imagePath, dataPath, radius );
					if ( !importer.checkInput() || !importer.process() )
					{
						System.err.println( importer.getErrorMessage() );
						return;
					}
					final TrackMate trackmate = importer.getResult();

					// Fine-tune image.
					final Settings settings = trackmate.getSettings();
					final ImagePlus imp = settings.imp;
					imp.getCalibration().setUnit( spaceUnits );
					imp.getCalibration().pixelWidth = pixelSize;
					imp.getCalibration().pixelHeight = pixelSize;
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
							? "Resaving image succesful. Saved TrackMate file might not reload properly.\n"
							: "Problem resaving the image to TIF file: " + tifImagePath + '\n';
					logText.append( saveMsg );

					// Update settings accordingly.
					settings.setFrom( imp );

					// Launch TrackMate controller.
					final TrackMateGUIController controller = new TrackMateGUIController( trackmate );
					GuiUtils.positionWindow( controller.getGUI(), imp.getWindow() );

					// Fine tune the view.
					final Model model = trackmate.getModel();
					final TrackMateModelView view = new HyperStackDisplayer( model, new SelectionModel( model ), imp );
					controller.getGuimodel().addView( view );

					final Map< String, Object > displaySettings = controller.getGuimodel().getDisplaySettings();
					final SpotColorGenerator spotColorGenerator = ( SpotColorGenerator ) displaySettings.get( TrackMateModelView.KEY_SPOT_COLORING );
					spotColorGenerator.setFeature( ExTrackProbabilitiesFeature.P_STUCK );
					final TrackColorGenerator trackColorGenerator = ( TrackColorGenerator ) displaySettings.get( TrackMateModelView.KEY_TRACK_COLORING );
					trackColorGenerator.setFeature( ExTrackEdgeFeatures.P_STUCK );
					displaySettings.put( TrackMateModelView.KEY_SPOT_COLORING, spotColorGenerator );
					displaySettings.put( TrackMateModelView.KEY_TRACK_COLORING, trackColorGenerator );
					displaySettings.put( TrackMateModelView.KEY_TRACK_DISPLAY_MODE, TrackMateModelView.TRACK_DISPLAY_MODE_LOCAL );
					displaySettings.put( TrackMateModelView.KEY_TRACK_DISPLAY_DEPTH, 20 );

					for ( final String key : displaySettings.keySet() )
						view.setDisplaySettings( key, displaySettings.get( key ) );

					controller.setGUIStateString( ConfigureViewsDescriptor.KEY );

					// Log all of this.
					controller.getGUI().getLogPanel().setTextContent( logText.toString() );

					// Finish.
					view.render();

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
				new ImageFileFilter( "Image files" ),
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
		final JFrame frame = new JFrame( "ExTrack importer" );
		frame.setIconImage( ICON.getImage() );
		frame.getContentPane().add( new ExTrackImporterPanel( "samples/img.tif", "samples/tracks.npy", 0.08, 0.12, "µm" ) );
		frame.pack();
		frame.setVisible( true );
	}
}