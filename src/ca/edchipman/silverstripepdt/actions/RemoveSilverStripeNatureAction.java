package ca.edchipman.silverstripepdt.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
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
public class RemoveSilverStripeNatureAction implements IObjectActionDelegate {

	private IWorkbenchPart part = null;
	private IProject selProj = null; 
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		part = targetPart;
	}

    public void run(IAction action) {
		if(selProj == null)
			return;
		
		try {

			IProjectDescription description = selProj.getProject().getDescription();
			
			if(description.hasNature(SilverStripeNature.ID)) {
				// remove the nature
				String[] prevNatures = description.getNatureIds();
				String[] newNatures = new String[prevNatures.length - 1];
				
				int i=0;
				for (String nature : prevNatures) {
					if(!nature.equals(SilverStripeNature.ID)) {
						newNatures[i++] = nature;
					}
				}
				
				description.setNatureIds(newNatures);
				selProj.getProject().setDescription(description, null);  
	            
	            //Restore asp and short tags setting
                ProjectOptions.setSupportingAspTags(Boolean.parseBoolean(CorePreferencesSupport.getInstance().getPreferencesValue(Keys.EDITOR_USE_ASP_TAGS, null, null)), selProj.getProject());
                CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue(Keys.EDITOR_USE_SHORT_TAGS, CorePreferencesSupport.getInstance().getPreferencesValue(Keys.EDITOR_USE_SHORT_TAGS, null, null), selProj.getProject());
			}
		}catch (Exception e) {
			MessageDialog.openInformation(
			         this.part.getSite().getShell(),
			         "SilverStripe PDT",
			         "Could not remove SilverStripe nature");
			
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

					if(!description.hasNature(SilverStripeNature.ID)) {
						action.setEnabled(false);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
}
