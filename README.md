Eclipse SilverStripe Development Tools
======================
Adds templates and other tools for working with SilverStripe in Eclipse.

 
### Requirements:
* [Eclipse Kepler (4.3.2)](http://eclipse.org/kepler/)
* [Eclipse PDT 3.2.0] (http://www.eclipse.org/pdt/downloads/)
* [Eclipse WST 3.5.2] (http://eclipse.org/webtools/releases/3.5.2/)
* [YEdit] (https://marketplace.eclipse.org/content/yedit)

Plugin may work with newer versions of the above but that is my current testing environment so I know it works there.

### Eclipse Update Site URL:
http://silverstripedt.edchipman.ca/

---

#### Features
* New SilverStripe Project Wizard
  * 3 Project Layouts including fully functional starter structure and files for mysite, module and theme projects
  * 6 SilverStripe Versions available for install (3.3, 3.2, 3.1, 3.0, 2.4, and 2.3)
* New SilverStripe Template Wizard
  * By default with 3 code templates one for top level, layout and a blank file, with minor variations for the 2.x versions of SilverStripe
* New SilverStripe class wizard
  * Create from a stub template or create from a parent class and/or interface
* Built in auto complete for Framework (sapphire prior to SilverStripe 3.x) and CMS classes and functions
* Optional project configuration for using framework only (SilverStripe 3.x only)
* Additional Code Template Variable ${file_name}
* New module project based on the [SilverStripe module standard](https://github.com/silverstripe/silverstripe-module)
* 16 code templates
  * New File Templates
    * Page Type
    * Data Object
    * Content Controller
    * SilverStripe 2.x language files
    * Extension (SilverStripe 3.x and 2.x variants)
    * Data Object Decorator
    * Data Extension (SilverStripe 3.x)
    * General Class
    * Top Level SilverStripe Template
    * SilverStripe Layout Template
    * Blank SilverStripe Template
  * Auto Complete Templates
    * Page Type
    * Data Object
    * Content Controller
    * SilverStripe 2.x language files
    * Extension (SilverStripe 3.x and 2.x variants)
    * Data Object Decorator
    * Data Extension (SilverStripe 3.x)
    * General Class
    * getCMSFields() stub (SilverStripe 3.x and 2.x variants)
    * Object::add_extension
    * Object::remove_extension
    * SilverStripe 2.x language file line
    * updateCMSFields() stub (SilverStripe 3.x and 2.x variants)
    * updateCMSActions() stub
    * Top Level SilverStripe Template
    * SilverStripe Layout Template
    * Blank SilverStripe Template
    * Pagination Template (SilverStripe 3.x only)
* All code templates can be edited per the users preference
* SilverStripe Template Syntax Highlighting
* Dev/Build Viewer, accessible via a Toolbar Button, or keyboard shortcut (ALT+SHIFT+X, B)
* Tasks and Unit Test views
__And More__