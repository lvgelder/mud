var socket = new WebSocket("ws://localhost:8080/messages");

socket.onmessage = function(event) {
   var message = JSON.parse(event.data)["message"];
   $( ".message" ).html( message );
}

