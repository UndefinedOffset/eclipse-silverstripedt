package ca.edchipman.silverstripepdt.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.internal.core.ModelElement;
import org.eclipse.dltk.internal.core.ScriptProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.php.internal.core.buildpath.BuildPathUtils;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.php.internal.core.preferences.CorePreferenceConstants.Keys;
import org.eclipse.php.internal.core.project.ProjectOptions;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import ca.edchipman.silverstripepdt.SilverStripeNature;
import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripeVersion;

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
            
            
            //Add Build Path Entry
            ScriptProject project=new ScriptProject(selProj, null);
            IBuildpathEntry ssBuildPath=DLTKCore.newContainerEntry(new Path(SilverStripePDTPlugin.NATURE_ID));
            List<IBuildpathEntry> buildPath = new ArrayList<IBuildpathEntry>();
        	buildPath.add(ssBuildPath);
        	
        	try {
        		BuildPathUtils.addNonDupEntriesToBuildPath(project, buildPath);
        	}catch (NullPointerException e) {
        		//TODO Figure out why there is one nothing seems to be null in the trace and it appears to work
        	}
            
            CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue("silverstripe_version", SilverStripeVersion.DEFAULT_VERSION, selProj);
            
            selProj.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
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
				    if(selProj.getProject().isAccessible()) {
    					description = selProj.getProject().getDescription();
    
    					if(description.hasNature(SilverStripeNature.ID)) {
    						action.setEnabled(false);
    					}
				    }
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
			}
		}
	}	
	
}
