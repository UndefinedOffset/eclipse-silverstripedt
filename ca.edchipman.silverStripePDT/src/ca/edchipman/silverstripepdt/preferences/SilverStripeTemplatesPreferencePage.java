package ca.edchipman.silverstripepdt.preferences;

import org.eclipse.php.internal.ui.IPHPHelpContextIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;

@SuppressWarnings("restriction")
public class SilverStripeTemplatesPreferencePage extends TemplatePreferencePage {
	public SilverStripeTemplatesPreferencePage() {
		setPreferenceStore(SilverStripePDTPlugin.getDefault().getPreferenceStore());
		setTemplateStore(SilverStripePDTPlugin.getDefault().getTemplateStore());
		setContextTypeRegistry(SilverStripePDTPlugin.getDefault().getTemplateContextRegistry());
	}
	
    @Override
    protected Control createContents(Composite parent) {
        // TODO Auto-generated method stub
        createHeader(parent);
        return super.createContents(parent);
    }
	
    public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IPHPHelpContextIds.TEMPLATES_PREFERENCES);
		getControl().notifyListeners(SWT.Help, new Event());
    }
	
	private void createHeader(Composite contents) {
        final Shell shell = contents.getShell();
        String text = "Note that there are more SilverStripe templates on the <a>PHP Templates</a> preference page.";
        Link link = new Link(contents, SWT.NONE);
        link.setText(text);
        link.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(shell, "org.eclipse.php.ui.preferences.PHPTemplatesPreferencePage", null, null); //$NON-NLS-1$
            }
        });
        // TODO replace by link-specific tooltips when
        // bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=88866 gets fixed
        link.setToolTipText("Show the shared php templates");

        GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gridData.widthHint = 150; // only expand further if anyone else requires it
        link.setLayoutData(gridData);
    }
}