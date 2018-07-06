package ca.edchipman.silverstripepdt.preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
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
import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.contentassist.SSTemplateCompletionProcessor;
import ca.edchipman.silverstripepdt.controls.SSVersionCheck;
import ca.edchipman.silverstripepdt.templates.SilverStripeTemplate;
import ca.edchipman.silverstripepdt.templates.SilverStripeTemplateStore;

@SuppressWarnings("restriction")
public class SilverStripeCATemplatesPreferencePage extends TemplatePreferencePage {
    public SilverStripeCATemplatesPreferencePage() {
        setPreferenceStore(SilverStripePDTPlugin.getDefault().getPreferenceStore());
        setTemplateStore(SilverStripePDTPlugin.getDefault().getCATemplateStore());
        
        //Build registry
        ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();
        registry.addContextType(new CodeTemplateContextType(SSTemplateCompletionProcessor.TEMPLATE_CONTEXT_ID));
        
        setTemplateStore(new SilverStripeTemplateStore(registry, this.getPreferenceStore(),"ca.edchipman.silverstripepdt.contentassist.templates"));
        try {
            this.getTemplateStore().load();
        } catch (IOException e) {
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
        private SilverStripeTemplate fOriginalTemplate;
        private Template fNewTemplate;
        private ArrayList<String> fSSVersions;
        private ArrayList<SSVersionCheck> ssVersionChecks;
        
        
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
                ssVersionChecks=new ArrayList<SSVersionCheck>();
                HashMap<String, IConfigurationElement> registeredVersions=SilverStripeVersion.getLangRegistry();
                if(registeredVersions==null) {
                    return parent;
                }
                
                for(String versionCode : registeredVersions.keySet()) {
                    IConfigurationElement version=registeredVersions.get(versionCode);
                    
                    SSVersionCheck checkButton = new SSVersionCheck(fGroup, versionCode);
                    checkButton.setText(version.getAttribute("display_name")); //$NON-NLS-1$
                    checkButton.setSelection((ssVersions.size()==0 ? true:ssVersions.contains(versionCode)));
                    if(this.fOriginalTemplate.getContextTypeId().equals("php_new_ss_project_context")) {
                        checkButton.setEnabled(false);
                    }
                    
                    ssVersionChecks.add(checkButton);
                }
                
                
                return parent;
            }
            
            return super.createDialogArea(ancestor);
        }
        
        /*
         * @since 3.1
         */
        protected void okPressed() {
            if(SilverStripeVersion.getLangRegistry(true)==null) {
                this.cancelPressed();
                return;
            }
            
            if(this.fOriginalTemplate!=null) {
                this.fSSVersions=new ArrayList<String>();
                
                if(ssVersionChecks.size()>0) {
                    for(int i=0;i<ssVersionChecks.size();i++) {
                        SSVersionCheck checkButton=ssVersionChecks.get(i);
                        if(checkButton.getSelection()) {
                            this.fSSVersions.add(checkButton.getSSVersion());
                        }
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