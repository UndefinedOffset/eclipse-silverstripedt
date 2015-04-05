package ca.edchipman.silverstripedt.ss_3_1.language;

import org.eclipse.dltk.core.IScriptProject;

import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.versioninterfaces.ISilverStripeLanguageModelProvider;
import ca.edchipman.silverstripepdt.wizards.NewSilverStripeTemplatesWizardPage;

public class DefaultLanguageModelProvider implements ISilverStripeLanguageModelProvider {
    private static final String LANGUAGE_LIBRARY_PATH = "$nl$/resources/SS"; //$NON-NLS-1$
    
    /**
     * Gets the path to the language provider
     * @param project Project to look for the language library path for
     * @param ssFrameworkModel Framework model to load
     * @return String path to the language library
     */
    public String getLanguageLibraryPath(IScriptProject project, String ssFrameworkModel) {
        //For framework only 3.1 focus into framework only
        if(ssFrameworkModel.equals(SilverStripeVersion.FRAMEWORK_ONLY)) {
            return LANGUAGE_LIBRARY_PATH + "3.1/framework";
        }
        
        return LANGUAGE_LIBRARY_PATH + "3.1";
    }
    
    /**
     * Gets the template context to be used
     * @return Gets the constant used for the templates
     */
    public String getTemplateContext() {
        return NewSilverStripeTemplatesWizardPage.NEW_SS_30_TEMPLATE_CONTEXTTYPE;
    }
}
