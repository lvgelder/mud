var socket = new WebSocket(websocketUrl);

socket.onmessage = function(event) {
   var message = JSON.parse(event.data)["message"];
   $( ".message" ).html( message );
}

