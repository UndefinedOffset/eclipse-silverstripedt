package ca.edchipman.silverstripedt.ss_3_1.language;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.php.internal.core.Logger;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.language.ISilverStripeLanguageModelProvider;

@SuppressWarnings("restriction")
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
