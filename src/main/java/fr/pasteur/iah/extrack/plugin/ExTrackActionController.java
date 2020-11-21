package fr.pasteur.iah.extrack.plugin;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import Jama.Matrix;
import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.TrackMate;
import fr.pasteur.iah.extrack.compute.ExTrackParameterOptimizer;
import fr.pasteur.iah.extrack.compute.ExTrackParameters;
import fr.pasteur.iah.extrack.util.ExTrackUtil;
import fr.pasteur.iah.extrack.util.FileChooser;
import fr.pasteur.iah.extrack.util.FileChooser.DialogType;

public class ExTrackActionController
{

	private static final String FILE_EXTENSION = "json";

	private static final FileFilter FILE_FILTER = new FileNameExtensionFilter( "JSon files", FILE_EXTENSION );

	private final ExTrackActionPanel gui;

	private static String selectedFile;

	private final TrackMate trackmate;

	private final Logger logger;

	private ExTrackParameterOptimizer cancelable;

	public ExTrackActionController( final TrackMate trackmate, final Logger logger )
	{
		this.trackmate = trackmate;
		this.logger = logger;
		this.gui = new ExTrackActionPanel();
		gui.btnEstimCancel.setEnabled( false );
		gui.btnEstimStart.setEnabled( true );

		gui.btnSave.addActionListener( e -> save() );
		gui.btnLoad.addActionListener( e -> load() );
		gui.btnEstimStart.addActionListener( e -> startEstimation() );
		gui.btnEstimCancel.addActionListener( e -> cancelEstimation() );
	}

	private void cancelEstimation()
	{
		if ( cancelable != null )
		{
			gui.btnEstimCancel.setEnabled( false );
			gui.log( "Canceling..." );
			logger.log( "User canceled." );
			cancelable.cancel( "User canceled" );
		}
	}

	private void startEstimation()
	{
		final EverythingDisablerAndReenabler reenabler = new EverythingDisablerAndReenabler(
				SwingUtilities.getWindowAncestor( gui ),
				new Class[] { JLabel.class } );
		reenabler.disable();

		gui.btnEstimCancel.setEnabled( true );
		gui.btnEstimStart.setEnabled( false );
		gui.log( "Estimation started." );
		new Thread( () -> {
			try
			{
				final Consumer< double[] > valueWatcher = array -> {
					SwingUtilities.invokeLater( () -> gui.setEstimationParameters( ExTrackParameters.fromArray( array ) ) );
				};
				final Map< Integer, Matrix > tracks = ExTrackUtil.toMatrix( trackmate.getModel() );
				final ExTrackParameters startPoint = gui.getManualParameters();
				final ExTrackParameterOptimizer optimizer = new ExTrackParameterOptimizer( startPoint, tracks, logger, valueWatcher );
				this.cancelable = optimizer;
				optimizer.run();
			}
			finally
			{
				reenabler.reenable();
				gui.btnEstimCancel.setEnabled( false );
				gui.btnEstimStart.setEnabled( true );
				if ( cancelable.isCanceled() )
					gui.log( "User canceled." );
				else
					gui.log( "Estimation completed." );
			}
		} ).start();
	}

	private void load()
	{
		final EverythingDisablerAndReenabler reenabler = new EverythingDisablerAndReenabler(
				SwingUtilities.getWindowAncestor( gui ),
				new Class[] { JLabel.class } );
		reenabler.disable();
		try
		{
			final String dialogTitle = "Load ExTrack parameters from a JSON file";
			final DialogType dialogType = DialogType.LOAD;
			final File chosenFile = FileChooser.chooseFile( gui, selectedFile, FILE_FILTER, dialogTitle, dialogType );
			if ( chosenFile == null )
			{
				gui.log( "Loading aborted." );
				return;
			}

			final Gson gson = new Gson();
			try
			{
				final String content = new String( Files.readAllBytes( Paths.get( chosenFile.getAbsolutePath() ) ) );
				try
				{
					final ExTrackParameters params = gson.fromJson( content, ExTrackParameters.class );
					gui.setManualParameters( params );
					gui.log( "Loaded from " + selectedFile );
					selectedFile = chosenFile.getAbsolutePath();
					gui.log( "Loaded parameters from " + selectedFile );
				}
				catch ( final JsonSyntaxException jse )
				{
					gui.error( "File " + chosenFile + " is not an ExTrack parameter file." );
					jse.printStackTrace();
				}
			}
			catch ( final IOException e )
			{
				gui.error( "Problem reading from file " + chosenFile );
				e.printStackTrace();
			}
		}
		finally
		{
			reenabler.reenable();
		}
	}

	private void save()
	{
		final EverythingDisablerAndReenabler reenabler = new EverythingDisablerAndReenabler(
				SwingUtilities.getWindowAncestor( gui ),
				new Class[] { JLabel.class } );
		reenabler.disable();
		try
		{
			final String dialogTitle = "Save ExTrack parameters to a JSON file";
			final DialogType dialogType = DialogType.SAVE;
			File chosenFile = FileChooser.chooseFile( gui, selectedFile, FILE_FILTER, dialogTitle, dialogType );
			if ( chosenFile == null )
			{
				gui.log( "Saving aborted." );
				return;
			}
			if ( !chosenFile.getAbsolutePath().endsWith( '.' + FILE_EXTENSION ) )
				chosenFile = new File( chosenFile.getAbsolutePath() + '.' + FILE_EXTENSION );

			final ExTrackParameters params = gui.getManualParameters();
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (FileWriter file = new FileWriter( chosenFile ))
			{
				final String serialized = gson.toJson( params );
				file.write( serialized );
				file.flush();
				selectedFile = chosenFile.getAbsolutePath();
				gui.log( "Saved parameters to " + selectedFile );
			}
			catch ( final IOException e )
			{
				gui.error( "Problem writing to file " + chosenFile );
				e.printStackTrace();
			}
		}
		finally
		{
			reenabler.reenable();
		}
	}

	public void show()
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{

				final JFrame frame = new JFrame();
				frame.addWindowListener( new WindowAdapter()
				{
					@Override
					public void windowClosing( final WindowEvent e )
					{
						cancelEstimation();
					}
				} );
				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
				frame.getContentPane().add( gui );
				frame.pack();
				frame.setIconImage( ExTrackGuiUtil.ICON.getImage() );
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		} );
	}
}
