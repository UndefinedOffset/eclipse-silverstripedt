package ca.edchipman.silverstripepdt.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.php.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.php.internal.ui.util.ExceptionHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

public class SilverStripeClassCreationWizard extends Wizard implements INewWizard {
    protected ISelection selection;
    protected NewSilverStripeClassWizardPage fPage;
    
    public SilverStripeClassCreationWizard() {
        setWindowTitle("New SilverStripe Class");
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize
     * from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    @Override
    public void addPages() {
        fPage = new NewSilverStripeClassWizardPage(selection);
        addPage(fPage);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
        fPage.createType(monitor); // use the full progress monitor
    }
    
    @Override
    public boolean performFinish() {
        IWorkspaceRunnable op= new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
                try {
                    finishPage(monitor);
                } catch (InterruptedException e) {
                    throw new OperationCanceledException(e.getMessage());
                }
            }
        };
        try {
            ISchedulingRule rule= null;
            Job job= Job.getJobManager().currentJob();
            if (job != null)
                rule= job.getRule();
            IRunnableWithProgress runnable= null;
            if (rule != null)
                runnable= new WorkbenchRunnableAdapter(op, rule, true);
            else
                runnable= new WorkbenchRunnableAdapter(op, getSchedulingRule());
            getContainer().run(false/*canRunForked()*/, true, runnable);
        } catch (InvocationTargetException e) {
            handleFinishException(getShell(), e);
            return false;
        } catch  (InterruptedException e) {
            return false;
        }
        return true;
    }
    
    protected void handleFinishException(Shell shell, InvocationTargetException e) {
        ExceptionHandler.handle(e, shell, "New", "Creation of class failed.");
    }
    
    protected boolean canRunForked() {
        return true;
    }
    
    /**
     * Returns the scheduling rule for creating the element.
     * @return returns the scheduling rule
     */
    protected ISchedulingRule getSchedulingRule() {
        return ResourcesPlugin.getWorkspace().getRoot(); // look all by default
    }
}
