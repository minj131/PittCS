require_relative 'graph'
require 'parallel'

# require 'flamegraph'

# Flamegraph.generate('word_finder.html') do
grapher = Graph.new

# create instance of dictionary which will look for wordlist.txt
# and add words to a set using a seperate thread
dict = []
t1 = Thread.new do
  dict = grapher.create_dict 'wordlist.txt' # use create_dict (for slurping) for hdict
end
# dict = grapher.create_dict # use create_dict (for slurping) for hdict
exit 1 if dict.nil?
t1.join

# create hash from dictionary whos keys are strings sorted by char
# and whose values are arrays containing all words in dict
# which are anagrams of the key

# split dict into 4 approximate equal parts and build seperate hashmaps
subs = dict.each_slice(dict.length / 4).to_a
subs[3].concat subs[4]
subs.pop

hdicts = Parallel.map(subs, in_processes: 8, isolation: true) do |s|
  raise Parallel::Kill if s == 1

  grapher.dict_to_hash(s)
end

# Pass in arg file and read in file to make graph
res = grapher.make_graph(ARGV)
exit 1 if res.nil?

# finds all paths from every node to every end node
paths = grapher.make_paths

# Once we get all possible paths, convert to string
words = grapher.convert_words_and_find(paths, hdicts)

# pwords = grapher.permute(words)
# list = grapher.find_words(pwords, dict)

grapher.print_words(words)
# end
