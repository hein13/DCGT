package userinterface;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import userinterface.profile.SettingsPanel;
import creator.CycleProducer;
import creator.Router;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OptionsPanel extends JPanel {
	
	//See Documentation For Information On These Values
	public double optionValues[] = {
			5, 20, 65, 3.0, 1800.0, 0.026, 3.0, 0.27, 3750.0, 150, 1.01, .974, 3.128
	};
	
	private static final long serialVersionUID = 1L;
	
	public List<CoordinateField> coordinates; 
	JButton btnAddWaypoint;
	JButton btnRemWaypoint;
	JButton btnRun;
	JButton btnProfiles;
	
	public MainFrame mainFrame;

	/**
	 * Create the panel.
	 */
	public OptionsPanel(MainFrame mainFrame) {
		
		this.mainFrame = mainFrame;
        initComponents();
        this.setBounds(0, 0, 526, 240);	
 
        
	}
	
	public void setValue(List<Coordinate> existingCoordList){
		coordinates.get(0).textFieldLatitude.setText(String.valueOf(existingCoordList.get(0).getLat()));
		coordinates.get(0).textFieldLongitude.setText(String.valueOf(existingCoordList.get(0).getLon()));
		for(int i = 1; i < existingCoordList.size() - 2; i++){
			this.addWaypointField();
			coordinates.get(i).textFieldLatitude.setText(String.valueOf(existingCoordList.get(0).getLat()));
			coordinates.get(i).textFieldLongitude.setText(String.valueOf(existingCoordList.get(0).getLon()));
		}
		coordinates.get(coordinates.size() - 1).textFieldLatitude.setText(
				String.valueOf(existingCoordList.get(existingCoordList.size() - 1).getLat()));
		coordinates.get(coordinates.size() - 1).textFieldLongitude.setText(
				String.valueOf(existingCoordList.get(existingCoordList.size() - 1).getLon()));
        
    }
	
	void initComponents()
	{
		setLayout(null);
	
		JLabel lblLatitude = new JLabel("Latitude");
		lblLatitude.setHorizontalAlignment(SwingConstants.CENTER);
		lblLatitude.setBounds(178, 20, 140, 16);
		add(lblLatitude);
		
		JLabel lblLongitude = new JLabel("Longitude");
		lblLongitude.setHorizontalAlignment(SwingConstants.CENTER);
		lblLongitude.setBounds(330, 20, 140, 16);
		add(lblLongitude);
		
		coordinates = new ArrayList<CoordinateField>();
		
		coordinates.add(new CoordinateField(CoordinateField.STARTCOORDINATE, this, null));
		coordinates.add(new CoordinateField(CoordinateField.ENDCOORDINATE, this, coordinates.get(0)));
		
		btnRun = new JButton("Run");
		btnRun.addMouseListener(new RunButtonMouseAdapter(mainFrame, this));
		btnRun.setBounds(433, 160, 87, 50);
		add(btnRun);
		
		btnProfiles = new JButton("Edit Profiles");
		btnProfiles.addMouseListener(new ProfilesButtonMouseAdapter(this));
		btnProfiles.setBounds(10, 160, 87, 50);
		add(btnProfiles);
		
		btnAddWaypoint = new JButton("Add Waypoint");
        btnAddWaypoint.setBounds(40, 120, 117, 29);
        btnAddWaypoint.addMouseListener(new AddWayPointMouseAdapter(this));
        add(btnAddWaypoint);
        
        btnRemWaypoint = new JButton("Remove Last Waypoint");
        btnRemWaypoint.setBounds(btnAddWaypoint.getLocation().x + btnAddWaypoint.getWidth() + 20,
        		btnAddWaypoint.getLocation().y, 200, btnAddWaypoint.getHeight());
        btnRemWaypoint.setVisible(false);
        btnRemWaypoint.addMouseListener(new RemoveWayPointMouseAdapter(this));
        add(btnRemWaypoint);
	}
	
	public void addWaypointField() {
		coordinates.add(coordinates.size() - 1, new CoordinateField(CoordinateField.WAYPOINT, 
				this, coordinates.get(coordinates.size() - 2)));
		coordinates.get(coordinates.size() - 1).yLocation += 40;
		coordinates.get(coordinates.size() - 1).updateLocation();
		btnAddWaypoint.setBounds(btnAddWaypoint.getLocation().x, btnAddWaypoint.getLocation().y + 40,
				btnAddWaypoint.getWidth(), btnAddWaypoint.getHeight());
		int titleBarHeight = mainFrame.getHeight() - this.getHeight();
		
		this.setSize(this.getWidth(), this.getHeight() + 40);

		mainFrame.getContentPane().setSize(this.getSize());
		mainFrame.setSize(this.getWidth(), 
				this.getHeight() + titleBarHeight);;

		btnRun.setLocation(btnRun.getLocation().x, btnRun.getLocation().y + 40);		
		btnProfiles.setLocation(btnProfiles.getLocation().x, btnProfiles.getLocation().y + 40);
		
		btnRemWaypoint.setVisible(true);
		btnRemWaypoint.setLocation(btnRemWaypoint.getLocation().x, btnRemWaypoint.getLocation().y + 40);
		if(coordinates.size() > 15){
			btnAddWaypoint.setVisible(false);
		}
	}
	
	class AddWayPointMouseAdapter extends MouseAdapter{
		OptionsPanel parent;
		
		public AddWayPointMouseAdapter(OptionsPanel parent)
		{
			this.parent = parent;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			parent.addWaypointField();
		}
		
	}
	
	
	class RemoveWayPointMouseAdapter extends MouseAdapter{
		OptionsPanel parent;
		
		public RemoveWayPointMouseAdapter(OptionsPanel parent)
		{
			this.parent = parent;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			coordinates.get(coordinates.size() - 2).remove();
			coordinates.get(coordinates.size() - 1).yLocation -= 40;
			coordinates.get(coordinates.size() - 1).updateLocation();
			btnAddWaypoint.setBounds(btnAddWaypoint.getLocation().x, btnAddWaypoint.getLocation().y - 40,
					btnAddWaypoint.getWidth(), btnAddWaypoint.getHeight());
			int titleBarHeight = ((JFrame)SwingUtilities.getWindowAncestor(parent)).getHeight() - parent.getHeight();
			
			parent.setSize(parent.getWidth(), parent.getHeight() - 40);

			((JFrame)SwingUtilities.getWindowAncestor(parent)).getContentPane().setSize(parent.getSize());
			((JFrame)SwingUtilities.getWindowAncestor(parent)).setSize(parent.getWidth(), 
					parent.getHeight() + titleBarHeight);;

			btnRun.setLocation(btnRun.getLocation().x, btnRun.getLocation().y - 40);
			btnProfiles.setLocation(btnProfiles.getLocation().x, btnProfiles.getLocation().y - 40);
			
			btnRemWaypoint.setLocation(btnRemWaypoint.getLocation().x, btnRemWaypoint.getLocation().y - 40);
			if(coordinates.size() <= 2){
				btnRemWaypoint.setVisible(false);
			}
			btnAddWaypoint.setVisible(true);
			
		}
		
	}
	
	
	class CoordinateField {
		
		public static final int STARTCOORDINATE = 0;
		public static final int WAYPOINT = 1;
		public static final int ENDCOORDINATE = 2;
		
		JTextField textFieldLatitude;
		JTextField textFieldLongitude;
		JLabel titleLabel;
		public int yLocation;
		OptionsPanel parent;
		
		CoordinateField(int fieldType, OptionsPanel parent, CoordinateField prevField) {		
			this.parent = parent;

			this.textFieldLatitude = new JTextField();
			if(fieldType == STARTCOORDINATE) this.yLocation = 40;
			else this.yLocation = prevField.yLocation + 40;
			this.textFieldLatitude.setBounds(178, yLocation, 140, 28);
			this.textFieldLatitude.setHorizontalAlignment(SwingConstants.CENTER);
			parent.add(this.textFieldLatitude);
			this.textFieldLatitude.setColumns(10);
			
			this.textFieldLongitude = new JTextField();
			this.textFieldLongitude.setHorizontalAlignment(SwingConstants.CENTER);
			this.textFieldLongitude.setColumns(1);
			this.textFieldLongitude.setBounds(330, this.yLocation, 140, 28);
			parent.add(this.textFieldLongitude);
			
			switch(fieldType){
			case STARTCOORDINATE:
				titleLabel = new JLabel("Starting Coordinate:");
				break;
			case WAYPOINT:
				titleLabel = new JLabel("Waypoint:");
				break;
			default:
				titleLabel = new JLabel("Ending Coordinate:");
				break;			
			}
			
			this.titleLabel.setSize(this.titleLabel.getPreferredSize());
			int yLoc = this.textFieldLatitude.getLocation().y + (this.textFieldLatitude.getHeight()
					- this.titleLabel.getHeight())/2;	
			int xLoc = this.textFieldLatitude.getLocation().x - this.titleLabel.getWidth() - 10;		
			titleLabel.setBounds(xLoc, yLoc, titleLabel.getWidth(), titleLabel.getHeight());

			titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
			parent.add(titleLabel);
		}
		
		public void updateLocation() {
			this.textFieldLatitude.setBounds(178, yLocation, 140, 28);
			this.textFieldLongitude.setBounds(330, this.yLocation, 140, 28);
			int yLoc = this.textFieldLatitude.getLocation().y + (this.textFieldLatitude.getHeight()
					- this.titleLabel.getHeight())/2;			
			int xLoc = this.textFieldLatitude.getLocation().x - this.titleLabel.getWidth() - 10;		
			titleLabel.setBounds(xLoc, yLoc, titleLabel.getWidth(), titleLabel.getHeight());
		}
		
		public void remove() {
			coordinates.remove(this);
			parent.remove(this.textFieldLatitude);
			parent.remove(this.textFieldLongitude);
			parent.remove(this.titleLabel);
		}
	}
	
	
	class RunButtonMouseAdapter extends MouseAdapter{
		MainFrame mainFrame;
		OptionsPanel parent;
		
		public RunButtonMouseAdapter(MainFrame mainFrame, OptionsPanel parent)
		{
			this.mainFrame = mainFrame;
			this.parent = parent;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			try{
				List<Coordinate> coordinateList = new ArrayList<Coordinate>();
				Iterator<CoordinateField> it = coordinates.iterator();
				while(it.hasNext()){
					CoordinateField coord = it.next();
					if(!coord.textFieldLatitude.getText().isEmpty() && 
							!coord.textFieldLongitude.getText().isEmpty()){
						coordinateList.add(new Coordinate(Double.parseDouble(coord.textFieldLatitude.getText()),
								Double.parseDouble(coord.textFieldLongitude.getText())));
					}
				}
                mainFrame.route = new Router("", coordinateList, parent);         
	        }catch(NumberFormatException e1){
	            JOptionPane.showMessageDialog(parent , "Invalid Coordinates");
	            return;
	        }
			if(!mainFrame.route.success){
				return;
			}
	        mainFrame.segmentList = mainFrame.route.segmentList;
	        try{
	             mainFrame.producerInfo = new CycleProducer(mainFrame.segmentList, 
	                    optionValues);
	        }catch(NumberFormatException e1){
	            JOptionPane.showMessageDialog(parent, "Invalid Top Speed or Frequency");
	            return;
	        }
	        mainFrame.routeInfo = mainFrame.producerInfo.routeInfo;
	        mainFrame.createResultView();
		}
	}
	
	class ProfilesButtonMouseAdapter extends MouseAdapter{
		
		OptionsPanel parent;
		
		public ProfilesButtonMouseAdapter(OptionsPanel parent) {
			this.parent = parent;
		}
		
		
		public void mouseClicked(MouseEvent e) {
			new SettingsPanel(parent);
		}
		
	}
	
}
