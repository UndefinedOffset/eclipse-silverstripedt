package ca.edchipman.silverstripepdt.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.preferences.PreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripePluginImages;

@SuppressWarnings("restriction")
public class NewSilverStripeClassWizardPage extends WizardPage {
    private Text sourceFolder;
    private Text className;
    private ISelection selection;
    private Button[] btnWizardModeModifiers;
    
    /**
     * Create the wizard.
     */
    public NewSilverStripeClassWizardPage(final ISelection selection) {
        super("wizardPage");
        setPageComplete(false);
        setTitle("SilverStripe Class");
        setDescription("Create a new SilverStripe class.");
        setImageDescriptor(SilverStripePluginImages.DESC_ADD_SS_FILE);
        
        this.selection = selection;
    }

    /**
     * Create contents of the wizard.
     * @param parent
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        GridLayout gl_container = new GridLayout(3, false);
        gl_container.verticalSpacing = 9;
        container.setLayout(gl_container);
        
        Label lblSourceFolder = new Label(container, SWT.NONE);
        lblSourceFolder.setText("Source folder:");
        
        sourceFolder = new Text(container, SWT.BORDER);
        sourceFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        sourceFolder.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                dialogChanged(false);
            }
        });
        
        Button btnBrowse = new Button(container, SWT.NONE);
        btnBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                handleBrowse();
            }
        });
        GridData gd_btnBrowse = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_btnBrowse.widthHint = 80;
        btnBrowse.setLayoutData(gd_btnBrowse);
        btnBrowse.setText("Browse...");
        
        Label lblName = new Label(container, SWT.NONE);
        lblName.setText("Name:");
        
        className = new Text(container, SWT.BORDER);
        className.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        className.setFocus();
        className.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                dialogChanged(false);
            }
        });
        new Label(container, SWT.NONE);
        
        Label lblModifiers = new Label(container, SWT.NONE);
        lblModifiers.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        lblModifiers.setText("Mode:");
        
        Composite modifiersComp = new Composite(container, SWT.NONE);
        modifiersComp.setLayout(new GridLayout(1, false));
        GridData gd_modifiersComp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_modifiersComp.widthHint = 401;
        modifiersComp.setLayoutData(gd_modifiersComp);
        
        btnWizardModeModifiers = new Button[2];
        Button firstButton = new Button(modifiersComp, SWT.RADIO);
        String lastTemplate=this.getLastTemplateName();
        firstButton.setText("Create from Template"+(lastTemplate!=null && lastTemplate.length()>0 ? " ("+lastTemplate+")":""));
        firstButton.setSelection(true);
        firstButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                dialogChanged(false);
            }
        });
        
        btnWizardModeModifiers[0] = firstButton;
        
        Button parentButton = new Button(modifiersComp, SWT.RADIO);
        parentButton.setText("Create from Super Class or Interfaces");
        btnWizardModeModifiers[1] = parentButton;
        new Label(container, SWT.NONE);
        
        initialize();
        dialogChanged(true);
    }
    
    /**
     * Load the last template name used in New HTML File wizard.
     */
    protected String getLastTemplateName() {
        return SilverStripePDTPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.NEW_PHP_FILE_TEMPLATE);
    }
    
    public String getFileName() {
        return getClassName()+".php";
    }

    protected void updateStatus(final String message) {
    	if(getContainerName().isEmpty()==false && getClassName().isEmpty()==false && (message==null || message.isEmpty())) {
    		this.setPageComplete(true);
    	}else {
    		this.setPageComplete(false);
    	}
    	
    	if(this.btnWizardModeModifiers[0].getSelection()==true) {
    	    ((SilverStripeClassCreationWizard) this.getWizard()).setTemplateMode(true);
            if(this.isCurrentPage()) {
                this.getContainer().updateButtons();
            }
    	}else {
    	    ((SilverStripeClassCreationWizard) this.getWizard()).setTemplateMode(false);
    	    if(this.isCurrentPage()) {
    	        this.getContainer().updateButtons();
    	    }
    	}
    	
    	
        setErrorMessage(message);
    }

    protected IContainer getContainer(final String text) {
        final Path path = new Path(text);

        final IResource resource = ResourcesPlugin.getWorkspace().getRoot()
                .findMember(path);
        return resource instanceof IContainer ? (IContainer) resource : null;
    }
    
    /**
     * Ensures that both text fields are set.
     */
    protected void dialogChanged(Boolean wizardInit) {
        final String container = getContainerName();
        final String fileName = getFileName();

        if (container.length() == 0) {
            updateStatus(PHPUIMessages.PHPFileCreationWizardPage_10); //$NON-NLS-1$
            return;
        }
        final IContainer containerFolder = getContainer(container);
        if (containerFolder == null || !containerFolder.exists()) {
            updateStatus(PHPUIMessages.PHPFileCreationWizardPage_11); //$NON-NLS-1$
            return;
        }
        if (!containerFolder.getProject().isOpen()) {
            updateStatus(PHPUIMessages.PHPFileCreationWizardPage_12); //$NON-NLS-1$
            return;
        }
        
        if(wizardInit) {
            updateStatus(null);
            return;
        }
        
        if (fileName != null && !fileName.equals("") && containerFolder.getFile(new Path(fileName)).exists()) { //$NON-NLS-1$
            updateStatus("Specified class already exists"); //$NON-NLS-1$
            return;
        }

        if (getClassName().length() == 0) {
            updateStatus("Class name must be specified"); //$NON-NLS-1$
            return;
        }
        
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            String fileNameWithoutExtention = fileName.substring(0, dotIndex);
            if(fileNameWithoutExtention.matches("^(?=_*[A-z]+)[A-z0-9_]+$")==false) {
                updateStatus("Class name contains illegal characters"); //$NON-NLS-1$
                return;
            }
        }
        
        updateStatus(null);
    }
    
    /**
     * Returns true if the wizard is in class mode
     * @return True if in class mode false if not
     */
    public boolean isClassMode() {
    	return btnWizardModeModifiers[1].getSelection();
    }
    
    /**
     * Returns true if the wizard is in template mode
     * @return True if in template mode false if not
     */
    public boolean isTemplateMode() {
    	return btnWizardModeModifiers[0].getSelection();
    }

    /**
     * Tests if the current workbench selection is a suitable container to use.
     */
    private void initialize() {
        if (selection != null && selection.isEmpty() == false
                && selection instanceof IStructuredSelection) {
            final IStructuredSelection ssel = (IStructuredSelection) selection;
            if (ssel.size() > 1) {
                return;
            }

            Object obj = ssel.getFirstElement();
            if (obj instanceof IAdaptable) {
                obj = ((IAdaptable) obj).getAdapter(IResource.class);
            }

            IContainer container = null;
            if (obj instanceof IResource) {
                if (obj instanceof IContainer) {
                    container = (IContainer) obj;
                } else {
                    container = ((IResource) obj).getParent();
                }
            }

            if (container != null) {
                sourceFolder.setText(container.getFullPath().toString());
            }
        }
    }
    
    /**
     * Uses the standard container selection dialog to choose the new value for
     * the container field.
     */
    private void handleBrowse() {
        final ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, "Select New File Folder"); //$NON-NLS-1$
        dialog.showClosedProjects(false);
        
        if (dialog.open() == Window.OK) {
            final Object[] result = dialog.getResult();
            if (result.length == 1)
                sourceFolder.setText(((Path) result[0]).toOSString());
        }
    }

    public String getContainerName() {
        return sourceFolder.getText();
    }

    public IProject getProject() {
        String projectName = getContainerName();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(new Path(projectName));
        IProject project = null;
        if (resource instanceof IProject) {
            project = (IProject) resource;
        } else if (resource != null) {
            project = resource.getProject();
        }
        return project;
    }
    
    public IScriptProject getScriptProject() {
        return DLTKCore.create(getProject());
    }
    
    public boolean getIsCurrentPage() {
        return this.isCurrentPage();
    }
    
    /**
     * Returns the content of the classname input field.
     * 
     * @return the classname name
     */
    public String getClassName() {
        return className.getText();
    }
    
}
