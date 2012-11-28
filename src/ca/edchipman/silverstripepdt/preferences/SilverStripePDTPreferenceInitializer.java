package ca.edchipman.silverstripepdt.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames;
import org.eclipse.wst.sse.ui.internal.preferences.ui.ColorHelper;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.style.IStyleConstantsSS;

@SuppressWarnings("restriction")
public class SilverStripePDTPreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        IPreferenceStore store = SilverStripePDTPlugin.getDefault().getPreferenceStore();
        ColorRegistry registry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
        
        // HTML Style Preferences
        String NOBACKGROUNDBOLD = " | null | true"; //$NON-NLS-1$
        String NOBACKGROUNDNOBOLD = " | null | false"; //$NON-NLS-1$
        String JUSTITALIC = " | null | false | true"; //$NON-NLS-1$
        
        
        //Set style for else
        String styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_ELSE, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_ELSE, styleValue);
        
        //Set style for else_if and if
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_CONDITIONAL_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_CONDITIONAL_OPEN, styleValue);
        
        //Set style for else_if and if contents
        styleValue = "null" + JUSTITALIC;
        store.setDefault(IStyleConstantsSS.SS_CONDITIONAL_TEXT, styleValue);
        
        //Set style for end_if
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_END_IF, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_END_IF, styleValue);
        
        //Set style for require
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_REQUIREMENT_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_REQUIREMENT_OPEN, styleValue);
        
        //Set style for require text
        styleValue = "null" + JUSTITALIC;
        store.setDefault(IStyleConstantsSS.SS_REQUIREMENT_CONTENT, styleValue);
        
        //Set style for include
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_INCLUDE_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_INCLUDE_OPEN, styleValue);
        
        //Set style for include text
        styleValue = "null" + JUSTITALIC;
        store.setDefault(IStyleConstantsSS.SS_INCLUDE_CONTENT, styleValue);
        
        //Set style for control
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_CONTROL_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_CONTROL_OPEN, styleValue);
        
        //Set style for control text
        styleValue = "null" + JUSTITALIC;
        store.setDefault(IStyleConstantsSS.SS_CONTROL_CONTENT, styleValue);
        
        //Set style for end_control
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_END_CONTROL, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_END_CONTROL, styleValue);
        
        //Set style for cacheblock
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_CACHEBLOCK_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_CACHEBLOCK_OPEN, styleValue);
        
        //Set style for cacheblock text
        styleValue = "null" + JUSTITALIC;
        store.setDefault(IStyleConstantsSS.SS_CACHEBLOCK_CONTENT, styleValue);
        
        //Set style for end_cacheblock
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_END_CACHEBLOCK, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_END_CACHEBLOCK, styleValue);
        
        //Set style for uncacheblock
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_UNCACHED_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_UNCACHED_OPEN, styleValue);
        
        //Set style for end_uncacheblock
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_END_UNCACHED, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_END_UNCACHED, styleValue);
        
        //Set style for base_tag
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_REQUIREMENT_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD; //$NON-NLS-1$
        store.setDefault(IStyleConstantsSS.SS_BASE_TAG, styleValue); // specified value is black; leaving as widget default
        
        //Set style for require content
        styleValue = "null" + JUSTITALIC;
        store.setDefault(IStyleConstantsSS.SS_REQUIREMENT_CONTENT, styleValue);
        
        //Set style for with
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_WITH_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_WITH_OPEN, styleValue);
        
        //Set style for with text
        styleValue = "null" + JUSTITALIC;
        store.setDefault(IStyleConstantsSS.SS_WITH_CONTENT, styleValue);
        
        //Set style for end_with
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_END_WITH, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_END_WITH, styleValue);
        
        //Set style for loop
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_LOOP_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_LOOP_OPEN, styleValue);
        
        //Set style for loop text
        styleValue = "null" + JUSTITALIC;
        store.setDefault(IStyleConstantsSS.SS_LOOP_CONTENT, styleValue);
        
        //Set style for end_loop
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_END_LOOP, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_END_LOOP, styleValue);
        
        //Set style for i18n
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_I18N_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_I18N_OPEN, styleValue);
        
        //Set style for i18n text
        styleValue = "null" + JUSTITALIC;
        store.setDefault(IStyleConstantsSS.SS_I18N_CONTENT, styleValue);

        //SS Delims
        styleValue = "null" + NOBACKGROUNDBOLD; //$NON-NLS-1$
        store.setDefault(IStyleConstantsSS.SS_DELIM, styleValue); // specified value is black; leaving as widget default
        
        //Set style for variables
        styleValue = "null" + NOBACKGROUNDBOLD; //$NON-NLS-1$
        store.setDefault(IStyleConstantsSS.SS_VARIABLE, styleValue); // specified value is black; leaving as widget default
        
        //Set style for comment delims
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_COMMENT_DELIM, 63, 95, 191) + NOBACKGROUNDNOBOLD;
        store.setDefault(IStyleConstantsSS.SS_COMMENT_DELIM, styleValue);
        
        //Set style for comment text
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_COMMENT_TEXT, 63, 95, 191) + NOBACKGROUNDNOBOLD;
        store.setDefault(IStyleConstantsSS.SS_COMMENT_TEXT, styleValue);
        
        //Set style for template functions
        styleValue = ColorHelper.findRGBString(registry, IStyleConstantsSS.SS_TEMPLATE_FUNCTION_OPEN, 127, 0, 85) + NOBACKGROUNDBOLD;
        store.setDefault(IStyleConstantsSS.SS_TEMPLATE_FUNCTION_OPEN, styleValue);
        
        //Set style for template functions
        styleValue = "null" + JUSTITALIC;
        store.setDefault(IStyleConstantsSS.SS_TEMPLATE_FUNCTION_CONTENT, styleValue);
        
        // Defaults for Content Assist preference page
        store.setDefault(HTMLUIPreferenceNames.CONTENT_ASSIST_DO_NOT_DISPLAY_ON_DEFAULT_PAGE, "");
        store.setDefault(HTMLUIPreferenceNames.CONTENT_ASSIST_DO_NOT_DISPLAY_ON_OWN_PAGE, "");
        store.setDefault(HTMLUIPreferenceNames.CONTENT_ASSIST_DEFAULT_PAGE_SORT_ORDER,
             "org.eclipse.wst.html.ui.proposalCategory.htmlTags\0" +
             "ca.edchipman.silverstripepdt.ssTemplates\0" +
             "org.eclipse.wst.css.ui.proposalCategory.css\0" +
             "org.eclipse.wst.html.ui.proposalCategory.htmlTemplates\0" +
             "org.eclipse.wst.css.ui.proposalCategory.cssTemplates");
        store.setDefault(HTMLUIPreferenceNames.CONTENT_ASSIST_OWN_PAGE_SORT_ORDER,
             "org.eclipse.wst.html.ui.proposalCategory.htmlTemplates\0"+
             "ca.edchipman.silverstripepdt.ssTemplates\0" +
             "org.eclipse.wst.css.ui.proposalCategory.cssTemplates\0" +
             "org.eclipse.wst.html.ui.proposalCategory.htmlTags\0" +
             "org.eclipse.wst.css.ui.proposalCategory.css");
    }
}
