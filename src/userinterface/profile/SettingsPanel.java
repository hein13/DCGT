package userinterface.profile;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import userinterface.OptionsPanel;



public class SettingsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	OptionsPanel parent;
	JDialog frame;
	
	public final static int WINDOWHEIGHT = 455;
	public final static int WINDOWWIDTH = 310;
	
	public final static int STARTINGWIDTH = 10;
	public final static int STARTINGHEIGHT = 10;
	public final static int HORIZONTALSPACING = 10;
	public final static int VERTICALSPACING = 50;
	public final static int ITEMHEIGHT = 40;
	public final static int MAINLABELWIDTH = 85;
	

	public List<OptionTextField> options;
	
	
	public SettingsPanel(OptionsPanel parent) {
		this.parent = parent;
		frame = new JDialog();
		frame.setResizable(false);
		frame.getContentPane().setSize(WINDOWWIDTH, WINDOWHEIGHT);
		frame.setSize(WINDOWWIDTH, WINDOWHEIGHT);
		this.setSize(WINDOWWIDTH, WINDOWHEIGHT);
		frame.add(this);
		frame.setUndecorated(true);	
        frame.setResizable(false);   
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); 
        frame.setModal(true);        
        initComponents();
        setCurrentValues();
        frame.setVisible(true);     
	}
	
	void initComponents() {
		
		setLayout(null);
		options = new ArrayList<OptionTextField>();
		
		JButton closeButton = new JButton("Cancel");
		closeButton.setBounds(10, this.getHeight() - 50, 70, 40);
		closeButton.addMouseListener(new closeButtonMouseAdapter());
		this.add(closeButton);
		
		JButton acceptButton = new JButton("Accept");
		acceptButton.setBounds(this.getWidth() - 80, this.getHeight() - 50, 70, 40);
		acceptButton.addMouseListener(new acceptButtonMouseAdapter());
		this.add(acceptButton);
		
		
		//Tabbed Area
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, this.getWidth(), this.getHeight() - 60);
		
		
		//General Options Tab
		JPanel generalTab = new JPanel();
		generalTab.setLayout(null);
		generalTab.setSize(tabbedPane.getWidth(), tabbedPane.getHeight());

		generalTab.add(new DividingLine());

		String generalMainLabels[] = {
				"Simulation Frequency", "Intersection Wait Time",
				"Highway Cutoff Speed"
				};
		String generalUnitLabels[] = {
				"s per Cycle", "s", "km/h"
		};
		
		for(int i = 0; i < generalMainLabels.length; i++){
			generalTab.add(new MainLabel(generalMainLabels[i], i, this)); 
			generalTab.add(new OptionTextField(i, 60, this));
			generalTab.add(new UnitLabel(generalUnitLabels[i], this, i));
		}

		tabbedPane.addTab("General", generalTab);
		
		
		//Vehicle Options Tab
		JPanel vehicleTab = new JPanel();
		vehicleTab.setLayout(null);
		vehicleTab.setSize(tabbedPane.getWidth(), tabbedPane.getHeight());
		
		vehicleTab.add(new DividingLine());

		String vehicleMainLabels[] = {
				"Maximum Acceleration", "Mass of Vehicle",
				"Rolling Resistance", "Front Facing Area",
				"Wind Friction", "Transmission Friction",
				"Maximum Velocity"
				};
		String vehicleUnitLabels[] = {
				"m/s^2", "kg", "" , "m^2", "", "W", "km/h"
		};
		
		for(int i = 0; i < vehicleMainLabels.length; i++){
			vehicleTab.add(new MainLabel(vehicleMainLabels[i], i, this)); 
			vehicleTab.add(new OptionTextField(i, 80, this));
			vehicleTab.add(new UnitLabel(vehicleUnitLabels[i], this, i));
		}
		
		tabbedPane.addTab("Vehicle", vehicleTab);
		
		
		//Driver Options Tab
		JPanel driverTab = new JPanel();
		driverTab.setSize(tabbedPane.getWidth(), tabbedPane.getHeight());
		driverTab.setLayout(null);
		
		driverTab.add(new DividingLine());

		String driverMainLabels[] = {
				"Speed Limit Multiplier", "Velocity Deviation",
				"Average Max Acceleration"
				};
		String driverUnitLabels[] = {
				"", "m/s", "m/s^2"
		};
		
		for(int i = 0; i < driverMainLabels.length; i++){
			driverTab.add(new MainLabel(driverMainLabels[i], i, this)); 
			driverTab.add(new OptionTextField(i, 80, this));
			driverTab.add(new UnitLabel(driverUnitLabels[i], this, i));
		}
		
		tabbedPane.addTab("Driver", driverTab);
		
		this.add(tabbedPane);
		
	}
	
	
	void setCurrentValues() {
		for(int i = 0; i < parent.optionValues.length; i++){
			options.get(i).setText(String.valueOf(parent.optionValues[i]));
		}
	}
	
	
	class acceptButtonMouseAdapter extends MouseAdapter{
		@Override
		public void mouseClicked(MouseEvent e) {
			parent.optionValues = new double[options.size()];
			
			for(int i = 0; i < parent.optionValues.length; i++){
				parent.optionValues[i] = Double.parseDouble(options.get(i).getText());
			}
			frame.dispose();
		}
	}
	
	
	class closeButtonMouseAdapter extends MouseAdapter{		
		@Override
		public void mouseClicked(MouseEvent e) {
			frame.dispose();
		}
	}
}
