package ca.edchipman.silverstripepdt;

import java.util.Iterator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.ui.IStartup;
import org.osgi.framework.*;
import org.eclipse.php.internal.ui.PHPUiPlugin;
import org.eclipse.php.internal.ui.corext.template.php.CodeTemplateContextType;

import ca.edchipman.silverstripepdt.templates.FileNameTemplateResolver;
import ca.edchipman.silverstripepdt.wizards.NewSilverStripeProjectWizard;
import ca.edchipman.silverstripepdt.wizards.NewSilverStripeTemplatesWizardPage;

@SuppressWarnings("restriction")
public class RegisterResolvers implements IStartup {
    /**
     * {@inheritDoc}
     * @see IStartup#earlyStartup()
     */
    public void earlyStartup() {
        // check if plug-in org.eclipse.jdt.ui is already active
        final Bundle bundle = Platform.getBundle(SilverStripePDTPlugin.PLUGIN_ID);
        if (bundle != null && bundle.getState() == Bundle.ACTIVE) {
            // register resolvers
            registerResolvers();
            
            ContextTypeRegistry registry = PHPUiPlugin.getDefault().getCodeTemplateContextRegistry();
            registry.addContextType(new CodeTemplateContextType(NewSilverStripeTemplatesWizardPage.NEW_SS_TEMPLATE_CONTEXTTYPE));
            registry.addContextType(new CodeTemplateContextType(NewSilverStripeProjectWizard.NEW_SS_PROJECT_TEMPLATE_CONTEXTTYPE));
        } else {
            // register listener to get informed, when plug-in becomes active
            final BundleContext bundleContext = SilverStripePDTPlugin.getDefault().getBundle().getBundleContext();
            bundleContext.addBundleListener(new BundleListener() {
                public void bundleChanged(final BundleEvent pEvent) {
                    final Bundle bundle2 = pEvent.getBundle();
                    if (!bundle2.getSymbolicName().equals(SilverStripePDTPlugin.PLUGIN_ID)) {
                        return;
                    }
                    if (bundle2.getState() == Bundle.ACTIVE) {
                        registerResolvers();
                        bundleContext.removeBundleListener(this);
                    }
                }
            });
        }
    }

    /**
     * Internal method to register resolvers with all context types.
     */
    private void registerResolvers() {
        //Add to template context
        final ContextTypeRegistry templateContextRegistry = PHPUiPlugin.getDefault().getTemplateContextRegistry();
        final Iterator<?> tIter = templateContextRegistry.contextTypes();
        
        while (tIter.hasNext()) {
            final TemplateContextType contextType = (TemplateContextType) tIter.next();
            contextType.addResolver(new FileNameTemplateResolver());
        }
        
        
        //Add to code template context
        final ContextTypeRegistry codeTemplateContextRegistry = PHPUiPlugin.getDefault().getCodeTemplateContextRegistry();
        final Iterator<?> ctIter = codeTemplateContextRegistry.contextTypes();
        
        while (ctIter.hasNext()) {
            final TemplateContextType contextType = (TemplateContextType) ctIter.next();
            contextType.addResolver(new FileNameTemplateResolver());
        }
    }
}
