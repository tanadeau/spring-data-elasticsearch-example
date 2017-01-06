#!/usr/local/bin/node
var kcToken = process.env.KC_ACCESS_TOKEN
var wsUrl = 'ws://localhost:8080/websocket'
var Stomp = require('stompjs')
var websocket = require('websocket')
var wrapWS = function(url, auth) {
  var WebSocketClient = websocket.client;
  var connection;
  var ws = {
    url: url,
    send : function(d) {
      connection.sendUTF(d);
    },
    close : function() {
      connection.close();
    }
  };
  
  var socket = new WebSocketClient();
  socket.on('connectFailed', function(error) {
    console.log('Connect Error: ' + error.toString());
  });
  socket.on('connect', function(conn) {
      connection = conn;
      ws.onopen();
      connection.on('error', function(error) {
        console.log('error', error)
        if (ws.onclose) {
          ws.onclose(error);
        }
      });
      connection.on('close', function() {
        console.log('ws close')
        if (ws.onclose) {
          ws.onclose();
        }
      });
      connection.on('message', function(message) {
          console.log('ws event', message)
          if (message.type === 'utf8') {
            // wrap the data in an event object
            var event = {
              'data': message.utf8Data
            };
            ws.onmessage(event);
          }
      });
  });

  socket.connect(url, null, null, {'authorization': `BEARER ${auth}`});
  console.log('socket connected')
  return ws;
}

console.log("Connecting to websocket")
var client = Stomp.over(wrapWS(wsUrl, kcToken))


function onMessage(message) {
    // called when the client receives a STOMP message from the server
    console.log(`Got a message: ${JSON.stringify(message)}`)
}

client.debug = function(str) {
    console.log(`DEBUG: ${str}`)
};


console.log("Logging into stomp")
var headers = {
    login: 'stapljd1',
    passcode: '1234'
}

client.connect(headers, () => { 
    console.log("Connected!!"); 
    var subscription = client.subscribe("/topic/activity", onMessage)
}, 
    (err) => { console.log(`ERROR: ${JSON.stringify(err)}`); 
})
console.log("End of script")
