$(document).ready(function () {
    // 채팅 목록 데이터를 서버에서 가져오는 함수
    function loadChatList() {
        $.ajax({
            url: "/chat/roomlist", // 서버에서 채팅 목록을 제공하는 엔드포인트
            type: "GET",
            success: function (response) {
                const chatListContainer = $(".chat-list");

                // 기존 목록 초기화
                chatListContainer.find(".chat-item").remove();

                // 이미지 확장자 목록
                const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];

                // 현재 로그인된 사용자의 이름을 hidden input에서 가져오기
                const currentUsername = $("#myNameCheck").val(); // input 태그에서 사용자 이름을 가져옵니다.

                // 가져온 채팅 목록 데이터 처리
                response.roomListDTO.forEach(chat => {
                    // 기본 메시지 설정
                    let latestMessageContent = chat.latestMessage && chat.latestMessage.content
                        ? chat.latestMessage.content
                        : "대화가 없습니다."; // 메시지가 없을 경우 기본 메시지
                    const latestMessageTime = chat.lastMessageTime || "시간 정보 없음"; // 시간이 없으면 기본값 설정

                    // 메시지 내용이 이미지 URL인지 확인
                    if (imageExtensions.some(ext => latestMessageContent.toLowerCase().endsWith(ext))) {
                        latestMessageContent = "이미지"; // 이미지일 경우 "이미지"로 설정
                    }

                    // 상대방의 닉네임을 선택 (현재 로그인된 사용자가 아닌 이름)
                    const otherPersonName = currentUsername === chat.buyerName ? chat.sellerName : chat.buyerName;

                    // 채팅 항목 HTML 생성
                    const chatItem = `
                        <div class="chat-item" onclick="openChatWindow(${chat.itemId})">
                            <img src="/images/logo.jpg" alt="프로필 이미지" class="profile-img">
                            <div class="chat-info">
                                <h3>${otherPersonName}</h3> <!-- 상대방의 이름만 표시 -->
                                <p>${latestMessageContent}</p>
                                <span class="chat-time">${latestMessageTime}</span>
                            </div>
                        </div>`;

                    // 채팅 항목 추가
                    chatListContainer.append(chatItem);
                });
            },
            error: function (error) {
                console.error("채팅 목록을 불러오는 중 오류 발생:", error);
            }
        });
    }

    // 채팅 창 열기 함수
    window.openChatWindow = function (itemId) {
        // itemId를 기반으로 채팅방으로 이동
        window.location.href = `/chat/${itemId}`;
    };

    // 초기 로딩
    loadChatList();
});
