package ca.edchipman.silverstripepdt.wizards;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.PHPUiPlugin;
import org.eclipse.php.internal.ui.preferences.PHPProjectLayoutPreferencePage;
import org.eclipse.php.internal.ui.preferences.PreferenceConstants;
import org.eclipse.php.internal.ui.wizards.CompositeData;
import org.eclipse.php.internal.ui.wizards.DetectGroup;
import org.eclipse.php.internal.ui.wizards.LocationGroup;
import org.eclipse.php.internal.ui.wizards.NameGroup;
import org.eclipse.php.internal.ui.wizards.PHPProjectWizardFirstPage;
import org.eclipse.php.internal.ui.wizards.WizardFragment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.PreferencesUtil;

@SuppressWarnings("restriction")
public class SilverStripeProjectWizardFirstPage extends PHPProjectWizardFirstPage {
    protected SilverStripeLayoutGroup fLayoutGroup;
    
    public SilverStripeProjectWizardFirstPage() {
        super();
    }
    
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        final Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(initGridLayout(new GridLayout(1, false), false));
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        // create UI elements
        fNameGroup = new NameGroup(composite, fInitialName, getShell());
        fPHPLocationGroup = new LocationGroup(composite, fNameGroup, getShell());

        CompositeData data = new CompositeData();
        data.setParetnt(composite);
        data.setSettings(getDialogSettings());
        data.setObserver(fPHPLocationGroup);
        fragment = (WizardFragment) Platform.getAdapterManager().loadAdapter(
                data, PHPProjectWizardFirstPage.class.getName());

        fVersionGroup = new VersionGroup(composite);
        fLayoutGroup = new SilverStripeLayoutGroup(composite);
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
     * @return Returns boolean true if the new module is a project layout
     */
    public boolean IsModuleLayout() {
        return fLayoutGroup.isModuleLayout();
    }
    
    /**
     * Request a project layout.
     */
    public class SilverStripeLayoutGroup implements Observer, SelectionListener, IDialogFieldListener {
        private final SelectionButtonDialogField fProjectRadio, fThemeRadio, fModuleRadio;
        private Group fGroup;

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

            // createContent
            fGroup = new Group(composite, SWT.NONE);
            fGroup.setFont(composite.getFont());
            fGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fGroup.setLayout(initGridLayout(new GridLayout(numColumns, false), true));
            fGroup.setText(PHPUIMessages.LayoutGroup_OptionBlock_Title); //$NON-NLS-1$

            fProjectRadio.doFillIntoGrid(fGroup, 2);
            fThemeRadio.doFillIntoGrid(fGroup, 2);
            fModuleRadio.doFillIntoGrid(fGroup, 2);
            
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

            String prefID = PHPProjectLayoutPreferencePage.PREF_ID;

            Map data = null;
            PreferencesUtil.createPreferenceDialogOn(getShell(), prefID, new String[] { prefID }, data).open();
        }
    }
    
    public class SilverStripeJavaScriptSupportGroup extends JavaScriptSupportGroup {
        public SilverStripeJavaScriptSupportGroup(Composite composite, WizardPage projectWizardFirstPage) {
            super(composite, projectWizardFirstPage);
            
            fEnableJavaScriptSupport.setSelection(true);
        }
    }
}
