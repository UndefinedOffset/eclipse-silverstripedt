Eclipse SilverStripe Development Tools
======================
Adds templates and other tools for working with SilverStripe in Eclipse. Although SilverStripe 3.1 is supported there are a few issues see [issue #3](https://github.com/UndefinedOffset/eclipse-silverstripedt/issues/3).

 
### Requirements:
* [Eclipse Indigo (3.7.2)](http://archive.eclipse.org/eclipse/downloads/drops/R-3.7.2-201202080800/)
* [Eclipse PDT 3.0.1] (http://www.eclipse.org/downloads/download.php?file=/tools/pdt/downloads/drops/3.0.0/M201201110400/pdt-SDK-M201201110400.zip)
* [Eclipse WST 3.3.2] (http://download.eclipse.org/webtools/downloads/drops/R3.3.2/R-3.3.2-20120210195245/)

Plugin may work with newer versions of the above but that is my current testing environment so I know it works there.

### Eclipse Update Site URL: 
http://silverstripedt.edchipman.ca/indigo/

---

#### Features
* New SilverStripe Project Wizard
  * 3 Project Layouts including fully functional starter structure and files for mysite, module and theme projects
  * 4 SilverStripe Versions (3.1, 3.0, 2.3, and 2.4)
* New SilverStripe Template Wizard
  * By default with 3 code templates one for top level, layout and a blank file, with minor variations for the 2.x versions of SilverStripe
* Built in auto complete for Framework (sapphire prior to SilverStripe 3.x) and CMS classes and functions
* Optional project configuration for using framework only (SilverStripe 3.x only)
* Additional Code Template Variable ${file_name}
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
 * All code templates can be edited per the users preference
 * SilverStripe Template Syntax Highlighting
 * Dev/Build Viewer, accessible via a Toolbar Button, or keyboard shortcut (ALT+SHIFT+X, B)
