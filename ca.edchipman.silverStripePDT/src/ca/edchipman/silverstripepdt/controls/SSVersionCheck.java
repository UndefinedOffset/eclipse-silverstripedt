package ca.edchipman.silverstripepdt.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SSVersionCheck extends Composite {
    private String _ssVersion;
    private Button fButton;
    
    /**
     * Initializes the radio pre-setting the SilverStripe version code
     * @param parent Parent composite
     * @param version Version code to use i.e SS3.1
     * @constructor
     */
    public SSVersionCheck(Composite parent, String version) {
        super(parent, SWT.NULL);
        
        this.setFont(parent.getFont());
        this.setLayout(new FillLayout());
        
        fButton=new Button(this, SWT.CHECK);
        
        this._ssVersion=version;
    }
    
    /**
     * Initializes the radio without setting the SilverStripe version code
     * @param parent Parent composite
     * @constructor
     */
    public SSVersionCheck(Composite parent) {
        super(parent, SWT.CHECK);
        
        fButton=new Button(this, SWT.CHECK);
    }
    
    /**
     * Sets the SilverStripe version code
     * @param version Version code to use i.e SS3.1
     */
    public void setSSVersion(String version) {
        this._ssVersion=version;
    }
    
    /**
     * Gets the SilverStripe version code
     * @return Version code i.e SS3.1
     */
    public String getSSVersion() {
        return this._ssVersion;
    }
    
    /**
     * Wrapper for Button.setText()
     * @param text Text to use on the button
     */
    public void setText(String text) {
        this.fButton.setText(text);
    }
    
    /**
     * Wrapper for Button.getText()
     * @return Button text
     */
    public String getText() {
        return this.fButton.getText();
    }
    
    /**
     * Wrapper for Button.setSelection()
     * @param value Selected or not
     */
    public void setSelection(boolean value) {
        this.fButton.setSelection(value);
    }
    
    /**
     * Wrapper for Button.getSelection()
     * @return Selected or not
     */
    public boolean getSelection() {
        return this.fButton.getSelection();
    }
    
    /**
     * Wrapper for Button.setSelection()
     * @param value Enabled or not
     */
    public void setEnabled(boolean value) {
        this.fButton.setEnabled(value);
    }
    
    /**
     * Wrapper for Button.getEnabled()
     * @return Enabled or not
     */
    public boolean getEnabled() {
        return this.fButton.getEnabled();
    }
}
