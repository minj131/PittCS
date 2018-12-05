require 'minitest/autorun'
require_relative 'graph'
require 'set'

# MiniTest testing class for graph.rb
class GraphTest < MiniTest::Test
  # setup method so we don't have to create a new Graph object for every test
  def setup
    # create graph object for each test
    @grapher = Graph.new
  end

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD create_dict
  # Equivalence classes:
  #   wordlist.txt does not exist -> returns nil else returns array of words
  #   wordlist.txt exists -> return 

  # return a print statement if the file does not exist
  def test_valid_create_dict
    assert_output ("File not found!\n") {@grapher.create_dict('hi.txt')}
  end

  # raise an exception with an integer as a filename
  def test_integer_create_dict
    assert_raises "TypeError: Integer as filename." do
      @grapher.create_dict(1)
    end
  end

  # if file exists, verify method output is not nil
  def test_create_dict_initialize
    refute_nil @grapher.create_dict 'wordlist.txt'
  end

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD create_dict_set
  # Equivalence classes:
  #   wordlist.txt does not exist -> returns nil else verify not nil
  #   wordlist contains duplicates

  # true if create_dict_set method returns a kind of Set
  def test_create_dict_set_is_a_set
    assert_kind_of Set, @grapher.create_dict_set('wordlist.txt')
  end

  # if file exists, dict length will be greater
  def test_create_dict_set_initialized
    refute_nil @grapher.create_dict_set('wordlist.txt')
  end

  # if file does not exist, match output 'File not found!\n'
  def test_create_dict_set_not_initialized
    assert_output ("File not found!\n") {@grapher.create_dict_set('hi.txt')}
  end

  # raise an exception with an integer as a filename
  def test_create_dict_set_integer
    assert_raises "TypeError: Integer as filename." do
      @grapher.create_dict_set(1)
    end
  end

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD dict_to_hash
  # Equivalence classes:
  #  dict is empty -> hashed dict length will be 0
  #    otherwise hashed dict length will be greater than 0
  #  dict is nil -> returns nil if dict object is nil

  # test will pass if output is a Hash, fail otherwise
  def test_valid_string_dict_to_hash
    assert_kind_of Hash, @grapher.dict_to_hash(Set.new())
  end

  # if dict is empty, hash length will be 0
  def test_invalid_empty_dict_to_hash
    assert @grapher.dict_to_hash(Set.new([])).size.zero?
  end

  # if dict is not empty, hash length will be greater than 0
  def test_dict_to_hash_not_empty
    assert @grapher.dict_to_hash(Set.new(['a', 'bd'])).size > 0
  end

  # if dict contains integers, raise exception
  def test_dict_to_hash_not_empty
    assert_raises "Error: Invalid integer in wordlist.txt" do
      @grapher.dict_to_hash(Set.new([1]))
    end
  end

  # TODO: need test method for anagram

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD make_graph
  # Equivalence classes:
  #  args is empty or is not length 1 -> returns nil and informs of proper usage
  #  if file does not exist -> informs file was not found
  #  

  # if file is valid verify output will not be nil
  def test_valid_file_correct_args_length
    refute_nil @grapher.make_graph(['wordlist.txt'])
  end

  # if no command line arguments then match statement
  def test_invalid_no_arguments
    assert_output("Usage:\nruby word_finder.rb *name_of_file*\n*name_of_file* should be a file\n") {@grapher.make_graph([])}
  end

  # if file is not found then match the statement
  def test_invalid_file_correct_args_length
    assert_output("File not found!\n") {@grapher.make_graph(['hi.txt'])}
  end

  # raise exception if integer is passed in as an argument
  def test_invalid_integer_argument
    assert_raises "TypeError: Invalid command line arguments." do
      @grapher.make_graph([1])
    end
  end

  # test 1 string and 1 integer argument
  def test_string_and_integer_argument
    assert_output("Usage:\nruby word_finder.rb *name_of_file*\n*name_of_file* should be a file\n") {@grapher.make_graph(['wordlist.txt', 1])}
  end

  # test 2 in valid string arguments
  def test_two_string_invalid_args_length
    assert_output("Usage:\nruby word_finder.rb *name_of_file*\n*name_of_file* should be a file\n") {@grapher.make_graph(['wordlist.txt', 'hi'])}
  end

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD parse_line_tokens
  # Equivalence classes:
  #  line is empty or nil -> returns (i.e. skip)
  #  line is wrong format -> returns (i.e. skip)
  #  line does not include ';' -> returns (i.e. skip)
  #  otherwise will split and parse the tokens and update data structures

  def test_parse_line_empty
    assert_nil @grapher.parse_line_tokens('')
  end

  def test_parse_line_nil
    assert_nil @grapher.parse_line_tokens(nil)
  end

  def test_parse_line_wrong_format
    assert_nil @grapher.parse_line_tokens('hi')
  end

  def test_parse_line_correct_format
    assert_output () {@grapher.parse_line_tokens(['1;C;2,3'])}
    # id = @nmap['1']
    # ends = @ends
    # nnmap = @nnmap
    # assert_equal 
  end

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD make_paths
  # Equivalence classes:
  # 
  def test_make_paths_nmap_empty
    @grapher.nmap = []
    assert_equal [], @grapher.make_paths
  end

  def test_make_paths_nnmap_empty
    @grapher.nnmap = []
    assert_equal [], @grapher.make_paths
  end


  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD convert_allwords
  # Equivalence classes:
  #   paths is empty -> returns an empty list
  #   paths is nil -> returns nil
  #   otherwise returns a sorted list of words with longest strings first

  # tests when paths are nil
  def test_convert_words_and_find_nil
    paths = nil
    hdicts = [{'ahy'=>['hay','yah']}, {'ab'=>['ab','ba']}]
    assert_nil @grapher.convert_words_and_find(paths, hdicts)
  end

  # tests when paths are empty
  def test_convert_words_and_find_empty
    paths = []
    hdicts = [{'ahy'=>['hay','yah']}, {'ab'=>['ab','ba']}]
    assert_equal [], @grapher.convert_words_and_find(paths, hdicts)
  end

  # returns sorted list of words found via mapping
  def test_convert_words_and_find_valid
    @grapher.nmap = {'1'=>'a', '2'=>'b', '3'=>'c'}
    paths = [['1','2','3','1'], ['3','2','1']]
    hdicts = [{'ahy'=>['hay','yah']}, {'abc'=>['cab','bac']}]
    exp = ['bac','cab']
    assert_equal exp, @grapher.convert_words_and_find(paths, hdicts)
  end

  # should return nil if an invalid node to mapping is found
  def test_convert_words_and_find_invalid
    @grapher.nmap = {'1'=>'a', '2'=>'b', '3'=>'c'}
    paths = [['1','2','4','1'], ['3','2','1']]
    hdicts = [{'ahy'=>['hay','yah']}, {'ab'=>['ab','ba']}]
    exp = ['abca','cba']
    assert_nil @grapher.convert_words_and_find(paths, hdicts)
  end

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD convert_word
  # Equivalence classes:
  #   arr is empty -> returns empty string
  #   arr is nil -> returns nil
  #   otherwise gets letter from id and appends to word and returns word
  #     (ex. 1->C, 2->A, etc)
  #   tries to get a node that doesnt exist in map

  # if word is nil, should return nil
  def test_convert_word_nil
    arr = nil
    assert_nil @grapher.convert_word(arr)
  end

  # if word is empty, nothing to convert check if []
  def test_convert_word_empty
    arr = ''
    assert_equal [], @grapher.convert_word(arr)
  end

  # using attr_acc, see if the mapping works
  def test_convert_word_valid
    arr = ['1','2','3']
    @grapher.nmap = {'1'=>'a', '2'=>'b', '3'=>'c'}
    assert_equal 'abc', @grapher.convert_word(arr)
  end

  # should return nil if an invalid node to mapping is found
  def test_convert_word_invalid
    arr = ['1','2','4']
    @grapher.nmap = {'1'=>'a', '2'=>'b', '3'=>'c'}
    assert_nil @grapher.convert_word(arr)
  end

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD permute
  # Equivalence classes:
  #  words is empty -> returns []
  #  words is nil -> returns nil

  # if word is a nil type, should return nil
  def test_permute_isnil
    word = nil
    assert_nil @grapher.permute(word)
  end

  # if word is empty, it should just return an empty list
  def test_permute_empty
    word = []
    assert_equal [], @grapher.permute(word)
  end

  # should return the permutation of the word
  def test_permute_valid
    word = ['abc']
    exp = ['abc','acb','bca','cab','cba','bac'].sort
    assert_equal exp, @grapher.permute(word).sort
  end

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD find_words
  # Equivalence classes:
  #   words or dict is nil -> returns nil
  #   words or dict is empty -> returns []

  # if words is nil type, we return a nil type
  def test_find_words_words_nil
    words = nil
    dict = ['apple','bat','candy']
    assert_nil @grapher.find_words(words, dict)
  end

  # if dict is nil type, we return a ni ltype
  def test_find_words_dict_nil
    words = ['apple','bat','candy']
    dict = nil
    assert_nil @grapher.find_words(words, dict)
  end

  # if words is empty, we return []
  def test_find_words_words_empty
    words = []
    dict = Set['apple','bat','candy']
    assert_equal [], @grapher.find_words(words, dict)
  end

  # if dict is empty, we return []
  def test_find_words_dict_empty
    words = ['apple','bat','candy']
    dict = Set.new
    assert_equal [], @grapher.find_words(words, dict)
  end

  # given a valid list of words and dict, we return the longest words that
  # are in the dict
  def test_find_words_valid
    words = ['apple','bat','cat']
    dict = Set['apple','bat','cat']
    assert_equal Set["apple"], @grapher.find_words(words, dict)
  end

  # should return a list of words found if there are multiple words of that length
  def test_find_words_multiple
    words = ['apple','candy','bat']
    dict = Set['apple','bat','candy','cat']
    assert_equal Set["apple","candy"], @grapher.find_words(words, dict)
  end

  # should return an empty list when no words in words exist in dict
  def test_find_words_none_exist
    words =['apple', 'candy']
    dict = Set['zebra','ultra']
    assert_equal Set[], @grapher.find_words(words, dict)
  end

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD find_words_with_hdict
  # Equivalence classes:
  #   words or hdict is nil -> returns nil
  #   words or hdict is empty -> returns []
  #   gets list of mappings from hdict and breaks once we find a word thats smaller in size
  
  # # returns nil if words is nil type
  # def test_find_words_hdict_word_nil
  #   words = nil
  #   hdict = {'act'=>['act','cat','tac']}
  #   assert_nil @grapher.find_words_with_hdict(words, hdict)
  # end

  # # returns nil if hdict is nil type
  # def test_find_words_hdict_nil
  #   words = ['act','cat','tac']
  #   hdict = nil
  #   assert_nil @grapher.find_words_with_hdict(words, hdict)
  # end

  # # returns [] if words is empty
  # def test_find_words_hdict_word_empty
  #   words = []
  #   hdict = {'act'=>['act','cat','tac']}
  #   assert_equal [], @grapher.find_words_with_hdict(words, hdict)
  # end

  # # returns [] if hdict is empty
  # def test_find_words_hdict_empty
  #   words = ['act','cat','tac']
  #   hdict = []
  #   assert_equal [], @grapher.find_words_with_hdict(words, hdict)
  # end

  # # gets mapping list of anagrams of the hashed word
  # def test_find_words_hdict_valid
  #   words = ['act','cat','tac','d','e','f']
  #   hdict = [{'act'=>['act','cat','tac']}]
  #   assert_equal ['cat','tac','act'], @grapher.find_words_with_hdict(words, hdict)
  # end

  # # should only return the words of the longest length
  # def test_find_words_hdict_size_break
  #   words = ['hay','yah','ba', 'ab', 'd']
  #   hdicts = [{'ahy'=>['hay','yah']}, {'ab'=>['ab','ba']}]
  #   assert_equal ['yah','hay'], @grapher.find_words_with_hdict(words, hdicts)
  # end

  # # returns empty [] if no words found
  # def test_find_words_hdict_none_exist
  #   words = ['apple', 'bat', 'd']
  #   hdict = [{'act'=>['act','cat','tac']}]
  #   assert_equal [], @grapher.find_words_with_hdict(words, hdict)
  # end

  # --------------------------------------------------------------------------------------------------------------------
  # UNIT TESTS FOR METHOD print_words
  # Equivalence classes:
  #   list is nil -> return nil
  #   prints words in list in uppercase

  # If list is nil, it should return a nil object
  def test_print_words_nil
    li = nil
    assert_nil @grapher.print_words(li)
  end

  # If list is empty, should return []
  def test_print_words_empty
    li = []
    assert_equal [], @grapher.print_words(li)
  end

  # With a list of words, return the list with all uppercase
  def test_print_words
    li = ['apple', 'baby']
    assert_output("Longest valid word(s):\nAPPLE\nBABY\n") do @grapher.print_words(li) end
  end
end
