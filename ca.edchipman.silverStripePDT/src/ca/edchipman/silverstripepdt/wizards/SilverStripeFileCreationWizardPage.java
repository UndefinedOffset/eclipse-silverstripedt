package ca.edchipman.silverstripepdt.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.php.internal.core.documentModel.provisional.contenttype.ContentTypeIdForPHP;
import org.eclipse.php.internal.ui.IPHPHelpContextIds;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.wizards.PHPFileCreationWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import ca.edchipman.silverstripepdt.SilverStripePluginImages;
import ca.edchipman.silverstripepdt.SilverStripeVersion;

@SuppressWarnings("restriction")
public class SilverStripeFileCreationWizardPage extends WizardNewFileCreationPage {
    public SilverStripeFileCreationWizardPage(final ISelection selection) {
        super("SilverStripeFileCreationWizardPage", //$NON-NLS-1$
                selection instanceof IStructuredSelection ? (IStructuredSelection) selection : null);
        
        setTitle("New SilverStripe Template File"); //$NON-NLS-1$
        setDescription("Create a new SilverStripe template file"); //$NON-NLS-1$
        setImageDescriptor(SilverStripePluginImages.DESC_ADD_SS_FILE);
    }
    
    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(final Composite parent) {
        super.createControl(parent);
        
        setFileName("newfile.ss");
        
        if(SilverStripeVersion.getLangRegistry()==null) {
            this.setErrorMessage("No SilverStripe Versions are available cannot continue");
            parent.setEnabled(false);
            this.setPageComplete(false);
        }else {
            setPageComplete(validatePage());
        }
    }

    @Override
    public void performHelp() {
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
                IPHPHelpContextIds.CREATING_A_PHP_FILE_WITHIN_A_PROJECT);
        super.performHelp();
    }
    
    /**
     * This method is overridden to set additional validation specific to html
     * files.
     */
    @Override
    protected boolean validatePage() {
        setMessage(null);
        setErrorMessage(null);

        if (!super.validatePage()) {
            return false;
        }
        
        final IPath container = this.getContainerFullPath();
        final String fileName = getFileName();

        if (container.toString().length() == 0) {
            setErrorMessage(PHPUIMessages.PHPFileCreationWizardPage_10); //$NON-NLS-1$
            return false;
        }
        
        final IContainer containerFolder = getContainer(container);
        if (containerFolder == null || !containerFolder.exists()) {
            setErrorMessage(PHPUIMessages.PHPFileCreationWizardPage_11); //$NON-NLS-1$
            return false;
        }
        
        if (!containerFolder.getProject().isOpen()) {
            setErrorMessage(PHPUIMessages.PHPFileCreationWizardPage_12); //$NON-NLS-1$
            return false;
        }
        
        if (fileName != null && !fileName.equals("") && containerFolder.getFile(new Path(fileName)).exists()) { //$NON-NLS-1$
            setErrorMessage(PHPUIMessages.PHPFileCreationWizardPage_14); //$NON-NLS-1$
            return false;
        }

        int dotIndex = fileName.lastIndexOf('.');
        if (fileName.length() == 0 || dotIndex == 0) {
            setErrorMessage(PHPUIMessages.PHPFileCreationWizardPage_15);
            return false;
        }

        if (dotIndex != -1) {
            String fileNameWithoutExtention = fileName.substring(0, dotIndex);
            for (int i = 0; i < fileNameWithoutExtention.length(); i++) {
                char ch = fileNameWithoutExtention.charAt(i);
                if (!(Character.isJavaIdentifierPart(ch) || ch == '.' || ch == '-')) {
                    setErrorMessage(PHPUIMessages.PHPFileCreationWizardPage_16);
                    return false;
                }
            }
        }

        final IContentType contentType = Platform.getContentTypeManager().getContentType("ca.edchipman.silverstripepdt.SilverStripeTemplateSource");
        if (!contentType.isAssociatedWith(fileName)) {
            // fixed bug 195274
            // get the extensions from content type
            final String[] fileExtensions = contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
            StringBuilder buffer = new StringBuilder(PHPUIMessages.PHPFileCreationWizardPage_17);
            buffer.append(fileExtensions[0]);
            
            for (String extension : fileExtensions) {
                buffer.append(", ").append(extension); //$NON-NLS-1$
            }
            
            buffer.append("]"); //$NON-NLS-1$
            setErrorMessage(buffer.toString());
            
            return false;
        }

        setErrorMessage(null);
        
        return true;
    }

    protected IContainer getContainer(final IPath path) {

        final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        return resource instanceof IContainer ? (IContainer) resource : null;

    }

    public IProject getProject() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(getContainerFullPath());
        IProject project = null;
        if (resource instanceof IProject) {
            project = (IProject) resource;
        } else if (resource != null) {
            project = resource.getProject();
        }
        return project;
    }
}
