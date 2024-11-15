var ws;
var userName = $("#username").val() || '게스트';
var roomNo = $("#roomNo").val();
var sessionId = localStorage.getItem('sessionId');
var itemId = $("#itemId").val();

$(document).ready(function() {
    // 엔터 키를 눌렀을 때 메시지 전송
    $('#chatting').on('keypress', function(event) {
        // 엔터 키(13번)가 눌렸을 때
        if (event.keyCode === 13) {
            event.preventDefault(); // 기본 엔터 동작(폼 제출)을 막음
            send();  // 메시지 전송 함수 호출
        }
    });

    // 세션 ID가 이미 로컬 스토리지에 있으면 사용하고, 없으면 새로운 세션 생성
    if (sessionId) {
        $("#sessionId").val(sessionId);
        wsOpen(roomNo);
    } else {
        wsOpen(roomNo);  // sessionId가 없으면 WebSocket 연결 시도
    }
});

function wsOpen(roomNo) {
    ws = new WebSocket("ws://" + location.host + "/chatting?roomNo=" + roomNo);
    console.log(roomNo);
    wsEvt(roomNo);
}

function wsEvt(roomNo) {
    ws.onopen = function() {
        console.log("WebSocket Connection Established");
        // 방 번호와 sessionId를 서버로 전송
        var option = {
            type: "joinRoom",  // 채팅방에 입장
            roomNo: roomNo,
            itemId: itemId,
            sessionId: $("#sessionId").val()  // sessionId를 포함해서 보내기
        };
        ws.send(JSON.stringify(option)); // 서버에 채팅방 번호와 sessionId 전달
        console.log("sessionId:", $("#sessionId").val());
    };

    ws.onmessage = function(event) {
        var msg = event.data;
        if (msg != null && msg.trim() !== '') {
            try {
                var response = JSON.parse(msg);

                // 서버에서 sessionId를 받을 때
                if (response.type === "getId") {
                    var si = response.sessionId || "";
                    if (si) {
                        $("#sessionId").val(si);
                        localStorage.setItem('sessionId', si);  // sessionId 로컬 스토리지에 저장
                        console.log("session Id: " + si);
                    }
                } else if (response.type === "message") {
                    // 메시지 출력 처리
                    var chatMessage = response.msg.replace(/ /g, "&nbsp;"); // 공백을 &nbsp;로 변환

                    // 자신의 메시지와 다른 사람의 메시지 구분
                    if (response.sessionId === $("#sessionId").val()) {
                        // 자신의 메시지
                        if (response.msg.startsWith('http')) {  // 이미지 URL인 경우
                            $("#chating").append("<p class='me'><img src='" + chatMessage + "' alt='이미지' class='chat-image'></p>");
                        } else {
                            $("#chating").append("<p class='me'>" + chatMessage + "</p>");
                        }
                    } else {
                        // 다른 사람의 메시지
                        if (response.msg.startsWith('http')) {  // 이미지 URL인 경우
                            $("#chating").append("<p class='other'>" + response.userName + " : <img src='" + chatMessage + "' alt='이미지' class='chat-image'></p>");
                        } else {
                            $("#chating").append("<p class='other'>" + response.userName + " : " + chatMessage + "</p>");
                        }
                    }
                } else {
                    console.warn("알 수 없는 메시지 유형:", response.type);
                }
            } catch (e) {
                console.error("JSON 파싱 오류: ", e);
            }
        }
    };

    ws.onclose = function(event) {
        console.log("WebSocket connection closed", event);
        alert("서버와의 연결이 끊겼습니다.");
    };
}

// 메시지 전송 함수
function send() {
    const roomNo = document.getElementById('roomNo').value;
    const username = document.getElementById('username').value;
    const sessionId = document.getElementById('sessionId').value;
    const messageContent = document.getElementById('chatting').value;
    const itemId = document.getElementById('itemId').value;
    const fileInput = document.getElementById('uploadImg');

    if (fileInput.files.length > 0) {
        const formData = new FormData();
        formData.append("file", fileInput.files[0]);
        formData.append("roomNo", roomNo);
        formData.append("username", username);
        formData.append("sessionId", sessionId);
        formData.append("msg", messageContent);
        formData.append("itemId", itemId);

        fetch('/chat/uploadImage', {
            method: 'POST',
            body: formData
        }).then(response => response.json())
          .then(data => {
              if (data.success) {
                  // 업로드된 이미지 URL을 메시지에 포함시켜 WebSocket으로 전송
                  var option = {
                      type: "message",
                      sessionId: $("#sessionId").val(),
                      userName: userName,
                      msg: data.fileUrl,  // 파일 URL을 메시지로 보내기
                      roomNo: roomNo,
                      itemId: itemId
                  };

                  // WebSocket으로 메시지 전송
                  ws.send(JSON.stringify(option));

                  console.log('Image sent successfully');
              } else {
                  console.error('Failed to send image: ' + data.error);
              }

              // 파일 업로드 후 input 필드 초기화
              fileInput.value = "";  // 파일 입력 필드 초기화
          }).catch(error => {
              console.error('Error:', error);
          });
    } else {
        var message = $("#chatting").val().trim();

        if (message === "") {
            return;  // 메시지가 비어있으면 전송하지 않음
        }

        // WebSocket으로 전송할 메시지 구성
        var option = {
            type: "message",  // 메시지 타입
            sessionId: $("#sessionId").val(),
            userName: userName,
            msg: message,
            roomNo: roomNo,
            itemId: itemId
        };

        // WebSocket으로 메시지 전송
        ws.send(JSON.stringify(option));

        // 메시지 입력 필드 초기화
        $('#chatting').val("");
    }
}
