package ca.edchipman.silverstripepdt.wizards;

import org.eclipse.php.internal.ui.wizards.PHPFileCreationWizard;


@SuppressWarnings("restriction")
public class SilverStripeTemplateFileCreationWizard extends PHPFileCreationWizard {
	/**
	 * Constructor for SampleNewWizard.
	 */
	public SilverStripeTemplateFileCreationWizard() {
		super();
		setWindowTitle("New SilverStripe Template File"); //$NON-NLS-1$
	}
	
	public void addPages() {
		phpFileCreationWizardPage = new SilverStripeFileCreationWizardPage(selection);
		addPage(phpFileCreationWizardPage);

		newPhpTemplatesWizardPage = new NewSilverStripeTemplatesWizardPage();
		addPage(newPhpTemplatesWizardPage);
	}
}
