package ca.edchipman.silverstripepdt.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.internal.core.ScriptProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.php.internal.core.preferences.CorePreferenceConstants.Keys;
import org.eclipse.php.internal.core.project.ProjectOptions;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import ca.edchipman.silverstripepdt.SilverStripeNature;

@SuppressWarnings("restriction")
public class AddSilverStripeNatureAction implements IObjectActionDelegate {

	private IProject selProj = null;
	private IWorkbenchPart part = null; 
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		part = targetPart;
	}

    public void run(IAction action) {
		try {
			if(selProj == null || selProj.getNature("org.eclipse.php.core.PHPNature")==null)
				return;
		}catch (CoreException e1) {
			return;
		}
		
		try {
			IProjectDescription description = selProj.getProject().getDescription();
            String[] prevNatures = description.getNatureIds();
            String[] newNatures = new String[prevNatures.length + 1];
            newNatures[0] = SilverStripeNature.ID;                       
            System.arraycopy(prevNatures, 0, newNatures, 1, prevNatures.length);
            description.setNatureIds(newNatures);
            selProj.getProject().setDescription(description, null);
            
            //Disable asp tags
            if(CorePreferencesSupport.getInstance().getPreferencesValue(Keys.EDITOR_USE_ASP_TAGS, null, null)=="true") {
                ProjectOptions.setSupportingAspTags(false, selProj.getProject());
            }
            
            //Disable short tags
            if(CorePreferencesSupport.getInstance().getPreferencesValue(Keys.EDITOR_USE_SHORT_TAGS, null, null)=="true") {
                CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue(Keys.EDITOR_USE_SHORT_TAGS, "false", selProj.getProject());
            }
            
            ScriptProject project=new ScriptProject(selProj, null);
            
            IBuildpathEntry[] rawBuildPath = project.getRawBuildpath();
            IBuildpathEntry ssBuildPath=DLTKCore.newContainerEntry(new Path("ca.edchipman.silverstripepdt.LANGUAGE"));
            boolean entryFound=false;
            for(int i=0;i<rawBuildPath.length;i++) {
            	if(rawBuildPath[i].equals(ssBuildPath)) {
            		entryFound=true;
            		break;
            	}
            }
            
            if(entryFound) {
                List<IBuildpathEntry> buildPath = new ArrayList<IBuildpathEntry>();
                buildPath.addAll(Arrays.asList(rawBuildPath));
            	buildPath.add(ssBuildPath);
            	
            	IProgressMonitor monitor = new IProgressMonitor() {
					@Override
					public void worked(int work) {}
					
					@Override
					public void subTask(String name) {}
					
					@Override
					public void setTaskName(String name) {}
					
					@Override
					public void setCanceled(boolean value) {}
					
					@Override
					public boolean isCanceled() {
						return false;
					}
					
					@Override
					public void internalWorked(double work) {}
					
					@Override
					public void done() {}
					
					@Override
					public void beginTask(String name, int totalWork) {}
				};
				
            	project.setRawBuildpath(buildPath.toArray(rawBuildPath), monitor); //TODO not working
            }
            
            CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue("silverstripe_version", "SS2.4", selProj);
		}catch (Exception e) {
			MessageDialog.openInformation(
			         this.part.getSite().getShell(),
			         "SilverStripe PDT",
			         "Could not add SilverStripe nature");
			
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {		
		if(selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSel = (IStructuredSelection)selection;
			if(!selection.isEmpty()) {
				selProj = (IProject)structuredSel.getFirstElement();

				IProjectDescription description;
				try {
					description = selProj.getProject().getDescription();

					if(description.hasNature(SilverStripeNature.ID)) {
						action.setEnabled(false);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
			}
		}
	}	
	
}
