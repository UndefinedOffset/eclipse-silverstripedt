package ca.edchipman.silverstripedt.ss_3_1.language;

import org.eclipse.dltk.core.IScriptProject;
import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.language.ISilverStripeLanguageModelProvider;

public class DefaultLanguageModelProvider implements ISilverStripeLanguageModelProvider {
    private static final String LANGUAGE_LIBRARY_PATH = "$nl$/resources/SS"; //$NON-NLS-1$
    
    public String getLanguageLibraryPath(IScriptProject project, String ssFrameworkModel) {
        //For framework only 3.1 focus into framework only
        if(ssFrameworkModel.equals(SilverStripeVersion.FRAMEWORK_ONLY)) {
            return LANGUAGE_LIBRARY_PATH + "3.1/framework";
        }
        
        return LANGUAGE_LIBRARY_PATH + "3.1";
    }
}
