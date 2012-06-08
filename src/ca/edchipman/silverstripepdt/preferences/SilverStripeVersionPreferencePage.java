package ca.edchipman.silverstripepdt.preferences;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dltk.internal.ui.preferences.PropertyAndPreferencePage;
import org.eclipse.dltk.internal.ui.util.CoreUtility;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.editor.SilverStripeTemplateStructuredEditor;

@SuppressWarnings("restriction")
public class SilverStripeVersionPreferencePage extends PropertyAndPreferencePage {
    public static final String PREF_ID = "ca.edchipman.silverstripepdt.preferences.SilverStripeVersionPreferencePage"; //$NON-NLS-1$
    public static final String PROP_ID = "ca.edchipman.silverstripepdt.propertyPages.SilverStripeVersionPreferencePage"; //$NON-NLS-1$

    private SilverStripeVersionGroup fConfigurationBlock;

    public SilverStripeVersionPreferencePage() {
        setPreferenceStore(SilverStripePDTPlugin.getDefault().getPreferenceStore());

        // only used when page is shown programatically
        setTitle("SilverStripe Version");
    }

    /*
     * @see
     * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
     * .Composite)
     */
    public void createControl(Composite parent) {
        IWorkbenchPreferenceContainer container = (IWorkbenchPreferenceContainer) getContainer();
        fConfigurationBlock = new SilverStripeVersionGroup(getProject(), parent);

        super.createControl(parent);
        
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IPHPHelpContextIds.PHP_INTERPRETER_PREFERENCES);
    }
    
    protected boolean isProjectPreferencePage() {
    	return false;
    }
    
    protected boolean offerLink() {
    	return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#
     * createPreferenceContent(org.eclipse.swt.widgets.Composite)
     */
    protected Control createPreferenceContent(Composite composite) {
        return fConfigurationBlock.createContents(composite);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#
     * hasProjectSpecificOptions(org.eclipse.core.resources.IProject)
     */
    protected boolean hasProjectSpecificOptions(IProject project) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#
     * getPreferencePageID()
     */
    protected String getPreferencePageID() {
        return PREF_ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#
     * getPropertyPageID()
     */
    protected String getPropertyPageID() {
        return PROP_ID;
    }

    /*
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        if (fConfigurationBlock != null && !fConfigurationBlock.performOk()) {
            return false;
        }
        
        return super.performOk();
    }

    /*
     * @see org.eclipse.jface.preference.IPreferencePage#performApply()
     */
    public void performApply() {
        if (fConfigurationBlock != null) {
            fConfigurationBlock.performApply();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    public void dispose() {
        if (fConfigurationBlock != null) {
            //fConfigurationBlock.dispose();
        }
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#setElement
     * (org.eclipse.core.runtime.IAdaptable)
     */
    public void setElement(IAdaptable element) {
        super.setElement(element);
        setDescription(null); // no description for property page
    }
    
    /**
     * Request a SilverStripe Version.
     */
    public class SilverStripeVersionGroup implements Observer, SelectionListener, IDialogFieldListener {
        private Group fGroup;
        private IProject fProject;
        private Shell fShell;
        private SelectionButtonDialogField fSS24Radio, fSS23Radio, fSS30Radio;
        private IWorkbenchPreferenceContainer fContainer;
        
        public boolean hasChanges = false;
        
        public SilverStripeVersionGroup(IProject project, Composite composite) {
            fProject=project;
        }

        protected void setShell(Shell shell) {
            fShell = shell;
        }
        
        protected Shell getShell() {
            return fShell;
        }
        
        public Control createContents(Composite parent) {
            setShell(parent.getShell());
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.verticalSpacing = 10;
            composite.setLayout(layout);
            
            fSS30Radio = new SelectionButtonDialogField(SWT.RADIO);
            fSS30Radio.setLabelText("SilverStripe 3.0"); //$NON-NLS-1$
            fSS30Radio.setDialogFieldListener(this);
            
            fSS24Radio = new SelectionButtonDialogField(SWT.RADIO);
            fSS24Radio.setLabelText("SilverStripe 2.4"); //$NON-NLS-1$
            fSS24Radio.setDialogFieldListener(this);
            
            fSS23Radio = new SelectionButtonDialogField(SWT.RADIO);
            fSS23Radio.setLabelText("SilverStripe 2.3"); //$NON-NLS-1$
            fSS23Radio.setDialogFieldListener(this);
            
            // createContent
            fGroup = new Group(composite, SWT.NONE);
            fGroup.setFont(composite.getFont());
            fGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fGroup.setLayout(layout);
            fGroup.setText(PHPUIMessages.LayoutGroup_OptionBlock_Title); //$NON-NLS-1$
            
            fSS30Radio.doFillIntoGrid(fGroup, 2);
            fSS24Radio.doFillIntoGrid(fGroup, 2);
            fSS23Radio.doFillIntoGrid(fGroup, 2);
            
            String ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_version", "SS3.0", fProject.getProject());
            
            if(ssVersion=="SS3.0") {
                fSS30Radio.setSelection(true);
            }else if(ssVersion=="SS2.4") {
                fSS24Radio.setSelection(true);
            }else if(ssVersion=="SS2.3") {
                fSS23Radio.setSelection(true);
            }else {
                fSS30Radio.setSelection(true);                
            }
            
            return composite;
        }
        
        /**
         * Gets the whether the SilverStripe 3.0 radio is selected
         * @return Returns boolean true if the SilverStripe 3.0 radio is selected
         */
        public boolean isSS30() {
            return fSS30Radio.isSelected();
        }
        
        /**
         * Gets the whether the SilverStripe 2.4 radio is selected
         * @return Returns boolean true if the SilverStripe 2.4 radio is selected
         */
        public boolean isSS24() {
            return fSS24Radio.isSelected();
        }
        
        /**
         * Gets the whether the SilverStripe 2.3 radio is selected
         * @return Returns boolean true if the SilverStripe 2.3 radio is selected
         */
        public boolean isSS23() {
            return fSS23Radio.isSelected();
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse
         * .swt.events.SelectionEvent)
         */
        public void widgetSelected(SelectionEvent e) {
            widgetDefaultSelected(e);
        }

        public void widgetDefaultSelected(SelectionEvent e) {
            fSS24Radio.setSelection(true);
        }

        @Override
        public void dialogFieldChanged(DialogField arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void update(Observable o, Object arg) {
            // TODO Auto-generated method stub
            
        }
        
        public boolean performOk() {
            return processChanges(fContainer);
        }

        public boolean performApply() {
            return processChanges(null); // apply directly
        }

        protected boolean processChanges(IWorkbenchPreferenceContainer container) {
            String ssVersion="SS3.0";
            
            if(fSS30Radio.isSelected()) {
                ssVersion="SS3.0";
            }else if(fSS24Radio.isSelected()) {
                ssVersion="SS2.4";
            }else if(fSS23Radio.isSelected()) {
                ssVersion="SS2.3";
            }
            
            if(ssVersion!=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_version", "SS3.0", fProject.getProject())) {
                hasChanges=true;
            }
            
            boolean doBuild = hasChanges;
            if (doBuild) {
                prepareForBuild();
            }
            if (container != null) {
                // no need to apply the changes to the original store: will be done
                // by the page container
                if (doBuild) { // post build
                    container.registerUpdateJob(CoreUtility.getBuildJob(fProject));
                }
            } else {
                // apply changes right away
                CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue("silverstripe_version", ssVersion, fProject);
                
                if (doBuild) {
                    CoreUtility.getBuildJob(fProject).schedule();
                }
            }
            
            if(hasChanges) {
                IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                IWorkbenchPage page = win.getActivePage();
                if (page != null) {
                    IEditorReference[] editors = page.getEditorReferences();
                    for(int i=0;i<editors.length;i++) {
                        IEditorReference editorRef = editors[i];
                        IEditorPart editor = editorRef.getEditor(false);
                        
                        if(editor instanceof SilverStripeTemplateStructuredEditor) {
                            ((SilverStripeTemplateStructuredEditor) editor).setSSVersion(ssVersion);
                        }
                    }
                }
            }
            
            return true;
        }

        protected void prepareForBuild() {
            // implement this method for any actions that need to be taken before
            // running build
        }
    }

    @Override
    protected String getPreferencePageId() {
        return SilverStripeVersionPreferencePage.PREF_ID;
    }

    @Override
    protected String getPropertyPageId() {
        return SilverStripeVersionPreferencePage.PROP_ID;
    }
}