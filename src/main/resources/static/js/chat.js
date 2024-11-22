      var ws;
      var userName = $("#username").val() || '게스트';

      $(document).ready(function() {
          wsOpen();
      });

      function wsOpen(){
        ws = new WebSocket("ws://" + location.host + "/chatting");
        wsEvt();
      }

      function wsEvt(){
        ws.onopen = function(){
            console.log("WebSocket Connection Established")
        }

        ws.onmessage = function(event){
            var msg = event.data;
            if(msg != null && msg.trim() != ''){
                var test = JSON.parse(msg);
                if(test.type == "getId"){
                    var si = test.sessionId != null ? test.sessionId : "";
                    if(si != ''){
                        $("#sessionId").val(si);
                        console.log("session Id: " + si);
                    }
                }else if(test.type == "message"){
                    if(test.sessionId == $("#sessionId").val()){
                        $("#chating").append("<p class='me'>" + test.msg.replace(/ /g, "&nbsp;") + "</p>");
                    }else{
                        $("#chating").append("<p class='other'>" + test.userName + " :" + test.msg.replace(/ /g, "&nbsp;") + "</p>");
                    }
                }else{
                    console.warn("뭐만 하면 오류나고 뭐만 하면 안되고")
                }
            }
        }

        document.addEventListener("keypress", function(e){
            if(e.keyCode == 13){
                send();
            }
        });
      }

	function send() {
	    var message = $("#chatting").val();

	    if(message.replace(/ /g, "") === ""){
	        return;
	    }

		var option ={
			type: "message",
			sessionId : $("#sessionId").val(),
			userName : userName,
			msg : message
		}
		ws.send(JSON.stringify(option))
		$('#chatting').val("");
	}