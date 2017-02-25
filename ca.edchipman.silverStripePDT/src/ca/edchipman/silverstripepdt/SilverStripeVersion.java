package ca.edchipman.silverstripepdt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

public class SilverStripeVersion {
    
    public static final String FRAMEWORK_ONLY="framework";
    public static final String FULL_CMS="cms";
    public static final String VERSION_EXTENSION_ID="ca.edchipman.silverStripePDT.ss_version";
    public static final String DEFAULT_SITECONFIG_MODULE="N";
    public static final String DEFAULT_REPORTS_MODULE="N";
    public static final String SITECONFIG_MODULE_ENABLED="Y";
    public static final String REPORTS_MODULE_ENABLED="Y";
    
    private static LinkedHashMap<String, IConfigurationElement> lang_registry;
    private static String default_version="";
    
    /**
     * Initializes the language registry when the plugin is activated by mapping the configuration elements to the language key
     * @param registry Extension Registry to look for plugins in
     */
    public static void initLanguageRegistry(IExtensionRegistry registry) {
        IConfigurationElement[] extensions=registry.getConfigurationElementsFor(VERSION_EXTENSION_ID);
        Arrays.sort(extensions, new Comparator<IConfigurationElement>() {
            public int compare(IConfigurationElement left, IConfigurationElement right) {
                Double leftVersion=Double.parseDouble(left.getAttribute("release_chain"));
                Double rightVersion=Double.parseDouble(right.getAttribute("release_chain"));
                
                return Double.compare(rightVersion, leftVersion);
            }
        });
        
        
        SilverStripeVersion.lang_registry=new LinkedHashMap<String, IConfigurationElement>();
        if(extensions.length>0) {
            Integer i=0;
            
            //Add all languages to the registry
            for(IConfigurationElement language : extensions) {
                String versionKey="SS"+language.getAttribute("release_chain");
                SilverStripeVersion.lang_registry.put(versionKey, language);
                
                //If we're looking at the first element set it as the default version
                if(i==0) {
                    SilverStripeVersion.default_version=versionKey;
                }
                
                i++;
            }
        }
        
        
        //Ensure plugins have been registered
        if(SilverStripeVersion.lang_registry.size()==0) {
            Display.getDefault().asyncExec(new NoVersionsInstalledDialog());
        }
    }
    
    /**
     * Gets the language configuration element from the language registry
     * @param ssVersion SilverStripe version key to use i.e SS3.1
     * @return Returns the IConfigurationElement representing the SilverStripe version plugin or null if its not found
     */
    public static IConfigurationElement getLanguageDefinition(String ssVersion) {
        if(SilverStripeVersion.lang_registry.containsKey(ssVersion)) {
            return SilverStripeVersion.lang_registry.get(ssVersion);
        }
        
        return null;
    }
    
    /**
     * Gets the current language registry
     * @return Returns a hash map of the language configuration elements
     */
    public static LinkedHashMap<String, IConfigurationElement> getLangRegistry() {
        return SilverStripeVersion.getLangRegistry(false);
    }
    
    /**
     * Gets the current language registry
     * @param suppressError Suppress the error message or not
     * @return Returns a hash map of the language configuration elements
     */
    public static LinkedHashMap<String, IConfigurationElement> getLangRegistry(boolean suppressError) {
        //Ensure plugins have been registered
        if(SilverStripeVersion.lang_registry.size()==0) {
            if(suppressError==false) {
                Display.getDefault().asyncExec(new NoVersionsInstalledDialog());
            }
            
            return null;
        }
        
        return SilverStripeVersion.lang_registry;
    }
    
    /**
     * Gets the default version
     */
    public static String getDefaultVersion() {
        if(SilverStripeVersion.getDefaultVersion().isEmpty()==false) {
            return SilverStripeVersion.getDefaultVersion();
        }
        
        return null;
    }
    
    public static class NoVersionsInstalledDialog implements Runnable {
        @Override
        public void run() {
            MessageBox dialog=new MessageBox(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(), SWT.ICON_ERROR | SWT.OK);
            dialog.setText("No SilverStripe Version Package");
            dialog.setMessage(
                            "You have not installed any SilverStripe Version Package, you need to install atleast one."+
                            "You can install new SilverStripe Version Package by going to Help > Install New Software then select the SilverStripe DT Update site and installing a SilverStripe Version Package."
                        );
            dialog.open();
        }
    }
}
