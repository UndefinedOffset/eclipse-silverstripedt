package ca.edchipman.silverstripepdt.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.wizards.PHPFileCreationWizardPage;
import org.eclipse.swt.widgets.Composite;

import ca.edchipman.silverstripepdt.SilverStripePluginImages;

@SuppressWarnings("restriction")
public class SilverStripeFileCreationWizardPage extends PHPFileCreationWizardPage {
	public SilverStripeFileCreationWizardPage(ISelection selection) {
		super(selection);
		
		setTitle("New SilverStripe Template File"); //$NON-NLS-1$
		setDescription("Create a new SilverStripe template file"); //$NON-NLS-1$
		setImageDescriptor(SilverStripePluginImages.DESC_ADD_SS_FILE);
	}
	
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(final Composite parent) {
		super.createControl(parent);
		
		setInitialFileName("newfile.ss");
	}
	
	/**
	 * Ensures that both text fields are set.
	 */
	protected void dialogChanged() {
		final String container = getContainerName();
		final String fileName = getFileName();

		if (container.length() == 0) {
			updateStatus(PHPUIMessages.PHPFileCreationWizardPage_10); //$NON-NLS-1$
			return;
		}
		final IContainer containerFolder = getContainer(container);
		if (containerFolder == null || !containerFolder.exists()) {
			updateStatus(PHPUIMessages.PHPFileCreationWizardPage_11); //$NON-NLS-1$
			return;
		}
		if (!containerFolder.getProject().isOpen()) {
			updateStatus(PHPUIMessages.PHPFileCreationWizardPage_12); //$NON-NLS-1$
			return;
		}
		if (fileName != null
				&& !fileName.equals("") && containerFolder.getFile(new Path(fileName)).exists()) { //$NON-NLS-1$
			updateStatus(PHPUIMessages.PHPFileCreationWizardPage_14); //$NON-NLS-1$
			return;
		}

		int dotIndex = fileName.lastIndexOf('.');
		if (fileName.length() == 0 || dotIndex == 0) {
			updateStatus(PHPUIMessages.PHPFileCreationWizardPage_15); //$NON-NLS-1$
			return;
		}

		if (dotIndex != -1) {
			String fileNameWithoutExtention = fileName.substring(0, dotIndex);
			for (int i = 0; i < fileNameWithoutExtention.length(); i++) {
				char ch = fileNameWithoutExtention.charAt(i);
				if (!(Character.isJavaIdentifierPart(ch) || ch == '.' || ch == '-')) {
					updateStatus(PHPUIMessages.PHPFileCreationWizardPage_16); //$NON-NLS-1$
					return;
				}
			}
		}

		final IContentType contentType = Platform.getContentTypeManager().getContentType("ca.edchipman.silverstripepdt.SilverStripeTemplateSource");
		if (!contentType.isAssociatedWith(fileName)) {
			StringBuffer buffer = new StringBuffer(PHPUIMessages.PHPFileCreationWizardPage_17); //$NON-NLS-1$
			buffer.append(".ss");
			buffer.append("]"); //$NON-NLS-1$
			updateStatus(buffer.toString());
			return;
		}

		updateStatus(null);
	}
}
