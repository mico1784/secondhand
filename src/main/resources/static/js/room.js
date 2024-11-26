$(document).ready(function() {

});

function wsOpen() {
    ws = new WebSocket("wss://" + location.host + "/roomlist")
    wsEvt();
}

function wsEvt() {

}