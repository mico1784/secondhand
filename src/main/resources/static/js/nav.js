// DOM이 완전히 로드된 후 실행되도록 설정
document.addEventListener("DOMContentLoaded", function() {
    const dropdownMenu = document.getElementById("dropdownMenu");
    const hamburgerButton = document.querySelector(".hamburger-button");

    // 드롭다운 메뉴와 햄버거 아이콘 변환 토글 함수
    function toggleDropdownMenu() {
        if (dropdownMenu) {
            dropdownMenu.style.display = dropdownMenu.style.display === "block" ? "none" : "block";
        }
        hamburgerButton.classList.toggle("active"); // X자 모양 토글
    }

    // 햄버거 버튼 클릭 시 드롭다운 메뉴 표시
    if (hamburgerButton) {
        hamburgerButton.addEventListener("click", toggleDropdownMenu);
    }

    // 페이지의 다른 부분을 클릭하면 드롭다운 메뉴가 닫히고 햄버거 버튼이 원래대로 돌아가도록 설정
    document.addEventListener("click", function(event) {
        if (dropdownMenu && hamburgerButton &&
            !dropdownMenu.contains(event.target) && !hamburgerButton.contains(event.target)) {
            dropdownMenu.style.display = "none";
            hamburgerButton.classList.remove("active"); // 원래 햄버거 모양으로 복구
        }
    });
});
