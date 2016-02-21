# See https://github.com/silverstripe-labs/silverstripe-travis-support for setup details

sudo: false

language: php

php:
  - 5.3
  - 5.4
  - 5.5
  - 5.6

env:
  - DB=MYSQL CORE_RELEASE=3.2

matrix:
  include:
    - php: 5.6
      env: DB=MYSQL CORE_RELEASE=3.0
    - php: 5.6
      env: DB=MYSQL CORE_RELEASE=3.1
    - php: 5.6
      env: DB=PGSQL CORE_RELEASE=3.2

before_script:
  - composer self-update || true
  - git clone git://github.com/silverstripe-labs/silverstripe-travis-support.git ~/travis-support
  - php ~/travis-support/travis_setup.php --source `pwd` --target ~/builds/ss
  - cd ~/builds/ss
  - composer install

script:
  - vendor/bin/phpunit module/tests