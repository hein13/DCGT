package userinterface.profile;

import javax.swing.JTextArea;

public class MainLabel extends JTextArea {

	private static final long serialVersionUID = 1L;
	
	public MainLabel(String label, int numInStack, SettingsPanel parent) {
		super(label);
		this.setBounds(SettingsPanel.STARTINGWIDTH, SettingsPanel.STARTINGHEIGHT + 
				SettingsPanel.VERTICALSPACING * numInStack, SettingsPanel.MAINLABELWIDTH,  
				SettingsPanel.ITEMHEIGHT);
		this.setLineWrap(true);
		this.setWrapStyleWord(true);
		this.setEditable(false);
		this.setBackground(parent.getBackground());
		this.setCaretPosition(0);
	}

}
