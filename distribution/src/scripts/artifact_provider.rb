class ArtifactProvider

  def initialize(bundle_name, base_dir)
    @base_bundle_name = bundle_name
    @module_dir = base_dir
    @target_dir = @module_dir + "/target"
    @release_version =  getPomVersion()
    @sourceForgeArtifacts = prepareSourceForgeArtifacts()
    @docs_directory = prepareDocsDirectory()

    puts ""
    puts "Releasing " + @base_bundle_name + " version " + @release_version
    puts ""
    puts " The following SourceForge artifacts will be uploaded:"
    @sourceForgeArtifacts.each { |x| puts "  * " + x }
    puts ""
    puts " The following directory will be synced to JBoss Docs server:"
    puts "  * " + @docs_directory
    puts ""
    puts " Continue [y/N]"
    continueRelease = $stdin.gets
    continueRelease.chomp!
    abort("Aborting release process") unless continueRelease == "y"
  end

  def getReleaseVersion
    return @release_version
  end

  def getSourceForgeArtifacts()
    return @sourceForgeArtifacts
  end

  def getDocsDirectory()
    return @docs_directory
  end

  private

  def prepareDocsDirectory()
    docs_dir = getBundlePath(@target_dir, @base_bundle_name, @release_version, :dir)
    docs_dir << "/"<< @base_bundle_name << "-" << @release_version << "/docs"

    abort("Docs directory " + docs_dir + " cannot be found.") unless File.exist?(docs_dir)
    return docs_dir
  end

  def prepareSourceForgeArtifacts()
    readme = @module_dir + "/../README.md"
    abort(readme + " cannot be found. List of SourceForge artifacts not complete.") unless File.exist?(readme)

    changelog = @module_dir + "/../changelog.txt"
    abort(changelog + " cannot be found. List of SourceForge artifacts not complete.") unless File.exist?(changelog)

    tar_bundle = getBundlePath(@target_dir, @base_bundle_name, @release_version, :tar)
    abort("tar bundle '" + tar_bundle + "' cannot be found. List of SourceForge artifacts not complete.") unless File.exist?(tar_bundle)

    zip_bundle = getBundlePath(@target_dir, @base_bundle_name, @release_version, :zip)
    abort("zip bundle '" + zip_bundle + "' cannot be found. List of SourceForge artifacts not complete.") unless File.exist?(zip_bundle)

    return [readme, changelog, tar_bundle, zip_bundle]
  end

  def getPomVersion()
    pom = @module_dir + "/pom.xml"
    if(!File.file?( pom ))
      abort("Unable to locate pom file")
    end

    f = File.open(pom)
    doc = Nokogiri::XML(f)
    f.close
    doc.remove_namespaces!

    if(!(/hibernate-validator-distribution/ =~ doc.xpath('//artifactId').text))
      abort("Script is not run from the distribution directory")
    end

    return doc.xpath('//parent/version').text
  end

  def getBundlePath(target_dir, project, version, type)
    bundle_name = target_dir + "/" + project + "-" + version + "-dist"
    if(type == :tar)
      bundle_name << ".tar.gz"
    elsif (type == :zip)
      bundle_name << ".zip"
    elsif (type == :dir)
    else
      abort("Wrong bundle type: '" + type + "'")
    end
    return bundle_name
  end
end
