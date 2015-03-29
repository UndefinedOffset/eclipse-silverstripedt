package ca.edchipman.silverstripepdt.preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.php.internal.ui.IPHPHelpContextIds;
import org.eclipse.php.internal.ui.corext.template.php.CodeTemplateContextType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.templates.SilverStripeTemplate;
import ca.edchipman.silverstripepdt.templates.SilverStripeTemplateStore;
import ca.edchipman.silverstripepdt.wizards.NewSilverStripeClassWizardTemplatePage;

@SuppressWarnings("restriction")
public class SilverStripeTemplatesPreferencePage extends TemplatePreferencePage {
	public SilverStripeTemplatesPreferencePage() {
		setPreferenceStore(SilverStripePDTPlugin.getDefault().getPreferenceStore());
		
		//Build registry
		Iterator contexts=SilverStripePDTPlugin.getDefault().getTemplateContextRegistry().contextTypes();
		ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();
		registry.addContextType(new CodeTemplateContextType(NewSilverStripeClassWizardTemplatePage.NEW_CLASS_CONTEXTTYPE));
		while(contexts.hasNext()) {
			registry.addContextType((TemplateContextType) contexts.next());
		}
		
		setTemplateStore(new SilverStripeTemplateStore(registry, this.getPreferenceStore(), "ca.edchipman.silverstripepdt.SilverStripe.templates"));
		try {
			this.getTemplateStore().load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setContextTypeRegistry(registry);
	}
	
    public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IPHPHelpContextIds.TEMPLATES_PREFERENCES);
		getControl().notifyListeners(SWT.Help, new Event());
    }
	
	/*
	 * @see PreferencePage#performCancel()
	 */
	public boolean performCancel() {
		//Re-load the other template stores
		try {
			SilverStripePDTPlugin.getDefault().getCATemplateStore().load();
			SilverStripePDTPlugin.getDefault().getTemplateStore().load();
		} catch (IOException e) {
			openReadErrorDialog(e);
			return false;
		}
		
		return super.performCancel();
	}
	
	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		//Re-load the other template stores
		try {
			SilverStripePDTPlugin.getDefault().getCATemplateStore().load();
			SilverStripePDTPlugin.getDefault().getTemplateStore().load();
		} catch (IOException e) {
			openReadErrorDialog(e);
			return false;
		}
		
		return super.performOk();
	}
	
	/*
	 * @since 3.2
	 */
	private void openReadErrorDialog(IOException ex) {
		IStatus status= new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, "Failed to read templates.", ex); //$NON-NLS-1$
		TextEditorPlugin.getDefault().getLog().log(status);
		String title="Reading Templates";
		String message="Failed to read templates. See the error log for details.";
		MessageDialog.openError(getShell(), title, message);
	}

	/**
	 * Creates the edit dialog. Subclasses may override this method to provide a
	 * custom dialog.
	 *
	 * @param template the template being edited
	 * @param edit whether the dialog should be editable
	 * @param isNameModifiable whether the template name may be modified
	 * @return the created or modified template, or <code>null</code> if the edition failed
	 * @since 3.1
	 */
	protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
		IStructuredSelection selection= (IStructuredSelection) this.getTableViewer().getSelection();

		Object[] objects= selection.toArray();
		if ((objects != null) || (objects.length == 1)) {
			TemplatePersistenceData data=(TemplatePersistenceData) selection.getFirstElement();
			if(data.getTemplate() instanceof SilverStripeTemplate) {
				template=new SilverStripeTemplate((SilverStripeTemplate) data.getTemplate());
			}
		}
		
		EditTemplateDialog dialog= new SSEditTemplateDialog(getShell(), template, edit, isNameModifiable, this.getContextTypeRegistry());
		if (dialog.open() == Window.OK) {
			return dialog.getTemplate();
		}
		return null;
	}
    
    protected static class SSEditTemplateDialog extends EditTemplateDialog {
    	private Button fSS23Check;
    	private Button fSS24Check;
    	private Button fSS30Check;
    	private Button fSS31Check;
    	private SilverStripeTemplate fOriginalTemplate;
		private Template fNewTemplate;
		private ArrayList<String> fSSVersions;
    	
    	
		public SSEditTemplateDialog(Shell parent, Template template, boolean edit, boolean isNameModifiable, ContextTypeRegistry registry) {
			super(parent, template, edit, isNameModifiable, registry);
			
			this.fNewTemplate=null;
			if(template instanceof SilverStripeTemplate) {
				this.fOriginalTemplate=(SilverStripeTemplate) template;
			}
		}
		
		protected Control createDialogArea(Composite ancestor) {
			if(this.fOriginalTemplate!=null) {
				super.createDialogArea(ancestor);
			
				Composite parent= new Composite(ancestor, SWT.NONE);
				GridLayout layout= new GridLayout();
				layout.numColumns= 2;
				layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
				layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
				layout.verticalSpacing= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
				layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
				parent.setLayout(layout);
				parent.setLayoutData(new GridData(GridData.FILL_BOTH));
				
				
	            Group fGroup = new Group(parent, SWT.NONE);
	            fGroup.setFont(parent.getFont());
	            fGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	            fGroup.setLayout(initGridLayout(new GridLayout(4, false), true));
	            fGroup.setText("SilverStripe Versions"); //$NON-NLS-1$
				
	            
	            List<String> ssVersions=Arrays.asList(this.fOriginalTemplate.ssVersions());
	            
				fSS31Check = new Button(fGroup, SWT.CHECK);
	            fSS31Check.setText("SilverStripe 3.1"); //$NON-NLS-1$
	            fSS31Check.setSelection((ssVersions.size()==0 ? true:ssVersions.contains("SS3.1")));
	            
	            fSS30Check = new Button(fGroup, SWT.CHECK);
	            fSS30Check.setText("SilverStripe 3.0"); //$NON-NLS-1$
	            fSS30Check.setSelection((ssVersions.size()==0 ? true:ssVersions.contains("SS3.0")));
	            
	            fSS24Check = new Button(fGroup, SWT.CHECK);
	            fSS24Check.setText("SilverStripe 2.4"); //$NON-NLS-1$
	            fSS24Check.setSelection((ssVersions.size()==0 ? true:ssVersions.contains("SS2.4")));
	            
	            fSS23Check = new Button(fGroup, SWT.CHECK);
	            fSS23Check.setText("SilverStripe 2.3"); //$NON-NLS-1$
	            fSS23Check.setSelection((ssVersions.size()==0 ? true:ssVersions.contains("SS2.3")));
	            
	            if(this.fOriginalTemplate.getContextTypeId().equals("php_new_ss_project_context")) {
		            fSS31Check.setEnabled(false);
		            fSS30Check.setEnabled(false);
		            fSS24Check.setEnabled(false);
		            fSS23Check.setEnabled(false);
	            }
            	
	            return parent;
            }
			
			return super.createDialogArea(ancestor);
		}
		
		/*
		 * @since 3.1
		 */
		protected void okPressed() {
			if(this.fOriginalTemplate!=null) {
				this.fSSVersions=new ArrayList<String>();
				
				if(!(fSS31Check.getSelection() && fSS30Check.getSelection() && fSS24Check.getSelection() && fSS23Check.getSelection())) {
					if(fSS31Check.getSelection()) {
						this.fSSVersions.add("SS3.1");
					}
					
					if(fSS30Check.getSelection()) {
						this.fSSVersions.add("SS3.0");
					}
					
					if(fSS24Check.getSelection()) {
						this.fSSVersions.add("SS2.4");
					}
					
					if(fSS23Check.getSelection()) {
						this.fSSVersions.add("SS2.3");
					}
				}
			}
			
			super.okPressed();
		}
		
		/**
		 * Initialize a grid layout with the default Dialog settings.
		 */
		public GridLayout initGridLayout(GridLayout layout, boolean margins) {
			layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
			if (margins) {
				layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
				layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			} else {
				layout.marginWidth = 0;
				layout.marginHeight = 0;
			}
			return layout;
		}

		/**
		 * Returns the created template.
		 *
		 * @return the created template
		 * @since 3.1
		 */
		public Template getTemplate() {
			if(this.fOriginalTemplate!=null) {
				if(fNewTemplate==null) {
					Template template=super.getTemplate();
					
					//Re-create the template
					fNewTemplate=new SilverStripeTemplate(template, this.fSSVersions.toArray(new String[0]));
				}
				
				return fNewTemplate;
			}
			
			return super.getTemplate();
		}
    }
}