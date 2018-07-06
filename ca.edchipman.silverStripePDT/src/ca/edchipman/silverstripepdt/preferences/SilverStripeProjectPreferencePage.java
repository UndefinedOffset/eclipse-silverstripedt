package ca.edchipman.silverstripepdt.preferences;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.dltk.internal.ui.dialogs.StatusUtil;
import org.eclipse.dltk.internal.ui.preferences.PropertyAndPreferencePage;
import org.eclipse.dltk.internal.ui.util.CoreUtility;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.dltk.ui.dialogs.StatusInfo;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripePreferences;
import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.controls.SSVersionOption;
import ca.edchipman.silverstripepdt.editor.SilverStripeTemplateStructuredEditor;

@SuppressWarnings("restriction")
public class SilverStripeProjectPreferencePage extends PropertyAndPreferencePage {
    public static final String PREF_ID = "ca.edchipman.silverstripepdt.preferences.SilverStripeBasePreferencePage"; //$NON-NLS-1$
    public static final String PROP_ID = "ca.edchipman.silverstripepdt.propertyPages.SilverStripeProjectPreferencePage"; //$NON-NLS-1$

    private SilverStripeVersionGroup fConfigurationBlock;
    private SiteBase fSiteBaseBlock;

    public SilverStripeProjectPreferencePage() {
        setPreferenceStore(SilverStripePDTPlugin.getDefault().getPreferenceStore());

        // only used when page is shown programatically
        setTitle("SilverStripe Project Preferences");
    }

    /*
     * @see
     * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
     * .Composite)
     */
    public void createControl(Composite parent) {
        setTitle("SilverStripe Project Preferences");
        
        IProject project=getProject();
        fConfigurationBlock = new SilverStripeVersionGroup(project, parent);
        
        fSiteBaseBlock = new SiteBase(project, parent, this);

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
    protected Control createPreferenceContent(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        fConfigurationBlock.createContents(composite);
        fSiteBaseBlock.createContents(composite);
        
        return composite;
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
        if (fConfigurationBlock != null && !fConfigurationBlock.performOk() && fSiteBaseBlock != null && !fSiteBaseBlock.performOk()) {
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
        
        if (fSiteBaseBlock != null) {
            fSiteBaseBlock.performApply();
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
    
    public void updateStatus(IStatus status) {
        setValid(!status.matches(IStatus.ERROR));
        StatusUtil.applyToStatusLine(this, status);
    }

    
    /**
     * Request a SilverStripe Version.
     */
    public class SilverStripeVersionGroup implements SelectionListener, IDialogFieldListener, ISelectionChangedListener {
        private Group fGroup;
        private IProject fProject;
        private Shell fShell;
        private SelectionButtonDialogField fFrameworkModel, fIncludeSiteConfigModule, fIncludeReportsModule;
        private ArrayList<SSVersionOption> ssVersionRadios;
        private IWorkbenchPreferenceContainer fContainer;
        private String _selectedVersion;
        private Combo fCombo;
        private ComboViewer viewer;
        private Composite _parent;
        private ExcludableComposite frameworkGroup, moduleGroup;
        
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
            this._parent=parent;
            
            setShell(parent.getShell());
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.verticalSpacing = 10;
            
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            
            frameworkGroup=new ExcludableComposite(composite, SWT.NONE);
            GridLayout frameworkLayout=new GridLayout(1, true);
            frameworkLayout.marginWidth=0;
            frameworkLayout.marginHeight=0;
            frameworkGroup.setLayout(frameworkLayout);
            frameworkGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            
            fFrameworkModel = new SelectionButtonDialogField(SWT.CHECK);
            fFrameworkModel.setLabelText("Use SilverStripe Framework Only"); //$NON-NLS-1$
            fFrameworkModel.setDialogFieldListener(this);
            fFrameworkModel.setEnabled(false);
            fFrameworkModel.doFillIntoGrid(frameworkGroup, 1);
            
            
            moduleGroup=new ExcludableComposite(frameworkGroup, SWT.NONE);
            GridLayout moduleLayout=new GridLayout(1, true);
            moduleLayout.marginWidth=0;
            moduleLayout.marginHeight=0;
            moduleGroup.setLayout(moduleLayout);
            moduleGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            
            
            fIncludeSiteConfigModule = new SelectionButtonDialogField(SWT.CHECK);
            fIncludeSiteConfigModule.setLabelText("Include SiteConfig Module"); //$NON-NLS-1$
            fIncludeSiteConfigModule.setDialogFieldListener(this);
            fIncludeSiteConfigModule.doFillIntoGrid(moduleGroup, 1);
            
            fIncludeReportsModule = new SelectionButtonDialogField(SWT.CHECK);
            fIncludeReportsModule.setLabelText("Include Reports Module"); //$NON-NLS-1$
            fIncludeReportsModule.setDialogFieldListener(this);
            fIncludeReportsModule.doFillIntoGrid(moduleGroup, 1);
            
            
            // createContent
            fGroup = new Group(composite, SWT.NONE);
            fGroup.setFont(composite.getFont());
            fGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fGroup.setLayout(layout);
            fGroup.setText("SilverStripe Version"); //$NON-NLS-1$
            
            
            
            String ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_VERSION, SilverStripeVersion.getDefaultVersion(), fProject.getProject());
            String ssFrameworkModel=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_FRAMEWORK_MODEL, SilverStripeVersion.FULL_CMS, fProject.getProject());
            String ssSiteConfigModule=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_SITECONFIG_MODULE, SilverStripeVersion.DEFAULT_SITECONFIG_MODULE, fProject.getProject());
            String ssReportsModule=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_REPORTS_MODULE, SilverStripeVersion.DEFAULT_REPORTS_MODULE, fProject.getProject());
            
            
            fCombo=new Combo(fGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
            fCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            
            
            viewer=new ComboViewer(fCombo);
            viewer.setContentProvider(new ArrayContentProvider());
            viewer.setLabelProvider(new LabelProvider());
            
            
            ssVersionRadios=new ArrayList<SSVersionOption>();
            HashMap<String, IConfigurationElement> registeredVersions=SilverStripeVersion.getLangRegistry();
            if(registeredVersions==null) {
                composite.setEnabled(false);
                return composite;
            }
            
            SSVersionOption selectedOption=null;
            for(String versionCode : registeredVersions.keySet()) {
                IConfigurationElement version=registeredVersions.get(versionCode);
                
                //Create the radio based on the version code
                SSVersionOption option=new SSVersionOption(versionCode, version.getAttribute("display_name"));
                option.setSupportsFrameworkOnly(version.getAttribute("supports_framework_only")!=null && version.getAttribute("supports_framework_only").toLowerCase().equals("true"));
                option.setSupportsSiteConfigSO(version.getAttribute("supports_siteconfig_module")!=null && version.getAttribute("supports_siteconfig_module").toLowerCase().equals("true"));
                option.setSupportsReportsSO(version.getAttribute("supports_reports_module")!=null && version.getAttribute("supports_reports_module").toLowerCase().equals("true"));
                
                if(versionCode.equals(ssVersion)) {
                    this._selectedVersion=option.getSSVersion();
                    selectedOption=option;
                    
                    if(option.getSupportsFrameworkOnly()==true) {
                        if(ssFrameworkModel.equals(SilverStripeVersion.FRAMEWORK_ONLY)) {
                            fFrameworkModel.setSelection(true);
                            
                            
                            if(option.getSupportsSiteConfigSO() || option.getSupportsSiteConfigSO()) {
                                if(option.getSupportsSiteConfigSO()==true) {
                                    if(ssSiteConfigModule.equals(SilverStripeVersion.SITECONFIG_MODULE_ENABLED)) {
                                        fIncludeSiteConfigModule.setSelection(true);
                                    }
                                }
                                
                                if(option.getSupportsReportsSO()==true) {
                                    if(ssReportsModule.equals(SilverStripeVersion.REPORTS_MODULE_ENABLED)) {
                                        fIncludeReportsModule.setSelection(true);
                                    }
                                }
                            }
                        }
                    }
                }
                
                //Add to the radio list
                ssVersionRadios.add(option);
            }
            
            viewer.setInput(ssVersionRadios);
            viewer.addSelectionChangedListener(this);
            
            viewer.setSelection(new StructuredSelection(selectedOption));
            this.setSelection(selectedOption);
            
            
            frameworkGroup.setParent(fGroup);
            
            
            Label versionNoticeLabel=new Label(fGroup, SWT.SMOOTH);
            versionNoticeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            versionNoticeLabel.setText("Not seeing the SilverStripe Version you are expecting? You can install new versions from the SilverStripe DT update site.");
            
            
            return composite;
        }
        
        /**
         * Gets the whether the SilverStripe 3.1 radio is selected
         * @return Returns boolean true if the SilverStripe 3.1 radio is selected
         */
        public String getSelectedVersion() {
            return this._selectedVersion;
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
            //Find the default version radio and set the selection
            for(SSVersionOption versionOption : this.ssVersionRadios) {
                if(versionOption.getSSVersion().equals(SilverStripeVersion.getDefaultVersion())) {
                    viewer.setSelection(new StructuredSelection(versionOption));
                    this.setSelection(versionOption);
                    break;
                }
            }
        }

        @Override
        public void dialogFieldChanged(DialogField arg0) {
            StructuredSelection selection=((StructuredSelection) this.viewer.getSelection());
            if(selection!=null) {
                SSVersionOption selectedOption=(SSVersionOption) selection.getFirstElement();
                if(selectedOption!=null) {
                    this.setSelection(selectedOption);
                }
            }
        }
        
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            StructuredSelection selection=((StructuredSelection) event.getSelection());
            if(selection!=null) {
                SSVersionOption selectedOption=(SSVersionOption) selection.getFirstElement();
                if(selectedOption!=null) {
                    this.setSelection(selectedOption);
                }
            }
        }
        
        protected void setSelection(SSVersionOption selectedOption) {
            this._selectedVersion=selectedOption.getSSVersion();
            this.frameworkGroup.setVisible(selectedOption.getSupportsFrameworkOnly());
            this.fFrameworkModel.setEnabled(selectedOption.getSupportsFrameworkOnly());
            
            Boolean showModules=(this.fFrameworkModel.isEnabled() && this.fFrameworkModel.isSelected() && (selectedOption.getSupportsSiteConfigSO() || selectedOption.getSupportsReportsSO()));
            this.moduleGroup.setVisible(showModules);
            this.fIncludeSiteConfigModule.setEnabled(selectedOption.getSupportsSiteConfigSO() && this.fFrameworkModel.isEnabled() && this.fFrameworkModel.isSelected());
            this.fIncludeReportsModule.setEnabled(selectedOption.getSupportsReportsSO() && this.fFrameworkModel.isEnabled() && this.fFrameworkModel.isSelected());
            this._parent.layout();
        }
        
        public boolean performOk() {
            return processChanges(fContainer);
        }

        public boolean performApply() {
            return processChanges(null); // apply directly
        }

        protected boolean processChanges(IWorkbenchPreferenceContainer container) {
            String ssVersion=SilverStripeVersion.getDefaultVersion();
            String ssFrameworkModel=SilverStripeVersion.FULL_CMS;
            String ssReportsModule=SilverStripeVersion.DEFAULT_REPORTS_MODULE;
            String ssSiteConfigModule=SilverStripeVersion.DEFAULT_SITECONFIG_MODULE;
            
            
            //Find the selected version option
            StructuredSelection selection=((StructuredSelection) this.viewer.getSelection());
            SSVersionOption selectedOption=(SSVersionOption) selection.getFirstElement();
            
            ssVersion=selectedOption.getSSVersion();
            
            
            //If framework only is supported and is selected
            if(fFrameworkModel.isSelected() && selectedOption.getSupportsFrameworkOnly()) {
                ssFrameworkModel=SilverStripeVersion.FRAMEWORK_ONLY;
                
                //If siteconfig is supported as a standalone module and is selected
                if(fIncludeSiteConfigModule.isSelected() && selectedOption.getSupportsSiteConfigSO()) {
                    ssSiteConfigModule=SilverStripeVersion.SITECONFIG_MODULE_ENABLED;
                }
                
                //If reports is supported as a standalone module and is selected
                if(fIncludeReportsModule.isSelected() && selectedOption.getSupportsReportsSO()) {
                    ssReportsModule=SilverStripeVersion.REPORTS_MODULE_ENABLED;
                }
            }
            
            
            //If the SilverStripe version value does not equal the preference we have changes
            if(ssVersion.equals(CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_VERSION, SilverStripeVersion.getDefaultVersion(), fProject.getProject()))==false) {
                hasChanges=true;
            }
            
            //If the framework only value does not equal the preference we have changes
            if(ssFrameworkModel.equals(CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_FRAMEWORK_MODEL, SilverStripeVersion.FULL_CMS, fProject.getProject()))==false) {
                hasChanges=true;
            }
            
            //If the siteconfig module value does not equal the preference we have changes
            if(ssSiteConfigModule.equals(CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_SITECONFIG_MODULE, SilverStripeVersion.DEFAULT_SITECONFIG_MODULE, fProject.getProject()))==false) {
                hasChanges=true;
            }
            
            //If the reports module value does not equal the preference we have changes
            if(ssReportsModule.equals(CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_REPORTS_MODULE, SilverStripeVersion.DEFAULT_REPORTS_MODULE, fProject.getProject()))==false) {
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
                CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_VERSION, ssVersion, fProject);
                CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_FRAMEWORK_MODEL, ssFrameworkModel, fProject);
                CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_SITECONFIG_MODULE, ssSiteConfigModule, fProject);
                CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_REPORTS_MODULE, ssReportsModule, fProject);
                
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
    
    /**
     * Request a SiteBase Field
     */
    public class SiteBase implements Observer, SelectionListener, IDialogFieldListener {
        private IProject fProject;
        private Shell fShell;
        private StringDialogField fSiteBase;
        private IWorkbenchPreferenceContainer fContainer;
        
        public boolean hasChanges = false;
        private IStatus fSiteBaseStatus;
        private SilverStripeProjectPreferencePage fParent;
        
        public SiteBase(IProject project, Composite composite, SilverStripeProjectPreferencePage parent) {
            fProject=project;
            fParent=parent;
        }

        protected void setShell(Shell shell) {
            fShell = shell;
        }
        
        protected Shell getShell() {
            return fShell;
        }
        
        public Control createContents(Composite parent) {
            setShell(parent.getShell());
            Composite composite=new Composite(parent, SWT.NONE);
            GridLayout layout=new GridLayout();
            layout.verticalSpacing=10;
            layout.numColumns=2;
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            
            
            fSiteBase=new StringDialogField();
            fSiteBase.setLabelText("Site Base URL: ");
            fSiteBase.setMessage("http://localhost/");
            fSiteBase.setDialogFieldListener(this);
            fSiteBase.doFillIntoGrid(composite, 2);
                        
            String siteBase=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_site_base", "", fProject.getProject());
            fSiteBase.setText(siteBase);
            
            
            LayoutUtil.setHorizontalGrabbing(fSiteBase.getTextControl(null));
            
            return composite;
        }
        
        /**
         * Gets the site base set in the field
         * @return String value of the site base field
         */
        public String getSiteBaseValue() {
            return fSiteBase.getText();
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

        public boolean performOk() {
            return processChanges(fContainer);
        }

        public boolean performApply() {
            return processChanges(null); // apply directly
        }

        protected boolean processChanges(IWorkbenchPreferenceContainer container) {
            String siteBase=fSiteBase.getText();
            
            
            if(siteBase!=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_site_base", null, fProject.getProject())) {
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
                CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue("silverstripe_site_base", siteBase, fProject);
                
                if (doBuild) {
                    CoreUtility.getBuildJob(fProject).schedule();
                }
            }
            
            return true;
        }

        protected void prepareForBuild() {
            // implement this method for any actions that need to be taken before
            // running build
        }

        @Override
        public void dialogFieldChanged(DialogField arg0) {
            fSiteBaseStatus=updateURLStatus();
            fParent.updateStatus(fSiteBaseStatus);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            
        }

        @Override
        public void update(Observable arg0, Object arg1) {
            fSiteBaseStatus=updateURLStatus();
            fParent.updateStatus(fSiteBaseStatus);
        }
        
        private IStatus updateURLStatus() {
            StatusInfo status=new StatusInfo();
            try {
                String address=fSiteBase.getText();
                if(address.length()==0) {
                    return status;
                }
                
                URL url=new URL(address);
                if(("http".equals(url.getProtocol())==false && "https".equals(url.getProtocol())==false) || url.getHost().length()==0) { //$NON-NLS-1$
                    status.setError("URL is not a web address");
                    return status;
                }
            }catch (MalformedURLException e) {
                status.setError("Invalid URL");
                return status;
            }

            return status;
        }
    }

    @Override
    protected String getPreferencePageId() {
        return SilverStripeProjectPreferencePage.PREF_ID;
    }

    @Override
    protected String getPropertyPageId() {
        return SilverStripeProjectPreferencePage.PROP_ID;
    }
    
    private class ExcludableComposite extends Composite {
        public ExcludableComposite(Composite parent, int style) {
            super(parent, style);
        }
        
        /**
         * Sets the visiblity of the composite, it also sets the value of the GridData's exclude property to the opposite of the visibility
         * @param visible
         */
        public void setVisible(Boolean visible) {
            super.setVisible(visible);
            
            GridData layout=((GridData) this.getLayoutData());
            if(layout!=null) {
                layout.exclude=!visible;
            }
        }
    }
}