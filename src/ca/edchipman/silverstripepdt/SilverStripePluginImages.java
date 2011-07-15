package ca.edchipman.silverstripepdt;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;

public class SilverStripePluginImages {
    public static final ImageDescriptor DESC_ADD_SS_FILE = create("newssfile_wiz.gif","wizban");//$NON-NLS-1$
    public static final ImageDescriptor DESC_NEW_SS_PROJECT = create("newssprj_wiz.png","wizban");//$NON-NLS-1$
    
    private static ImageDescriptor create(String name, String type) {
        try {
            return ImageDescriptor.createFromURL(makeIconFileURL(name, type));
        } catch (MalformedURLException e) {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    private static URL makeIconFileURL(String name, String type)
        throws MalformedURLException {
            URL fgIconBaseURL=Platform.getBundle(Activator.PLUGIN_ID).getEntry("/icons/full/"+type+"/");
            if (fgIconBaseURL == null)
                throw new MalformedURLException();
    
            return new URL(fgIconBaseURL, name);
        }
}
