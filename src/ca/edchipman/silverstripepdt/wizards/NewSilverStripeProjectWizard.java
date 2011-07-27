package ca.edchipman.silverstripepdt.wizards;

import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.wizards.PHPProjectCreationWizard;
import org.eclipse.php.internal.ui.wizards.PHPProjectWizardThirdPage;

import ca.edchipman.silverstripepdt.SilverStripePluginImages;

@SuppressWarnings("restriction")
public class NewSilverStripeProjectWizard extends PHPProjectCreationWizard {
    public static final String NEW_SS_PROJECT_TEMPLATE_CONTEXTTYPE="php_new_ss_project_context";
    
    public NewSilverStripeProjectWizard() {
        super();
        
        setWindowTitle("New SilverStripe Project"); //$NON-NLS-1$
        setDefaultPageImageDescriptor(SilverStripePluginImages.DESC_NEW_SS_PROJECT);
    }
    
    public void addPages() {
        // First page
        fFirstPage = new SilverStripeProjectWizardFirstPage();
        fFirstPage.setTitle("Create a SilverStripe project");
        fFirstPage.setDescription("Create a SilverStripe project in the workspace or in an external location.");
        addPage(fFirstPage);
        
        // Second page (Include Path)
        fSecondPage=new SilverStripeProjectWizardSecondPage(fFirstPage);
        fSecondPage.setTitle(PHPUIMessages.PHPProjectCreationWizard_Page2Title);
        fSecondPage.setDescription(PHPUIMessages.PHPProjectCreationWizard_Page2Description);
        addPage(fSecondPage);
        
        // Third page (Include Path)
        fThirdPage=new PHPProjectWizardThirdPage(fFirstPage);
        fThirdPage.setTitle(PHPUIMessages.PHPProjectCreationWizard_Page3Title);
        fThirdPage.setDescription(PHPUIMessages.PHPProjectCreationWizard_Page3Description);
        addPage(fThirdPage);
        
        fLastPage = fSecondPage;
    }
}
