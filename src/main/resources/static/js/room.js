var roomSocket;

function connectRoomListWebSocket() {
    roomSocket = new WebSocket("wss://" + location.host + "/roomlist");

    roomSocket.onopen = function () {
        console.log("Room List WebSocket Connected");
    };

    roomSocket.onmessage = function (event) {
        const data = JSON.parse(event.data);
        if (data.type === "updateRoomList") {
            updateRoomList(data.rooms); // 서버에서 전달된 방 목록으로 업데이트
        } else {
            console.warn("Unknown message type:", data.type);
        }
    };

    roomSocket.onclose = function (event) {
        console.log("Room List WebSocket Disconnected", event);
        alert("채팅방 목록 서버와 연결이 끊어졌습니다.");
    };
}

function updateRoomList(rooms) {
    const roomListContainer = document.querySelector(".chat-list");
    roomListContainer.innerHTML = ""; // 기존 목록 초기화

    if (rooms.length === 0) {
        roomListContainer.innerHTML = "<p>참여 중인 채팅방이 없습니다.</p>";
        return;
    }

    rooms.forEach(room => {
        // 현재 로그인된 사용자의 이름을 hidden input에서 가져오기
        const currentUsername = $("#myNameCheck").val();
        // 상대방의 닉네임 (현재 로그인된 사용자와 다른 사용자)
        const otherPersonName = room.buyerName === currentUsername ? room.sellerName : room.buyerName;

        // 최신 메시지 설정 (기본 메시지 설정)
        let latestMessageContent = room.latestMessage && room.latestMessage.content
            ? room.latestMessage.content
            : "대화가 없습니다."; // 메시지가 없을 경우 기본 메시지
        const latestMessageTime = room.lastMessageTime || ""; // 시간이 없으면 기본값 설정

        // 이미지 확장자 목록
        const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];

        // 메시지 내용이 이미지 URL인지 확인
        if (imageExtensions.some(ext => latestMessageContent.toLowerCase().endsWith(ext))) {
            latestMessageContent = "이미지"; // 이미지일 경우 "이미지"로 설정
        }

        // 채팅 항목 HTML 생성
        const chatItem = `
            <div class="chat-item" data-room-no="${room.roomNo}">
                <div class="chat-info">
                    <h3>${otherPersonName}</h3>
                    <p>${latestMessageContent}</p>
                    <span class="chat-time">${latestMessageTime}</span>
                </div>
            </div>`;

        // 채팅 항목 추가
        roomListContainer.innerHTML += chatItem;
    });

    // 각 채팅 항목에 이벤트 추가
    const chatItems = document.querySelectorAll(".chat-item");
    chatItems.forEach(item => {
        item.addEventListener("click", function () {
            const roomNo = item.getAttribute("data-room-no");
            window.location.href = `/chat/rn/${roomNo}`; // 채팅방으로 이동
        });
    });
}

function initializeRoomList() {
    const roomListContainer = document.querySelector(".chat-list");
    roomListContainer.innerHTML = "<p>채팅방 목록을 불러오는 중...</p>"; // 초기 상태 표시
}

document.addEventListener("DOMContentLoaded", function () {
    initializeRoomList(); // 초기 UI 상태 설정
    connectRoomListWebSocket(); // WebSocket 연결
});
