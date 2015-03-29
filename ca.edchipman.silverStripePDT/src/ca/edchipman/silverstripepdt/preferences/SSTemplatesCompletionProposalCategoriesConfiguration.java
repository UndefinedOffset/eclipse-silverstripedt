package ca.edchipman.silverstripepdt.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wst.html.ui.internal.preferences.HTMLCompletionProposalCategoriesConfiguration;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;

@SuppressWarnings("restriction")
public class SSTemplatesCompletionProposalCategoriesConfiguration extends HTMLCompletionProposalCategoriesConfiguration {
    /**
     * @see org.eclipse.wst.sse.ui.preferences.AbstractCompletionProposalCategoriesConfiguration#getPreferenceStore()
     */
    protected IPreferenceStore getPreferenceStore() {
        return SilverStripePDTPlugin.getDefault().getPreferenceStore();
    }
}
