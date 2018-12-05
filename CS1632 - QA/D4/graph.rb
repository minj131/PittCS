require 'set'

# Contains methods for the word finder application
class Graph
  attr_accessor :nmap, :nnmap, :ends, :paths

  def initialize
    # contains mapping for nodes to letter
    @nmap = {}
    # contains mapping for nodes to neighbors
    @nnmap = {}
    # contains end nodes
    @ends = []
    # contains all paths
    @paths = Set.new
  end

  # slurps wordlist.txt splits file into array of words
  # returns dict
  def create_dict(file)
    # Since wordlist is constant
    if File.file?(file)
      IO.readlines(file, chomp: true)
    else
      puts 'File not found!'
    end
  end

  # for each puts words into a set for faster access
  def create_dict_set(file)
    dict = Set.new
    if File.file?(file)
      File.foreach(file) do |line|
        dict << line.chomp
      end
    else
      puts 'File not found!'
    end
    dict
  end

  # takes in dict and converts to a hashed dict
  def dict_to_hash(dict)
    nil if dict.nil?
    # something is wrong with this|| dict.empty?
    hdict = dict.each_with_object(Hash.new { |h, k| h[k] = [] }) do |w, h|
      h[w.each_char.sort.join] << w
      # need to maybe add a rescue to easily test files with numbers
    end
    hdict.default = nil
    hdict
  end

  # read in file here
  def make_graph(args)
    # raise TypeError unless args[0].is_a? String
    # check command args
    if args.length == 1
      # check if file
      if File.file?(args[0])
        lines = IO.readlines(args[0])

        # tokens: ID, LETTER, NEIGHBORS
        lines.each do |line|
          parse_line_tokens(line)
        end
      else
        puts 'File not found!'
        nil
      end
    else
      puts 'Usage:'
      puts 'ruby word_finder.rb *name_of_file*'
      puts '*name_of_file* should be a file'
      nil
    end
  end

  # util for parsing each line to tokens
  def parse_line_tokens(line)
    return if line.nil? || line.empty? || !line.include?(';')

    tokens = line.split(';')
    id = tokens[0]
    letter = tokens[1]
    neighbors = tokens[2]

    # create mapping id to letter
    @nmap[id] = letter

    # if neighbors is empty, we reached a end node
    @ends << id if neighbors.strip.empty?

    # add neighbors if neighbors for that id exist
    @nnmap[id] = neighbors.strip.split(',')
  end

  # want to do a dfs of a given graph
  # keep a store of visited nodes and add to list of paths once we reach an end node
  # then backtrack until we reach another node
  # do this for all nodes
  def get_allpaths(source, dest, visited, path)
    # mark visited
    visited[source] = 1
    path << source

    if source.eql? dest
      @paths << path.dup
    else
      # recurse for all neighboring nodes
      @nnmap[source].each do |n|
        get_allpaths(n, dest, visited, path) if visited[n].eql? 0
      end
    end

    path.pop
    visited[source] = 0
  end

  # method caller for paths finder for graph
  def make_paths
    visited = {}
    path = []
    return path if @nmap.empty? || @nnmap.empty?

    # 0 is false
    @nmap.each do |k, _|
      visited[k] = 0
    end

    # for each node that is not an end node to an end node
    @nnmap.each do |s, _|
      # if s is an end node
      @paths << [s] if @ends.include? s

      # for each end node as desintation
      @ends.each do |d|
        get_allpaths(s, d, visited, path)
      end
    end
    @paths.sort_by(&:length).reverse
  end

  # Now that we have the node ids for all the possible paths
  # Convert the ids to letters using its hashed value
  # using longest word first, find the words that exist in hdicts
  def convert_words_and_find(paths, hdicts)
    return nil if paths.nil?
    return [] if paths.empty?

    list = []
    size = -1
    paths.each do |path|
      word = convert_word(path)
      # return nil here since there is no node mapping for an invalid node
      return nil if word.nil?
      # return early if we get to a word that's smaller than the largest size word that is found
      return list.sort if word.length < size

      # using hashed dict instead of converting all words
      # simply take each word and check if exists and append to list of found words
      # and return once we try a word that's less than biggest size
      hdicts.each do |h|
        key = word.downcase.chars.sort.join
        values = h.key?(key) ? (h[key] - [word]) : []

        # if we reach a word, set size to that length
        unless values.empty?
          size = values[0].length
          list.concat values
        end
      end
    end
    list.sort
  end

  # helper method for convert_allwords
  def convert_word(arr)
    return nil if arr.nil?
    return [] if arr.empty?

    word = ''
    arr.each do |n|
      return nil unless @nmap.key? n

      word += @nmap[n]
    end
    word
  end

  # TODO: Find all permutations of words using threads or something
  # as ruby times out for ultra_big_graph
  def permute(words)
    return nil if words.nil?
    return [] if words.empty?

    pwords = []
    words.each do |w|
      list = w.chars.to_a.permutation.map(&:join)
      pwords.concat list
    end
    pwords
  end

  # For each word, find if word exists in dict set
  def find_words(words, dict)
    return nil if words.nil? || dict.nil?
    return [] if words.empty? || dict.empty?

    size = -1
    list = Set.new

    words.each do |w|
      if dict.include? w.downcase
        break if w.length < size

        size = w.length
        list << w
      end
    end
    list
  end

  # method util to print words
  def print_words(list)
    return nil if list.nil?

    puts 'Longest valid word(s):'
    list.each do |w|
      puts w.upcase
    end
  end
end
