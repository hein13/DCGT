package userinterface.profile;

import javax.swing.JLabel;

public class UnitLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	public UnitLabel(String label, SettingsPanel parent, int numInStack) {
		super(label);
		OptionTextField field = parent.options.get(parent.options.size() - 1);
		this.setBounds(field.getWidth() + field.getX() + SettingsPanel.HORIZONTALSPACING, 
				SettingsPanel.STARTINGHEIGHT + numInStack * SettingsPanel.VERTICALSPACING,
				parent.getWidth() - (field.getWidth() + field.getX())  - 
				2 * SettingsPanel.HORIZONTALSPACING,  SettingsPanel.ITEMHEIGHT);
	}
}
