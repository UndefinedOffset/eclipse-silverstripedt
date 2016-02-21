# SilverStripe supported module skeleton

A useful skeleton to more easily create modules that conform to the [Module Standard]
(https://docs.silverstripe.org/en/3.2/developer_guides/extending/modules/#module-standard).

This readme contains descriptions of the parts of this module base you should customize to meet you own module needs.
For example, the module name in the H1 above should be you own module name, and the description text you are reading now
is where you should provide a good short explanation of what your module does.

Where possible we have included default text that can be included as is into your module and indicated in 
other places where you need to customize it.

Below is a template of the sections of your README.md you should ideally include to met the Module Standard 
and help others make use of your modules.

Make sure if you update your .travis.yml according to your needs, make sure to adjust the last line particularly the ``module/tests`` to point to your module's folder so tests will run. See the SilverStripe [Testing documentation](https://docs.silverstripe.org/en/developer_guides/testing/) for more information.

## Requirements
 * SilverStripe ^3.1
 * Other module
 * Other server requirement
 * Etc

## Installation
Add some installation instructions here, having a 1 line composer copy and paste is useful. 
Here is a composer command to create a new module project. Ensure you read the ['publishing a module']
(https://docs.silverstripe.org/en/developer_guides/extending/how_tos/publish_a_module/) guide update you module 
composer.json to designate your code as a SilverStripe module. 

```
composer create-project your-vendor-name/your-package-name
```

## License
See [License](LICENSE.md)

We have included a 3-clause BSD license you can use as a default. We advocate for the BSD license as 
it is one of the most permissive and open licenses.

Feel free to alter the [LICENSE.md](LICENSE.md) to suit if you want to use an alternative license.
You can use [choosealicense.com](http://choosealicense.com) to help pick a suitable license for your project.

## Documentation
Add links into your docs/<language> folder here unless your module only requires minimal documentation 
in that case, add here and remove the docs folder. You might use this as a quick table of content if you
mhave multiple documentation pages.

## Example configuration (optional)
If your module makes use of the config API in SIlverStripe it's a good idea to provide an example config
 here that will get the module working out of the box and expose the user to the possible configuration options.

Provide a yaml code example where possible.

```yaml

Page:
  config_option: true
  another_config:
    - item1
    - item2
  
```

## Maintainers
 * Person here <person@emailaddress.com>
 * Another maintainer <maintain@emailaddress.com>
 
## Bugtracker
Bugs are tracked in the issues section of this repository. Before submitting an issue please read over 
existing issues to ensure yours is unique. 
 
If the issue does look like a new bug:
 
 - Create a new issue
 - Describe the steps required to reproduce your issue, and the expected outcome. Unit tests, screenshots 
 and screencasts can help here.
 - Describe your environment as detailed as possible: SilverStripe version, Browser, PHP version, 
 Operating System, any installed SilverStripe modules.
 
Please report security issues to the module maintainers directly. Please don't file security issues in the bugtracker.
 
## Development and contribution
If you would like to make contributions to the module please ensure you raise a pull request and discuss with the module maintainers.
