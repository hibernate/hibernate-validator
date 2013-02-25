# Releasing Hibernate Validator

The script in this directory allow you to deploy the release bundles for a Hibernate Validator release
on SourceForge. It also can sync the

## Prerequisites

* ssh key-based authentication to Sourceforge and JBoss docs server
* Ruby >= 1.9
* Bundler

        #install bundler
        > gem install bundler

        # install ruby dependencies (gems) via bundler
        > bundle install

## How to run the script

    > ./release.rb  -s <sourceforge-user>

## Tips & Tricks

* To list all available script options use '-h'

        > ./release.rb -h

## Resources

* [Bundler](http://gembundler.com/)
* [SourceForge sftp](https://sourceforge.net/apps/trac/sourceforge/wiki/SFTP)
* [SourceForge ssh]()
