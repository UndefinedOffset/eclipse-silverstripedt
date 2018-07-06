package ca.edchipman.silverstripepdt.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;
import org.eclipse.php.internal.ui.wizards.PHPFileCreationWizard;


@SuppressWarnings("restriction")
public class SilverStripeTemplateFileCreationWizard extends PHPFileCreationWizard {
    protected NewSilverStripeTemplatesWizardPage newSSTemplatesWizardPage;
    
    /**
     * Constructor for SampleNewWizard.
     */
    public SilverStripeTemplateFileCreationWizard() {
        super();
        setWindowTitle("New SilverStripe Template File"); //$NON-NLS-1$
    }
    
    public void addPages() {
        phpFileCreationWizardPage = new SilverStripeFileCreationWizardPage(selection);
        addPage(phpFileCreationWizardPage);

        newSSTemplatesWizardPage = new NewSilverStripeTemplatesWizardPage();
        addPage(newSSTemplatesWizardPage);
    }
    
    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {
        final String containerName = phpFileCreationWizardPage.getContainerFullPath().toString();
        final String fileName = phpFileCreationWizardPage.getFileName();
        
        final IFile file = phpFileCreationWizardPage.createNewFile();
        if (file == null) {
            return false;
        }
        
        newSSTemplatesWizardPage.resetTableViewerInput();
        final PHPTemplateStore.CompiledTemplate template = this.newSSTemplatesWizardPage.compileTemplate(containerName, fileName);

        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException {
                try {
                    new FileCreator().createFile(SilverStripeTemplateFileCreationWizard.this, file, monitor, template.string, template.offset);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(),
                    PHPUIMessages.PHPFileCreationWizard_0,
                    realException.getMessage()); //$NON-NLS-1$
            return false;
        }
        
        return true;
    }
    
    public IProject getCurrentProject() {
        String projectName = phpFileCreationWizardPage.getContainerFullPath().toString();
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
}
