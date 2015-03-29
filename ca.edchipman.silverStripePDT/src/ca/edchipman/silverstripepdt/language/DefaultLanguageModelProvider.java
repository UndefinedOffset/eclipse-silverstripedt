package ca.edchipman.silverstripepdt.language;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.php.core.language.ILanguageModelProvider;
import org.eclipse.php.internal.core.Logger;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripeVersion;

@SuppressWarnings("restriction")
public class DefaultLanguageModelProvider implements ILanguageModelProvider {
    public IPath getPath(IScriptProject project) {
        try {
            String ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_version", SilverStripeVersion.DEFAULT_VERSION, project.getProject());
            String ssFrameworkModel=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_framework_model", SilverStripeVersion.FULL_CMS, project.getProject());
            
            return new Path(getLanguageLibraryPath(project, ssVersion, ssFrameworkModel));
        } catch (Exception e) {
            Logger.logException(e);
            return null;
        }
    }

    public String getName() {
        return "SilverStripe Core API";
    }

    private String getLanguageLibraryPath(IScriptProject project, String ssVersion, String ssFrameworkModel) {
        IConfigurationElement languageProvider=SilverStripeVersion.getLanguageDefinition(ssVersion);
        
        if(languageProvider!=null) {
            Object o;
            try {
                o = languageProvider.createExecutableExtension("language_provider");
                if(o instanceof ISilverStripeLanguageModelProvider) {
                    return ((ISilverStripeLanguageModelProvider) o).getLanguageLibraryPath(project, ssFrameworkModel);
                }
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return null;
    }

    public Plugin getPlugin() {
        return SilverStripePDTPlugin.getDefault();
    }
    
    public Plugin getPlugin(IScriptProject project) {
        String ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_version", SilverStripeVersion.DEFAULT_VERSION, project.getProject());
        
        IConfigurationElement languageProvider=SilverStripeVersion.getLanguageDefinition(ssVersion);
        if(languageProvider!=null) {
            Object o;
            try {
                o = languageProvider.createExecutableExtension("activator");
                if(o instanceof AbstractUIPlugin) {
                    return (AbstractUIPlugin) o;
                }
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return SilverStripePDTPlugin.getDefault();
    }
}
