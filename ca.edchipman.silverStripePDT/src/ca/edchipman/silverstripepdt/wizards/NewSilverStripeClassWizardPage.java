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
import org.eclipse.dltk.internal.ui.wizards.buildpath.SetFilterWizardPage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.dialogs.saveFiles.ResourceAndContainerGroup;
import org.eclipse.php.internal.ui.preferences.PreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripePluginImages;

@SuppressWarnings("restriction")
public class NewSilverStripeClassWizardPage extends WizardPage implements Listener {
    private ResourceAndContainerGroup resourceGroup;
    private ISelection selection;
    
    /**
     * Create the wizard.
     */
    public NewSilverStripeClassWizardPage(final ISelection selection) {
        super("wizardPage");
        setPageComplete(false);
        setTitle("SilverStripe Class");
        setDescription("Create a new SilverStripe class");
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
        GridLayout gl_container = new GridLayout(1, false);
        gl_container.verticalSpacing = 9;
        container.setLayout(gl_container);
        
        resourceGroup=new ResourceAndContainerGroup(container, this, "Class Name:", IDEWorkbenchMessages.WizardNewFileCreationPage_file, false, 250);
        
        String lastTemplate=this.getLastTemplateName();
        if(lastTemplate.length()>0) {
            Label finishNextLabel = new Label(container, SWT.FILL);
            finishNextLabel.setText("Clicking finish will create a new class based on the \""+lastTemplate+"\" template, click next to change templates");
            finishNextLabel.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
        }
        
        initialize();
        validatePage(true);
    }
    
    /**
     * Load the last template name used in New HTML File wizard.
     */
    public String getLastTemplateName() {
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
        
        setErrorMessage(message);
    }

    protected IContainer getContainer(final String text) {
        final Path path = new Path(text);

        final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        return resource instanceof IContainer ? (IContainer) resource : null;
    }
    
    /**
     * Ensures that both text fields are set.
     */
    protected Boolean validatePage(Boolean wizardInit) {
        final String container = getContainerName();
        final String fileName = getFileName();

        if (container.length() == 0) {
            updateStatus(PHPUIMessages.PHPFileCreationWizardPage_10); //$NON-NLS-1$
            return false;
        }
        final IContainer containerFolder = getContainer(container);
        if (containerFolder == null || !containerFolder.exists()) {
            updateStatus(PHPUIMessages.PHPFileCreationWizardPage_11); //$NON-NLS-1$
            return false;
        }
        if (!containerFolder.getProject().isOpen()) {
            updateStatus(PHPUIMessages.PHPFileCreationWizardPage_12); //$NON-NLS-1$
            return false;
        }
        
        if(wizardInit) {
            updateStatus(null);
            return false;
        }
        
        if (fileName != null && !fileName.equals("") && containerFolder.getFile(new Path(fileName)).exists()) { //$NON-NLS-1$
            updateStatus("Specified class already exists"); //$NON-NLS-1$
            return false;
        }

        if (getClassName().length() == 0) {
            updateStatus("Class name must be specified"); //$NON-NLS-1$
            return false;
        }
        
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            String fileNameWithoutExtention = fileName.substring(0, dotIndex);
            if(fileNameWithoutExtention.matches("^(?=_*[A-z]+)[A-z0-9_]+$")==false) {
                updateStatus("Class name contains illegal characters"); //$NON-NLS-1$
                return false;
            }
        }
        
        updateStatus(null);
        return true;
    }
    
    /**
     * Tests if the current workbench selection is a suitable container to use.
     */
    private void initialize() {
        if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection) {
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
                resourceGroup.setContainerFullPath(container.getFullPath());
            }
        }
    }

    public String getContainerName() {
        return resourceGroup.getContainerFullPath().toString();
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
        return resourceGroup.getResource();
    }

    @Override
    public void handleEvent(Event event) {
        setPageComplete(validatePage(false));
    }
}
