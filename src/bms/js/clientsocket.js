var net = require('net');

var client = net.connect(9889, 'localhost');

client.write('Hello from node.js');

client.end();