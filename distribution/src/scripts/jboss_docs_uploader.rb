class JBossDocsUploader

  def initialize(local_docs_dir, release_version)
    @docs = local_docs_dir
    @version = release_version[0..2]
    @base_dir = "/docs_htdocs/hibernate/validator"
    @remote = @base_dir + "/" + @version

    puts " Docs upload to " + @remote
    puts " Continue [y/N]"
    continueRelease = $stdin.gets
    continueRelease.chomp!
    abort("Aborting release process") unless continueRelease == "y"
  end

  def put
    user = "hibernate"
    host = "filemgmt.jboss.org"

    Net::SFTP.start(host, user) do |sftp|
      puts "Preparing to upload docs from: " + @docs
      begin
        sftp.stat!(@remote)
        puts "Deleting existing remote path: " + @remote
        recursive_delete(sftp, @remote)
      rescue Net::SFTP::StatusException => e
        raise unless e.code == 2
        puts "Creating remote path: " + @remote
        sftp.mkdir!(@remote)
      end
      puts "Uploading docs"
      sftp.upload!(@docs, @remote) do |event, uploader, *args|
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
      puts "Docs upload complete"
    end
  end

  private

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
