package ca.edchipman.silverstripepdt.preferences;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;

public class SilverStripeBasePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public SilverStripeBasePreferencePage() {
		// TODO Auto-generated constructor stub
		super();
		setPreferenceStore(SilverStripePDTPlugin.getDefault().getPreferenceStore());
		setDescription("SilverStripe");

	}

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		initializeDialogUnits(parent);

		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = 0;
		layout.verticalSpacing = convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		result.setLayout(layout);

		return result;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

}
