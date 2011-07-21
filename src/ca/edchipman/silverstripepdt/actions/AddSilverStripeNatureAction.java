package ca.edchipman.silverstripepdt.actions;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IScriptProject;
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

	private IScriptProject selProj = null;
	private IWorkbenchPart part = null; 
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		part = targetPart;
	}

    public void run(IAction action) {
		if(selProj == null)
			return;
		
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
				selProj = (IScriptProject)structuredSel.getFirstElement();

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
