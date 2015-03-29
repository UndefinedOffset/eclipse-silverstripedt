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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.php.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.php.internal.ui.util.ExceptionHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

@SuppressWarnings("restriction")
public class SilverStripeClassCreationWizard extends Wizard implements INewWizard {
    protected ISelection selection;
    protected NewSilverStripeClassWizardPage fPage;
    protected NewSilverStripeClassWizardTemplatePage tPage;
    protected NewSilverStripeClassWizardClassPage cPage;
    protected boolean inTemplateMode=false;
    
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
        
        cPage = new NewSilverStripeClassWizardClassPage(selection, fPage);
        addPage(cPage);
        
        tPage = new NewSilverStripeClassWizardTemplatePage(selection, fPage);
        addPage(tPage);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
    	if(fPage.isClassMode()) {
    		cPage.createType(monitor);
    	}else {
    		tPage.createFile(monitor);
    	}
    }
    
    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public boolean canFinish() {
        return ((fPage.getIsCurrentPage() && fPage.isPageComplete() && this.inTemplateMode) || cPage.getIsCurrentPage() || tPage.getIsCurrentPage());
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

    /*
     * (non-Javadoc) Method declared on IWizard. The default behavior is to
     * return the page that was added to this wizard after the given page.
     */
    public IWizardPage getNextPage(IWizardPage page) {
        if(fPage.isClassMode()) {
        	return cPage;
        }
        
        return tPage;
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
    
    public void setTemplateMode(boolean mode) {
        this.inTemplateMode=mode;
    }
}
