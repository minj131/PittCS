def check_command_line_args
  unless ARGV.length == 2 && ARGV[0].to_i && ARGV[1].to_i && ARGV[1].to_i >= 0
    puts 'Usage:'
    puts 'ruby gold_rush.rb *seed* *num_prospectors*'
    puts '*seed* should be an integer'
    puts '*num_prospectors* should be a non-negative integer'
    exit 1
  end
  [ARGV[0].to_i, ARGV[1].to_i]
end
