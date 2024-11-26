document.getElementById("imgFile").addEventListener("change", function(event) {
  const file = event.target.files[0];  // 선택된 파일
  const previewContainer = document.getElementById("preview-container");  // 미리보기 컨테이너
  const preview = document.getElementById("img-preview");  // 미리보기 이미지 태그
  const existingImageContainer = document.getElementById("existing-image-container"); // 기존 이미지 컨테이너
  const previewBox = document.getElementById("preview-box"); // 새로 추가된 박스 (기존 미리보기 박스)

  // 파일이 선택되면 미리보기 업데이트
  if (file) {
    const reader = new FileReader();

    // 파일 읽기 완료 후 미리보기 이미지로 표시
    reader.onload = function(e) {
      preview.src = e.target.result;  // 새로 선택한 이미지로 src 업데이트

      // 미리보기 영역을 보이게 함
      previewContainer.style.display = "block";

      // 새 이미지 선택 시 previewBox 숨기기
      if (previewBox) {
        previewBox.style.display = "none";
      }

      // 수정 페이지에서는 기존 이미지 숨기기
      if (existingImageContainer) {
        existingImageContainer.style.display = "none";  // 기존 이미지를 숨김
      }
    };

    // 파일 읽기
    reader.readAsDataURL(file);
  } else {
    // 파일이 없으면 미리보기 영역 숨기기
    previewContainer.style.display = "none";

    // 선택 취소 시 기존 이미지를 다시 표시
    if (existingImageContainer) {
      existingImageContainer.style.display = "block";
    }

    // 미리보기 박스가 있으면 다시 표시
    if (previewBox) {
      previewBox.style.display = "block";
    }
  }
});

// 페이지 로딩 시 기존 이미지가 미리보기로 보이도록 설정
window.onload = function() {
  const previewContainer = document.getElementById("preview-container");  // 미리보기 컨테이너
  const preview = document.getElementById("img-preview");  // 미리보기 이미지 태그
  const existingImageContainer = document.getElementById("existing-image-container"); // 기존 이미지 컨테이너

  // 아이템 수정 페이지인 경우
  if (existingImageContainer) {
    // 기존 이미지 URL로 미리보기 설정 (처음에는 기존 이미지로 설정)
    const existingImgUrl = document.querySelector('small img').src; // 기존 이미지 URL (이미 `th:src`로 렌더링됨)

    // 기존 이미지 URL로 미리보기 설정
    preview.src = existingImgUrl;

    // 기존 이미지는 미리보기로 표시하고, 새 이미지가 업로드되면 기존 이미지는 숨겨짐
    previewContainer.style.display = "none";  // 페이지 로드 시에는 기존 이미지만 보이도록 설정

    // 기존 이미지가 있으면 해당 이미지를 보여줌
    existingImageContainer.style.display = "block";
  } else {
    // 아이템 등록 페이지인 경우, 미리보기 영역을 숨기지 않음
    previewContainer.style.display = "none";
  }
};
