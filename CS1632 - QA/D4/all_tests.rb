require 'simplecov'
SimpleCov.start

# start SimpleCov, ignore test files
SimpleCov.start do
  add_filter 'graph_test.rb'
end

# All other test files
require_relative 'graph_test'
