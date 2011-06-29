/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package ca.edchipman.silverstripepdt;

import org.eclipse.php.internal.ui.wizards.NewPhpTemplatesWizardPage;

@SuppressWarnings("restriction")
public class NewSilverStripeTemplatesWizardPage extends NewPhpTemplatesWizardPage {
	public static final String NEW_SS_TEMPLATE_CONTEXTTYPE="php_ss";
	
	public NewSilverStripeTemplatesWizardPage() {
		super();
	}

	protected String getTemplateContextTypeId() {
		return NewSilverStripeTemplatesWizardPage.NEW_SS_TEMPLATE_CONTEXTTYPE;
	}
}
