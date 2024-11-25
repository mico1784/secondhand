var ws;
var userName = $("#username").val() || '게스트';
var roomNo = $("#roomNo").val();
var sessionId = localStorage.getItem('sessionId');  // 로컬 스토리지에서 sessionId 가져오기
var itemId = $("#itemId").val();
var lastDate = null;

$(document).ready(function() {
    // 엔터 키를 눌렀을 때 메시지 전송
    $('#chatting').on('keypress', function(event) {
        // Shift + Enter 키가 눌렸을 때 줄바꿈
        if (event.keyCode === 13 && event.shiftKey) {
            return;  // 줄바꿈을 허용
        }
        // Enter 키(13번)만 눌렸을 때 메시지 전송
        if (event.keyCode === 13 && !event.shiftKey) {
            event.preventDefault();  // 기본 엔터 동작(폼 제출)을 막음
            send();  // 메시지 전송 함수 호출
        }
    });

    // roomNo 값이 없을 경우, chat-window를 비우는 처리
    if (!roomNo || roomNo.trim() === "") {
        $(".chat-window").html(`
        <div class="no-chat-box">
            <div class="no-chat-selected">
                <h3>선택된 채팅방 없어요</h3>
                <strong>판매자 분과 채팅해보세요 :)</strong>
            </div>
        </div>
        `);
    }

    // 세션 ID가 이미 로컬 스토리지에 있으면 사용하고, 없으면 새로운 세션 생성
    if (sessionId) {
        $("#sessionId").val(sessionId);
        wsOpen(roomNo);  // sessionId가 있으면 WebSocket 연결
    } else {
        wsOpen(roomNo);  // sessionId가 없으면 WebSocket 연결 시도
    }
});

function wsOpen(roomNo) {
    ws = new WebSocket("wss://" + location.host + "/chatting?roomNo=" + roomNo);
    console.log(roomNo);
    wsEvt(roomNo);
}

function wsEvt(roomNo) {
    ws.onopen = function() {
        console.log("WebSocket Connection Established");

        // sessionId가 없으면 서버에서 받아옴
        if (!sessionId) {
            var option = {
                type: "getId",  // 세션 ID를 요청
                roomNo: roomNo,
                username: userName
            };
            ws.send(JSON.stringify(option));  // 서버에 세션 ID 요청
        } else {
            // 방 번호와 sessionId를 서버로 전송
            var option = {
                type: "joinRoom",  // 채팅방에 입장
                roomNo: roomNo,
                username: userName,
                itemId: itemId,
                sessionId: sessionId  // sessionId를 포함해서 보내기
            };
            ws.send(JSON.stringify(option)); // 서버에 채팅방 번호와 sessionId 전달
            console.log("sessionId:", sessionId);
        }
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
                        sessionId = si;
                        $("#sessionId").val(si);
                        localStorage.setItem('sessionId', si);  // sessionId 로컬 스토리지에 저장
                        console.log("session Id: " + si);

                        // sessionId가 확보되었으면 채팅방에 입장
                        var option = {
                            type: "joinRoom",  // 채팅방에 입장
                            roomNo: roomNo,
                            username: userName,
                            itemId: itemId,
                            sessionId: si  // 새로운 sessionId로 전송
                        };
                        ws.send(JSON.stringify(option));
                    }
                } else if (response.type === "message") {
                    // 메시지 출력 처리
                    var chatMessage = response.msg.replace(/\n/g, "<br>"); // 줄바꿈을 <br> 태그로 변환
                    var timestamp = response.timestamp; // 서버에서 전달된 timestamp

                    // timestamp가 Date 객체가 아니라면 Date 객체로 변환
                    if (!(timestamp instanceof Date)) {
                        timestamp = new Date(timestamp);  // timestamp를 Date 객체로 변환
                    }

                    // 시간을 오전/오후 형식으로 변환 (HH:MM)
                    var hours = timestamp.getHours();
                    var minutes = timestamp.getMinutes();
                    var ampm = hours >= 12 ? '오후' : '오전';
                    hours = hours % 12;
                    hours = hours ? hours : 12;  // 0시는 12로 표시
                    minutes = minutes < 10 ? '0' + minutes : minutes;  // 10분 미만은 0 추가
                    var timeString = ampm + " " + hours + ":" + minutes;  // 오전/오후 HH:MM 형식

                    var messageDate = timestamp.toLocaleDateString();

                    // 날짜가 바뀌었을 경우 날짜와 구분선을 표시
                    if (lastDate !== messageDate) {
                        $("#chating").append("<p class='date-separator'>" + messageDate + "</p>");
                        lastDate = messageDate;  // 현재 날짜를 lastDate로 설정
                        $("#chating").append("<hr class='date-line'>");  // 구분선 추가
                    }

                    // 자신의 메시지와 다른 사람의 메시지 구분
                    if (response.userName === userName) {
                        // 자신의 메시지
                        if (response.msg.startsWith('http')) {  // 이미지 URL인 경우
                            $("#chating").append("<div class='me'><img src='" + chatMessage + "' alt='이미지' class='chat-image'></div><div class='chat-time-box'><p class='timestamp me-time'>"+ timeString + "</p></div>");
                        } else {
                            $("#chating").append("<div class='me'>" + chatMessage + "</div><div class='chat-time-box'><p class='timestamp me-time'>" + timeString + "</p></div>");
                        }
                    } else {
                        // 다른 사람의 메시지
                        if (response.msg.startsWith('http')) {  // 이미지 URL인 경우
                            $("#chating").append("<span>" + response.userName + "</span><div class='other'><img src='" + chatMessage + "' alt='이미지' class='chat-image'></div><p class='timestamp time-other'>" + timeString + "</p>");
                        } else {
                            $("#chating").append("<span>" + response.userName + "</span><div class='other'>"+ chatMessage + "</div><p class='timestamp'>" + timeString + "</p>");
                        }
                    }

                    // 채팅 메시지가 추가된 후 스크롤을 맨 아래로 내림
                    var chatMessages = document.getElementById('chating');
                    chatMessages.scrollTop = chatMessages.scrollHeight;
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
    const fileInput = document.getElementById('uploadImg');

    if (fileInput.files.length > 0) {
        const formData = new FormData();
        formData.append("file", fileInput.files[0]);
        formData.append("roomNo", roomNo);
        formData.append("username", username);
        formData.append("sessionId", sessionId);
        formData.append("msg", messageContent);

        fetch('/chat/uploadImage', {
            method: 'POST',
            body: formData
        }).then(response => response.json())
          .then(data => {
              if (data.success) {
                  // 업로드된 이미지 URL을 메시지에 포함시켜 WebSocket으로 전송
                  var option = {
                      type: "message",
                      sessionId: sessionId,
                      userName: username,
                      msg: data.fileUrl,  // 파일 URL을 메시지로 보내기
                      roomNo: roomNo,
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

        // 줄바꿈을 <br>로 변환하여 메시지 전송
        var formattedMessage = message.replace(/\n/g, "<br>");

        // WebSocket으로 전송할 메시지 구성
        var option = {
            type: "message",  // 메시지 타입
            sessionId: sessionId,
            userName: userName,
            msg: formattedMessage,
            roomNo: roomNo,
        };

        // WebSocket으로 메시지 전송
        ws.send(JSON.stringify(option));

        // 메시지 입력 필드 초기화
        $('#chatting').val("");
    }
}

