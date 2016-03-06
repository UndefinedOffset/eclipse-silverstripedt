package ca.edchipman.silverstripepdt.language;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.php.core.language.ILanguageModelProvider;
import org.eclipse.php.internal.core.Logger;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.versioninterfaces.ISilverStripeLanguageModelProvider;

@SuppressWarnings("restriction")
public class DefaultLanguageModelProvider implements ILanguageModelProvider {
    /**
     * Gets the path to the language for the specific project
     * @param project Project to get the language path for
     * @return Could return null on error or the Path object representing the language library path
     */
    public IPath getPath(IScriptProject project) {
        try {
            String ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_version", SilverStripeVersion.DEFAULT_VERSION, project.getProject());
            String ssFrameworkModel=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_framework_model", SilverStripeVersion.FULL_CMS, project.getProject());
            
            
            String path=getLanguageLibraryPath(project, ssVersion, ssFrameworkModel);
            if(path==null || path.isEmpty()) {
                String versionNumber=ssVersion.replaceFirst("SS", "");
                Display.getDefault().asyncExec(new MissionVersionDialog(versionNumber));
                
                return null;
            }
            
            return new Path(path);
        } catch (Exception e) {
            Logger.logException(e);
            return null;
        }
    }
    
    /**
     * Gets the name of this language
     * @return Language name
     */
    public String getName() {
        return "SilverStripe Core API";
    }
    
    /**
     * Gets the language model provider based on the project
     * @param project Project to look for the language
     * @return Language provider for the current project
     */
    public ISilverStripeLanguageModelProvider getLanguageModelProvider(IScriptProject project) {
        String ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_version", SilverStripeVersion.DEFAULT_VERSION, project.getProject());
        
        return this.getLanguageModelProvider(ssVersion);
    }
    
    /**
     * Gets the language model provider based on the SilverStripe version
     * @param ssVersion SilverStripe Version to look for the language provider for
     * @return Language provider for the SilverStripe Version
     */
    public ISilverStripeLanguageModelProvider getLanguageModelProvider(String ssVersion) {
        IConfigurationElement languageProvider=SilverStripeVersion.getLanguageDefinition(ssVersion);
        
        if(languageProvider!=null) {
            Object o;
            try {
                o = languageProvider.createExecutableExtension("language_provider");
                if(o instanceof ISilverStripeLanguageModelProvider) {
                    return (ISilverStripeLanguageModelProvider) o;
                }
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    /**
     * Gets the language library path for the project
     * @param project Project to look for the language for
     * @param ssVersion SilverStripe Version to look for
     * @param ssFrameworkModel Framework model (framework only or full cms)
     * @return Plugin path to the language library
     */
    private String getLanguageLibraryPath(IScriptProject project, String ssVersion, String ssFrameworkModel) {
        ISilverStripeLanguageModelProvider languageProvider=this.getLanguageModelProvider(ssVersion);
        
        if(languageProvider!=null) {
            return languageProvider.getLanguageLibraryPath(project, ssFrameworkModel);
        }
        
        return null;
    }
    
    /**
     * Gets the default SilverStripe DT plugin
     * @return Default SilverStripe DT plugin
     */
    public Plugin getPlugin() {
        return SilverStripePDTPlugin.getDefault();
    }
    
    /**
     * Gets the plugin based on the project
     * @param project Project to look for the language plugin for
     * @return Language plugin for the current project
     */
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
    
    private class MissionVersionDialog implements Runnable {
        private String versionNumber;
        
        public MissionVersionDialog(String versionNumber) {
            this.versionNumber=versionNumber;
        }
        
        @Override
        public void run() {
            MessageBox dialog=new MessageBox(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(), SWT.ICON_ERROR | SWT.OK);
            dialog.setText("Missing SilverStripe Version Package");
            dialog.setMessage(
                            "The current project uses the SilverStripe Version Package for SilverStripe "+versionNumber+", auto completing for the SilverStripe core will not be available."+
                            "You can install the SilverStripe Version Package by going to Help > Install New Software then select the SilverStripe DT Update site and installing the SilverStripe "+versionNumber+" Version Package."
                        );
            dialog.open();
        }
    }
}
