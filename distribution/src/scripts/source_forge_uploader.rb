class SourceForgeUploader

  def initialize(upload_artifacts, sourceforge_user, release_version)
    @artifacts = upload_artifacts
    @user = sourceforge_user
    @version = release_version
    @base_dir = "/home/frs/project/hibernate/hibernate-validator"
    @release_dir = @base_dir + "/" + @version

    puts " SourceFroge upload to " + @release_dir
    puts " Continue [y/N]"
    continueRelease = $stdin.gets
    continueRelease.chomp!
    abort("Aborting release process") unless continueRelease == "y"
  end

  def put
    # SourceForge
    frs_user = @user + ",hibernate"
    frs_host = "frs.sourceforge.net"
    Net::SFTP.start(frs_host, frs_user) do |sftp|
      begin
        sftp.stat!(@release_dir)
        abort("Remote directory " + @release_dir + " exists")
      rescue Net::SFTP::StatusException => e
        raise unless e.code == 2
      end

      sftp.mkdir! @release_dir

      @artifacts.each do |artifact|
        local = artifact
        remote = @release_dir + "/" + File.basename(local)
        puts "Uploading " + File.basename(local)
        sftp.upload!(local, remote)do |event, uploader, *args|
          case event
          when :open then
            # args[0] : file metadata
            puts "starting upload: #{args[0].local} -> #{args[0].remote} (#{args[0].size} bytes}"
          when :put then
            # args[0] : file metadata
            # args[1] : byte offset in remote file
            # args[2] : data being written (as string)
            puts "writing #{args[2].length} bytes to #{args[0].remote} starting at #{args[1]}"
          when :close then
            # args[0] : file metadata
            puts "finished with #{args[0].remote}"
          when :finish then
            puts ""
            puts ""
          end
        end
      end
    end
    puts "SourceForge upload complete"
  end
end
