var socket = new WebSocket("wss://sudthemud.herokuapp.com/messages");

socket.onmessage = function(event) {
   var message = JSON.parse(event.data)["message"];
   $( ".message" ).html( message );
}

