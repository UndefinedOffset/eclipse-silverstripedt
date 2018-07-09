package ca.edchipman.silverstripepdt.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.php.internal.ui.Logger;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;
import org.eclipse.php.internal.ui.wizards.NewPHPTemplatesWizardPage;
import org.eclipse.php.internal.ui.wizards.PHPFileCreationWizard;
import org.eclipse.php.internal.ui.wizards.PHPFileCreationWizardPage;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;


@SuppressWarnings("restriction")
public class SilverStripeTemplateFileCreationWizard extends Wizard implements INewWizard {
    protected SilverStripeFileCreationWizardPage newSSFileCreationWizardPage;
    protected ISelection selection;
    protected NewSilverStripeTemplatesWizardPage newSSTemplatesWizardPage;
    
    /**
     * Constructor for SampleNewWizard.
     */
    public SilverStripeTemplateFileCreationWizard() {
        super();
        setWindowTitle("New SilverStripe Template File"); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
    
    public void addPages() {
        newSSFileCreationWizardPage = new SilverStripeFileCreationWizardPage(selection);
        addPage(newSSFileCreationWizardPage);

        newSSTemplatesWizardPage = new NewSilverStripeTemplatesWizardPage();
        addPage(newSSTemplatesWizardPage);
    }
    
    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {
        final String containerName = newSSFileCreationWizardPage.getContainerFullPath().toString();
        final String fileName = newSSFileCreationWizardPage.getFileName();
        
        final IFile file = newSSFileCreationWizardPage.createNewFile();
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
        String projectName = newSSFileCreationWizardPage.getContainerFullPath().toString();
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
    
    /**
     * A static nested class for the creation of a new PHP File.
     */
    public static class FileCreator {

        /**
         * The worker method. It will find the container, create the file if missing or
         * just replace its contents, and open the editor on the newly created file.
         * This method does not take an editor id to use when opening the file.
         * 
         * @param wizard
         * @param containerName
         * @param fileName
         * @param monitor
         * @param contents
         * @throws CoreException
         * @see {@link #createFile(Wizard, IFile, IProgressMonitor, String, String)}
         */
        public void createFile(Wizard wizard, IFile file, IProgressMonitor monitor, String contents)
                throws CoreException {
            createFile(wizard, file, monitor, contents, 0, null);
        }

        public void createFile(Wizard wizard, IFile file, IProgressMonitor monitor, String contents, int offset)
                throws CoreException {
            createFile(wizard, file, monitor, contents, offset, null);
        }

        /**
         * The worker method. It will find the container, create the file if missing or
         * just replace its contents, and open the editor on the newly created file.
         * 
         * @param wizard
         * @param containerName
         * @param fileName
         * @param monitor
         * @param contents
         * @param editorID
         *            An optional editor ID to use when opening the file (can be null).
         * @throws CoreException
         */
        public void createFile(Wizard wizard, final IFile file, IProgressMonitor monitor, String contents, int offset,
                final String editorID) throws CoreException {
            // create a sample file
            if (file != null) {
                if (!file.isLinked()) {
                    IContainer container = file.getParent();
                    // adopt project's/workspace's line delimiter (separator)
                    String lineSeparator = Platform.getPreferencesService().getString(Platform.PI_RUNTIME,
                            Platform.PREF_LINE_SEPARATOR, null,
                            new IScopeContext[] { new ProjectScope(container.getProject()) });
                    if (lineSeparator == null) {
                        lineSeparator = Platform.getPreferencesService().getString(Platform.PI_RUNTIME,
                                Platform.PREF_LINE_SEPARATOR, null, new IScopeContext[] { InstanceScope.INSTANCE });
                    }
                    if (lineSeparator == null) {
                        lineSeparator = System.getProperty(Platform.PREF_LINE_SEPARATOR);
                    }
                    if (contents != null) {
                        if (offset <= contents.length()) {
                            // recalculate caret position after that the line separators changed
                            offset = contents.substring(0, offset).replaceAll("\r\n?|\n", lineSeparator).length(); //$NON-NLS-1$
                        }
                        contents = contents.replaceAll("\r\n?|\n", lineSeparator); //$NON-NLS-1$
                    }

                    try {
                        InputStream stream = openContentStream(contents, getCharSetValue(container));
                        if (file.exists()) {
                            file.setContents(stream, true, true, monitor);
                        } else {
                            file.create(stream, true, monitor);
                        }
                        stream.close();
                    } catch (IOException e) {
                        Logger.logException(e);
                        return;
                    }

                }
                final int newOffset = offset;
                monitor.worked(1);
                monitor.setTaskName(NLS.bind(PHPUIMessages.newPhpFile_openning, file.getName()));
                wizard.getShell().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        try {
                            normalizeFile(file);
                            IEditorPart editor;
                            if (editorID == null) {
                                editor = IDE.openEditor(page, file, true);
                            } else {
                                editor = IDE.openEditor(page, file, editorID, true);
                            }
                            if (editor instanceof PHPStructuredEditor) {
                                StructuredTextViewer textViewer = ((PHPStructuredEditor) editor).getTextViewer();
                                textViewer.setSelectedRange(newOffset, 0);
                            }
                        } catch (PartInitException e) {
                        }
                    }
                });
                monitor.worked(1);
            }

        }

        /**
         * We will initialize file contents with a sample text.
         */
        private static InputStream openContentStream(String contents, String charSet) {
            if (contents == null) {
                contents = ""; //$NON-NLS-1$
            }
            byte[] bytes;
            try {
                bytes = contents.getBytes(charSet);
            } catch (UnsupportedEncodingException e) {
                bytes = contents.getBytes();
            }

            return new ByteArrayInputStream(bytes);
        }

        protected String getCharSetValue(IContainer container) {
            try {
                return container.getDefaultCharset(true);
            } catch (CoreException e) {// If there is an error return the
                                        // default
                return WorkbenchEncoding.getWorkbenchDefaultEncoding();
            }

        }

        /**
         * @param file
         */
        protected void normalizeFile(IFile file) {
        }

    }
}
