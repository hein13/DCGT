package userinterface.profile;

import javax.swing.JTextField;

public class OptionTextField extends JTextField {
	private static final long serialVersionUID = 1L;

	public OptionTextField(int numInStack, int width, SettingsPanel parent) {
		super();
		this.setBounds(SettingsPanel.MAINLABELWIDTH + SettingsPanel.STARTINGWIDTH + SettingsPanel.HORIZONTALSPACING,  
				SettingsPanel.STARTINGHEIGHT + numInStack * SettingsPanel.VERTICALSPACING, width,
				SettingsPanel.ITEMHEIGHT);
		parent.options.add(this);
	}
}
