package ca.edchipman.silverstripepdt.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.wizards.CompositeData;
import org.eclipse.php.internal.ui.wizards.DetectGroup;
import org.eclipse.php.internal.ui.wizards.LocationGroup;
import org.eclipse.php.internal.ui.wizards.NameGroup;
import org.eclipse.php.internal.ui.wizards.PHPProjectWizardFirstPage;
import org.eclipse.php.internal.ui.wizards.WizardFragment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.controls.SSVersionOption;

@SuppressWarnings("restriction")
public class SilverStripeProjectWizardFirstPage extends PHPProjectWizardFirstPage {
    protected SilverStripeLayoutGroup fLayoutGroup;
    protected SilverStripeVersionGroup fSSVersionGroup;
    
    public SilverStripeProjectWizardFirstPage() {
        super();
    }
    
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        final Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(initGridLayout(new GridLayout(1, false), false));
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        
        //Detect no versions and disable all fields
        if(SilverStripeVersion.getLangRegistry()==null) {
            this.setErrorMessage("No SilverStripe Versions are available cannot continue");
            composite.setEnabled(false);
        }
        
        // create UI elements
        fNameGroup = new NameGroup(composite, fInitialName, getShell());
        fPHPLocationGroup = new LocationGroup(composite, fNameGroup, getShell());

        CompositeData data = new CompositeData();
        data.setParetnt(composite);
        data.setSettings(getDialogSettings());
        data.setObserver(fPHPLocationGroup);
        fragment = (WizardFragment) Platform.getAdapterManager().loadAdapter(data, PHPProjectWizardFirstPage.class.getName());

        fVersionGroup = new VersionGroup(composite, null);
        fLayoutGroup = new SilverStripeLayoutGroup(composite);
        fSSVersionGroup = new SilverStripeVersionGroup(composite);
        fJavaScriptSupportGroup = new SilverStripeJavaScriptSupportGroup(composite, this);

        fDetectGroup = new DetectGroup(composite, fPHPLocationGroup, fNameGroup);

        // establish connections
        fNameGroup.addObserver(fPHPLocationGroup);
        fDetectGroup.addObserver(fLayoutGroup);

        fPHPLocationGroup.addObserver(fDetectGroup);
        // initialize all elements
        fNameGroup.notifyObservers();
        // create and connect validator
        fPdtValidator = new Validator();

        fNameGroup.addObserver(fPdtValidator);
        fPHPLocationGroup.addObserver(fPdtValidator);

        setControl(composite);
        Dialog.applyDialogFont(composite);

        // set the focus to the project name
        fNameGroup.postSetFocus();

        setHelpContext(composite);
    }
    
    /**
     * Gets the whether the new project is a project layout
     * @return Returns boolean true if the new project is a project layout
     */
    public boolean IsProjectLayout() {
        return fLayoutGroup.isProjectLayout();
    }
    
    /**
     * Gets the whether the new project is a theme layout
     * @return Returns boolean true if the new project is a theme layout
     */
    public boolean IsThemeLayout() {
        return fLayoutGroup.isThemeLayout();
    }
    
    /**
     * Gets the whether the new project is a module layout
     * @return Returns boolean true if the new project is a module layout
     */
    public boolean IsModuleLayout() {
        return fLayoutGroup.isModuleLayout();
    }
    
    /**
     * Gets the whether the new project is the module standard layout
     * @return Returns boolean true if the new project is a module and is the module standard layout
     */
    public boolean IsModuleStdLayout() {
        return this.IsModuleLayout() && fLayoutGroup.isModuleStdLayout();
    }
    
    /**
     * Gets the selected SilverStripe version radio
     * @return Returns the selected SilverStripe version
     */
    public String getSelectedVersion() {
        return fSSVersionGroup.getSelectedVersion();
    }
    
    /**
     * Gets the whether the project is a SilverStripe Framework Only project
     * @return Returns boolean true if the project is a SilverStripe Framework Only project
     */
    public boolean IsFrameworkOnlyProject() {
        return fSSVersionGroup.isFrameworkOnly();
    }
    
    /**
     * Gets the whether the project should include the siteconfig module
     * @return Returns boolean true if the project should include the siteconfig module
     */
    public boolean IncludeSiteConfig() {
        return fSSVersionGroup.includeSiteConfig();
    }
    
    /**
     * Gets the whether the project should include the reports module
     * @return Returns boolean true if the project should include the reports module
     */
    public boolean IncludeReports() {
        return fSSVersionGroup.includeReports();
    }
    
    /**
     * Request a project layout.
     */
    public class SilverStripeLayoutGroup implements Observer, SelectionListener, IDialogFieldListener {
        private final SelectionButtonDialogField fProjectRadio, fThemeRadio, fModuleRadio, fModuleStdCheck;
        private Group fGroup;
        private Link fModuleStdLink;

        public SilverStripeLayoutGroup(Composite composite) {
            final int numColumns = 3;
            
            fProjectRadio = new SelectionButtonDialogField(SWT.RADIO);
            fProjectRadio.setLabelText("SilverStripe project folder (usually mysite)"); //$NON-NLS-1$
            fProjectRadio.setDialogFieldListener(this);
            fProjectRadio.setSelection(true);

            fThemeRadio = new SelectionButtonDialogField(SWT.RADIO);
            fThemeRadio.setLabelText("SilverStripe theme"); //$NON-NLS-1$
            fThemeRadio.setDialogFieldListener(this);

            fModuleRadio = new SelectionButtonDialogField(SWT.RADIO);
            fModuleRadio.setLabelText("SilverStripe module"); //$NON-NLS-1$
            fModuleRadio.setDialogFieldListener(this);
            
            fModuleStdCheck = new SelectionButtonDialogField(SWT.CHECK);
            fModuleStdCheck.setLabelText("Create using the SilverStripe Module Standard?");
            fModuleStdCheck.setSelection(true);
            fModuleStdCheck.setEnabled(false);
            
            // createContent
            fGroup = new Group(composite, SWT.NONE);
            fGroup.setFont(composite.getFont());
            fGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fGroup.setLayout(initGridLayout(new GridLayout(numColumns, false), true));
            fGroup.setText(PHPUIMessages.LayoutGroup_OptionBlock_Title); //$NON-NLS-1$

            fProjectRadio.doFillIntoGrid(fGroup, 3);
            fThemeRadio.doFillIntoGrid(fGroup, 3);
            fModuleRadio.doFillIntoGrid(fGroup, 3);
            
            
            fModuleStdCheck.doFillIntoGrid(fGroup, 2);
            
            fModuleStdLink = new Link(fGroup, SWT.WRAP);
            fModuleStdLink.setText("(<a>What's This?</a>)");
            fModuleStdLink.setFont(composite.getFont());
            fModuleStdLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            fModuleStdLink.setEnabled(false);
            fModuleStdLink.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    Program.launch("https://www.silverstripe.org/software/addons/supported-modules-definition/");
                }
            });
            
            
            
            updateEnableState();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Observer#update(java.util.Observable,
         * java.lang.Object)
         */
        public void update(Observable o, Object arg) {
            updateEnableState();
        }

        private void updateEnableState() {
            if (fDetectGroup == null)
                return;

            final boolean detect = fDetectGroup.mustDetect();
            fProjectRadio.setEnabled(!detect);
            fThemeRadio.setEnabled(!detect);
            fModuleRadio.setEnabled(!detect);
            
            fModuleStdCheck.setEnabled(!detect && fModuleRadio.isSelected());
            fModuleStdLink.setEnabled(fModuleStdCheck.isEnabled());

            if (fGroup != null) {
                fGroup.setEnabled(!detect);
            }
        }

        /**
         * Return <code>true</code> if the user specified to create
         * 'application' and 'public' folders.
         * 
         * @return returns <code>true</code> if the user specified to create
         *         'source' and 'bin' folders.
         */
        public boolean isDetailedLayout() {
            return false;
        }
        
        /**
         * Gets the whether the project radio is selected
         * @return Returns boolean true if the project radio is selected
         */
        public boolean isProjectLayout() {
            return fProjectRadio.isSelected();
        }
        
        /**
         * Gets the whether the theme radio is selected
         * @return Returns boolean true if the theme radio is selected
         */
        public boolean isThemeLayout() {
            return fThemeRadio.isSelected();
        }
        
        /**
         * Gets the whether the module radio is selected
         * @return Returns boolean true if the module radio is selected
         */
        public boolean isModuleLayout() {
            return fModuleRadio.isSelected();
        }
        
        /**
         * Gets the whether the module standard is checked
         * @return Returns boolean true if the module standard is checked
         */
        public boolean isModuleStdLayout() {
            return fModuleStdCheck.isSelected();
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

        /*
         * @see
         * org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener
         * #dialogFieldChanged(org.eclipse.jdt.internal.ui.wizards.dialogfields.
         * DialogField)
         * 
         * @since 3.5
         */
        public void dialogFieldChanged(DialogField field) {
            updateEnableState();
        }

        public void widgetDefaultSelected(SelectionEvent e) {
            fProjectRadio.setSelection(true);
        }
    }
    
    /**
     * Request a SilverStripe Version.
     */
    public class SilverStripeVersionGroup implements ISelectionChangedListener, IDialogFieldListener {
        private final SelectionButtonDialogField fFrameworkModel, fIncludeSiteConfigModule, fIncludeReportsModule;
        private ArrayList<SSVersionOption> ssVersionRadios;
        private SSVersionOption _selectedVersion;
        private Combo fCombo;
        private ComboViewer viewer;
        private Composite _parent;
        private ExcludableComposite frameworkGroup, moduleGroup;

        public SilverStripeVersionGroup(Composite composite) {
            final int numColumns = 1;
            this._parent=composite;
            
            
            frameworkGroup=new ExcludableComposite(composite, SWT.NONE);
            GridLayout frameworkLayout=new GridLayout(1, true);
            frameworkLayout.marginWidth=0;
            frameworkLayout.marginHeight=0;
            frameworkGroup.setLayout(frameworkLayout);
            frameworkGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            
            
            fFrameworkModel = new SelectionButtonDialogField(SWT.CHECK);
            fFrameworkModel.setLabelText("Use SilverStripe Framework Only"); //$NON-NLS-1$
            fFrameworkModel.setDialogFieldListener(this);
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
            fIncludeSiteConfigModule.setEnabled(false);
            
            fIncludeReportsModule = new SelectionButtonDialogField(SWT.CHECK);
            fIncludeReportsModule.setLabelText("Include Reports Module"); //$NON-NLS-1$
            fIncludeReportsModule.setDialogFieldListener(this);
            fIncludeReportsModule.doFillIntoGrid(moduleGroup, 1);
            fIncludeReportsModule.setEnabled(false);
            
            
            // createContent
            Group fGroup = new Group(composite, SWT.NONE);
            fGroup.setFont(composite.getFont());
            fGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fGroup.setLayout(initGridLayout(new GridLayout(numColumns, false), true));
            fGroup.setText("SilverStripe Version"); //$NON-NLS-1$
            
            
            fCombo=new Combo(fGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
            fCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            
            
            viewer=new ComboViewer(fCombo);
            viewer.setContentProvider(new ArrayContentProvider());
            viewer.setLabelProvider(new LabelProvider());
            
            
            ssVersionRadios=new ArrayList<SSVersionOption>();
            HashMap<String, IConfigurationElement> registeredVersions=SilverStripeVersion.getLangRegistry(true);
            if(registeredVersions==null) {
                //No Versions available abort
                return;
            }
            
            
            SSVersionOption selectedOption=null;
            for(String versionCode : registeredVersions.keySet()) {
                IConfigurationElement version=registeredVersions.get(versionCode);
                
                //Create the radio based on the version code
                SSVersionOption option=new SSVersionOption(versionCode, version.getAttribute("display_name"));
                option.setSupportsFrameworkOnly(version.getAttribute("supports_framework_only")!=null && version.getAttribute("supports_framework_only").toLowerCase().equals("true"));
                option.setSupportsSiteConfigSO(version.getAttribute("supports_siteconfig_module")!=null && version.getAttribute("supports_siteconfig_module").toLowerCase().equals("true"));
                option.setSupportsReportsSO(version.getAttribute("supports_reports_module")!=null && version.getAttribute("supports_reports_module").toLowerCase().equals("true"));
                
                if(versionCode.equals(SilverStripeVersion.getDefaultVersion())) {
                    this._selectedVersion=option;
                    selectedOption=option;
                }
                
                //Add to the radio list
                ssVersionRadios.add(option);
            }
            
            viewer.setInput(ssVersionRadios);
            viewer.addSelectionChangedListener(this);
            
            viewer.setSelection(new StructuredSelection(selectedOption));
            
            
            frameworkGroup.setParent(fGroup);
            
            
            Label versionNoticeLabel=new Label(fGroup, SWT.SMOOTH);
            versionNoticeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            versionNoticeLabel.setText("Not seeing the SilverStripe Version you are expecting? You can install new versions from the SilverStripe DT update site.");
        }
        
        /**
         * Gets the whether the SilverStripe 3.1 radio is selected
         * @return Returns boolean true if the SilverStripe 3.1 radio is selected
         */
        public String getSelectedVersion() {
            return this._selectedVersion.getSSVersion();
        }
        
        /**
         * Gets the whether the SilverStripe Framework only checkbox is selected
         * @return Returns boolean true if the SilverStripe Framework only checkbox is selected
         */
        public boolean isFrameworkOnly() {
            return this._selectedVersion.getSupportsFrameworkOnly() && fFrameworkModel.isSelected();
        }
        
        /**
         * @TODO
         * @return
         */
        public boolean includeSiteConfig() {
            return this.isFrameworkOnly() && this._selectedVersion.getSupportsSiteConfigSO() && fIncludeSiteConfigModule.isSelected();
        }
        
        /**
         * @TODO
         * @return
         */
        public boolean includeReports() {
            return this.isFrameworkOnly() && this._selectedVersion.getSupportsReportsSO() && fIncludeReportsModule.isSelected();
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
            this._selectedVersion=selectedOption;
            this.frameworkGroup.setVisible(selectedOption.getSupportsFrameworkOnly());
            this.fFrameworkModel.setEnabled(selectedOption.getSupportsFrameworkOnly());
            
            Boolean showModules=(this.fFrameworkModel.isEnabled() && this.fFrameworkModel.isSelected() && (selectedOption.getSupportsSiteConfigSO() || selectedOption.getSupportsReportsSO()));
            this.moduleGroup.setVisible(showModules);
            this.fIncludeSiteConfigModule.setEnabled(selectedOption.getSupportsSiteConfigSO() && this.fFrameworkModel.isEnabled() && this.fFrameworkModel.isSelected());
            this.fIncludeReportsModule.setEnabled(selectedOption.getSupportsReportsSO() && this.fFrameworkModel.isEnabled() && this.fFrameworkModel.isSelected());
            this._parent.layout();
        }
    }
    
    public class SilverStripeJavaScriptSupportGroup extends JavaScriptSupportGroup {
        public SilverStripeJavaScriptSupportGroup(Composite composite, WizardPage projectWizardFirstPage) {
            super(composite, projectWizardFirstPage);
            
            fEnableJavaScriptSupport.setSelection(true);
        }
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
