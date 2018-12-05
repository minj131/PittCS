require 'sinatra'
require 'sinatra/reloader'

def make_truthtable(argtrue, argfalse, argsize)
  bitnumber = ([*0..argsize-1]*' ').reverse!
  bitrows = [argtrue, argfalse].repeated_permutation(argsize).to_a
  androws = []
  orrows = []
  xorrows = []
  bitrows.each do |row|
    # check for AND
    if row.include? argfalse
      androws.push argfalse
    else
      androws.push argtrue
    end

    # check for OR
    if row.include? argtrue
      orrows.push argtrue
    else
      orrows.push argfalse
    end

    # check for XOR
    if row.count(argtrue)%2 == 1
      xorrows.push argtrue
    else
      xorrows.push argfalse
    end
  end
  return bitnumber, bitrows, androws, orrows, xorrows
end

# If a GET request comes in at /, do the following.
get '/' do
  # Get the parameter named guess and store it in args
  erb :index
end

post '/' do
  validarg = false
  t_arg = params['true']
  f_arg = params['false']
  s_arg = params['size']

  # set to default if args are empty
  if t_arg.length == 0
    t_arg = 'T'
  end
  if f_arg.length == 0
    f_arg = 'F'
  end
  if s_arg.length == 0
    s_arg = 3
  end

  # check for valid args
  if (t_arg.length != 1 || f_arg.length != 1 || (t_arg.eql? f_arg) || s_arg.to_i < 2)
    puts t_arg
    puts f_arg
    puts s_arg
    status 400
    erb :error400
  else   
    rargs = make_truthtable t_arg, f_arg, s_arg.to_i  
    bitnumber = rargs[0]
    bitrows = rargs[1]
    androws = rargs[2]
    orrows = rargs[3]
    xorrows = rargs[4]
    erb :truthtable, :locals => { bitnumber: bitnumber, bitrows: bitrows, androws: androws, orrows: orrows, xorrows: xorrows }
  end
end

not_found do
  status 404
  erb :error404
end
