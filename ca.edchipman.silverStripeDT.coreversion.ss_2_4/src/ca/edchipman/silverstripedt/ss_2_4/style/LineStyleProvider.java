package ca.edchipman.silverstripedt.ss_2_4.style;

import org.eclipse.jface.text.TextAttribute;

import ca.edchipman.silverstripepdt.regions.SilverStripeRegionContext;
import ca.edchipman.silverstripepdt.style.IStyleConstantsSS;
import ca.edchipman.silverstripepdt.versioninterfaces.SilverStripeVersionStyleProvider;

public class LineStyleProvider extends SilverStripeVersionStyleProvider {
    @Override
    public void loadVersionColors() {
        //Remove loop and with block as well as i18n tag support, since it's not supported in 2.4
        this.lineStyleProvider.removeTextAttribute(IStyleConstantsSS.SS_LOOP_OPEN);
        this.lineStyleProvider.removeTextAttribute(IStyleConstantsSS.SS_LOOP_CONTENT);
        this.lineStyleProvider.removeTextAttribute(IStyleConstantsSS.SS_END_LOOP);
        this.lineStyleProvider.removeTextAttribute(IStyleConstantsSS.SS_WITH_OPEN);
        this.lineStyleProvider.removeTextAttribute(IStyleConstantsSS.SS_WITH_CONTENT);
        this.lineStyleProvider.removeTextAttribute(IStyleConstantsSS.SS_END_WITH);
        this.lineStyleProvider.removeTextAttribute(IStyleConstantsSS.SS_I18N_OPEN);
        this.lineStyleProvider.removeTextAttribute(IStyleConstantsSS.SS_I18N_CONTENT);
        
        
        //Add control block support
        this.lineStyleProvider.addTextAttribute(IStyleConstantsSS.SS_CONTROL_OPEN);
        this.lineStyleProvider.addTextAttribute(IStyleConstantsSS.SS_CONTROL_CONTENT);
        this.lineStyleProvider.addTextAttribute(IStyleConstantsSS.SS_END_CONTROL);
    }

    @Override
    public TextAttribute getAttributeFor(String type) {
        if (type == SilverStripeRegionContext.SS_CONTROL_OPEN) {
            return (TextAttribute)this.lineStyleProvider.getTextAttributes().get(IStyleConstantsSS.SS_CONTROL_OPEN);
        } else if (type == SilverStripeRegionContext.SS_CONTROL_CONTENT) {
            return (TextAttribute)this.lineStyleProvider.getTextAttributes().get(IStyleConstantsSS.SS_CONTROL_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_END_CONTROL) {
            return (TextAttribute)this.lineStyleProvider.getTextAttributes().get(IStyleConstantsSS.SS_END_CONTROL);
        }
        
        return null;
    }
}
