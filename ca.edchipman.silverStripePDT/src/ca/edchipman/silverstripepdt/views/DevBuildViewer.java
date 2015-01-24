package ca.edchipman.silverstripepdt.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.part.ViewPart;

public class DevBuildViewer extends ViewPart {
    public static final String ID = "ca.edchipman.silverstripepdt.views.DevBuildViewer"; //$NON-NLS-1$
    
    private Browser swtBrowser;
    private ProgressBar progressBar;
    private String targetURL;
    private Control oldFocus;

    private BuildProgressListener progressListener;
    
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = true;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);
        RowLayout rowLayout = new RowLayout();
        rowLayout.spacing = 1;

        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        
        
        GridData progressLayout = new GridData();
        progressLayout.horizontalAlignment=SWT.FILL;
        
        progressBar=new ProgressBar(container, SWT.SMOOTH | SWT.HORIZONTAL);
        progressBar.setVisible(false);
        progressBar.setLayoutData(progressLayout);
        progressBar.setMinimum(0);
        
        try {
            swtBrowser = new Browser(container, SWT.NONE);
            swtBrowser.setLayoutData(gridData);
            if(targetURL!=null && targetURL.length()>0) {
                oldFocus=Display.getCurrent().getFocusControl();
                
                swtBrowser.setUrl(targetURL);
                
                if(oldFocus.isDisposed()==false) {
                    oldFocus.setFocus();
                }
                
                oldFocus=null;
            }
            
            progressListener=new BuildProgressListener(swtBrowser, progressBar);
            swtBrowser.addProgressListener(progressListener);
        } catch (SWTError error) {
            swtBrowser.removeProgressListener(progressListener);
            swtBrowser = null;
            Label label = new Label(container, SWT.WRAP);
            label.setText("SWT Browser control is not available. Please refer to: http://www.eclipse.org/swt/faq.php#whatisbrowser for more information.");
            label.setLayoutData(gridData);
        }
    }
    
    /**
     * Sets the target url for the browser
     * @param url URL to navigate to
     */
    public void setTargetURL(String url) {
        targetURL=url;
        
        if(swtBrowser!=null) {
            oldFocus=Display.getCurrent().getFocusControl();
            
            swtBrowser.setUrl(url);
            
            if(oldFocus.isDisposed()==false) {
                oldFocus.setFocus();
            }
            
            oldFocus=null;
            
            if(progressListener!=null) {
                progressListener.resetProgressBar();
            	progressListener.showProgressBar();
            }
        }
    }
    
    /**
     * Gets the current target url
     * @return Target url for the browser
     */
    public String getTargetURL() {
        return targetURL;
    }
    
    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }
    
    
    
    class BuildProgressListener implements ProgressListener {
        private ProgressBar progress;
        private Browser browser;

        /**
         * @param bar Progress bar to update
         */
        public BuildProgressListener(Browser browser, ProgressBar bar) {
            this.browser=browser;
            progress = bar;
        }
        
        /**
         * Handles when the progress changes
         * @param ProgressEvent Progress event data
         */
        public void changed(ProgressEvent event) {
            if(progressBar.isDisposed()==false && event.total>0) {
                if(event.total>progress.getMaximum()) {
                    progressBar.setMaximum(event.total);
                }
                
                if(event.current<progress.getMaximum()) {
                    progress.setVisible(true);
                    progress.setSelection(event.current);
                }else {
                    progress.setSelection(event.current);
                }
            }
        }
        
        /**
         * Handles when the progress completes
         * @param ProgressEvent Progress event data
         */
        public void completed(ProgressEvent event) {
            progress.setVisible(false);
            progress.setMaximum(1);
            progress.setSelection(0);
            browser.evaluate("var links=document.getElementsByTagName('a');" +
                            "for(var i=0;i<links.length;i++) { "+
                                "links[i].href='#'; "+
                            "}");
        }
        
        /**
         * Gets the currently active progress bar instance
         */
        public void showProgressBar() {
        	progress.setVisible(true);
        }
        
        /**
         * Resets the currently active progress bar instance
         */
        public void resetProgressBar() {
            progress.setSelection(0);
        }
    }
}
