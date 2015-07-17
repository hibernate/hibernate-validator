class JBossDocsUploader

  def initialize(local_docs_dir, release_version)
    @docs = local_docs_dir
    @version = release_version[0..2]
    @versioned_validator_dir = "/docs_htdocs/hibernate/validator/" + @version
    @stable_validator_dir = "/docs_htdocs/hibernate/stable/validator"

    puts " Docs upload to " + @versioned_validator_dir + " [y/N]"
    continueRelease = $stdin.gets
    continueRelease.chomp!
    abort("Aborting release process") unless continueRelease == "y"

    puts " Upload to stable " + @stable_validator_dir + " [y/N]"
    @uploadToStable = $stdin.gets
    @uploadToStable.chomp!
  end

  def put
    sync_remote(@versioned_validator_dir)
    if @uploadToStable == "y"
      sync_remote(@stable_validator_dir)
    end
  end

  private
  def sync_remote(remote_dir)
    user = "hibernate"
    host = "filemgmt.jboss.org"

    Net::SFTP.start(host, user) do |sftp|
      puts "Preparing to upload docs from: " + @docs
      begin
        sftp.stat!(remote_dir)
        puts "Deleting existing remote path: " + remote_dir
        recursive_delete(sftp, remote_dir)
      rescue Net::SFTP::StatusException => e
        raise unless e.code == 2
        puts "Creating remote path: " + remote_dir
        sftp.mkdir!(remote_dir)
      end
      puts "Uploading docs"
      sftp.upload!(@docs, remote_dir) do |event, uploader, *args|
        case event
        when :open then
          # args[0] : file metadata
          puts "#{args[0].local} -> #{args[0].remote} (#{args[0].size} bytes}"
        when :put then
          # args[0] : file metadata
          # args[1] : byte offset in remote file
          # args[2] : data being written (as string)
          #puts "writing #{args[2].length} bytes to #{args[0].remote} starting at #{args[1]}"
        when :close then
          # args[0] : file metadata
          #puts "finished with #{args[0].remote}"
        when :finish then
          puts ""
          puts ""
        end
      end
      puts "Docs upload complete"
    end
  end

  def recursive_delete(sftp, dir)
    handle = sftp.opendir!(dir)

    while (items = sftp.readdir!(handle)) do
        items.each { |item|
          full_path = dir + "/" + item.name
          if item.directory?
            if item.name != '.' && item.name != '..'
              recursive_delete(sftp, full_path)
              sftp.rmdir!(full_path)
            end
          else
            sftp.remove!(full_path)
          end
        }
      end
      sftp.close(handle)
    end
  end
