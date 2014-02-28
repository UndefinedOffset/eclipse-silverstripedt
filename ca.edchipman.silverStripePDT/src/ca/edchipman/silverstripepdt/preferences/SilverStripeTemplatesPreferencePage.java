package ca.edchipman.silverstripepdt.preferences;

import java.util.Iterator;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.php.internal.ui.IPHPHelpContextIds;
import org.eclipse.php.internal.ui.corext.template.php.CodeTemplateContextType;
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
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.wizards.NewSilverStripeClassWizardTemplatePage;

@SuppressWarnings("restriction")
public class SilverStripeTemplatesPreferencePage extends TemplatePreferencePage {
	public SilverStripeTemplatesPreferencePage() {
		setPreferenceStore(SilverStripePDTPlugin.getDefault().getPreferenceStore());
		setTemplateStore(SilverStripePDTPlugin.getDefault().getTemplateStore());
		
		
		//Build registry
		Iterator contexts=SilverStripePDTPlugin.getDefault().getTemplateContextRegistry().contextTypes();
		ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();
		registry.addContextType(new CodeTemplateContextType(NewSilverStripeClassWizardTemplatePage.NEW_CLASS_CONTEXTTYPE));
		while(contexts.hasNext()) {
			registry.addContextType((TemplateContextType) contexts.next());
		}
		
		setContextTypeRegistry(registry);
	}
	
    public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IPHPHelpContextIds.TEMPLATES_PREFERENCES);
		getControl().notifyListeners(SWT.Help, new Event());
    }
}