
exports.dirname = __dirname;

var net = require('net');
var client = null;


exports.requestMsg = function(msg) {
    client = net.connect(9889, 'localhost');
    // Add a 'data' event handler for the client socket
    // data is what the server sent to this socket
    client.on('data', function(data) {

        //console.log(data);
        var retStr = bin2String(data);
        var obj = JSON.parse(retStr);
        console.log(obj);

        client.destroy();

        if(obj.msg_name=='response server status') {
            revealApp();
        }

    });

    // Add a 'close' event handler for the client socket
    client.on('close', function() {
        console.log('Connection closed');
    });

    client.on('error', function(err) {
        console.log(err);
        setTimeout(exports.requestMsg,500,msg);
    });

    client.write(msg,'UTF-8');
    client.end();
};

function bin2String(array) {

    var result = "";
    var foundJSONstr = false;
    for (var i = 0; i < array.length; i++) {
        if(array[i]==123&&!foundJSONstr) {
            foundJSONstr = true;
        }
        if(foundJSONstr) {
            result += String.fromCharCode(array[i]);
        }

    }
    console.log(result);
    return result;
}
exports.formatControlMessage = function(cmsg,params) {

    if(cmsg=="request server status") {
        return JSON.stringify({
           msg_name: 'request server status',
           msg_params: null
        });
    }

};

