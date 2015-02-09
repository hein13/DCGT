package userinterface.profile;

import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class DividingLine extends JSeparator {
	private static final long serialVersionUID = 1L;
	
	public DividingLine() {
		super(SwingConstants.VERTICAL);
		this.setBounds(SettingsPanel.MAINLABELWIDTH + SettingsPanel.HORIZONTALSPACING/2 +
				SettingsPanel.STARTINGWIDTH, 0, 10, SettingsPanel.WINDOWHEIGHT);
	}
}
