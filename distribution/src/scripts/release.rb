#!/usr/bin/env ruby
# encoding: UTF-8

require "rubygems"
require "bundler/setup"

require 'choice'
require 'nokogiri'
require 'net/sftp'

require_relative 'artifact_provider'
require_relative 'source_forge_uploader'
require_relative 'jboss_docs_uploader'

Choice.options do
  header 'Application options:'

  separator 'Required:'

  option :sourceforge_user, :required => true do
    short '-s'
    long '--sourceforge-user=<user>'
    desc 'The SourceForge user name'
  end

  separator 'Common:'

  option :help do
    short '-h'
    long '--help'
    desc 'Show this message.'
  end
end

artifactProvider = ArtifactProvider.new("hibernate-validator", "../..")

# Upload to SourceForge
sourceForgeUploader = SourceForgeUploader.new( artifactProvider.getSourceForgeArtifacts, Choice.choices.sourceforge_user, artifactProvider.getReleaseVersion )
sourceForgeUploader.put

# Upload to JBoss Docs server
jbossUploader = JBossDocsUploader.new( artifactProvider.getDocsDirectory, artifactProvider.getReleaseVersion )
jbossUploader.put
