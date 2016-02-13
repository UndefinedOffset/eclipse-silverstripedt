package ca.edchipman.silverstripepdt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

public class SilverStripeVersion {
    public static final String DEFAULT_VERSION="SS3.3";
    
    public static final String FRAMEWORK_ONLY="framework";
    public static final String FULL_CMS="cms";
    public static final String VERSION_EXTENSION_ID="ca.edchipman.silverStripePDT.ss_version";
    
    private static LinkedHashMap<String, IConfigurationElement> lang_registry;
    
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
            for(IConfigurationElement language : extensions) {
                String versionKey="SS"+language.getAttribute("release_chain");
                SilverStripeVersion.lang_registry.put(versionKey, language);
            }
        }
        
        
        //Ensure plugins have been registered
        if(SilverStripeVersion.lang_registry.size()==0 && PlatformUI.getWorkbench().getActiveWorkbenchWindow()!=null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()!=null) {
            MessageBox dialog=new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
            dialog.setText("No Language Plugin");
            dialog.setMessage(
                            "You have not installed any SilverStripe Version plugins, you need to install atleast one."+
                            "You can install new SilverStripe Version plugins by going to Help > Install New Software then select the SilverStripe DT Update site and installing a SilverStripe version plugin."
                        );
            dialog.open();
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
        if(SilverStripeVersion.lang_registry.size()==0 && PlatformUI.getWorkbench().getActiveWorkbenchWindow()!=null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()!=null) {
            if(suppressError==false) {
                MessageBox dialog=new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                dialog.setText("No Language Plugin");
                dialog.setMessage(
                                "You have not installed any SilverStripe Version plugins, you need to install atleast one."+
                                "You can install new SilverStripe Version plugins by going to Help > Install New Software then select the SilverStripe DT Update site and installing a SilverStripe version plugin."
                            );
                dialog.open();
            }
            
            return null;
        }
        
        return SilverStripeVersion.lang_registry;
    }
}
